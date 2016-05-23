package powerglobe.wwd.annotations;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import powerglobe.project.Resource;

/**
 * Базовая аннотация
 * @author 1
 *
 */
@XmlRootElement
public abstract class AbstractAnnotation {
	/**
	 * Параметры аннотации
	 */
	protected Map<String, String> state = new HashMap<>();
	
	public Map<String, String> getState(){
		return state;
	};
	public void setState(Map<String, String> params){
		this.state = params;
	}
	/**
	 * Возвращает параметр как вещественное число
	 * @param code
	 * @return
	 */
	double getDoubleParam(String code){
		try{
			double v = Double.parseDouble(state.get(code));	
			return v;
		}catch(NumberFormatException e){
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
		    return 0;
		}
	}
	/**
	 * Возвращает параметр как целое число
	 * @param code
	 * @return
	 */
	int getIntParam(String code){
		try{
			int v = Integer.parseInt(state.get(code));	
			return v;
		}catch(NumberFormatException e){
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
		    return 0;
		}		
	}
	String getParam(String code){
		return state.get(code);
	}
	/**
	 * Удаляет параметр-картинку
	 * @param code
	 */
	void disposeImage(String code){
		String value = state.get(code);
		if(value!=null){
			Resource.forId(value).dispose();
			state.put(code, null);
		}
	}
	/**
	 * Возвращает настроенную аннотацию 
	 * @param position
	 * @return
	 */
	public abstract GlobeAnnotation getAnnotation(Position position);
	/**
	 * Удаляет файлы аннотации
	 */	
	public void dispose(){ };
}
