package powerglobe.wwd.annotations;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwindx.examples.util.PowerOfTwoPaddedImage;
import powerglobe.project.Resource;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;
import powerglobe.wwd.annotations.props.DecimalValue;
import powerglobe.wwd.annotations.props.ImageUpload;
import powerglobe.wwd.annotations.props.Multiline;
@XmlRootElement
public class TextAndImageAnnotation extends AbstractAnnotation {

	{
		state.put("text", "Описание местности");
		state.put("img", null);
		state.put("scale", "0.3");
	}
	public static String getName() {
		return "Картинка и текст снизу";
	}

	@Override
	public GlobeAnnotation getAnnotation(Position position) {
		GlobeAnnotation ga = new GlobeAnnotation(state.get("text"), Position.fromDegrees(position.latitude.degrees, position.longitude.degrees, 0));
        
		String imgId = state.get("img");
		if(imgId!=null){
			Resource rc = Resource.forId(imgId);
	        PowerOfTwoPaddedImage image = PowerOfTwoPaddedImage.fromPath(rc.getPath().toString());
	    	int inset = 10; // pixels
	        ga.getAttributes().setInsets(new Insets((int)Math.round(image.getOriginalHeight()*getDoubleParam("scale")) + inset * 2, inset, inset, inset));
	        ga.getAttributes().setImageSource(image.getPowerOfTwoImage());
	        ga.getAttributes().setImageOffset(new Point(inset, inset));
	        ga.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
	        ga.getAttributes().setImageOpacity(1);
	        ga.getAttributes().setImageScale(getDoubleParam("scale"));
	        ga.getAttributes().setSize(new Dimension((int)Math.round(image.getOriginalWidth()*getDoubleParam("scale")) + inset * 2, 0));
		}
        ga.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
        ga.getAttributes().setBackgroundColor(Color.WHITE);
        ga.getAttributes().setTextColor(Color.BLACK);
        ga.getAttributes().setBorderColor(Color.BLACK);
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
