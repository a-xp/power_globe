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
import gov.nasa.worldwindx.examples.util.PowerOfTwoPaddedImage;
import powerglobe.project.Resource;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;
import powerglobe.wwd.annotations.props.DecimalValue;
import powerglobe.wwd.annotations.props.ImageUpload;
import powerglobe.wwd.annotations.props.Multiline;

@XmlRootElement
public class LongTextWithBackgroundAnnotation extends AbstractAnnotation {
	
	{
		state.put("text", "Описание географического объекта");
		state.put("img", null);
		state.put("scale", "0.4");
	}
	
	public static String getName(){
		return "Описание с фоном";
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
		
        Resource rc = Resource.forId(state.get("img"));
        PowerOfTwoPaddedImage image = PowerOfTwoPaddedImage.fromPath(rc.getPath().toString());
        
        GlobeAnnotation ga = new GlobeAnnotation(state.get("text"), Position.fromDegrees(position.latitude.degrees, position.longitude.degrees, 0), Font.decode("Arial-BOLD-12"));
            ga.getAttributes().setDefaults(defaultAttributes);
            ga.getAttributes().setImageSource(image.getPowerOfTwoImage());
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_Y);
            ga.getAttributes().setImageOpacity(.6);
            ga.getAttributes().setImageScale(getDoubleParam("scale"));
            ga.getAttributes().setImageOffset(new Point(1, 1));
            ga.getAttributes().setInsets(new Insets(6, 28, 6, 6));

		return ga;
	}

	@Override
	public void dispose() {
		disposeImage("img");
	}
	
	public static Map<String, AbstractAnnotationProp> getControls(){
		Map<String, AbstractAnnotationProp> result = new HashMap<>();
		result.put("text", new Multiline("Описание"));
		result.put("img", new ImageUpload("Фон"));
		result.put("scale", new DecimalValue("Масштаб фона"));
		return result;
	}
	
}
