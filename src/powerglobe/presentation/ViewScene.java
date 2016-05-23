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
 * ����� ���������� ����� ��������� �����������
 * @author 1
 *
 */
public class ViewScene {
	final int VIEW_ORBIT =1;
	final int VIEW_FLY = 2;
	
	protected Shell shell; // ���� SWT
	
	private AnnotationLayer namesLayer;
	private AnnotationLayer annotationLayer;   //���� ���������
	WorldWindowGLCanvas wwd;    // ����� WW
	//protected ViewControlsLayer viewControlsLayer;
	public List<Slide> slides;  // ������ ������� 
	protected int index = 0;   // ����� �������� ������
	private AutoWatcher aw;     // ����� ��� �������������
	private ScreenRelativeAnnotation name;
	private GlobeAnnotation ann;  // ��������� �������� ������  

	ControlPanel cp;
	
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
		 * ��������� ���� � ��������� ������
		 */
    	namesLayer = new AnnotationLayer();
    	ApplicationTemplate.insertBeforePlacenames(wwd, namesLayer);
		
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
				cp = new ControlPanel(ViewScene.this);
				cp.setControls();
				
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
    	
    	hideAnnotation();
    	/**
    	 * �������� ��������� ���� �� ����� WW � ������������� �������� �������� ���������
    	 */
    	BasicOrbitView view = (BasicOrbitView) wwd.getView();
    	view.getViewInputHandler().stopAnimators();
    	Position curCenter = view.getCenterPosition();
    	Angle dist = LatLon.greatCircleAzimuth(curCenter, slide.position);
    	System.out.println(dist);
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
        
    public void hideAnnotation(){
		/**
		 * ������� ������� ���������
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
     * ��������� ��������� ������ � ��������
     * @param slide
     */   
    public void showAnnotation(Slide slide){
    	hideAnnotation();
    	
    	if(slide.annotation!=null){
       		/**
    		 * ����� ��������� �������� ������ � ������ �� ���� ���������
    		 */
    		ann = slide.annotation.getAnnotation(slide.position);
			annotationLayer.addAnnotation(ann);
    	}   
    	
    	/**
    	 * ������ �������� ������
    	 */        
    	name = createNameAnnotation(slide.getTitle());
        namesLayer.addAnnotation(name); 
        wwd.redraw();
    }
    
    /**
     * ������� ��������� ������
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
	public Shell getShell() {
		return shell;
	}
	public WorldWindowGLCanvas getWwd() {
		return wwd;
	}
    
  
    
}
