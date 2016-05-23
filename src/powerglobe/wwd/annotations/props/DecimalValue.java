package powerglobe.wwd.annotations.props;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
/**
 * Класс вещественных параметров
 * @author 1
 *
 */
public class DecimalValue extends SingleLine {

	@Override
	public boolean validate() {
		try{
			Double.parseDouble(input.getText());
			return true;
		}catch(NumberFormatException e){
			try{
				 MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
			}catch(Throwable e2){}
			return false;
		}		
	}

	public DecimalValue(String title) {
		super(title);
	}
	
}
