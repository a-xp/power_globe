package powerglobe.project;

/**
 * Упрощенный класс Position для сохранения в XML
 * @author 1
 *
 */
public class AdaptedPosition {
	public double elevation;  // высота 
	public double latitude;   // долгота
	public double longitude;  // широта
	public AdaptedPosition(double elevation, double latitude, double longitude) {
		this.elevation = elevation;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public AdaptedPosition() {
	}
	
}
