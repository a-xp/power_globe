package powerglobe.project;


import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import gov.nasa.worldwind.geom.Position;
import powerglobe.wwd.annotations.AbstractAnnotation;

/**
 * Класс отдельного слайда
 * @author 1
 *
 */
public class Slide {
	/**
	 * Позиция в списке
	 */
	public int index;
	/**
	 * Координаты на сфере, в xml сохраняется в через упрощенный класс AdaptedPosition
	 */
	@XmlJavaTypeAdapter(PositionAdapter.class)
	public Position position;
	/**
	 * Название слайда
	 */
	public String title;
	/**
	 * Тип аннотации (подкласс аннотации выбирается по названию тега)
	 */
	@XmlElementRef
	public AbstractAnnotation annotation;
	/**
	 * Задержка на слайде при просмотре в мс
	 */
	public int delay = 2000;
	/**
	 * Время перемещения к слайду (градусов в секунду) 
	 */
	public double moveSpeed = 5.0;
	/**
	 * Включить пути к этому слайду
	 */
	public boolean enablePath = false;
	/**
	 * Наклон камеры к вертикали В градусах
	 */
	public double cameraPitch = 25.0;
	/**
	 */
	public double turnSpeed = 240.0;
	
	public Slide(Position position, String title, AbstractAnnotation annotation,
			 int index) {
		this.position = position;
		this.title = title;
		this.index = index;
		this.annotation = annotation;
	}	
	

	public Slide(int index, Position position, String title, AbstractAnnotation annotation, int delay, double moveSpeed,
			boolean enablePath, double cameraPitch, double turnSpeed) {
		super();
		this.index = index;
		this.position = position;
		this.title = title;
		this.annotation = annotation;
		this.delay = delay;
		this.moveSpeed = moveSpeed;
		this.enablePath = enablePath;
		this.cameraPitch = cameraPitch;
		this.turnSpeed = turnSpeed;
	}

	public Slide(){
		
	}

	public int getIndex() {
		return index;
	}

	public Position getPosition() {
		return position;
	}

	public String getTitle() {
		return title;
	}

	public AbstractAnnotation getAnnotation() {
		return annotation;
	}

	public int getDelay() {
		return delay;
	}


	public double getMoveSpeed() {
		return moveSpeed;
	}


	public boolean isEnablePath() {
		return enablePath;
	}


	public double getCameraPitch() {
		return cameraPitch;
	}


	public double getTurnSpeed() {
		return turnSpeed;
	}

}
