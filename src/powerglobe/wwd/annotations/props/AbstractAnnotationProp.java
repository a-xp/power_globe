package powerglobe.wwd.annotations.props;


import java.util.Locale;

import org.eclipse.swt.widgets.Composite;

/**
 * Ѕазовый класс дл€ редактировани€ свойства аннотации на форме
 * @author 1
 *
 */

public abstract class AbstractAnnotationProp {
	protected String title;
	
	/**
	 * —оздает элементы ввода на форме
	 * @param parent
	 */
	public abstract void createControls(Composite parent);
	
	/**
	 * ¬озвращает значение параметра как строку
	 * @return
	 */
	public abstract String getValue();
	/**
	 * ¬озвращает значение параметра как вещественное число
	 * @return
	 */
	public double getDoubleValue(){
		try{
			return Double.parseDouble(getValue());			
		}catch(NumberFormatException e){
			return 0;
		}
	}
	/**
	 * ¬озвращает значение параметра как целое число
	 * @return
	 */
	public int getIntValue(){
		try{
			return Integer.parseInt(getValue());
		}catch(NumberFormatException e){
			return 0;
		}
	}
	
	public boolean getBoolValue(){
		return Boolean.parseBoolean(getValue());
	}
	
	public void setValue(boolean b){
		setValue(((Boolean)b).toString());
	}
	
	/**
	 * ”станавливает значение параметра как строку
	 * @param value
	 */
	public abstract void setValue(String value);
	/**
	 * ”станавливает значение параметра как вещественное число
	 * @param value
	 */
	public void setValue(double value){
		setValue(String.format(Locale.ENGLISH, "%.2f", value));
	};
	/**
	 * ”станавливает значение параметра как целое число
	 * @param value
	 */
	public void setValue(int value){
		setValue(String.format("%d", value));
	}
	
	public boolean validate(){
		return true;
	};
	
	public AbstractAnnotationProp(String title) {
		this.title = title;
	}
	
}
