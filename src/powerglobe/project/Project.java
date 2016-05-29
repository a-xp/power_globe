package powerglobe.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nasa.worldwind.geom.LatLon;
import powerglobe.dialogs.LayersSettings;
import powerglobe.dialogs.WmsLayerSettings;

/**
 * ����� ������� Power Globe
 *   
 * @author 1
 *
 */
@XmlRootElement
public class Project {
	@XmlElement
	public String name = "Unnamed";  //�������� ������� (�� ������������)
	@XmlElement
	public List<Slide> slides = new ArrayList<>();  // ������ �������
	@XmlElement
	public WmsLayerSettings wmsLayer;   // ��������� Wms
	@XmlElement
	public LayersSettings layers = new LayersSettings();   //��������� ����� ����� WW
	@XmlElement
	public Map<Integer, CameraPath> paths = new HashMap<>(); // ������ ����� �������� ����� ��������
	/**
	 * �������� ������ �� ������
	 * @param slide
	 */
	public void remove(Slide slide){
		if(slide.annotation!=null){
			//���� � ������ ���� ���������, ������� ��
			slide.annotation.dispose();
		}		
		slides.remove(slide); //������� ����� �� ������
		/**
		 * ��������� ������ ���������� �������
		 */
		updateIndexes();
		/**
		 * ��������� ������� "����� ������" �� ��� ����� ���������
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_REMOVE, slide);
	}
	
	/**
	 * ���������� ������ (������� ����� OldSlide �� newSlide)
	 * @param oldSlide
	 * @param newSlide
	 */
	public void replace(Slide oldSlide, Slide newSlide){
		/**
		 * ����� ����� ������� ������
		 */
		int k = slides.indexOf(oldSlide);
		slides.remove(k);  // ������� ��� �� ������
		newSlide.index = k;  // ������ ������ � ������ ������ 
		slides.add(k, newSlide);  // ��������� ����� ����� �� ����� �������
		// ��������� ������� ����� ������ �� ������ ������� 
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_REMOVE, oldSlide);
		// ��������� ������� ����� �������� � ����� �������
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, newSlide);
	}
	
	/**
	 * ����������� ������ � ������ �������
	 * @param slide
	 * @param newIndex
	 */
	public void move(Slide slide, int newIndex){
		/** 
		 * ���������� ����� � ������
		 */
		slides.remove(slide);
		slides.add(newIndex, slide);
		/**
		 * ��������� ������ ���� �������
		 */
		updateIndexes();
		/**
		 * ��������� ������� ����� ���������
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_MOVE, slide);
	}
	
	/**
	 * ���������� ������
	 * @param slide
	 */
	public void add(Slide slide){
		/**
		 * ������ ����� ������ ������
		 */
		slide.index = slides.size();
		/**
		 * ��������� ��� � ����� ������
		 */
		slides.add(slide);
		/**
		 * ��������� ������� ����� ��������
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, slide);
	}
	
	/**
	 * ���������� ���������� �������
	 * @return
	 */
	public int getSlidesCnt(){
		return slides.size();
	}
	
	/**
	 * ���������� ������ � ������� ������� ww
	 * @return
	 */
	public Slide newSlide(){
		/**
		 * ������� ����� ����� ��� ������� ������� ������
		 */
		Slide slide = new Slide(Workspace.getCurrent().getEditScene().getEyePosition(),
				"����� �����",  null, slides.size());
		/**
		 * ��������� ��� � ������ �������
		 */
		slides.add(slide);
		/**
		 * ��������� ������� ����� ��������
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, slide);
		return slide;
	}
	
	/**
	 * ���������� ��������� �����(��� ���������) � ������
	 * @param slide
	 * @return
	 */
	public Slide getNextSlide(Slide slide){
		int cnt = slides.size();
		if(cnt==0) return null;
		if(slides.contains(slide)){
			/** 
			 * ���� �������� ����� � ������
			 */
			int index = slides.indexOf(slide);
			/**
			 * ���� �� �� ���������, ���������� ��������� �������
			 */
			if(index<cnt-1)return slides.get(index+1);
			return null;  
		}else{
			/**
			 * �� ��������� �������� ������ �����
			 */
			return slides.get(0);
		}
	}
	
	/**
	 * ���������� ���������� �����(��� ���������)
	 * @param slide
	 * @return
	 */
	public Slide getPrevSlide(Slide slide){
		int cnt = slides.size();
		if(cnt>0 && slides.contains(slide)){
			/**
			 * ���������� ����� ��������� ������
			 */
			int index = slides.indexOf(slide);
			/**
			 * ���� �� �� ������, ���������� ���������� �������
			 */
			if(index>0)return slides.get(index-1);
		}
		return null;
	}
	
	/**
	 * ��� �������� �� ����� �������,
	 * �������� ������� �������� ��� ������� ������
	 */
	public void onRestore(){
		/**
		 * �������� �� ���� �������, ��������� ������� ����� ��������
		 */
		for(Slide slide: slides){
			Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_LOAD, slide);
		}
		/**
		 * ��������� ������� ����������� ��������� WMS
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_WMS_SETTINGS, wmsLayer);
		/**
		 * ��������� ������� ��������� ��������� ����� �����
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_LAYERS_SETTINGS, layers);
	}	
	
	/**
	 * 
	 * @param wmsLayer
	 */
	public void setWmsSettings(WmsLayerSettings wmsLayer){
		if( Objects.equals(this.wmsLayer, wmsLayer)){
			/**
			 * ���� ��������� �� ����������, �� �������
			 */
			return;
		}
		/**
		 * ��������� ��������� WMS �
		 * ��������� ������� ��������� �������� WMS 
		 */
		this.wmsLayer = wmsLayer;
		Workspace.getCurrent().fireEvent(Workspace.EVENT_WMS_SETTINGS, wmsLayer);
	}
	
	public void setLayerSettings(LayersSettings layers){
		if( Objects.equals(this.layers, layers)){
			/**
			 * ���� ��������� ����� �� ����������, �� �������
			 */
			return;
		}
		/**
		 * ��������� �������� ����� � ��������� �������
		 * ��������� �������� ����� �����
		 */
		this.layers = layers;
		Workspace.getCurrent().fireEvent(Workspace.EVENT_LAYERS_SETTINGS, layers);
	}

	public WmsLayerSettings getWmsLayer() {
		return wmsLayer;
	}

	public LayersSettings getLayers() {
		return layers;
	}
	
	public void addPath(int slideIndex, List<LatLon> path){
		paths.put(slideIndex, CameraPath.fromPath(path));
	}
	
	public List<LatLon> getPath(int slideIndex){
		if(slideIndex==0)return new ArrayList<>();
		return CameraPath.toPath(paths.get(slideIndex),
				slides.get(slideIndex-1).position,
				slides.get(slideIndex).position);
	}	
	
	protected void updateIndexes(){
		Map<Integer, CameraPath> newPaths = new HashMap<>();
		for(Slide s: slides){
			int k = slides.indexOf(s);
			newPaths.put(k, paths.get(s.getIndex()));
			s.index = k;
		}
		paths = newPaths;
	}
}
