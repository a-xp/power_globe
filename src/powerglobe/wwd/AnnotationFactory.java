package powerglobe.wwd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import powerglobe.wwd.annotations.GeoFeatureAnnotation;
import powerglobe.wwd.annotations.LongTextAnnotation;
import powerglobe.wwd.annotations.LongTextWithBackgroundAnnotation;
import powerglobe.wwd.annotations.SimpleTownAnnotation;
import powerglobe.wwd.annotations.SpecialPlaceAnnotation;
import powerglobe.wwd.annotations.TextAndImageAnnotation;
import powerglobe.wwd.annotations.WaterObjectAnnotation;

/**
 * ������ ��������� ���������
 * @author 1
 *
 */
public class AnnotationFactory {
	
	private static Class<?>[] list = new Class[]{
			TextAndImageAnnotation.class,
			LongTextWithBackgroundAnnotation.class,
			SimpleTownAnnotation.class,
			SpecialPlaceAnnotation.class,
			LongTextAnnotation.class,
			WaterObjectAnnotation.class,
			GeoFeatureAnnotation.class
	};
		
	/**
	 * ���������� ����� ��������������->�������� ��������� 
	 * @return
	 */
	public static Map<Class<?>, String> getNames(){		
		Map<Class<?>, String> result = new HashMap<>();
		/**
		 * �������� �� ������������������ �����
		 */
		for(Class<?> cls: list){			
			try {
				/**
				 * �������� ��� ������� ���� ����� getName,
				 * ��� �������� ���������
				 */
				Method m = cls.getMethod("getName");
				result.put(cls, (String)m.invoke(new Object[0])); 
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}			
		}
		return result;		
	}

}
