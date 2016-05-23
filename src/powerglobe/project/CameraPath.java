package powerglobe.project;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

@XmlRootElement
public class CameraPath {
	final int WW_ANIMATOR = 1;
	final int FOLLOW_PATH_ANIMATOR =2;
	
	@XmlJavaTypeAdapter(LatLonAdapter.class)
	public List<LatLon> positions = new ArrayList<>();
	public int animator = WW_ANIMATOR;
	
	public CameraPath() {
	}

	public CameraPath(List<LatLon> positions) {
		this.positions = positions;
	}
	
	public static CameraPath fromPath(List<LatLon> positions){
		if(positions.size()>2){
			return new CameraPath(new ArrayList<>(positions.subList(1, positions.size()-1)));
		}else{
			return new CameraPath(null);
		}		
	}
	
	public static List<LatLon> toPath(CameraPath camPath, LatLon from, LatLon to){
		List<LatLon> result = new ArrayList<>();
		result.add(from);
		if(camPath!=null){
			result.addAll(camPath.positions);
		}
		result.add(to);
		return result;
	}	
}
