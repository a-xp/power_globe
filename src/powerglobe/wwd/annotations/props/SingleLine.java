package powerglobe.wwd.annotations.props;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class SingleLine extends AbstractAnnotationProp{
	protected Text input;
	
	public SingleLine(String title) {
		super(title);
	}

	@Override
	public void createControls(Composite parent) {
		GridData labelFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(title);
		titleLabel.setLayoutData(labelFormat);

		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		input = new Text(parent, SWT.BORDER);
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
