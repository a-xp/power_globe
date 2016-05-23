package powerglobe.project;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

/**
 * ����������� Position �� WorldWind � ���������� ������ AdaptedPosition � ��������
 * @author 1
 *
 */
public class PositionAdapter extends XmlAdapter<AdaptedPosition, Position> {

	/**
	 * ����������� Position � AdaptedPosition
	 */
	@Override
	public AdaptedPosition marshal(Position position) throws Exception {
		return new AdaptedPosition(position.elevation, position.latitude.degrees, position.longitude.degrees);
	}

	/**
	 * ����������� AdaptedPosition � Position
	 */
	@Override
	public Position unmarshal(AdaptedPosition position) throws Exception {
		return new Position(Angle.fromDegrees(position.latitude), Angle.fromDegrees(position.longitude), position.elevation);
	}

}
