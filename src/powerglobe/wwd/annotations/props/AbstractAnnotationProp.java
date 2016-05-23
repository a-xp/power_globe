package powerglobe.wwd.annotations.props;


import java.util.Locale;

import org.eclipse.swt.widgets.Composite;

/**
 * ������� ����� ��� �������������� �������� ��������� �� �����
 * @author 1
 *
 */

public abstract class AbstractAnnotationProp {
	protected String title;
	
	/**
	 * ������� �������� ����� �� �����
	 * @param parent
	 */
	public abstract void createControls(Composite parent);
	
	/**
	 * ���������� �������� ��������� ��� ������
	 * @return
	 */
	public abstract String getValue();
	/**
	 * ���������� �������� ��������� ��� ������������ �����
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
	 * ���������� �������� ��������� ��� ����� �����
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
	 * ������������� �������� ��������� ��� ������
	 * @param value
	 */
	public abstract void setValue(String value);
	/**
	 * ������������� �������� ��������� ��� ������������ �����
	 * @param value
	 */
	public void setValue(double value){
		setValue(String.format(Locale.ENGLISH, "%.2f", value));
	};
	/**
	 * ������������� �������� ��������� ��� ����� �����
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
