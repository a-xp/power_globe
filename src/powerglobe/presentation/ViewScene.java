package powerglobe.presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.ScreenAnnotation;
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
 * ����� ���������� ����� ��������� �����������
 * @author 1
 *
 */
public class ViewScene {
	final int VIEW_ORBIT =1;
	final int VIEW_FLY = 2;
	
	protected Shell shell; // ���� SWT
	
	private AnnotationLayer annotationLayer;   //���� ���������
	WorldWindowGLCanvas wwd;    // ����� WW
	//protected ViewControlsLayer viewControlsLayer;
	public List<Slide> slides;  // ������ ������� 
	protected int index = 0;   // ����� �������� ������
	private AutoWatcher aw;     // ����� ��� �������������
	private GlobeAnnotation ann;  // ��������� �������� ������  
	
	/**
	 * ������
	 */
	protected ScreenAnnotation left;
	protected ScreenAnnotation right;
	protected ScreenAnnotation play;
	protected ScreenAnnotation stop;
	protected ScreenAnnotation exit;
	
	
	public ViewScene(){
		
		      
	}
	/**
	 * �������������� ����� ��������� �����������
	 */
	public void setUp(){
		/**
		 * �������� ������ �������
		 */
		slides = Workspace.getCurrent().currentProject.slides;
		if(slides==null || slides.size()==0){
			return;
		}
		/**
		 * ������� ����� ����
		 */
		Display display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display, SWT.NO_TRIM);    // ���� ��� �����
		shell.open();   // ���������
		shell.setFullScreen(true);   // ������ �����
		shell.setLayout(new FillLayout());    // ������� ��������� - ����������� ����� �� ���� ������ ����
		shell.setFocus();  // ��������� ����� ���������� �� ����
		
		/**
		 * ������� AWT ������ ��� WW
		 */
		Composite _embeddedContainer = new Composite(shell, SWT.EMBEDDED);
		
		java.awt.Frame frame = SWT_AWT.new_Frame(_embeddedContainer);
		java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
		frame.add(panel);			
		
		/**
		 * ������� ����� WW � ������ �� AWT ������
		 */
		wwd = new WorldWindowGLCanvas();
		wwd.setModel(new BasicModel());
		panel.add(wwd, java.awt.BorderLayout.CENTER);
		
		shell.redraw();
		shell.layout(true);
		_embeddedContainer.layout(true);
				
		/**
		 * ��������� ���� ��������� � WW
		 */
		annotationLayer = new AnnotationLayer();
		ApplicationTemplate.insertBeforePlacenames(wwd, annotationLayer);
		
		/**
		 * ��������� ���� � ������ ������/������� (��-��������� ��������)
		 */
		LatLonGraticuleLayer graticule = new LatLonGraticuleLayer();
		graticule.setEnabled(false);
		ApplicationTemplate.insertBeforeCompass(wwd, graticule);
		
		/**
		 * ��������� ���� � �������� ���������� ����� WW(��-��������� ��������)
		 */
		ViewControlsLayer viewControl = new ViewControlsLayer();
		viewControl.setEnabled(false);
		ApplicationTemplate.insertBeforeCompass(wwd, viewControl);	
		
		/**
		 * ��������� � ����� ��������� WMS �������� �������
		 */
		WmsLayerSettings wls = Workspace.getCurrent().currentProject.getWmsLayer();
		if(wls!=null){
			wls.apply(wwd, true);
		}
		/**
		 * ��������� � ����� ��������� ����� �������� �������
		 */
		LayersSettings ls = Workspace.getCurrent().currentProject.getLayers();
		ls.apply(wwd);
		
		/**
		 * ���� ��������� ������������� ���� WW - ������ ����� ������ ��������� ������� �� ����������
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
				 * ��������� ������� ����������(�� ����� ������ ��� �������������)
				 */
				wwd.removeGLEventListener(this);
				/**
				 * ������ ����� ���������� �� ����
				 */
				wwd.requestFocus();
				/**
				 * ������ ������ ����������
				 */
				setControls();
				/**
				 * ��������� ������ �� ������ �����
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
		 * ���������� ���������� � ���������� z/x
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
				 * ������� ���� �� ������� ESC(��� ������ 27)
				 */
				if(e.getKeyCode()==27){
					shell.getDisplay().asyncExec(new Runnable() {						
						@Override
						public void run() {
							shell.close();
							shell.dispose();
						}
					});
					e.consume();   // ����� ������� �� �������������� ������
					return;
				}
				/**
				 * ����� ������ ������� ������
				 */
				char code = e.getKeyChar();
				if(code=='x'){
					nextSlide();  //���������
					e.consume();
				}else if(code=='z'){
					prevSlide(); // ����������
					e.consume();
				}
			}
		});
		
	}
	
    /**
     * ������� ���� ��������� �����������
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
     * ���������� ������ �����
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
     * ������� � ������ ��� ���������� ����
     * @param slide
     */
    public void showSlide(Slide slide){
    	/**
    	 * ��������� ���������
    	 */
    	showAnnotation(slide);
    	
    	/**
    	 * �������� ��������� ���� �� ����� WW � ������������� �������� �������� ���������
    	 */
    	BasicOrbitView view = (BasicOrbitView) wwd.getView();
    	view.getViewInputHandler().stopAnimators();
    	Position curCenter = view.getCenterPosition();
    	Angle dist = LatLon.greatCircleAzimuth(curCenter, slide.position);
    	int time = (int)Math.round(Math.abs(1000*dist.degrees/slide.getMoveSpeed()));
        /**
         * ��������� �������� ������� � ������� ������ � �������� ��������
         * ��������������� ����������� ��� �� ��� 
         */
    	if(time<50)time=50;
    	view.addPanToAnimator(slide.position, Angle.ZERO, Angle.ZERO, slide.position.elevation, time, true);
    }
    
    /**
     * �������� �������� �������� ��� ��������� ������
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
    	 * �������� ������� �������� ����
    	 */
    	Position beginCenterPos = orbitView.getCenterPosition();
    	Angle beginHeading = orbitView.getHeading();
    	Angle beginPitch = orbitView.getPitch();
    	double beginZoom = orbitView.getZoom();    	
    	/**
    	 * ������� ����������� �������� �������� �� ����� � �����
    	 */
        FlyToOrbitViewAnimator panAnimator = FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView,
            beginCenterPos, endCenterPos, beginHeading, endHeading, beginPitch, endPitch,
            beginZoom, endZoom, timeToMove, altitudeMode);
        /**
         * ��������������� �������� �������� ������
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
         * ��������� �������� � ����
         */
        wwd.getView().getViewInputHandler().addAnimator(panAnimator);
        wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());
    }
        
    public void showAnnotation(Slide slide){
    	if(ann!=null){
    		/**
    		 * ������� ������� ���������
    		 */
    		annotationLayer.removeAnnotation(ann);
    		ann.dispose();
    		ann=null;
    	}
    	if(slide.annotation!=null){
       		/**
    		 * ����� ��������� �������� ������ � ������ �� ���� ���������
    		 */
    		ann = slide.annotation.getAnnotation(slide.position);
			annotationLayer.addAnnotation(ann);
    	}    	
    }
    
    
    /**
     * ������� �� ���������� �����
     */
    public void prevSlide(){
    	if(aw!=null){
    		/**
    		 * ������������� ������������
    		 */
    		aw.interrupt();
    		aw = null;
    	}
    	if(index == 0){
    		return;
    	}
    	/**
    	 * ��������� �� ����
    	 */
    	aw = new AutoWatcher(ViewScene.this, index, index-1);
    	aw.start();
    }
    
    /**
     * ������� �� ��������� �����
     */
    public void nextSlide(){
    	if(aw!=null){
    		/**
    		 * ������������� ������������
    		 */
    		aw.interrupt();
    		aw = null;
    	}
    	/**
    	 * �������� ����� ���������� ������
    	 */
    	int next = index + 1;
    	if(next>=slides.size()){
    		showFirstSlide(0);
    		return;    	
    	}else{
    		/**
        	 * ��������� �� ����
        	 */
        	aw = new AutoWatcher(ViewScene.this, index, next);
        	aw.start();
        	index++;
    	}    	
    }
    
    /**
     * ���� �� ������ �������� ��������?
     * @return
     */
    public boolean isAnimating(){
    	return wwd.getView().isAnimating();
    }
     
    /**
     * ��������� ������������ ����������� 
     */
    public void startAuto(){
    	if(aw==null){
    		/**
    		 * ���� ������������ ��� �� �������, ������� ����� �������������
			 * ������������� ����� ������ ����� �����������  - 
			 * �� �������� �� ����������    		 
    		 */
			aw = new AutoWatcher(ViewScene.this, 0, slides.size()-1);
			/**
			 * ��������� ������������
			 */
			aw.start();
		}	
    }
    
    /**
     * ���������� ������������ �����������
     */
    public void stopAuto(){
    	if(aw!=null){
    		aw.interrupt();
    		index = aw.getIndex();
    		showFirstSlide(index);
    		aw = null;
    	}
    }
    
    /**
     * ������ ������ ����������
     */
    public void setControls(){
	    
    	/**
    	 * ����� ������� ����� WW
    	 */
    	Rectangle r = wwd.getBounds();
    	/**
    	 * ������� ���� ��� ������ � ��������� � �����
    	 */
    	AnnotationLayer l = new AnnotationLayer();
    	ApplicationTemplate.insertBeforePlacenames(wwd, l);
    	
    	/**
    	 * ������� ��������� ����������� ������
    	 */
        AnnotationAttributes ca = new AnnotationAttributes();
        ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
        ca.setInsets(new Insets(6, 6, 6, 6));   // �������
        ca.setBorderWidth(0);
        ca.setCornerRadius(0);
        ca.setSize(new Dimension(64, 64));   // �������
        ca.setBackgroundColor(new Color(0, 0, 0, 0));
        ca.setImageOpacity(1);   // ������������
        ca.setScale(1);     // �������
   	
        /**
         * ������ ���������� �����
         */
    	left = new ScreenAnnotation("", new java.awt.Point(r.width/2-170, 30), ca);  //  ������� 170 px ����� ������, 30px �� ����
    	left.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
    	left.getAttributes().setImageSource(this.getClass().getResource("/icons/a_left.png"));
    	left.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(left);
     	
     	/**
     	 * ������ ��������� �����
     	 */
     	right = new ScreenAnnotation("", new java.awt.Point(r.width/2-100, 30), ca);  // 100px ����� ������, 30px �� ����
     	right.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	right.getAttributes().setImageSource(this.getClass().getResource("/icons/a_right.png"));
     	right.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(right);
     	
     	/**
     	 * ������ ������������ �����
     	 */
        play = new ScreenAnnotation("", new java.awt.Point(r.width/2-30, 30), ca);  // 30px ����� ������, 30px �� ����
        play.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
        play.getAttributes().setImageSource(this.getClass().getResource("/icons/a_play.png"));
        play.getAttributes().setSize(new Dimension(64, 64));
        l.addAnnotation(play);
     	
        /**
         * ������ ������������ ����
         */
     	stop = new ScreenAnnotation("", new java.awt.Point(r.width/2+40, 30), ca); // 40px ������ ������, 30px �� ����
     	stop.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	stop.getAttributes().setImageSource(this.getClass().getResource("/icons/a_stop.png"));
     	stop.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(stop);
     	
     	/**
     	 * ������ �����
     	 */
     	exit = new ScreenAnnotation("", new java.awt.Point(r.width/2+110, 30), ca); // 110 ������ ������ 30px �� ����
     	exit.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	exit.getAttributes().setImageSource(this.getClass().getResource("/icons/a_delete.png"));
     	exit.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(exit);
    	wwd.redraw();    	
    	
    	/**
    	 * ������ ���������� ������� �� ������
    	 */
    	wwd.addSelectListener(new SelectListener() {
			
			@Override
			public void selected(SelectEvent event) {
				MouseEvent me = event.getMouseEvent();
				/**
				 * ��������� ����� ������(1 - �����) � ���������� �������������(����� ������ ������ �������)
				 */
				if(me==null || me.getButton()!=1 || me.getModifiersEx()==0)return;
				/**
				 * ����� ������ ������
				 */
				ScreenAnnotation selectedObject = (ScreenAnnotation) event.getTopObject();
		        
		        if(selectedObject==play){
		        	// ������ ������ �������������
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							startAuto();							
						}
					});
		        }else if(selectedObject==left){
		        	// ������ ���������� �����
	        		shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							prevSlide();							
						}
					});
		        	
		        }else if(selectedObject==right){
		        	// ������ ��������� �����
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							nextSlide();							
						}
					});
		        }else if(selectedObject==exit){
		        	// ������ �����
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							finish();							
						}
					});
		        }else if(selectedObject==stop){
		        	// ������ ���� �������������
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							stopAuto();							
						}
					});
		        }
		        me.consume();  // ������� �� ������������ ������
			}
		});    	
    }
    
}
