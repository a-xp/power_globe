package powerglobe.project;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

/**
 * Преобразует Position из WorldWind в упрощенную версию AdaptedPosition и наоборот
 * @author 1
 *
 */
public class PositionAdapter extends XmlAdapter<AdaptedPosition, Position> {

	/**
	 * Преобразует Position в AdaptedPosition
	 */
	@Override
	public AdaptedPosition marshal(Position position) throws Exception {
		return new AdaptedPosition(position.elevation, position.latitude.degrees, position.longitude.degrees);
	}

	/**
	 * Преобразует AdaptedPosition в Position
	 */
	@Override
	public Position unmarshal(AdaptedPosition position) throws Exception {
		return new Position(Angle.fromDegrees(position.latitude), Angle.fromDegrees(position.longitude), position.elevation);
	}

}
