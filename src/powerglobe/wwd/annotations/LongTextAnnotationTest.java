package powerglobe.wwd.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;

public class LongTextAnnotationTest {
	
	@Test
	public void testDefaultParams(){
		LongTextAnnotation ann = new LongTextAnnotation();
		Map<String, String> params = ann.getState();
		assertTrue(params!=null);
		assertTrue(params.containsKey("text"));
		assertEquals("Длинное описание", ann.getParam("text"));
	}

	@Test
	public void testPropsSaved(){
		Map<String, String> params = new HashMap<>();
		params.put("text", "Длинный текст аннотации");
		params.put("scale", "0.2");
		params.put("count", "8");
		LongTextAnnotation ann = new LongTextAnnotation();
		ann.setState(params);
		
		assertEquals("Длинный текст аннотации", ann.getParam("text"));
		assertEquals(0.2, ann.getDoubleParam("scale"), 0.001);
		assertEquals(8, ann.getIntParam("count"));
	}
	
	@Test
	public void testAnnotationProduced(){
		Map<String, String> params = new HashMap<>();
		String text = "Длинный текст аннотации";
		params.put("text", text);		
		LongTextAnnotation ann = new LongTextAnnotation();
		ann.setState(params);

		Position pos = new Position(Angle.ZERO, Angle.ZERO, 100000);
		GlobeAnnotation ga = ann.getAnnotation(pos);
		
		assertTrue(ga!=null);
		AnnotationAttributes  aa = ga.getAttributes();
		assertEquals(text, ga.getText());
		assertEquals(new Color(.8f, .8f, .8f, .7f), aa.getBackgroundColor());
		assertEquals(Color.BLACK, aa.getBorderColor());
		assertEquals(AVKey.RIGHT, aa.getTextAlign());
		Position ap = ga.getPosition();
		assertEquals(pos.latitude.degrees, ap.latitude.degrees, 0.01);
		assertEquals(pos.longitude.degrees, ap.longitude.degrees, 0.01);
		assertEquals(0, ap.elevation, 0.01);
	}
	
	@Test
	public void testHaveControls(){
		Map<String, AbstractAnnotationProp> controls = LongTextAnnotation.getControls();
		assertTrue(controls!=null);
		assertTrue(controls.size()>0);
		assertTrue(controls.containsKey("text"));
	}
	
}
