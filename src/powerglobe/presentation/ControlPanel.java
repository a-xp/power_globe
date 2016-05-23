package powerglobe.presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.eclipse.swt.widgets.Shell;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class ControlPanel {
	ViewScene vs;
	WorldWindowGLCanvas wwd;
	Shell shell;
	/**
	 * ������
	 */
	protected ScreenAnnotation left;
	protected ScreenAnnotation right;
	protected ScreenAnnotation play;
	protected ScreenAnnotation stop;
	protected ScreenAnnotation exit;
	
	public ControlPanel(ViewScene vs){
		this.vs = vs;
		wwd = vs.getWwd();
		shell = vs.getShell();
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
							vs.startAuto();							
						}
					});
		        }else if(selectedObject==left){
		        	// ������ ���������� �����
	        		shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.prevSlide();							
						}
					});
		        	
		        }else if(selectedObject==right){
		        	// ������ ��������� �����
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.nextSlide();							
						}
					});
		        }else if(selectedObject==exit){
		        	// ������ �����
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.finish();							
						}
					});
		        }else if(selectedObject==stop){
		        	// ������ ���� �������������
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.stopAuto();							
						}
					});
		        }
		        me.consume();  // ������� �� ������������ ������
			}
		});   
    	
    	Thread animationCheck = new Thread(new Runnable() {			
			@Override
			public void run() {
				while(!shell.isDisposed()){
					if(wwd.getView().isAnimating()){
						animationStarted();
					}else{
						animationStopped();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
    	
    	animationCheck.start();
    }

    public void animationStarted(){
    	hide(play);
    	hide(left);
    	hide(right);
    	show(stop);
    }
    
    public void animationStopped(){
    	hide(stop);
    	show(left);
    	show(right);
    	show(play);
    }
    
    protected void show(Annotation ann){
    	ann.setMinActiveAltitude(-1);
    }
    
    protected void hide(Annotation ann){
    	ann.setMinActiveAltitude(1e12);
    }
    
}
