package powerglobe.wwd.annotations;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;
import powerglobe.wwd.annotations.props.SingleLine;
@XmlRootElement
public class GeoFeatureAnnotation extends  AbstractAnnotation{
	protected String text = "Равнина";
	
	public static String getName(){
		return "Текст - географический объект";
	}
	
	{
		state.put("text", "Название объекта");
	}
	
	@Override
	public GlobeAnnotation getAnnotation(Position position) {
        AnnotationAttributes defaultAttributes = new AnnotationAttributes();
        defaultAttributes.setCornerRadius(10);
        defaultAttributes.setInsets(new Insets(8, 8, 8, 8));
        defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        defaultAttributes.setTextColor(Color.WHITE);
        defaultAttributes.setDrawOffset(new Point(25, 25));
        defaultAttributes.setDistanceMinScale(.5);
        defaultAttributes.setDistanceMaxScale(2);
        defaultAttributes.setDistanceMinOpacity(.5);
        defaultAttributes.setLeaderGapWidth(14);
        defaultAttributes.setDrawOffset(new Point(20, 40));
        
		AnnotationAttributes geoAttr = new AnnotationAttributes();
        geoAttr.setDefaults(defaultAttributes);
        geoAttr.setFrameShape(AVKey.SHAPE_NONE);  // No frame
        geoAttr.setFont(Font.decode("Arial-ITALIC-12"));
        geoAttr.setTextColor(Color.GREEN);
        geoAttr.setTextAlign(AVKey.CENTER);
        geoAttr.setDrawOffset(new Point(0, 5)); // centered just above
        geoAttr.setEffect(AVKey.TEXT_EFFECT_OUTLINE);  // Black outline
        geoAttr.setBackgroundColor(Color.BLACK);
        
        return new GlobeAnnotation(state.get("text"),
                Position.fromDegrees(position.latitude.degrees, position.longitude.degrees, 0), geoAttr);
	}


	public static Map<String, AbstractAnnotationProp> getControls(){
		Map<String, AbstractAnnotationProp> result = new HashMap<>();
		result.put("text", new SingleLine("Текст метки"));
		return result;
	}
	
}
