package powerglobe.wwd.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;

public class SimpleTownAnnotationTest {
	@Test
	public void testDefaultParams(){
		SimpleTownAnnotation ann = new SimpleTownAnnotation();
		Map<String, String> params = ann.getState();
		assertTrue(params!=null);
		assertTrue(params.containsKey("text"));
		assertEquals("Неизвестный город", ann.getParam("text"));
	}
	
	@Test
	public void testPropsSaved(){
		Map<String, String> params = new HashMap<>();
		params.put("text", "Название города");
		params.put("size", "0.6");
		params.put("n", "7");
		SimpleTownAnnotation ann = new SimpleTownAnnotation();
		ann.setState(params);
		
		assertEquals("Название города", ann.getParam("text"));
		assertEquals(0.6, ann.getDoubleParam("size"), 0.001);
		assertEquals(7, ann.getIntParam("n"));
	}
	
	@Test
	public void testAnnotationProduced(){
		Map<String, String> params = new HashMap<>();
		String text = "Текст аннотации";
		params.put("text", text);		
		SimpleTownAnnotation ann = new SimpleTownAnnotation();
		ann.setState(params);

		Position pos = new Position(Angle.ZERO, Angle.ZERO, 100000);
		GlobeAnnotation ga = ann.getAnnotation(pos);
		
		assertTrue(ga!=null);
		AnnotationAttributes  aa = ga.getAttributes();
		assertEquals(text, ga.getText());
		assertEquals(new Color(0f, 0f, 0f, .5f), aa.getBackgroundColor());
		assertEquals(Color.WHITE, aa.getTextColor());
		assertEquals(aa.getCornerRadius(), 10);
		Position ap = ga.getPosition();
		assertEquals(pos.latitude.degrees, ap.latitude.degrees, 0.01);
		assertEquals(pos.longitude.degrees, ap.longitude.degrees, 0.01);
		assertEquals(0, ap.elevation, 0.01);
	}
	
	@Test
	public void testHaveControls(){
		Map<String, AbstractAnnotationProp> controls = SimpleTownAnnotation.getControls();
		assertTrue(controls!=null);
		assertTrue(controls.size()>0);
		assertTrue(controls.containsKey("text"));
	}
}
