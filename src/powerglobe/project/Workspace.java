package powerglobe.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import powerglobe.application.PowerGlobeRcpApplication;
import powerglobe.dialogs.WmsService;
import powerglobe.view.SliderList;
import powerglobe.wwd.Scene;

public class Workspace {
	public static final int MODE_EDIT = 0;
	public static final int MODE_SHOW = 0;
	public static final int MODE_AUTOSHOW = 0;
	
	/**
	 * Типы внутренних событий проекта
	 */
	public static final int EVENT_SLIDE_REMOVE = 1; //Удаление слайда
	public static final int EVENT_SLIDE_ADD = 2; //Добавление слайда
	public static final int EVENT_SLIDE_CHANGE = 3;  
	public static final int EVENT_SLIDE_IMAGE = 4; //Готов скриншот для слайда
	public static final int EVENT_SLIDE_SELECT = 5; //Слайд выбран
	public static final int EVENT_SLIDE_EDIT = 6; // Слайд редактируется
	public static final int EVENT_CLEAR = 7;  // Проект закрыт
	public static final int EVENT_SLIDE_LOAD =8;  //Слайд загружен из файла
	public static final int EVENT_SLIDE_MOVE =9; // Слайд перемещен в другую позицию
	public static final int EVENT_WMS_SETTINGS =10; //Добавлены настройки WMS
	public static final int EVENT_LAYERS_SETTINGS =11; //Добавлены настройки слоев сцены
	
	private static Workspace current; //Текущее приложение
	
	public Project currentProject = new Project(); //Текущий проект
	
	public String projectRoot;  // Путь до файла текущего проекта
	private boolean saved = false; // Флаг: был ли сохранен новый проект
	private Scene editScene;  // Сцена редактирования
	private SliderList sliderList;  // Блок список слайдов
	protected WmsService wmsService = new WmsService();  // Сервис работы с WMS серверами
	
	/**
	 * Подписчики на внутренние события
	 * Map<Код события, Список подписчиков>
	 */
	protected Map<Integer, List<Consumer<Object>>> listeners = new HashMap<>();	

	/**
	 * Возвращает текущее приложение (создает если еще не создано)
	 * @return
	 */
	public static Workspace getCurrent() {
		if(current == null) current = new Workspace();		
		return current;
	}
	
	/**
	 * Подписывает обработчик на событие проекта
	 * @param event
	 * @param callback
	 */
	public void addListener(int event, Consumer<Object> callback){
		if(!listeners.containsKey(event)){
			/**
			 * Если список подписчиков для данного события не существует, то создаем его
			 */
			listeners.put(event, new ArrayList<Consumer<Object>>());
		}
		listeners.get(event).add(callback);		
	}
	
	/**
	 * Запускает событие, вызывает все обработчики, подписанные на него асинхронно
	 * @param event
	 * @param data
	 */
	public void fireEvent(int event, final Object data){
		if(listeners.containsKey(event)){
			/**
			 * Проходим по списку подписчиков на данное событие
			 */
			listeners.get(event).forEach(new Consumer<Consumer<Object>>() {
				@Override
				public void accept(final Consumer<Object> r) {
					/**
					 * Вызываем подписчика асинхронно в потоке UI
					 */
					PowerGlobeRcpApplication.display.asyncExec(new Runnable() {						
						@Override
						public void run() {
							r.accept(data);						
						}
					});					
				}				
			});
		}		
	}
	
	/**
	 * Запускает обработчики синхронно в том же потоке
	 * @param event
	 * @param data
	 */
	public void syncEvent(int event, final Object data){
		if(listeners.containsKey(event)){
			/**
			 * Проходим по списку подписчиков на заданое событие
			 */			
			listeners.get(event).forEach(new Consumer<Consumer<Object>>() {
				@Override
				public void accept(Consumer<Object> r) {
					/**
					 * Вызываем подписчика
					 */
					r.accept(data);
				}				
			});
		}
	}
	
	public void fireEvent(int event){
		fireEvent(event, null);
	}

	public Scene getEditScene() {
		return editScene;
	}

	public void setEditScene(Scene editScene) {
		this.editScene = editScene;
	}

	public void setSliderList(SliderList sliderList) {
		this.sliderList = sliderList;
	}

	public SliderList getSliderList() {
		return sliderList;
	}
	
	/**
	 * Сохранение проекта
	 */
	public void saveProject(){
		if(!saved){
			/** 
			 * Открываем диалог, если проект новый
			 */
			Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); //берем главное окно приложения
			FileDialog dialog = new FileDialog(activeShell, SWT.SAVE);   // создаем диалог выбора файла
		    dialog.setFilterNames(new String[] { "Power Globe Project (*.pgml)", "All Files (*.*)" });  // Названия масок файлов
		    dialog.setFilterExtensions(new String[] { "*.pgml", "*.*" });  // Маски файлов
		    String homeDir = System.getProperty("user.home");   // Получаем системный параметр - домашняя директория пользователя
		    dialog.setFilterPath(homeDir); 				// Задаем его как текущую директорию в диалоге
		    dialog.setFileName("NewPowerGlobeProject.pgml");  // Имя файла по-умолчанию
		    String path = dialog.open();   //Открываем диалог и получаем выбранный путь
		    if(path!=null){
		    	/**
		    	 * Если выбран путь сохранения, то копируем файлы проекта из временной директории(текущий путь к файлу проекта)
		    	 */
		    	File source = new File(projectRoot+".files"); // Временная директория с файлами проекта
		    	if(source.exists()){
			    	File dest = new File(path+".files");  // Новая директория с файлами проекта
			    	try {
			    	    FileUtils.copyDirectory(source, dest);  // копируем директорию 
			    	} catch (IOException e) {
			    	    e.printStackTrace();
			    	}
		    	}
		    	/**
		    	 * Устанавливаем новый путь к файлу проекта
		    	 */
		    	projectRoot = path;
		    }
		}
		if(projectRoot!=null){
			/**
			 * Сохраняем проект в xml
			 */
			Loader.save(currentProject, projectRoot);
			saved = true;
		}
	}
	
	/**
	 * Загрузка проекта
	 */
	public void loadProject(){
		/**
		 * Открываем диалог
		 */
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); // Получаем основное окно приложения
		FileDialog dialog = new FileDialog(activeShell, SWT.OPEN);   // Создаем диалог выбора файла
	    dialog.setFilterNames(new String[] { "Power Globe Project (*.pgml)", "All Files (*.*)" });  // Названия масок файлов
	    dialog.setFilterExtensions(new String[] { "*.pgml", "*.*" });  // Маски файлов
	    String homeDir = System.getProperty("user.home"); // Получаем системный параметр - домашняя директория пользователя
	    dialog.setFilterPath(homeDir);  // Задаем его как текущую директорию в диалоге
		String path = dialog.open(); //Открываем диалог и получаем выбранный путь
		if(path!=null){
			/**
			 * Еслия файл выбран, Загружаем xml
			 */
			Project project = Loader.load(path);
			if(project!=null){
				/**
				 * Если проект загрузился, ставим его текущим в приложении
				 */
				syncEvent(EVENT_CLEAR, null); // Очищаем данные прежнего проекта
				currentProject = project;  // данные проекта
				projectRoot = path; // путь к файлу проекта
				saved = true;  
				project.onRestore();  // запускаем события проекта при загрузке
			}
		}
	}
	
	/**
	 * Новый пустой проект
	 */
	public void createNew(){
		syncEvent(EVENT_CLEAR, null);  // Очищаем данные текущего проекта
		currentProject = new Project();  // Создаем новый текущий проект
		projectRoot = tmpPath();  // Временный путь проекта
		saved = false; // Флаг - не сохранялся
	}
	
	/**
	 * Возвращает путь сохранения файлов проекта
	 * @return
	 */
	public String getProjectResourcesPath(){
		String path = projectRoot + ".files";  // путь к файлу проекта + .files
		Path dir = Paths.get(path);
		if(!Files.exists(dir)){
			/**
			 * Если директория не существует, создаем
			 */
			try {
				Files.createDirectory(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	/**
	 * Возвращает временный путь к файлам проекта в системной временной директории
	 * @return
	 */
	public static String tmpPath(){
		return Paths.get(System.getProperty("java.io.tmpdir"), "power_globe_"+UUID.randomUUID().toString()).toString();
	}
	
	public Workspace() {
		projectRoot = tmpPath();
	}
	
	public WmsService getWmsService(){
		return wmsService;
	}
	
}
