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

public class TextAndImageAnnotationTest {
	
	@Test
	public void testDefaultParams(){
		TextAndImageAnnotation ann = new TextAndImageAnnotation();
		Map<String, String> params = ann.getState();
		assertTrue(params!=null);
		assertTrue(params.containsKey("text"));
		assertTrue(params.containsKey("scale"));
		assertTrue(params.containsKey("img"));
		assertEquals(null, ann.getParam("img"));
		assertEquals(0.3, ann.getDoubleParam("scale"), 0.01);
		assertEquals("Описание местности", ann.getParam("text"));
	}
	
	@Test
	public void testPropsSaved(){
		Map<String, String> params = new HashMap<>();
		params.put("text", "Текст аннотации");
		params.put("scale", "0.3");
		params.put("img", "j2qdld-399-jd84hj");
		TextAndImageAnnotation ann = new TextAndImageAnnotation();
		ann.setState(params);
		
		assertEquals("Текст аннотации", ann.getParam("text"));
		assertEquals(0.3, ann.getDoubleParam("scale"), 0.001);
		assertEquals("j2qdld-399-jd84hj", ann.getParam("img"));
	}
	
	@Test
	public void testAnnotationProduced(){
		Map<String, String> params = new HashMap<>();
		String text = "Текст аннотации";
		params.put("text", text);	
		params.put("scale", "0.2");
		params.put("img", null);
		TextAndImageAnnotation ann = new TextAndImageAnnotation();
		ann.setState(params);

		Position pos = new Position(Angle.ZERO, Angle.ZERO, 100000);
		GlobeAnnotation ga = ann.getAnnotation(pos);
		
		assertTrue(ga!=null);
		AnnotationAttributes  aa = ga.getAttributes();
		assertEquals(text, ga.getText());
		assertEquals(Color.WHITE, aa.getBackgroundColor());
		assertEquals(Color.BLACK, aa.getTextColor());
		assertEquals(Color.BLACK, aa.getBorderColor());
		Position ap = ga.getPosition();
		assertEquals(pos.latitude.degrees, ap.latitude.degrees, 0.01);
		assertEquals(pos.longitude.degrees, ap.longitude.degrees, 0.01);
		assertEquals(0, ap.elevation, 0.01);
	}
	
	@Test
	public void testHaveControls(){
		Map<String, AbstractAnnotationProp> controls = TextAndImageAnnotation.getControls();
		assertTrue(controls!=null);
		assertTrue(controls.size()>0);
		assertTrue(controls.containsKey("text"));
		assertTrue(controls.containsKey("scale"));
		assertTrue(controls.containsKey("img"));
	}

}
