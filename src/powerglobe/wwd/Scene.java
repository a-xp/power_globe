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
 * ����� ���������� ����� WW ��� ��������������
 * @author 1
 *
 */
public class Scene {
	
	public WorldWindowGLCanvas wwd;
	
	/**
	 *  ������� ������� �� ��������� ���������
	 */
	private Queue<Slide> screenShotQueue = new LinkedList<>();
	/**
	 * ����, ��� ���� ��������� ���������
	 */
	private boolean isCapturing = false;
	/**
	 * Wms ����
	 */
	protected WmsLayerSettings wmsLayer;
	
	/**
	 * ��������� �������
	 */
	private Map<Slide, GlobeAnnotation> annotations = new HashMap<>();
	/**
	 * ���� � �����������
	 */
	private AnnotationLayer annotationLayer;
	
	public void setUp(){
		
		/**
		 * ����� ��� ��������� ���������� �������
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					synchronized (screenShotQueue) {
						if(!isCapturing){
							/**
							 * ����� ��������� ��������
							 */
							final Slide slide = screenShotQueue.poll();
							if(slide!=null){
								isCapturing = true;
								/**
								 * ������ ��� ���� ��������
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
							 * ���� ����� ������� � �������
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
		 * ������ ���������� ������� ������ ������ - ��������� �� ���� ������
		 */
		Workspace ws = Workspace.getCurrent();
		ws.addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
								goToSlide((Slide)t);
			}
		});
		/**
		 * ������ ���������� ������� ���������� ������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_ADD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				synchronized (screenShotQueue) {
					screenShotQueue.add((Slide)t); // ������ ����� � ������� �� ������ ���������
					screenShotQueue.notifyAll(); // ��������, ��� ���� ���������
				}				
			}
		});
		/**
		 * ������ ���������� ������� �������� ������
		 */		
		ws.addListener(Workspace.EVENT_SLIDE_LOAD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				synchronized (screenShotQueue) {
					screenShotQueue.add((Slide)t); // ������ ����� � ������� �� ������ ���������
					screenShotQueue.notifyAll();// ��������, ��� ���� ���������
				}
			}
		});
		/**
		 * ������ ���������� ������� �������� ������ - ������� ��� ���������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				/**
				 * ������� ��������� ������
				 */
				GlobeAnnotation ann = annotations.get((Slide)t);
				if(ann!=null){
					/**
					 * ���� ��� ����,
					 * ������� �� �� ���� � �� ������ 
					 */
					annotationLayer.removeAnnotation(ann);
					ann.dispose();
					annotations.remove((Slide)t);
				}
			}
		});
		/**
		 * ������ ���������� ������� ������ ������ - ���������� ��� ���������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Slide slide = (Slide)t;
				annotationLayer.removeAllAnnotations();	// ������� ��� ��������� �� ����		
				if(slide.annotation!=null){
					/**
					 * ������� ��������� ��� ������
					 */
					GlobeAnnotation ann = slide.annotation.getAnnotation(slide.position);
					/**
					 * ������ �� ���� � ��������� � ������
					 */
					annotationLayer.addAnnotation(ann);
					annotations.put(slide, ann);
				}
				wwd.redraw();
			}
		});
		/**
		 * ������ ���������� �������� ������� - ������� ��� ���������
		 */
		ws.addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				/**
				 * �������� �� ���� ���������� � �������
				 */
				for(GlobeAnnotation ann: annotations.values()){
					annotationLayer.removeAnnotation(ann);
					ann.dispose();
				}
				annotations.clear(); // ������� ������ ���������
				if(wmsLayer!=null){
					/**
					 * �������� ��������� wms
					 */
					wmsLayer.apply(wwd, false);
					wmsLayer = null;
				}
			}
		});
		
		/**
		 * ���������� ���������� �������� WMS
		 */
		ws.addListener(Workspace.EVENT_WMS_SETTINGS, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				if(wmsLayer!=null){
					/**
					 * ���� �� ����� ������ ���������, �� �������� ��
					 */
					wmsLayer.apply(wwd, false);
				}
				wmsLayer = (WmsLayerSettings)t;
				if(wmsLayer!=null){
					/**
					 * ��������� ����� ��������� WMS
					 */
					wmsLayer.apply(wwd, true);
				}
			}
		});
		
		/**
		 * ���������� ���������� �������� ����� �����
		 */
		ws.addListener(Workspace.EVENT_LAYERS_SETTINGS, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				LayersSettings layers = (LayersSettings)t;
				if(layers!=null){
					/**
					 * �������� ��������� ����� �����
					 */
					layers.apply(wwd);
				}
			}
		});
		
		/**
		 * ��������� � WW ���� ���������
		 */
		annotationLayer = new AnnotationLayer();		
		insertBeforeCompass(wwd, annotationLayer);
		
		/**
		 * ��������� ���� � ������ ������/������� (��-��������� ��������)
		 */		
		LatLonGraticuleLayer graticule = new LatLonGraticuleLayer();
		graticule.setEnabled(false);
		insertBeforeCompass(wwd, graticule);
		
		/**
		 * ��������� ���� � �������� ���������� ����� (��-��������� ��������)
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
	 * ������� ������ �� �����
	 * @param slide
	 */
	public void goToSlide(Slide slide){
		wwd.getView().goTo(slide.position, slide.position.elevation);	
	}
	
	/**
	 * ������� �������� ��� ������
	 * @param slide
	 */
	public void getImage(final Slide slide){
		/**
		 * ��������� �� ���� ������
		 */
		wwd.getView().setEyePosition(slide.position);
		/**
		 * ������� ������� ���������
		 */
		wwd.addRenderingListener(
			new RenderingListener() {				
				private int cnt = 0;
				@Override
				public void stageChanged(RenderingEvent event)
			    {
					/**
					 * ���� ���-�� ������������ �� ������
					 */
			        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
			        {
			        	/**
			        	 * ���������� 3(�������� ������� �����) ������ ����� ����� ��������� ������
			        	 */
			        	if(cnt++<3){
			        		wwd.redraw();
			        		return;
			        	}
			        	/**
			        	 * �������� �������� � ������
			        	 */
			            GLAutoDrawable glad = (GLAutoDrawable) event.getSource();
		                int[] viewport = new int[4];
		                glad.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		                final BufferedImage bi = com.sun.opengl.util.Screenshot.readToBufferedImage(viewport[2], viewport[3]);
		                //glad.getGL().glViewport(0, 0, glad.getWidth(), glad.getHeight());	            
		                wwd.removeRenderingListener(this);
		                /**
		                 * ����������� � ������ swt � ������ ������
		                 */
	            		Image img =  ImageUtil.makeSlideImage(bi, 150, 120, 3.0f) ;
	            		/**
	            		 * ���������� �������� �������� SLIDE_IMAGE
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
	 * ���������� ������� ������
	 * @return
	 */
	public Position getEyePosition(){
		return wwd.getView().getCurrentEyePosition();
	}
	
	/**
	 * ��������� ���� � WW
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
