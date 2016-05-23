package powerglobe.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

/**
 * ����� ����� � ����������
 * @author 1
 *
 */
public class Resource {
	public String name;  // ���������� ������������� �����
	
	public static Resource forId(String id){
		return new Resource(id);	
	}
	/**
	 * ������� ����� ���� � ������� �� ������ ����� ����������� � ���� path
	 * @param path
	 * @return
	 */
	public static Resource forFile(String path){
		/**
		 * ������� ���������� �������������
		 */
		String name = java.util.UUID.randomUUID().toString();
		/**
		 * ���� � ��������� �����
		 */
		Path original = Paths.get(path);
		/**
		 * ���� ���� ���������� � ���������� �������
		 */
		Path copy = Paths.get(Workspace.getCurrent().getProjectResourcesPath(), name);
		try {
			/**
			 * �������� ����
			 */
			Files.copy(original, copy);
		} catch (IOException e) {
			/**
			 * ���� �� ����������, ������ ������ ��������
			 */
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "File system error", "No access to file");
			e.printStackTrace();
		}
		return new Resource(name);
	}

	public Resource(String name) {
		this.name = name;
	}

	public Resource() {

	}
	
	/**
	 * ������� ���� �� �������
	 */
	public void dispose(){
		/**
		 * ���� �� ����� � ���������� �������
		 */
		Path file = Paths.get(Workspace.getCurrent().getProjectResourcesPath(), name);	
		try {
			/**
			 * ������� ����
			 */
			Files.delete(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���������� ���������� ���� � ����� �������
	 * @return String
	 */
	public Path getPath(){
		Path file = Paths.get(Workspace.getCurrent().getProjectResourcesPath(), name);
		return file.toAbsolutePath();
	}
	
	public String getId(){
		return name;
	}
		
}
