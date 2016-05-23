package powerglobe.wwd.annotations.props;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import powerglobe.project.Resource;

/**
 * ����� �������� - ����
 * @author 1
 *
 */
public class ImageUpload extends AbstractAnnotationProp{
	
	protected Label result;
	protected String file;
	
	public ImageUpload(String title) {
		super(title);
	}

	@Override
	public void createControls(final Composite parent) {
		
		/**
		 * ����� 
		 */
		GridData labelFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(title);
		titleLabel.setLayoutData(labelFormat);

		/**
		 * ������ ������� ����
		 */
		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		final Composite inputBlock = new Composite(parent, SWT.NONE);
		inputBlock.setLayoutData(inputFormat);
		inputBlock.setLayout(new RowLayout());
		Button button = new Button(inputBlock, SWT.PUSH);
		button.setText("������� ����");
		button.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {}
			
			/**
			 * ���������� ������� ������
			 */
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return; //����� ������ ����
				/**
				 * ������� ������ ������ ��������
				 */
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(activeShell, SWT.OPEN);
			    dialog.setFilterNames(new String[] { "Images (gif, jpeg, png, ico, bmp)*.png, )" });
			    dialog.setFilterExtensions(new String[] { "*.gif; *.jpg; *.png; *.ico; *.bmp" });
			    String homeDir = System.getProperty("user.home");
			    dialog.setFilterPath(homeDir); 
				String path = dialog.open();
				if(path!=null){
					/**
					 * ���� �������� �������, 
					 * ������� ������� �������� �� �����
					 */
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							result.setForeground(result.getDisplay().getSystemColor(SWT.COLOR_BLACK));
							result.setText("��������..");
							inputBlock.layout(true);
						}
					});
					/**
					 * ��������� ����
					 */
					file = Resource.forFile(path).getId();
					parent.layout(true);
					if(file==null){
						/**
						 * ���� ���� �� �����������, ������ ������ �� �����
						 */
						parent.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								result.setText("������ ��������");
								result.setForeground(result.getDisplay().getSystemColor(SWT.COLOR_RED));	
								inputBlock.layout(true);
							}
						});	
						parent.layout(true);
					}else{
						/**
						 * ���� ���� �����������, ������� ��������� 
						 */
						parent.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								result.setText("���� ��������");
								result.setForeground(result.getDisplay().getSystemColor(SWT.COLOR_GREEN));
								inputBlock.layout(true);
							}
						});	
						parent.layout(true);
					}
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
		
		result = new Label(inputBlock, SWT.NONE);
		parent.layout(true);
	}

	@Override
	public boolean validate() {
		if(file==null){
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "File upload", "Please choose image file");
			return false;
		}
		return true;
	}

	@Override
	public String getValue() {
		return file;
	}

	@Override
	public void setValue(String value) {
		file = value;
		if(value!=null){
			result.setText("���� ��������");
			result.setForeground(result.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		}else{
			result.setText("");
		}		
	}
	
}
