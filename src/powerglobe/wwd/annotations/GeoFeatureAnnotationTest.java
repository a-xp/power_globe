package powerglobe.wwd.annotations;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;


public class GeoFeatureAnnotationTest {
	
	@Test
	public void testDefaultParams(){
		GeoFeatureAnnotation ann = new GeoFeatureAnnotation();
		Map<String, String> params = ann.getState();
		assertTrue(params!=null);
		assertTrue(params.containsKey("text"));
		assertEquals("Название объекта", ann.getParam("text"));
	}
	
	@Test
	public void testPropsSaved(){
		Map<String, String> params = new HashMap<>();
		params.put("text", "Текст аннотации");
		params.put("scale", "0.3");
		params.put("count", "3");
		GeoFeatureAnnotation ann = new GeoFeatureAnnotation();
		ann.setState(params);
		
		assertEquals("Текст аннотации", ann.getParam("text"));
		assertEquals(0.3, ann.getDoubleParam("scale"), 0.001);
		assertEquals(3, ann.getIntParam("count"));
	}
	
	@Test
	public void testAnnotationProduced(){
		Map<String, String> params = new HashMap<>();
		String text = "Текст аннотации";
		params.put("text", text);		
		GeoFeatureAnnotation ann = new GeoFeatureAnnotation();
		ann.setState(params);

		Position pos = new Position(Angle.ZERO, Angle.ZERO, 100000);
		GlobeAnnotation ga = ann.getAnnotation(pos);
		
		assertTrue(ga!=null);
		AnnotationAttributes  aa = ga.getAttributes();
		assertEquals(text, ga.getText());
		assertEquals(Color.BLACK, aa.getBackgroundColor());
		assertEquals(Color.GREEN, aa.getTextColor());
		Position ap = ga.getPosition();
		assertEquals(pos.latitude.degrees, ap.latitude.degrees, 0.01);
		assertEquals(pos.longitude.degrees, ap.longitude.degrees, 0.01);
		assertEquals(0, ap.elevation, 0.01);
	}
	
	@Test
	public void testHaveControls(){
		Map<String, AbstractAnnotationProp> controls = GeoFeatureAnnotation.getControls();
		assertTrue(controls!=null);
		assertTrue(controls.size()>0);
	}

}
