package powerglobe.project;

/**
 * ���������� ����� Position ��� ���������� � XML
 * @author 1
 *
 */
public class AdaptedPosition {
	public double elevation;  // ������ 
	public double latitude;   // �������
	public double longitude;  // ������
	public AdaptedPosition(double elevation, double latitude, double longitude) {
		this.elevation = elevation;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public AdaptedPosition() {
	}
	
}
