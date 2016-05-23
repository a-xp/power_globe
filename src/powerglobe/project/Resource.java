package powerglobe.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

/**
 * Класс файла в аннотациях
 * @author 1
 *
 */
public class Resource {
	public String name;  // Уникальный идентификатор файла
	
	public static Resource forId(String id){
		return new Resource(id);	
	}
	/**
	 * Создает новый файл в проекте на основе файла переданного в пути path
	 * @param path
	 * @return
	 */
	public static Resource forFile(String path){
		/**
		 * Генерим уникальный идентификатор
		 */
		String name = java.util.UUID.randomUUID().toString();
		/**
		 * Путь к исходному файлу
		 */
		Path original = Paths.get(path);
		/**
		 * Путь куда копировать в директории проекта
		 */
		Path copy = Paths.get(Workspace.getCurrent().getProjectResourcesPath(), name);
		try {
			/**
			 * Копируем файл
			 */
			Files.copy(original, copy);
		} catch (IOException e) {
			/**
			 * Если не получилось, выодим ошибку диалогом
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
	 * Удаляет файл из проекта
	 */
	public void dispose(){
		/**
		 * Путь до файла в директории проекта
		 */
		Path file = Paths.get(Workspace.getCurrent().getProjectResourcesPath(), name);	
		try {
			/**
			 * Удаляем файл
			 */
			Files.delete(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Возвращает физический путь к файлу проекта
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
