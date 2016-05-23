package powerglobe.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.Layer;

/**
 * ��������� ����� �����
 * @author 1
 *
 */
@XmlRootElement
public class LayersSettings {
	/**
	 * ������ �����, ������� ����������� ���� �������
	 */
	protected static List<String> managedLayers = Arrays.asList(new String[]{
			"Stars", "Atmosphere", "NASA Blue Marble Image", "Blue Marble May 2004", 
			"Open Street Map", "Earth at Night", "Place Names", "World Map", "Scale bar", "Lat-Lon Graticule",
			"View Controls", "Compass"
		});
	
	/**
	 * ������ �����, ������� ����� �������� � �����
	 */
	@XmlElement
	protected Set<String> enabled;
		
	public LayersSettings(){
		/**
		 * ������������� ������ ��������� ����� ��-���������
		 */
		enabled = new HashSet<>();
		enabled.addAll(Arrays.asList(new String[]{
				"Stars", "Atmosphere", "NASA Blue Marble Image","Blue Marble May 2004",
				"Place Names", "World Map", "Scale bar", "Compass"
		}));
	}
	
	public LayersSettings(Set<String> enabled){
		this.enabled = enabled;			
	}
	
	public List<String> getLayers(){
		return managedLayers;
	}
	/**
	 * ����������� ���� ����� 
	 * @param wwd
	 */
	public void apply(WorldWindowGLCanvas wwd){
		/**
		 * �������� �� ���� ����� �����
		 */
		for(Layer l: wwd.getModel().getLayers()){
			String name = l.getName();
			/**
			 * ���� ���� ��������� � ������ ����������� �����,
			 * �� ��������(���� �� � ������ ���������)/���������(���� ��� � ������ ���������)
			 * 
			 */
			if(managedLayers.contains(name)){
				l.setEnabled(enabled.contains(name));
			}
		}
	}

	public void setEnabled(Set<String> enabled) {
		this.enabled = enabled;
	}

	/**
	 * �������������� ����� ��������� �������� ������� ������:
	 * ���������� �� ������ ����������� �����
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null)return false;
		Set<String> set2 = ((LayersSettings)obj).enabled;
		return enabled.equals(set2);
	}

	public boolean isEnabled(String name) {
		return enabled.contains(name);
	}	
}
