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
 * Класс для загрузки и сохранения проекта
 * @author 1
 *
 */
public class Loader {
	/**
	 * Список классов, о которых должен знать JAXB (они явно не указаны в классе Project)
	 */
	private static Class<?>[] classes;
	/**
	 * Формируем этот список. Их должен учитывать JAXB при сохранении и загрузке
	 * В список входят классы аннотаций и класс проекта, остальные классы JAXB находит сам по типам полей
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
	 * Сохранение проекта в XML файл(по пути path)
	 * @param project
	 * @param path
	 */
	public static void save( Project project, String path){
		
		try {
			/**
			 * Создаем контекст JAXB, передаем классы, которые явно не указаны в Project 
			 */
			JAXBContext context = JAXBContext.newInstance(classes);
			/**
			 * Открываем файл на запись
			 */
			OutputStream os = new FileOutputStream(path);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // Читаемый формат XML
			// Пишем в файл и закрываем
			marshaller.marshal(project, os);
			os.close();
		} catch (Exception e) {
			/**
			 * Если ошибка - сообщаем пользователю всплавающим окном
			 */
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Invalid data format", "Error occured when saving project: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Загрузка проекта из файла (по пути path)
	 * @param path
	 * @return
	 */
	public static Project load(String path){
		try {
			/**
			 * Создаем контекст JAXB (передаем список неявных классов)
			 */
			JAXBContext context = JAXBContext.newInstance(classes);
			/**
			 * Открываем файл на чтение
			 */
			InputStream is = new FileInputStream(path);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			/**
			 * Читаем проект из файла
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
