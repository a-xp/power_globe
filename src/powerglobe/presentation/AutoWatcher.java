package powerglobe.presentation;

import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Класс автовоспроизведения презентации
 * @author 1
 *
 */
public class AutoWatcher extends Thread{
	
	/**
	 * Текущий слайд
	 */
	private int index;
		
	/**
	 * До какого слайда крутить
	 */
	private int toIndex;
	/**
	 * Сцена просмотра
	 */
	private ViewScene vs;
	
	/**
	 * Запускает поток автоуправления анимацией
	 */
	@Override
	public void run() {
		/**
		 * Направление прокрутки
		 */
		int step = (toIndex>index)?1:-1;
	
		try {
			Slide slide = vs.slides.get(index);
			vs.showFirstSlide(index);
			sleep(slide.getDelay());
			
			/**
			 * Проходим по слайдам от начальной позиции до конечной
			 */
			while(index!=toIndex){	
				index+=step;
				Slide prevSlide = slide; // сохраняем прошлый слайд
				slide = vs.slides.get(index);  // берем текущий слайд
				/**
				 * Проверям включены ли пути у слайда 
				 * В зависимости от направления перехода(вперед-назад) - у текущего или предыдущего
				 */
				if( (step>0 && slide.enablePath) || (step<0 && prevSlide.enablePath )){
					//vs.hideAnnotation();
					if(step>0){
						List<LatLon> coord = Workspace.getCurrent().currentProject.getPath(index);
						pathAnimation(coord, slide);
					}else{
						List<LatLon> coord = Workspace.getCurrent().currentProject.getPath(index+1);
						Collections.reverse(coord);
						pathAnimation(coord, prevSlide);
					}
				}else{
					/**
					 * Если нет путей, то стандартная анимация
					 */
			    	vs.hideAnnotation();
					vs.showSlide(slide);
					waitAnimation();
				}
				vs.showAnnotation(slide);
				/**
				 * Ждем сколько прописано в слайде
				 */
				sleep(slide.getDelay());
			}
			vs.showSlide(vs.slides.get(index));
			waitAnimation();
			vs.stopAuto();	
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Ждем окончания анимации сцены
	 * @throws InterruptedException
	 */
	protected void waitAnimation() throws InterruptedException{
		while(vs.isAnimating()){
			sleep(200);
		}
	}
	
	/**
	 * Анимация прохождения пути
	 * @param coords
	 * @param slide
	 * @throws InterruptedException
	 */
    protected void pathAnimation(List<LatLon> coords, Slide slide) throws InterruptedException{
    	if(coords.size()==0)return;    	
		LatLon last = coords.get(0);		
		/**
		 * Проходим по всем точкам и запускаем для каждой анимацию перехода
		 */
		for(LatLon latlon: coords.subList(1, coords.size())){    		
			goToPosition(last, latlon, slide.getCameraPitch(), slide.getMoveSpeed(), slide.getTurnSpeed(), slide.getPosition().elevation);
			last = latlon;
		}
    }
    
    /**
     * Анимация перехода к точке пути
     * @param from
     * @param to
     */
    protected void goToPosition(LatLon from, LatLon to, double cameraPitch, double moveSpeed, double turnSpeed, double elevation)
    throws InterruptedException{
    	/**
    	 * Если уже в точке, то выходим
    	 */
    	if(LatLon.greatCircleDistance(from, to).degrees<0.1){
    		return;
    	}    	
    	BasicOrbitView view = (BasicOrbitView) vs.wwd.getView();
        view.getViewInputHandler().stopAnimators();

        /**
         * Текущие параметры 3Д вида
         */
        Angle pitch = Angle.fromDegrees(cameraPitch);
        Angle heading = LatLon.greatCircleAzimuth(from, to);
        Angle curHeading = view.getHeading();
        Angle curPitch = view.getPitch();
        Double curZoom = view.getZoom();
        
        /**
         * Высчитываем время установки камеры на нужное отклонение и высоту
         */
        int setTime = Math.max((int)Math.round(100*curPitch.angularDistanceTo(pitch).degrees),
        		(int)Math.round(Math.abs(Math.log10(curZoom)-Math.log10(elevation)))*100);
        if(setTime>0){
        	/**
        	 * Запускаем анимацию - фаза 1 (переход на высоту и наклон камеры)
        	 */
        	vs.addPathAnimator(from, curHeading, pitch, elevation, setTime);
        	waitAnimation();
        }
        
        /**
         * Высчитываем время поворота в сторону следующей точки
         */
        int turnTime = (int)Math.round(1000/turnSpeed*curHeading.angularDistanceTo(heading).degrees);
        if(turnTime>0){
        	/**
        	 * Запускаем анимацию - фаза 2 (поворот в сторону следующей точки)
        	 */
        	vs.addPathAnimator(from, heading, pitch, elevation, turnTime);
        	waitAnimation();
        }   
        
        /**
         * Высчитываем время перехода к следующей точке
         */
        int flyTime = (int) Math.round(1000/moveSpeed*LatLon.greatCircleDistance(from, to).degrees);
    	if(flyTime>0){
    		/**
    		 * Запускаем анимацию - фаза 3 (переход на следующую точку пути)
    		 * 
    		 */
    		vs.addPathAnimator(to, heading, pitch, elevation, flyTime);
    		waitAnimation();
        }
	}
	
	public AutoWatcher(ViewScene vs) {
		this.vs = vs;
	}
	
	public AutoWatcher(ViewScene vs, int from, int to) {
		this.vs = vs;
		index = from;
		toIndex = to; 
	}
	
	public void play(int from, int to){
		index = from;
		toIndex = to;
	}
	
	public int getIndex(){
		return index;
	}
	
		
}
