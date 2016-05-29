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
 * Класс проекта Power Globe
 *   
 * @author 1
 *
 */
@XmlRootElement
public class Project {
	@XmlElement
	public String name = "Unnamed";  //Название проекта (не используется)
	@XmlElement
	public List<Slide> slides = new ArrayList<>();  // Список слайдов
	@XmlElement
	public WmsLayerSettings wmsLayer;   // Настройки Wms
	@XmlElement
	public LayersSettings layers = new LayersSettings();   //Настройки слоев сцены WW
	@XmlElement
	public Map<Integer, CameraPath> paths = new HashMap<>(); // список путей перехода между слайдами
	/**
	 * Удаление слайда из списка
	 * @param slide
	 */
	public void remove(Slide slide){
		if(slide.annotation!=null){
			//Если у слайда есть аннотация, удаляем ее
			slide.annotation.dispose();
		}		
		slides.remove(slide); //Удаляем слайд из списка
		/**
		 * Обновляем номера оставшихся слайдов
		 */
		updateIndexes();
		/**
		 * Запускаем событие "Слайд удален" во все части программы
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_REMOVE, slide);
	}
	
	/**
	 * Обновление слайда (заменят слайд OldSlide на newSlide)
	 * @param oldSlide
	 * @param newSlide
	 */
	public void replace(Slide oldSlide, Slide newSlide){
		/**
		 * Берем номер старого слайда
		 */
		int k = slides.indexOf(oldSlide);
		slides.remove(k);  // удаляем его из списка
		newSlide.index = k;  // ставим индекс у нового слайда 
		slides.add(k, newSlide);  // Добавляем новый слайд на место старого
		// запускаем событие Слайд удален со старым слайдом 
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_REMOVE, oldSlide);
		// запускаем событие Слайд добавлен с новым слайдов
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, newSlide);
	}
	
	/**
	 * Перемещение слайда в другую позицию
	 * @param slide
	 * @param newIndex
	 */
	public void move(Slide slide, int newIndex){
		/** 
		 * Перемещаем слайд в списке
		 */
		slides.remove(slide);
		slides.add(newIndex, slide);
		/**
		 * Обновляем номера всех слайдов
		 */
		updateIndexes();
		/**
		 * Запускаем событие слайд перемещен
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_MOVE, slide);
	}
	
	/**
	 * Добавление слайда
	 * @param slide
	 */
	public void add(Slide slide){
		/**
		 * Ставим номер нового слайда
		 */
		slide.index = slides.size();
		/**
		 * Добавляем его в конец списка
		 */
		slides.add(slide);
		/**
		 * Запускаем событие слайд добавлен
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, slide);
	}
	
	/**
	 * Возвращяет количество слайдов
	 * @return
	 */
	public int getSlidesCnt(){
		return slides.size();
	}
	
	/**
	 * Добавление слайда в текущей позиции ww
	 * @return
	 */
	public Slide newSlide(){
		/**
		 * Создаем новый слайд для текущей позиции камеры
		 */
		Slide slide = new Slide(Workspace.getCurrent().getEditScene().getEyePosition(),
				"Новый слайд",  null, slides.size());
		/**
		 * Добавляем его в список слайдов
		 */
		slides.add(slide);
		/**
		 * Запускаем событие Слайд добавлен
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_ADD, slide);
		return slide;
	}
	
	/**
	 * Возвращает следующий слайд(для заданного) в списке
	 * @param slide
	 * @return
	 */
	public Slide getNextSlide(Slide slide){
		int cnt = slides.size();
		if(cnt==0) return null;
		if(slides.contains(slide)){
			/** 
			 * Ищем заданный слайд в списке
			 */
			int index = slides.indexOf(slide);
			/**
			 * Если он не последний, возвращаем следующую позицию
			 */
			if(index<cnt-1)return slides.get(index+1);
			return null;  
		}else{
			/**
			 * По умолчанию передаем первый слайд
			 */
			return slides.get(0);
		}
	}
	
	/**
	 * Возвращает предыдущий слайд(для заданного)
	 * @param slide
	 * @return
	 */
	public Slide getPrevSlide(Slide slide){
		int cnt = slides.size();
		if(cnt>0 && slides.contains(slide)){
			/**
			 * Определяем номер заданного слайда
			 */
			int index = slides.indexOf(slide);
			/**
			 * Если он не первый, возвращаем предыдущую позицию
			 */
			if(index>0)return slides.get(index-1);
		}
		return null;
	}
	
	/**
	 * При загрузке из файла проекта,
	 * вызывает события загрузки для каждого слайда
	 */
	public void onRestore(){
		/**
		 * Проходим по всем слайдам, запускаем событие Слайд добавлен
		 */
		for(Slide slide: slides){
			Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_LOAD, slide);
		}
		/**
		 * Запускаем событие Установлены параметры WMS
		 */
		Workspace.getCurrent().fireEvent(Workspace.EVENT_WMS_SETTINGS, wmsLayer);
		/**
		 * Запускаем событие Добавлены настройки слоев сцены
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
			 * Если настройки не изменились, то выходим
			 */
			return;
		}
		/**
		 * Обновляем настройки WMS и
		 * запускаем событие Обновлены настроки WMS 
		 */
		this.wmsLayer = wmsLayer;
		Workspace.getCurrent().fireEvent(Workspace.EVENT_WMS_SETTINGS, wmsLayer);
	}
	
	public void setLayerSettings(LayersSettings layers){
		if( Objects.equals(this.layers, layers)){
			/**
			 * Если настройки слоев не изменились, то выходим
			 */
			return;
		}
		/**
		 * Обновляем настроки слоев т запускаем событие
		 * Обновлены настроки слоев сцены
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
