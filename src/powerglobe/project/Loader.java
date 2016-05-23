package powerglobe.project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import powerglobe.wwd.AnnotationFactory;

/**
 * ����� ��� �������� � ���������� �������
 * @author 1
 *
 */
public class Loader {
	/**
	 * ������ �������, � ������� ������ ����� JAXB (��� ���� �� ������� � ������ Project)
	 */
	private static Class<?>[] classes;
	/**
	 * ��������� ���� ������. �� ������ ��������� JAXB ��� ���������� � ��������
	 * � ������ ������ ������ ��������� � ����� �������, ��������� ������ JAXB ������� ��� �� ����� �����
	 */
	static {
		Map<Class<?>, String> annotations = AnnotationFactory.getNames();
		Set<Class<?>> cls = annotations.keySet();
		int size = cls.size()+1;
		classes = new Class<?>[size];
		cls.toArray(classes);
		classes[size-1] = Project.class;
	}
	
	
	/**
	 * ���������� ������� � XML ����(�� ���� path)
	 * @param project
	 * @param path
	 */
	public static void save( Project project, String path){
		
		try {
			/**
			 * ������� �������� JAXB, �������� ������, ������� ���� �� ������� � Project 
			 */
			JAXBContext context = JAXBContext.newInstance(classes);
			/**
			 * ��������� ���� �� ������
			 */
			OutputStream os = new FileOutputStream(path);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // �������� ������ XML
			// ����� � ���� � ���������
			marshaller.marshal(project, os);
			os.close();
		} catch (Exception e) {
			/**
			 * ���� ������ - �������� ������������ ����������� �����
			 */
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Invalid data format", "Error occured when saving project: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * �������� ������� �� ����� (�� ���� path)
	 * @param path
	 * @return
	 */
	public static Project load(String path){
		try {
			/**
			 * ������� �������� JAXB (�������� ������ ������� �������)
			 */
			JAXBContext context = JAXBContext.newInstance(classes);
			/**
			 * ��������� ���� �� ������
			 */
			InputStream is = new FileInputStream(path);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			/**
			 * ������ ������ �� �����
			 */
			Project project = (Project)unmarshaller.unmarshal(is);
			return project;
		}catch (Exception e){
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Invalid file format", "Error occured when opening file: "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	

	

}
