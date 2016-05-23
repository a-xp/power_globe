package powerglobe.dialogs;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import powerglobe.project.Workspace;

/**
 * Класс хранения настроек WMS
 * @author 1
 *
 */
@XmlRootElement
public class WmsLayerSettings {
	/**
	 * Название сервера
	 */
	protected String serverName;
	/**
	 * Код слоя
	 */
	protected String layerName;
	/**
	 * Код стиля
	 */
	protected String styleName;
	/**
	 * Слой WW на основе данных настроек (не сохряется в xml)
	 */
	@XmlTransient
	protected Object component;
	
	public WmsLayerSettings(String serverName, String layerName, String styleName) {
		super();
		this.serverName = serverName;
		this.layerName = layerName;
		this.styleName = styleName;
	}

	public WmsLayerSettings(){
		
	}	

	public String getServerName() {
		return serverName;
	}

	public String getLayerName() {
		return layerName;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	/**
	 * Возвращает название WW слоя
	 * @return
	 */
	public String getTitle(){
		/**
		 * Получаем информацию о возможностях сервера
		 */
		WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
				
	    String[] lNames = layerName.split(",");
        String[] sNames = styleName != null ? styleName.split(",") : null;
    	
        /**
         * Проходим по кодам слоев
         */
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            /**
             * Получаем слой по коду и его название
             */
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;
        	
            /**
             * Получаем стиль по коду и его название
             */
            String styleName = sNames[i];
            WMSLayerStyle style = lc.getStyleByName(styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }
        	
        return sb.toString();
	}
	
	/**
	 * Создаем WW слой для заданных настроек
	 * @return
	 */
	protected Object getComponent(){
		if(component!=null) return component;
		/**
		 * Получаем информацию о настройках сервера
		 */
		WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
        /**
         * Создаем список параметров слоя
         */
		AVList params =  new AVListImpl();
        /**
         * Устанавливаем код слоя в параметры
         */
        params.setValue(AVKey.LAYER_NAMES, layerName);
        /**
         * Устанавливаем код стиля в параметры
         */       
        if (styleName != null)
            params.setValue(AVKey.STYLE_NAMES, styleName);
        /**
         * Получаем информацию о WMS слое и сохранеям в список параметров
         */
        WMSLayerCapabilities layerCaps = caps.getLayerByName(layerName);
        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs))
            params.setValue(AVKey.LAYER_ABSTRACT, abs);
        /**
         * Добавляем остальные параметры
         */
        params.setValue(AVKey.DISPLAY_NAME, getTitle());  //Название слоя
        params.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000); // Таймаут соединения при обращении к серверу
        params.setValue(AVKey.URL_READ_TIMEOUT, 30000);  // Таймаут запроса при чтении с сервера
        params.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
        params.setValue(AVKey.TILE_WIDTH, 512); // Размеры тайла
        params.setValue(AVKey.TILE_HEIGHT, 512);        
        try
        {
        	/**
        	 * Создаем фабрику для создания слоя
        	 */
            String factoryKey = getFactoryKeyForCapabilities(caps);   // Код фабрики WW
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey); //Получение фабрики по коду 
            /**
             * Создаем слой
             */            
            component = factory.createFromConfigSource(caps, params);
            return component;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
	}
	
	/**
	 * Применяем|Отменяем(enable) настройки к сцене
	 * @param wwd
	 * @param enable
	 */
	public void apply(WorldWindowGLCanvas wwd, boolean enable){
    	/**
    	 * Создаем объект по настройкам(обычно слой)
    	 */
		getComponent();
		if (component instanceof Layer)
        {
			/**
			 * Если создался слой
			 */
            Layer layer = (Layer) component;
            LayerList layers = wwd.getModel().getLayers();

            layer.setEnabled(enable);

            if (enable)
            {
            	/**
            	 * Если нужно применить, добавляем слой к WW
            	 */
                if (!layers.contains(layer))
                {
                    ApplicationTemplate.insertBeforePlacenames(wwd, layer);
                }
            }
            else
            {
            	/**
            	 * Если нужно отменить, удаляем слой
            	 */
                layers.remove(layer);
            }
        }
        else if (component instanceof ElevationModel)
        {
        	/**
        	 * Если по настройкам создалась модель высот
        	 */
            ElevationModel model = (ElevationModel) component;
            CompoundElevationModel compoundModel =
                (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

            if (enable)
            {
            	/**
            	 * Добавляем эту модель к расчету высоты WW,
            	 * если ее еще там нет
            	 */
                if (!compoundModel.getElevationModels().contains(model))
                    compoundModel.addElevationModel(model);
            }
        }
        wwd.redraw();
	}
	
	/**
	 * Возвращает код фабрики, которая занимается созданием объектов для данных настроек
	 * Модель высот или слой
	 * @param caps
	 * @return
	 */
	public static String getFactoryKeyForCapabilities(WMSCapabilities caps)
    {
        boolean hasApplicationBilFormat = false;
        
        /**
         * Проверяем какие файлы возвращает данный сервер
         */
        Set<String> formats = caps.getImageFormats();
        for (String s : formats)
        {
        	/**
        	 * Если возвращает форматы application/bil,
        	 * то его слои - это модели высот
        	 */
            if (s.contains("application/bil"))
            {
                hasApplicationBilFormat = true;
                break;
            }
        }
    	
        /**
         * Возвращаем код WW фабрики: фабрика слоев или фабрика моделей высот - в зависимости от проверки формата ответа сервера
         */
        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }

	/**
	 * Перезаписываем метод сравнения объектов данного класса:
	 * сравниваем по названию сервера, кодам слоя и стиля
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof WmsLayerSettings))return false;
		WmsLayerSettings wls = (WmsLayerSettings)obj;
		if(!wls.serverName.equals(serverName) || !wls.layerName.equals(layerName))return false;
		if(styleName==null){
			return wls.styleName==null;
		}else{
			return styleName.equals(wls.styleName);
		}
	}
	
	
}
