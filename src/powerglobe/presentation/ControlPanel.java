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
	 * Кнопки
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
     * Рисуем кнопки управления
     */
    public void setControls(){
	    
    	/**
    	 * Берем размеры сцены WW
    	 */
    	Rectangle r = wwd.getBounds();
    	/**
    	 * Создаем слой для кнопок и добавляем в сцену
    	 */
    	AnnotationLayer l = new AnnotationLayer();
    	ApplicationTemplate.insertBeforePlacenames(wwd, l);
    	
    	/**
    	 * Базовые настройки отображения кнопки
    	 */
        AnnotationAttributes ca = new AnnotationAttributes();
        ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
        ca.setInsets(new Insets(6, 6, 6, 6));   // отступы
        ca.setBorderWidth(0);
        ca.setCornerRadius(0);
        ca.setSize(new Dimension(64, 64));   // размеры
        ca.setBackgroundColor(new Color(0, 0, 0, 0));
        ca.setImageOpacity(1);   // прозрачность
        ca.setScale(1);     // Масштаб
   	
        /**
         * Кнопка Предыдущий слайд
         */
    	left = new ScreenAnnotation("", new java.awt.Point(r.width/2-170, 30), ca);  //  ставить 170 px левее центра, 30px от низа
    	left.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
    	left.getAttributes().setImageSource(this.getClass().getResource("/icons/a_left.png"));
    	left.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(left);
     	
     	/**
     	 * Кнопка Следующий слайд
     	 */
     	right = new ScreenAnnotation("", new java.awt.Point(r.width/2-100, 30), ca);  // 100px левее центра, 30px от низа
     	right.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	right.getAttributes().setImageSource(this.getClass().getResource("/icons/a_right.png"));
     	right.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(right);
     	
     	/**
     	 * Кнопка Автопросмотр Старт
     	 */
        play = new ScreenAnnotation("", new java.awt.Point(r.width/2-30, 30), ca);  // 30px левее центра, 30px от низа
        play.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
        play.getAttributes().setImageSource(this.getClass().getResource("/icons/a_play.png"));
        play.getAttributes().setSize(new Dimension(64, 64));
        l.addAnnotation(play);
     	
        /**
         * Кнопка Автопросмотр Стоп
         */
     	stop = new ScreenAnnotation("", new java.awt.Point(r.width/2+40, 30), ca); // 40px Правее центра, 30px От низа
     	stop.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	stop.getAttributes().setImageSource(this.getClass().getResource("/icons/a_stop.png"));
     	stop.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(stop);
     	     	
     	/**
     	 * Кнопка Выход
     	 */
     	exit = new ScreenAnnotation("", new java.awt.Point(r.width/2+110, 30), ca); // 110 Правее центра 30px от низа
     	exit.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
     	exit.getAttributes().setImageSource(this.getClass().getResource("/icons/a_delete.png"));
     	exit.getAttributes().setSize(new Dimension(64, 64));
     	l.addAnnotation(exit);
    	wwd.redraw();    	
    	
    	/**
    	 * Ставим обработчик нажатия на кнопки
    	 */
    	wwd.addSelectListener(new SelectListener() {
			
			@Override
			public void selected(SelectEvent event) {
				MouseEvent me = event.getMouseEvent();
				/**
				 * Проверяем номер кнопки(1 - левая) и отсутствие модификаторов(нужно только первое событие)
				 */
				if(me==null || me.getButton()!=1 || me.getModifiersEx()==0)return;
				/**
				 * Какая кнопка нажата
				 */
				ScreenAnnotation selectedObject = (ScreenAnnotation) event.getTopObject();
		        
		        if(selectedObject==play){
		        	// нажали запуск автопросмотра
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.startAuto();							
						}
					});
		        }else if(selectedObject==left){
		        	// нажали предыдущий слайд
	        		shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.prevSlide();							
						}
					});
		        	
		        }else if(selectedObject==right){
		        	// нажали следующий слайд
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.nextSlide();							
						}
					});
		        }else if(selectedObject==exit){
		        	// нажали выход
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.finish();							
						}
					});
		        }else if(selectedObject==stop){
		        	// нажали стоп автопросмотра
		        	shell.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							vs.stopAuto();							
						}
					});
		        }
		        me.consume();  // событие не обрабатывать дальше
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
