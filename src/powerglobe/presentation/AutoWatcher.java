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
			
			while(index!=toIndex){	
				index+=step;
				Slide prevSlide = slide; // сохраняем прошлый слайд
				slide = vs.slides.get(index);  // берем текущий слайд
				/**
				 * Проверям включены ли пути у слайда 
				 * В зависимости от направления перехода(вперед-назад) - у текущего или предыдущего
				 */
				if( (step>0 && slide.enablePath) || (step<0 && prevSlide.enablePath )){
					vs.hideAnnotation();
					if(step>0){
						List<LatLon> coord = Workspace.getCurrent().currentProject.getPath(index);
						pathAnimation(coord, slide);
					}else{
						List<LatLon> coord = Workspace.getCurrent().currentProject.getPath(index+1);
						Collections.reverse(coord);
						pathAnimation(coord, prevSlide);
					}
				}else{
					vs.showSlide(slide);
					waitAnimation();
				}
				vs.showAnnotation(slide);
				/**
				 * Ждем сколько прописано в слайде
				 */
				sleep(slide.getDelay());
			}	
			vs.stopAuto();	
		} catch (InterruptedException e) {
			return;
		}
	}

	protected void waitAnimation() throws InterruptedException{
		while(vs.isAnimating()){
			sleep(200);
		}
	}
	
    protected void pathAnimation(List<LatLon> coords, Slide slide) throws InterruptedException{
    	if(coords.size()==0)return;    	
		LatLon last = coords.get(0);		
		//Position curEye = vs.wwd.getView().getCurrentEyePosition();
    	for(LatLon latlon: coords.subList(1, coords.size())){    		
			goToPosition(last, latlon, slide.getCameraPitch(), slide.getMoveSpeed(), slide.getTurnSpeed(), slide.getPosition().elevation);
			waitAnimation();
			last = latlon;
		}
    }
    
    /**
     * Анимация перехода по пути
     * @param from
     * @param to
     */
    protected void goToPosition(LatLon from, LatLon to, double pitch, double moveSpeed, double turnSpeed, double elevation)
    throws InterruptedException{
    	if(LatLon.greatCircleDistance(from, to).degrees<0.1){
    		return;
    	}    	
    	BasicOrbitView view = (BasicOrbitView) vs.wwd.getView();
        view.getViewInputHandler().stopAnimators();
        /**
         * Запускаем анимацию переход к позиции слайда с заданным временем 
         */
        Angle curHeading = view.getHeading();        
        Angle heading = LatLon.greatCircleAzimuth(from, to);
        int turnTime = (int) Math.round(1000/turnSpeed*heading.angularDistanceTo(curHeading).degrees);
        int flyTime = (int) Math.round(1000/moveSpeed*LatLon.greatCircleDistance(from, to).degrees);
        if(turnTime>0){
        	vs.addPathAnimator(new Position(from, 0), heading, Angle.fromDegrees(pitch), elevation, turnTime, true);
        }
    	waitAnimation(); 
    	if(flyTime>0){
    		vs.addPathAnimator(new Position(to, 0), heading, Angle.fromDegrees(pitch), elevation, flyTime, true);
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
