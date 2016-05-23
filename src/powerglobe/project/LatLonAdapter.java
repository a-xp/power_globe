package powerglobe.project;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import gov.nasa.worldwind.geom.LatLon;

public class LatLonAdapter extends XmlAdapter<AdaptedLatLon, LatLon> {

	@Override
	public AdaptedLatLon marshal(LatLon arg0) throws Exception {
		return new AdaptedLatLon(arg0.latitude.degrees, arg0.longitude.degrees);
	}

	@Override
	public LatLon unmarshal(AdaptedLatLon arg0) throws Exception {
		return LatLon.fromDegrees(arg0.lat, arg0.lon);
	}
}
