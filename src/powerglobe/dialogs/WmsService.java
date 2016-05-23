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
 * Класс хранит список известных wms Серверов
 * @author 1
 *
 */
public class WmsService {
	
	/**
	 * Известные WMS сервера
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
	 * Кеш информации о серверах
	 */
	protected Map<String, WMSCapabilities> capsCache = new HashMap<>();	
	
	
	/**
	 * Возвращает онформацию о сервере по его названию 
	 * @param serverName
	 * @return
	 */
	public WMSCapabilities getByServerName(String serverName){
		/**
		 * Проверяем есть ли в кеше
		 */
		if(capsCache.containsKey(serverName))return capsCache.get(serverName);
        try
        {
        	/**
        	 * Загружаем инфу о сервере(стандартные методы WW)
        	 */
        	WMSCapabilities caps = WMSCapabilities.retrieve(serverURLs.get(serverName));
            caps.parse();
            /**
             * Сохраняем в кеш
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
	 * Возвращает названия известных серверов
	 * @return
	 */
	public List<String> getServers(){
		List<String> result = new ArrayList<>();
		result.addAll(serverURLs.keySet());
		return result;
	}
}
