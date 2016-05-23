package powerglobe.wwd.annotations.props;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Multiline extends AbstractAnnotationProp {

	public Multiline(String title) {
		super(title);
	}

	protected Text input;
	
	@Override
	public void createControls(Composite parent) {
		GridData labelFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(title);
		titleLabel.setLayoutData(labelFormat);

		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		inputFormat.heightHint = 130;
		input = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		input.setLayoutData(inputFormat);
	}

	@Override
	public String getValue() {
		return input.getText();
	}

	@Override
	public void setValue(String value) {
		input.setText(value);
	}
	
}
