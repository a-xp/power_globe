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
 * ����� �������� �������� WMS
 * @author 1
 *
 */
@XmlRootElement
public class WmsLayerSettings {
	/**
	 * �������� �������
	 */
	protected String serverName;
	/**
	 * ��� ����
	 */
	protected String layerName;
	/**
	 * ��� �����
	 */
	protected String styleName;
	/**
	 * ���� WW �� ������ ������ �������� (�� ��������� � xml)
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
	 * ���������� �������� WW ����
	 * @return
	 */
	public String getTitle(){
		/**
		 * �������� ���������� � ������������ �������
		 */
		WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
				
	    String[] lNames = layerName.split(",");
        String[] sNames = styleName != null ? styleName.split(",") : null;
    	
        /**
         * �������� �� ����� �����
         */
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            /**
             * �������� ���� �� ���� � ��� ��������
             */
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;
        	
            /**
             * �������� ����� �� ���� � ��� ��������
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
	 * ������� WW ���� ��� �������� ��������
	 * @return
	 */
	protected Object getComponent(){
		if(component!=null) return component;
		/**
		 * �������� ���������� � ���������� �������
		 */
		WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
        /**
         * ������� ������ ���������� ����
         */
		AVList params =  new AVListImpl();
        /**
         * ������������� ��� ���� � ���������
         */
        params.setValue(AVKey.LAYER_NAMES, layerName);
        /**
         * ������������� ��� ����� � ���������
         */       
        if (styleName != null)
            params.setValue(AVKey.STYLE_NAMES, styleName);
        /**
         * �������� ���������� � WMS ���� � ��������� � ������ ����������
         */
        WMSLayerCapabilities layerCaps = caps.getLayerByName(layerName);
        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs))
            params.setValue(AVKey.LAYER_ABSTRACT, abs);
        /**
         * ��������� ��������� ���������
         */
        params.setValue(AVKey.DISPLAY_NAME, getTitle());  //�������� ����
        params.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000); // ������� ���������� ��� ��������� � �������
        params.setValue(AVKey.URL_READ_TIMEOUT, 30000);  // ������� ������� ��� ������ � �������
        params.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
        params.setValue(AVKey.TILE_WIDTH, 512); // ������� �����
        params.setValue(AVKey.TILE_HEIGHT, 512);        
        try
        {
        	/**
        	 * ������� ������� ��� �������� ����
        	 */
            String factoryKey = getFactoryKeyForCapabilities(caps);   // ��� ������� WW
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey); //��������� ������� �� ���� 
            /**
             * ������� ����
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
	 * ���������|��������(enable) ��������� � �����
	 * @param wwd
	 * @param enable
	 */
	public void apply(WorldWindowGLCanvas wwd, boolean enable){
    	/**
    	 * ������� ������ �� ����������(������ ����)
    	 */
		getComponent();
		if (component instanceof Layer)
        {
			/**
			 * ���� �������� ����
			 */
            Layer layer = (Layer) component;
            LayerList layers = wwd.getModel().getLayers();

            layer.setEnabled(enable);

            if (enable)
            {
            	/**
            	 * ���� ����� ���������, ��������� ���� � WW
            	 */
                if (!layers.contains(layer))
                {
                    ApplicationTemplate.insertBeforePlacenames(wwd, layer);
                }
            }
            else
            {
            	/**
            	 * ���� ����� ��������, ������� ����
            	 */
                layers.remove(layer);
            }
        }
        else if (component instanceof ElevationModel)
        {
        	/**
        	 * ���� �� ���������� ��������� ������ �����
        	 */
            ElevationModel model = (ElevationModel) component;
            CompoundElevationModel compoundModel =
                (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();

            if (enable)
            {
            	/**
            	 * ��������� ��� ������ � ������� ������ WW,
            	 * ���� �� ��� ��� ���
            	 */
                if (!compoundModel.getElevationModels().contains(model))
                    compoundModel.addElevationModel(model);
            }
        }
        wwd.redraw();
	}
	
	/**
	 * ���������� ��� �������, ������� ���������� ��������� �������� ��� ������ ��������
	 * ������ ����� ��� ����
	 * @param caps
	 * @return
	 */
	public static String getFactoryKeyForCapabilities(WMSCapabilities caps)
    {
        boolean hasApplicationBilFormat = false;
        
        /**
         * ��������� ����� ����� ���������� ������ ������
         */
        Set<String> formats = caps.getImageFormats();
        for (String s : formats)
        {
        	/**
        	 * ���� ���������� ������� application/bil,
        	 * �� ��� ���� - ��� ������ �����
        	 */
            if (s.contains("application/bil"))
            {
                hasApplicationBilFormat = true;
                break;
            }
        }
    	
        /**
         * ���������� ��� WW �������: ������� ����� ��� ������� ������� ����� - � ����������� �� �������� ������� ������ �������
         */
        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }

	/**
	 * �������������� ����� ��������� �������� ������� ������:
	 * ���������� �� �������� �������, ����� ���� � �����
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
