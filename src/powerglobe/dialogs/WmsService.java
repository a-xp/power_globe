package powerglobe.dialogs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

/**
 * ����� ������ ������ ��������� wms ��������
 * @author 1
 *
 */
public class WmsService {
	
	/**
	 * ��������� WMS �������
	 */
	protected Map<String, URI> serverURLs = new HashMap<>();
	{
		try {
			serverURLs.put("NASA", new URI("http://neowms.sci.gsfc.nasa.gov/wms/wms"));
			serverURLs.put("SEDAC", new URI("http://sedac.ciesin.columbia.edu/geoserver/wcs"));
			serverURLs.put("JPL on Moon", new URI("http://onmoon.jpl.nasa.gov/wms.cgi?"));
			//serverURLs.put("Map-a-Planet Mars", new URI("http://www.mapaplanet.com/explorer-bin/imageMaker.cgi?map=Mars&"));
		} catch (URISyntaxException e) {			
			e.printStackTrace();
		}
	}
	/**
	 * ��� ���������� � ��������
	 */
	protected Map<String, WMSCapabilities> capsCache = new HashMap<>();	
	
	
	/**
	 * ���������� ���������� � ������� �� ��� �������� 
	 * @param serverName
	 * @return
	 */
	public WMSCapabilities getByServerName(String serverName){
		/**
		 * ��������� ���� �� � ����
		 */
		if(capsCache.containsKey(serverName))return capsCache.get(serverName);
        try
        {
        	/**
        	 * ��������� ���� � �������(����������� ������ WW)
        	 */
        	WMSCapabilities caps = WMSCapabilities.retrieve(serverURLs.get(serverName));
            caps.parse();
            /**
             * ��������� � ���
             */
            capsCache.put(serverName, caps);
            return caps;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * ���������� �������� ��������� ��������
	 * @return
	 */
	public List<String> getServers(){
		List<String> result = new ArrayList<>();
		result.addAll(serverURLs.keySet());
		return result;
	}
}
