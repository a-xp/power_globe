package powerglobe.wwd;

import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.dialogs.LayersSettings;
import powerglobe.dialogs.WmsLayerSettings;
import powerglobe.path.PathBuilder;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;
import powerglobe.view.ImageUtil;
import powerglobe.view.SlideImage;

/**
 * Класс управления окном WW при редактировании
 * @author 1
 *
 */
public class Scene {
	
	public WorldWindowGLCanvas wwd;
	
	/**
	 *  Очередь слайдов на генерацию скриншота
	 */
	private Queue<Slide> screenShotQueue = new LinkedList<>();
	/**
	 * Флаг, что идет генерация скриншота
	 */
	private boolean isCapturing = false;
	/**
	 * Wms слой
	 */
	protected WmsLayerSettings wmsLayer;
	
	/**
	 * Аннотации слайдов
	 */
	private Map<Slide, GlobeAnnotation> annotations = new HashMap<>();
	/**
	 * Слой с аннотациями
	 */
	private AnnotationLayer annotationLayer;
	
	public void setUp(){
		
		/**
		 * Поток для генерации скриншотов слайдов
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					synchronized (screenShotQueue) {
						if(!isCapturing){
							/**
							 * берем следующий скриншот
							 */
							final Slide slide = screenShotQueue.poll();
							if(slide!=null){
								isCapturing = true;
								/**
								 * делаем для него скриншот
								 */
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										getImage(slide);
									}
								});
							}
						}
						try {
							/**
							 * Ждем новых событий в очереди
							 */
							screenShotQueue.wait();
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
		}).start();
		
		/**
		 * Ставим обработчик события выбора слайда - переводим на него камеру
		 */
		Workspace ws = Workspace.getCurrent();
		ws.addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
								goToSlide((Slide)t);
			}
		});
		/**
		 * Ставим обработчик события добавления слайда
		 */
		ws.addListener(Workspace.EVENT_SLIDE_ADD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				synchronized (screenShotQueue) {
					screenShotQueue.add((Slide)t); // кладем слайд в очередь на снятие скриншота
					screenShotQueue.notifyAll(); // сообщаем, что есть изменения
				}				
			}
		});
		/**
		 * Ставим обработчик события загрузки слайда
		 */		
		ws.addListener(Workspace.EVENT_SLIDE_LOAD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				synchronized (screenShotQueue) {
					screenShotQueue.add((Slide)t); // кладем слайд в очередь на снятие скриншота
					screenShotQueue.notifyAll();// сообщаем, что есть изменения
				}
			}
		});
		/**
		 * Ставим обработчик события удаления слайда - удаляем его аннотацию
		 */
		ws.addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				/**
				 * Находим аннотацию слайда
				 */
				GlobeAnnotation ann = annotations.get((Slide)t);
				if(ann!=null){
					/**
					 * Если она есть,
					 * удаляем ее со слоя и из списка 
					 */
					annotationLayer.removeAnnotation(ann);
					ann.dispose();
					annotations.remove((Slide)t);
				}
			}
		});
		/**
		 * Ставим обработчик события выбора слайда - показываем его аннотацию
		 */
		ws.addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Slide slide = (Slide)t;
				annotationLayer.removeAllAnnotations();	// Удаляем все аннотации со слоя		
				if(slide.annotation!=null){
					/**
					 * Создаем аннотацию для слайда
					 */
					GlobeAnnotation ann = slide.annotation.getAnnotation(slide.position);
					/**
					 * Ставим на слой и сохраняем в список
					 */
					annotationLayer.addAnnotation(ann);
					annotations.put(slide, ann);
				}
				wwd.redraw();
			}
		});
		/**
		 * Ставим обработчик закрытия проекта - удаляем все аннотации
		 */
		ws.addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				/**
				 * Проходим по всем аннотациям и удаляем
				 */
				for(GlobeAnnotation ann: annotations.values()){
					annotationLayer.removeAnnotation(ann);
					ann.dispose();
				}
				annotations.clear(); // очищаем список аннотаций
				if(wmsLayer!=null){
					/**
					 * Отменяем настройки wms
					 */
					wmsLayer.apply(wwd, false);
					wmsLayer = null;
				}
			}
		});
		
		/**
		 * Обработчик добавления настроек WMS
		 */
		ws.addListener(Workspace.EVENT_WMS_SETTINGS, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				if(wmsLayer!=null){
					/**
					 * Если до этого стояли настройки, то отменяем их
					 */
					wmsLayer.apply(wwd, false);
				}
				wmsLayer = (WmsLayerSettings)t;
				if(wmsLayer!=null){
					/**
					 * Применяем новые настройки WMS
					 */
					wmsLayer.apply(wwd, true);
				}
			}
		});
		
		/**
		 * Обработчик добавления настроек слоев сцены
		 */
		ws.addListener(Workspace.EVENT_LAYERS_SETTINGS, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				LayersSettings layers = (LayersSettings)t;
				if(layers!=null){
					/**
					 * Применям настройки слоев сцены
					 */
					layers.apply(wwd);
				}
			}
		});
		
		/**
		 * Добавляем в WW слой аннотаций
		 */
		annotationLayer = new AnnotationLayer();		
		insertBeforeCompass(wwd, annotationLayer);
		
		/**
		 * Добавляем слой с сеткой широта/долгота (по-умолчанию выключен)
		 */		
		LatLonGraticuleLayer graticule = new LatLonGraticuleLayer();
		graticule.setEnabled(false);
		insertBeforeCompass(wwd, graticule);
		
		/**
		 * Добавляем слой с кнопками управления видом (по-умолчанию отключен)
		 */
		ViewControlsLayer viewControl = new ViewControlsLayer();
		viewControl.setEnabled(false);
		insertBeforeCompass(wwd, viewControl);	
		
		PathBuilder pb =new PathBuilder();
		pb.init(wwd);
		
	}

	public Scene(Panel panel) {
		wwd = new WorldWindowGLCanvas();
		wwd.setModel(new BasicModel());
		panel.add(wwd, java.awt.BorderLayout.CENTER);
	}
	
	/**
	 * Перевод камеры на слайд
	 * @param slide
	 */
	public void goToSlide(Slide slide){
		wwd.getView().goTo(slide.position, slide.position.elevation);	
	}
	
	/**
	 * Сделать скриншот для слайда
	 * @param slide
	 */
	public void getImage(final Slide slide){
		/**
		 * Переводим на него камеру
		 */
		wwd.getView().setEyePosition(slide.position);
		/**
		 * Слушаем события отрисовки
		 */
		wwd.addRenderingListener(
			new RenderingListener() {				
				private int cnt = 0;
				@Override
				public void stageChanged(RenderingEvent event)
			    {
					/**
					 * если что-то нарисовалось на экране
					 */
			        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
			        {
			        	/**
			        	 * пропускаем 3(получено опытным путем) первых кадра после установки камеры
			        	 */
			        	if(cnt++<3){
			        		wwd.redraw();
			        		return;
			        	}
			        	/**
			        	 * Копируем картинку с экрана
			        	 */
			            GLAutoDrawable glad = (GLAutoDrawable) event.getSource();
		                int[] viewport = new int[4];
		                glad.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		                final BufferedImage bi = com.sun.opengl.util.Screenshot.readToBufferedImage(viewport[2], viewport[3]);
		                //glad.getGL().glViewport(0, 0, glad.getWidth(), glad.getHeight());	            
		                wwd.removeRenderingListener(this);
		                /**
		                 * преобразуем в формат swt и нужный размер
		                 */
	            		Image img =  ImageUtil.makeSlideImage(bi, 150, 120, 3.0f) ;
	            		/**
	            		 * отправляем картинку событием SLIDE_IMAGE
	            		 */
	            		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_IMAGE, new SlideImage(slide, img));
						synchronized (screenShotQueue) {
							isCapturing = false;
							screenShotQueue.notifyAll();	
						}
			        }
			    }
			}
		);
		wwd.redraw();
	}
	
	/**
	 * Возвращает позицию камеры
	 * @return
	 */
	public Position getEyePosition(){
		return wwd.getView().getCurrentEyePosition();
	}
	
	/**
	 * Добавляет слой в WW
	 * @param wwd
	 * @param layer
	 */
    public static void insertBeforeCompass(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }
        
    public void redraw(){
    	wwd.redraw();
    }
    
}
