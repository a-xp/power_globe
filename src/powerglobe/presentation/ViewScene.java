package powerglobe.presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.render.ScreenRelativeAnnotation;
import gov.nasa.worldwind.view.ViewElevationAnimator;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewPropertyAccessor;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import powerglobe.dialogs.LayersSettings;
import powerglobe.dialogs.WmsLayerSettings;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Класс управления окном просмотра презентации
 * @author 1
 *
 */
public class ViewScene {
	final int VIEW_ORBIT =1;
	final int VIEW_FLY = 2;
	
	protected Shell shell; // окно SWT
	
	private AnnotationLayer namesLayer;
	private AnnotationLayer annotationLayer;   //слой аннотаций
	WorldWindowGLCanvas wwd;    // сцена WW
	//protected ViewControlsLayer viewControlsLayer;
	public List<Slide> slides;  // Список слайдов 
	protected int index = 0;   // Номер текущего слайда
	private AutoWatcher aw;     // Класс для автопросмотра
	private ScreenRelativeAnnotation name;
	private GlobeAnnotation ann;  // Аннотация текущего слайда  

	ControlPanel cp;
	
	public ViewScene(){
		
		      
	}
	/**
	 * Инициализирует сцену просмотра презентации
	 */
	public void setUp(){
		/**
		 * Копируем список слайдов
		 */
		slides = Workspace.getCurrent().currentProject.slides;
		if(slides==null || slides.size()==0){
			return;
		}
		/**
		 * Создаем новое окно
		 */
		Display display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display, SWT.NO_TRIM);    // окно без рамки
		shell.open();   // Открываем
		shell.setFullScreen(true);   // Полный экран
		shell.setLayout(new FillLayout());    // Базовая раскладка - растягивает детей на весь размер окна
		shell.setFocus();  // Переводим фокус клавиатуры на окно
		
		/**
		 * Создаем AWT панель для WW
		 */
		Composite _embeddedContainer = new Composite(shell, SWT.EMBEDDED);
		
		java.awt.Frame frame = SWT_AWT.new_Frame(_embeddedContainer);
		java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
		frame.add(panel);			
		
		/**
		 * Создаем сцену WW и ставим на AWT панель
		 */
		wwd = new WorldWindowGLCanvas();
		wwd.setModel(new BasicModel());
		panel.add(wwd, java.awt.BorderLayout.CENTER);
		
		shell.redraw();
		shell.layout(true);
		_embeddedContainer.layout(true);
		
		/**
		 * Добавляем слой с названием слайда
		 */
    	namesLayer = new AnnotationLayer();
    	ApplicationTemplate.insertBeforePlacenames(wwd, namesLayer);
		
		/**
		 * Добавляем слой аннотаций в WW
		 */
		annotationLayer = new AnnotationLayer();
		ApplicationTemplate.insertBeforePlacenames(wwd, annotationLayer);
		
		/**
		 * Добавляем слой с сеткой широта/долгота (по-умолчанию выключен)
		 */
		LatLonGraticuleLayer graticule = new LatLonGraticuleLayer();
		graticule.setEnabled(false);
		ApplicationTemplate.insertBeforeCompass(wwd, graticule);
		
		/**
		 * Добавляем слой с кнопками управления видом WW(по-умолчанию выключен)
		 */
		ViewControlsLayer viewControl = new ViewControlsLayer();
		viewControl.setEnabled(false);
		ApplicationTemplate.insertBeforeCompass(wwd, viewControl);	
		
		/**
		 * Применяем к сцене настройки WMS текущего проекта
		 */
		WmsLayerSettings wls = Workspace.getCurrent().currentProject.getWmsLayer();
		if(wls!=null){
			wls.apply(wwd, true);
		}
		/**
		 * Применяем к сцене настройки слоев текущего проекта
		 */
		LayersSettings ls = Workspace.getCurrent().currentProject.getLayers();
		ls.apply(wwd);
		
		/**
		 * Ждем окончания инициализации окна WW - момент когда начнут приходить события об отрисовках
		 */
		wwd.addGLEventListener(new GLEventListener() {			
			@Override
			public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
			}			
			@Override
			public void init(GLAutoDrawable arg0) {
			}			
			@Override
			public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
			}			
			@Override
			public void display(GLAutoDrawable arg0) {
				/**
				 * Отключаем текущий обработчик(он нужен только для инициализации)
				 */
				wwd.removeGLEventListener(this);
				/**
				 * Ставим фокус клавиатуры на окно
				 */
				wwd.requestFocus();
				/**
				 * рисуем кнопки управления
				 */
				cp = new ControlPanel(ViewScene.this);
				cp.setControls();
				
				/**
				 * переводим камеру на первый слайд
				 */
				shell.getDisplay().asyncExec(new Runnable() {					
					@Override
					public void run() {
						showFirstSlide(0);						
					}
				});				
			}
		});
		
		/**
		 * Обработчик управления с клавиатуры z/x
		 */
		wwd.addKeyListener(new java.awt.event.KeyListener() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent e) {
			}
			@Override
			public void keyReleased(java.awt.event.KeyEvent e) {
			}
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				/** 
				 * закрыть окно на нажатие ESC(код кнопки 27)
				 */
				if(e.getKeyCode()==27){
					shell.getDisplay().asyncExec(new Runnable() {						
						@Override
						public void run() {
							shell.close();
							shell.dispose();
						}
					});
					e.consume();   // чтобы событие не обрабатывалось дальше
					return;
				}
				/**
				 * берем символ нажатой кнопки
				 */
				char code = e.getKeyChar();
				if(code=='x'){
					nextSlide();  //Следующий
					e.consume();
				}else if(code=='z'){
					prevSlide(); // Предыдущий
					e.consume();
				}
			}
		});
		
	}
	
    /**
     * Закрыть окно просмотра презентации
     */
    public void finish(){
    	shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
		    	shell.close();
		    	shell.dispose();
			}
		});
    }
    
    /**
     * Показываем первый слайд
     */
    public void showFirstSlide(int index){
    	this.index = index;
    	Slide slide = slides.get(index);
    	showAnnotation(slide);
    	BasicOrbitView view = (BasicOrbitView) wwd.getView();
    	view.setHeading(Angle.ZERO);
    	view.setPitch(Angle.ZERO);
    	view.setEyePosition(slide.position);
    	view.getViewInputHandler().stopAnimators();
    	wwd.redraw();
    }    
    
    /**
     * Переход к слайду без следования пути
     * @param slide
     */
    public void showSlide(Slide slide){
    	
    	hideAnnotation();
    	/**
    	 * Получаем настройки вида от сцены WW И останавливаем активные анимации переходов
    	 */
    	BasicOrbitView view = (BasicOrbitView) wwd.getView();
    	view.getViewInputHandler().stopAnimators();
    	Position curCenter = view.getCenterPosition();
    	Angle dist = LatLon.greatCircleAzimuth(curCenter, slide.position);
    	System.out.println(dist);
    	int time = (int)Math.round(Math.abs(1000*dist.degrees/slide.getMoveSpeed()));
        /**
         * Запускаем анимацию переход к позиции слайда с заданным временем
         * Восстанавливаем стандартный вид на шар 
         */
    	if(time<50)time=50;
    	view.addPanToAnimator(slide.position, Angle.ZERO, Angle.ZERO, slide.position.elevation, time, true);
    }
    
    /**
     * Включает анимацию перехода без изменения высоты
     * 
     * @param endCenterPos
     * @param endHeading
     * @param endPitch
     * @param endZoom
     * @param timeToMove
     * @param endCenterOnSurface
     */
    public void addPathAnimator(Position endCenterPos, Angle endHeading,
             Angle endPitch, double endZoom, long timeToMove, boolean endCenterOnSurface){
    	int altitudeMode = endCenterOnSurface ? WorldWind.CLAMP_TO_GROUND : WorldWind.ABSOLUTE;
    	OrbitView orbitView = (OrbitView) wwd.getView();
    	/**
    	 * Получаем текущие натройки вида
    	 */
    	Position beginCenterPos = orbitView.getCenterPosition();
    	Angle beginHeading = orbitView.getHeading();
    	Angle beginPitch = orbitView.getPitch();
    	double beginZoom = orbitView.getZoom();    	
    	/**
    	 * Создаем стандартный аниматор перехода от точки к точке
    	 */
        FlyToOrbitViewAnimator panAnimator = FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView,
            beginCenterPos, endCenterPos, beginHeading, endHeading, beginPitch, endPitch,
            beginZoom, endZoom, timeToMove, altitudeMode);
        /**
         * Отфильтровываем ненужную анимацию высоты
         */
        List<Animator> list = new ArrayList<>();
        panAnimator.getAnimators().forEach(list::add);
        Animator[] filtered = list.stream().map(an -> {
        	if(an instanceof ViewElevationAnimator){
        		return new ViewElevationAnimator(null,
                        beginZoom, endZoom, beginCenterPos, endCenterPos, WorldWind.ABSOLUTE,
                        OrbitViewPropertyAccessor.createZoomAccessor(orbitView));
        	}else{
        		return an;	
        	}
        }).toArray(Animator[]::new);        
        
        panAnimator.setAnimators(filtered);
        /**
         * Добавляем аниматор к виду
         */
        wwd.getView().getViewInputHandler().addAnimator(panAnimator);
        wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());
    }
        
    public void hideAnnotation(){
		/**
		 * Удаляем текущие аннотации
		 */
    	if(ann!=null){
    		ann.dispose();
    		ann=null;
    	}
    	if(name!=null){
    		name.dispose();
    		name = null;
    	}
    	annotationLayer.removeAllAnnotations();
    	namesLayer.removeAllAnnotations(); 
    	wwd.redraw();
    }
    
    /**
     * Открывает аннотацию слайда и название
     * @param slide
     */   
    public void showAnnotation(Slide slide){
    	hideAnnotation();
    	
    	if(slide.annotation!=null){
       		/**
    		 * Берем аннотацию текущего слайда и ставим на слой аннотаций
    		 */
    		ann = slide.annotation.getAnnotation(slide.position);
			annotationLayer.addAnnotation(ann);
    	}   
    	
    	/**
    	 * Ставим название слайда
    	 */        
    	name = createNameAnnotation(slide.getTitle());
        namesLayer.addAnnotation(name); 
        wwd.redraw();
    }
    
    /**
     * Создает заголовок слайда
     * @param title
     * @return
     */
    protected ScreenRelativeAnnotation createNameAnnotation(String title){
    
    	AnnotationAttributes defaultAttributes = new AnnotationAttributes();
        defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, 0f));
        defaultAttributes.setTextColor(Color.YELLOW);
        defaultAttributes.setLeaderGapWidth(14);
        defaultAttributes.setCornerRadius(0);
        defaultAttributes.setSize(new Dimension(300, 0));
        defaultAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT); // use strict dimension width - 200
        defaultAttributes.setFont(Font.decode("Arial-BOLD-28"));
        defaultAttributes.setBorderWidth(0);
        defaultAttributes.setHighlightScale(1);             // No highlighting either
        defaultAttributes.setCornerRadius(0);
        
        ScreenRelativeAnnotation name = new ScreenRelativeAnnotation(title, 0.5, 0.9);
        name.setKeepFullyVisible(true);
        name.setXMargin(5);
        name.setYMargin(5);
        name.getAttributes().setDefaults(defaultAttributes);
        return name;
    }
    
    
    
    /**
     * Переход на предыдущий слайд
     */
    public void prevSlide(){
    	if(aw!=null){
    		/**
    		 * останавливаем автопросмотр
    		 */
    		aw.interrupt();
    		aw = null;
    	}
    	if(index == 0){
    		return;
    	}
    	/**
    	 * Переходим на него
    	 */
    	aw = new AutoWatcher(ViewScene.this, index, index-1);
    	aw.start();
    }
    
    /**
     * Переход на следующий слайд
     */
    public void nextSlide(){
    	if(aw!=null){
    		/**
    		 * останавливаем автопросмотр
    		 */
    		aw.interrupt();
    		aw = null;
    	}
    	/**
    	 * Выбираем номер следующего слайда
    	 */
    	int next = index + 1;
    	if(next>=slides.size()){
    		showFirstSlide(0);
    		return;    	
    	}else{
    		/**
        	 * Переходим на него
        	 */
        	aw = new AutoWatcher(ViewScene.this, index, next);
        	aw.start();
        	index++;
    	}    	
    }
    
    /**
     * Идет ли сейчас анимация перехода?
     * @return
     */
    public boolean isAnimating(){
    	return wwd.getView().isAnimating();
    }
     
    /**
     * Запустить автопросмотр презентации 
     */
    public void startAuto(){
    	if(aw==null){
    		/**
    		 * Если автопросмотр еще не запущен, создаем класс автопросмотра
			 * Устанавливаем какие слайды нужно просмотреть  - 
			 * от текущего до последнего    		 
    		 */
			aw = new AutoWatcher(ViewScene.this, 0, slides.size()-1);
			/**
			 * Запускаем автопросмотр
			 */
			aw.start();
		}	
    }
    
    /**
     * остановить автопросмотр презентации
     */
    public void stopAuto(){
    	if(aw!=null){
    		aw.interrupt();
    		index = aw.getIndex();
    		showFirstSlide(index);
    		aw = null;
    	}
    }
	public Shell getShell() {
		return shell;
	}
	public WorldWindowGLCanvas getWwd() {
		return wwd;
	}
    
  
    
}
