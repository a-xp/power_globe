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
 * ����� ������������������� �����������
 * @author 1
 *
 */
public class AutoWatcher extends Thread{
	
	/**
	 * ������� �����
	 */
	private int index;
		
	/**
	 * �� ������ ������ �������
	 */
	private int toIndex;
	/**
	 * ����� ���������
	 */
	private ViewScene vs;
	
	/**
	 * ��������� ����� �������������� ���������
	 */
	@Override
	public void run() {
		/**
		 * ����������� ���������
		 */
		int step = (toIndex>index)?1:-1;
	
		try {
			Slide slide = vs.slides.get(index);
			vs.showFirstSlide(index);
			sleep(slide.getDelay());
			
			/**
			 * �������� �� ������� �� ��������� ������� �� ��������
			 */
			while(index!=toIndex){	
				index+=step;
				Slide prevSlide = slide; // ��������� ������� �����
				slide = vs.slides.get(index);  // ����� ������� �����
				/**
				 * �������� �������� �� ���� � ������ 
				 * � ����������� �� ����������� ��������(������-�����) - � �������� ��� �����������
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
					 * ���� ��� �����, �� ����������� ��������
					 */
			    	vs.hideAnnotation();
					vs.showSlide(slide);
					waitAnimation();
				}
				vs.showAnnotation(slide);
				/**
				 * ���� ������� ��������� � ������
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
	 * ���� ��������� �������� �����
	 * @throws InterruptedException
	 */
	protected void waitAnimation() throws InterruptedException{
		while(vs.isAnimating()){
			sleep(200);
		}
	}
	
	/**
	 * �������� ����������� ����
	 * @param coords
	 * @param slide
	 * @throws InterruptedException
	 */
    protected void pathAnimation(List<LatLon> coords, Slide slide) throws InterruptedException{
    	if(coords.size()==0)return;    	
		LatLon last = coords.get(0);		
		/**
		 * �������� �� ���� ������ � ��������� ��� ������ �������� ��������
		 */
		for(LatLon latlon: coords.subList(1, coords.size())){    		
			goToPosition(last, latlon, slide.getCameraPitch(), slide.getMoveSpeed(), slide.getTurnSpeed(), slide.getPosition().elevation);
			last = latlon;
		}
    }
    
    /**
     * �������� �������� � ����� ����
     * @param from
     * @param to
     */
    protected void goToPosition(LatLon from, LatLon to, double cameraPitch, double moveSpeed, double turnSpeed, double elevation)
    throws InterruptedException{
    	/**
    	 * ���� ��� � �����, �� �������
    	 */
    	if(LatLon.greatCircleDistance(from, to).degrees<0.1){
    		return;
    	}    	
    	BasicOrbitView view = (BasicOrbitView) vs.wwd.getView();
        view.getViewInputHandler().stopAnimators();

        /**
         * ������� ��������� 3� ����
         */
        Angle pitch = Angle.fromDegrees(cameraPitch);
        Angle heading = LatLon.greatCircleAzimuth(from, to);
        Angle curHeading = view.getHeading();
        Angle curPitch = view.getPitch();
        Double curZoom = view.getZoom();
        
        /**
         * ����������� ����� ��������� ������ �� ������ ���������� � ������
         */
        int setTime = Math.max((int)Math.round(100*curPitch.angularDistanceTo(pitch).degrees),
        		(int)Math.round(Math.abs(Math.log10(curZoom)-Math.log10(elevation)))*100);
        if(setTime>0){
        	/**
        	 * ��������� �������� - ���� 1 (������� �� ������ � ������ ������)
        	 */
        	vs.addPathAnimator(from, curHeading, pitch, elevation, setTime);
        	waitAnimation();
        }
        
        /**
         * ����������� ����� �������� � ������� ��������� �����
         */
        int turnTime = (int)Math.round(1000/turnSpeed*curHeading.angularDistanceTo(heading).degrees);
        if(turnTime>0){
        	/**
        	 * ��������� �������� - ���� 2 (������� � ������� ��������� �����)
        	 */
        	vs.addPathAnimator(from, heading, pitch, elevation, turnTime);
        	waitAnimation();
        }   
        
        /**
         * ����������� ����� �������� � ��������� �����
         */
        int flyTime = (int) Math.round(1000/moveSpeed*LatLon.greatCircleDistance(from, to).degrees);
    	if(flyTime>0){
    		/**
    		 * ��������� �������� - ���� 3 (������� �� ��������� ����� ����)
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
