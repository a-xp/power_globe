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
	 * ���� ���������� ������� �������
	 */
	public static final int EVENT_SLIDE_REMOVE = 1; //�������� ������
	public static final int EVENT_SLIDE_ADD = 2; //���������� ������
	public static final int EVENT_SLIDE_CHANGE = 3;  
	public static final int EVENT_SLIDE_IMAGE = 4; //����� �������� ��� ������
	public static final int EVENT_SLIDE_SELECT = 5; //����� ������
	public static final int EVENT_SLIDE_EDIT = 6; // ����� �������������
	public static final int EVENT_CLEAR = 7;  // ������ ������
	public static final int EVENT_SLIDE_LOAD =8;  //����� �������� �� �����
	public static final int EVENT_SLIDE_MOVE =9; // ����� ��������� � ������ �������
	public static final int EVENT_WMS_SETTINGS =10; //��������� ��������� WMS
	public static final int EVENT_LAYERS_SETTINGS =11; //��������� ��������� ����� �����
	
	private static Workspace current; //������� ����������
	
	public Project currentProject = new Project(); //������� ������
	
	public String projectRoot;  // ���� �� ����� �������� �������
	private boolean saved = false; // ����: ��� �� �������� ����� ������
	private Scene editScene;  // ����� ��������������
	private SliderList sliderList;  // ���� ������ �������
	protected WmsService wmsService = new WmsService();  // ������ ������ � WMS ���������
	
	/**
	 * ���������� �� ���������� �������
	 * Map<��� �������, ������ �����������>
	 */
	protected Map<Integer, List<Consumer<Object>>> listeners = new HashMap<>();	

	/**
	 * ���������� ������� ���������� (������� ���� ��� �� �������)
	 * @return
	 */
	public static Workspace getCurrent() {
		if(current == null) current = new Workspace();		
		return current;
	}
	
	/**
	 * ����������� ���������� �� ������� �������
	 * @param event
	 * @param callback
	 */
	public void addListener(int event, Consumer<Object> callback){
		if(!listeners.containsKey(event)){
			/**
			 * ���� ������ ����������� ��� ������� ������� �� ����������, �� ������� ���
			 */
			listeners.put(event, new ArrayList<Consumer<Object>>());
		}
		listeners.get(event).add(callback);		
	}
	
	/**
	 * ��������� �������, �������� ��� �����������, ����������� �� ���� ����������
	 * @param event
	 * @param data
	 */
	public void fireEvent(int event, final Object data){
		if(listeners.containsKey(event)){
			/**
			 * �������� �� ������ ����������� �� ������ �������
			 */
			listeners.get(event).forEach(new Consumer<Consumer<Object>>() {
				@Override
				public void accept(final Consumer<Object> r) {
					/**
					 * �������� ���������� ���������� � ������ UI
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
	 * ��������� ����������� ��������� � ��� �� ������
	 * @param event
	 * @param data
	 */
	public void syncEvent(int event, final Object data){
		if(listeners.containsKey(event)){
			/**
			 * �������� �� ������ ����������� �� ������� �������
			 */			
			listeners.get(event).forEach(new Consumer<Consumer<Object>>() {
				@Override
				public void accept(Consumer<Object> r) {
					/**
					 * �������� ����������
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
	 * ���������� �������
	 */
	public void saveProject(){
		if(!saved){
			/** 
			 * ��������� ������, ���� ������ �����
			 */
			Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); //����� ������� ���� ����������
			FileDialog dialog = new FileDialog(activeShell, SWT.SAVE);   // ������� ������ ������ �����
		    dialog.setFilterNames(new String[] { "Power Globe Project (*.pgml)", "All Files (*.*)" });  // �������� ����� ������
		    dialog.setFilterExtensions(new String[] { "*.pgml", "*.*" });  // ����� ������
		    String homeDir = System.getProperty("user.home");   // �������� ��������� �������� - �������� ���������� ������������
		    dialog.setFilterPath(homeDir); 				// ������ ��� ��� ������� ���������� � �������
		    dialog.setFileName("NewPowerGlobeProject.pgml");  // ��� ����� ��-���������
		    String path = dialog.open();   //��������� ������ � �������� ��������� ����
		    if(path!=null){
		    	/**
		    	 * ���� ������ ���� ����������, �� �������� ����� ������� �� ��������� ����������(������� ���� � ����� �������)
		    	 */
		    	File source = new File(projectRoot+".files"); // ��������� ���������� � ������� �������
		    	if(source.exists()){
			    	File dest = new File(path+".files");  // ����� ���������� � ������� �������
			    	try {
			    	    FileUtils.copyDirectory(source, dest);  // �������� ���������� 
			    	} catch (IOException e) {
			    	    e.printStackTrace();
			    	}
		    	}
		    	/**
		    	 * ������������� ����� ���� � ����� �������
		    	 */
		    	projectRoot = path;
		    }
		}
		if(projectRoot!=null){
			/**
			 * ��������� ������ � xml
			 */
			Loader.save(currentProject, projectRoot);
			saved = true;
		}
	}
	
	/**
	 * �������� �������
	 */
	public void loadProject(){
		/**
		 * ��������� ������
		 */
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); // �������� �������� ���� ����������
		FileDialog dialog = new FileDialog(activeShell, SWT.OPEN);   // ������� ������ ������ �����
	    dialog.setFilterNames(new String[] { "Power Globe Project (*.pgml)", "All Files (*.*)" });  // �������� ����� ������
	    dialog.setFilterExtensions(new String[] { "*.pgml", "*.*" });  // ����� ������
	    String homeDir = System.getProperty("user.home"); // �������� ��������� �������� - �������� ���������� ������������
	    dialog.setFilterPath(homeDir);  // ������ ��� ��� ������� ���������� � �������
		String path = dialog.open(); //��������� ������ � �������� ��������� ����
		if(path!=null){
			/**
			 * ����� ���� ������, ��������� xml
			 */
			Project project = Loader.load(path);
			if(project!=null){
				/**
				 * ���� ������ ����������, ������ ��� ������� � ����������
				 */
				syncEvent(EVENT_CLEAR, null); // ������� ������ �������� �������
				currentProject = project;  // ������ �������
				projectRoot = path; // ���� � ����� �������
				saved = true;  
				project.onRestore();  // ��������� ������� ������� ��� ��������
			}
		}
	}
	
	/**
	 * ����� ������ ������
	 */
	public void createNew(){
		syncEvent(EVENT_CLEAR, null);  // ������� ������ �������� �������
		currentProject = new Project();  // ������� ����� ������� ������
		projectRoot = tmpPath();  // ��������� ���� �������
		saved = false; // ���� - �� ����������
	}
	
	/**
	 * ���������� ���� ���������� ������ �������
	 * @return
	 */
	public String getProjectResourcesPath(){
		String path = projectRoot + ".files";  // ���� � ����� ������� + .files
		Path dir = Paths.get(path);
		if(!Files.exists(dir)){
			/**
			 * ���� ���������� �� ����������, �������
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
	 * ���������� ��������� ���� � ������ ������� � ��������� ��������� ����������
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
