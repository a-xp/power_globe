package powerglobe.wwd.annotations.props;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class IntegerValue extends SingleLine{

	public IntegerValue(String title) {
		super(title);
	}

	@Override
	public boolean validate() {
		try{
			Integer.parseInt(input.getText());
			return true;
		}catch(NumberFormatException e){
			try{
				  MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
			}catch(Throwable e2){}
			return false;
		}	
	}
	

}
