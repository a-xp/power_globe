package powerglobe.wwd.annotations;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;
import powerglobe.wwd.annotations.props.Multiline;
@XmlRootElement
public class LongTextAnnotation extends AbstractAnnotation{

	public static String getName(){
		return "Длинное описание";
	}
	
	{
		state.put("text", "Длинное описание");
	}

	@Override
	public GlobeAnnotation getAnnotation(Position position) {
		GlobeAnnotation ga = new GlobeAnnotation(state.get("text"),
                Position.fromDegrees(position.latitude.degrees, position.longitude.degrees, 0), Font.decode("Arial-ITALIC-12"), Color.DARK_GRAY);
            ga.getAttributes().setTextAlign(AVKey.RIGHT);
            ga.getAttributes().setBackgroundColor(new Color(.8f, .8f, .8f, .7f));
            ga.getAttributes().setBorderColor(Color.BLACK);
            return ga;
		
	}
	
	public static Map<String, AbstractAnnotationProp> getControls(){
		Map<String, AbstractAnnotationProp> result = new HashMap<>();
		result.put("text", new Multiline("Описание"));
		return result;
	}
}
