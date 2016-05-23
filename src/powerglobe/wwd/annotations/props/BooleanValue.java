package powerglobe.wwd.annotations.props;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BooleanValue extends AbstractAnnotationProp{
	protected Button input;
	
	public BooleanValue(String title) {
		super(title);
	}

	@Override
	public void createControls(Composite parent) {
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText("");
		
		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		input = new Button(parent, SWT.CHECK);
		input.setSelection(getBoolValue());
		input.setText(title);
		input.pack();
		input.setLayoutData(inputFormat);		
	}

	@Override
	public String getValue() {
		return ((Boolean)input.getSelection()).toString();
	}

	@Override
	public void setValue(String value) {
		input.setSelection(Boolean.parseBoolean(value));
	}
}
