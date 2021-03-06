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
public class WaterObjectAnnotation extends AbstractAnnotation{

	{
		state.put("text", "�����");
	}
	public static String getName(){
		return "����� - ������ ������";
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
		
        AnnotationAttributes waterAttr = new AnnotationAttributes();
        waterAttr.setDefaults(defaultAttributes);
        waterAttr.setFrameShape(AVKey.SHAPE_ELLIPSE);
        waterAttr.setTextAlign(AVKey.CENTER);
        waterAttr.setFont(Font.decode("Arial-ITALIC-12"));
        waterAttr.setTextColor(Color.CYAN);
        waterAttr.setInsets(new Insets(8, 12, 9, 12));
        
        return new GlobeAnnotation(state.get("text"),
                Position.fromDegrees(position.latitude.degrees, position.longitude.degrees, 0), waterAttr);
		
	}
	
	public static Map<String, AbstractAnnotationProp> getControls(){
		Map<String, AbstractAnnotationProp> result = new HashMap<>();
		result.put("text", new SingleLine("�������� ������"));
		return result;
	}
}
