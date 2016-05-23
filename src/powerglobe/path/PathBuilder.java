package powerglobe.path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Класс редактирования путей
 * 
 * @author 1
 *
 */
public class PathBuilder {
	
	final double POINT_SELECT_RADIUS = 0.1;
	protected BasicDragger dragger;
	RenderableLayer layer = new RenderableLayer();
	WorldWindowGLCanvas wwd;
	protected Slide curSlide = null;
	
	public void init(WorldWindowGLCanvas wwd){
		dragger = new BasicDragger(wwd, true);
		this.wwd= wwd;	
		
		ApplicationTemplate.insertAfterPlacenames(wwd, layer);
		
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			
			@Override
			public void accept(Object t) {
				curSlide = (Slide)t;
				if(!curSlide.isEnablePath()){
					curSlide = null;
				}
				redrawPath();
			}
		});
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {
			
			@Override
			public void accept(Object t) {
				curSlide = null;
				redrawPath();
			}
		});		
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_CHANGE, new Consumer<Object>() {
			
			@Override
			public void accept(Object t) {
				curSlide = (Slide)t;
				if(!curSlide.isEnablePath()){
					curSlide = null;
				}
				redrawPath();
			}
		});	
		Workspace.getCurrent().addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {

			@Override
			public void accept(Object t) {
				curSlide = null;
				redrawPath();
			}
		});
		
		/**
		 * Ставим обработчик клика на путь
		 */
		wwd.addSelectListener(new SelectListener() {
			
			@Override
			public void selected(SelectEvent event) {
				if (event == null || !event.hasObjects())
	                return;
				Object o = event.getTopPickedObject().getObject();
				if(o instanceof Path){					
					Path path = (Path)o;
					if(event.isDrag()){
						DragSelectEvent drgEvent = (DragSelectEvent)event;
						splitPath(path, drgEvent.getPreviousPickPoint(), drgEvent.getPickPoint());
						event.consume();
					}
					if(event.isDragEnd()){
						Workspace ws = Workspace.getCurrent();
						List<LatLon> positions = new ArrayList<>(); 
						path.getPositions().forEach(positions::add);
						ws.currentProject.addPath(curSlide.index, positions);
						event.consume();
					}
					if(event.isRightClick()){
						removePoint(path, event.getPickPoint());
						event.consume();
					}
				}
			}
		});	
		
	}
	
	/**
	 * Рисует путь
	 */
	protected void redrawPath(){
        layer.removeAllRenderables();
		if(curSlide!=null){
	        List<LatLon> coord = Workspace.getCurrent().currentProject.getPath(curSlide.getIndex());
			if(coord!=null){
		        ShapeAttributes attrs = new BasicShapeAttributes();
		        attrs.setOutlineMaterial(Material.RED);
		        attrs.setOutlineWidth(2d);
		        
		        List<Position> positions = coord.stream().map(latlon -> new Position(latlon, 0)).collect(Collectors.toList());
		        Path path = new Path(positions);
		        path.setAttributes(attrs);
		        path.setVisible(true);
		        path.setExtrude(true);
		        path.setFollowTerrain(true);
		        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		        path.setPathType(AVKey.GREAT_CIRCLE);
		        layer.addRenderable(path);	        			        
			}	
		}
		wwd.redraw();
	}
	
	/**
	 * Удалени точки по правому клику
	 * @param path
	 * @param point
	 */
	protected void removePoint(Path path, Point point){
		List<Position> positions = new ArrayList<>(); 
		path.getPositions().forEach(positions::add);
		Position pos = pointToPosition(point);
		
		int pointClicked = findPathPoint(positions, pos);
		if(positions.size()>2 && pointClicked!=-1){
			positions.remove(pointClicked);
		}
		path.setPositions(positions);
		wwd.redraw();
	}
	
	/**
	 * Обрабатываем клик по пути
	 * @param path
	 * @param start
	 * @param stop
	 */
	public void splitPath(Path path, Point start, Point stop){
		List<Position> positions = new ArrayList<>(); 
		path.getPositions().forEach(positions::add);
		Position startPosition = pointToPosition(start);
		Position stopPosition = pointToPosition(stop);

		int pointClicked = findPathPoint(positions, startPosition);
		
		
		if(pointClicked==-1){
		/**
		 * Если клик не по кнопке, определяем номер сегмента
		 */
			int index = getSegmentIndex(positions, pointToPosition(start));
			if(index==-1)return;
			positions.add(index+1, stopPosition);
		}else{	
			/**
			 * Нельзя двигать начало и конец
			 */
			if(pointClicked!=0 && pointClicked!=positions.size()-1){
				positions.set(pointClicked, stopPosition);
			}
		}
		
		path.setPositions(positions);
		wwd.redraw();
	}
	
	/**
	 * Определяем координаты клика на шаре по координатам экрана
	 * @param point
	 * @return
	 */
	protected Position pointToPosition(Point point){
	    return wwd.getView().computePositionFromScreenPoint(point.x, point.y);
	}
	
	/**
	 * Определяем по какой кнопке был клик
	 * @param positions
	 * @param pos
	 * @return
	 */
	protected int findPathPoint(List<Position> positions, Position pos){
		Position closest = positions.stream().sorted((pos1, pos2)->Double.compare(
				Position.linearDistance(pos1, pos).degrees,
				Position.linearDistance(pos2, pos).degrees)).findFirst().get();
		if(Position.linearDistance(closest, pos).degrees>POINT_SELECT_RADIUS){
			return -1;
		}else{
			return positions.indexOf(closest);
		}
	}	
	
	/**
	 * Определяет сегмент пути, по которому был клик
	 * @param positions
	 * @param pos
	 * @return
	 */
	protected int getSegmentIndex(List<Position> positions, Position pos){
		
		for(int i=0;i<positions.size()-1;i++){
			Angle segmentAzimuth = LatLon.greatCircleAzimuth(positions.get(i), positions.get(i+1));
			Angle segmentDistance = LatLon.greatCircleDistance(positions.get(i), positions.get(i+1));
			
			Angle pointAzimuth = LatLon.greatCircleAzimuth(positions.get(i), pos);
			Angle pointDistance = LatLon.greatCircleDistance(positions.get(i), pos);
			
			double distance = Math.sin(pointAzimuth.angularDistanceTo(segmentAzimuth).radians)*pointDistance.degrees; 
			if(distance<0.1 && segmentDistance.compareTo(pointDistance)>=0){
				return i;
			}
		}
		
		return -1;
	}
   
	
   
}
