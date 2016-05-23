package powerglobe.project;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AdaptedLatLon {
	public double lat;
	public double lon;
	
	public AdaptedLatLon(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}
	
	public AdaptedLatLon(){
		
	}
	
}
