package powerglobe.wwd.annotations.props;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class IntegerValue extends SingleLine{
	protected Integer min;
	protected Integer max;
	
	public IntegerValue(String title) {
		super(title);
	}

	public IntegerValue(String title, Integer min, Integer max) {
		super(title);
		this.min = min;
		this.max = max;
	}	
	
	@Override
	public boolean validate() {
		Integer val;
		try{
			val = Integer.parseInt(input.getText());
		}catch(NumberFormatException e){
			try{
				  MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Number format error", "Value is invalid");
			}catch(Throwable e2){}
			return false;
		}
		if(min!=null && val<min){
			try{
				  MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Value constraint", "Value must be bigger than "+min);
			}catch(Throwable e2){}
			return false;	
		}
		if(max!=null && val>max){
			try{
				  MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Value constraint", "Value must be lower than "+max);
			}catch(Throwable e2){}
			return false;	
		}		
		return true;
	}
	

}
