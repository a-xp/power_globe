package powerglobe.wwd.annotations.props;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
/**
 * Класс вещественных параметров
 * @author 1
 *
 */
public class DecimalValue extends SingleLine {
	
	protected Double min;
	protected Double max;

	@Override
	public boolean validate() {
		Double val;
		try{
			val = Double.parseDouble(input.getText());
		}catch(NumberFormatException e){
			try{
				 MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
			}catch(Throwable e2){}
			return false;
		}
		if(min!=null && min>val){
			try{
				 MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Value constraint error", "Value must be bigger than "+min);
			}catch(Throwable e2){}	
			return false;
		}
		if(max!=null && max<val){
			try{
				 MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Value constraint error", "Value must be lower than "+max);
			}catch(Throwable e2){}	
			return false;
		}
		return true;
	}

	public DecimalValue(String title) {
		super(title);
	}
	
	public DecimalValue(String title, Double min, Double max){
		super(title);
		this.min = min;
		this.max = max;
	}	
}
