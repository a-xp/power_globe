package powerglobe.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import powerglobe.project.Workspace;
/**
 * Окно свойств проекта
 * @author 1
 *
 */
public class SettingsWindow {
	
	/**
	 * Элементы окна
	 */
	protected Shell shell;  //окно
	protected Combo srvSel; //выпадающий список wms серверов
	protected Combo lrSel;  // выпадающий список wms слоев
	protected List<String> lrOptions = new ArrayList<>();  //коды wms слоев в списке
	protected Combo stlSel; // выпадающий список wms стилей
	protected List<String> stlOptions = new ArrayList<>(); //коды wms стилей в списке
	protected Composite wmsPanel;
	
	protected Map<String, Button> layersCheckbox = new HashMap<>();  //чекбоксы слоев сцены
	
	public SettingsWindow(){
	}
	
	/**
	 * Инициализация окна
	 */
	public void open(){
		/**
		 * Создаем окно
		 */
		shell = new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());
		Composite frame = new Composite(shell, SWT.NONE);
		GridLayout frameLayout = new GridLayout(1,true);
		frame.setLayout(frameLayout);		
		
		/**
		 * Создаем вкладки
		 */
		TabFolder tabFolder = new TabFolder(frame, SWT.BORDER);
		GridData tabsFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tabsFormat.heightHint = 220;
		tabFolder.setLayoutData(tabsFormat);
		
		/**
		 * Заполняем вкладки
		 */
		showLayers(tabFolder);
		showWms(tabFolder);
		
		tabFolder.setSelection(0);
		
		/**
		 * Создаем кнопки 
		 */
		Composite btnGroup = new Composite(frame, SWT.NONE);
		btnGroup.setLayout(new RowLayout());
		GridData btnGroupFormat = new GridData(SWT.FILL, SWT.RIGHT, true, false);
		btnGroup.setLayoutData(btnGroupFormat);
		
		/**
		 * Кнопка Save
		 */
		Button save = new Button(btnGroup, SWT.PUSH);
		save.setText("Save");
		/**
		 * Обработчик нажатия - сохраняем гастройки и закрываем окно
		 */
		save.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return;
				save();
				shell.close();
				shell.dispose();				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/**
		 * Кнопка Cancel
		 */
		Button cancel = new Button(btnGroup, SWT.PUSH);
		cancel.setText("Cancel");
		/**
		 * Обработчик нажатия - просто закрываем окно
		 */
		cancel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return;
				shell.close();
				shell.dispose();				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/**
		 * Ставим окно посередине и открываем
		 */
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Point size = parent.getSize();
		shell.setBounds(Math.max(0, size.x/2-150), Math.max(0, size.y/2-125), 300, 300);
		shell.layout(true);
		shell.open();		
	}

	/**
	 * Заполняем вкладку слоев сцены
	 * @param tabFolder
	 */
	protected void showLayers(TabFolder tabFolder){
		/**
		 * Получаем настройки слоев проекта
		 */
		LayersSettings layerSettings = Workspace.getCurrent().currentProject.getLayers();
		/**
		 * Заполняем вкладку элементами
		 */
		TabItem layersTab = new TabItem(tabFolder, SWT.NONE);
		layersTab.setText("Layers settings");
		/**
		 * Скролл
		 */
		ScrolledComposite layersScroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.BORDER);
		layersScroll.setExpandVertical(true);
		layersScroll.setExpandHorizontal(true);
		layersScroll.setMinWidth(0);
		/**
		 * Сама панель с чекбоксами
		 */
		Composite layersPanel = new Composite(layersScroll, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		layersPanel.setLayout(gridLayout);
		layersTab.setControl(layersScroll);
		layersScroll.setContent(layersPanel);
		GridData chkFormat = new GridData(SWT.CENTER, SWT.CENTER,false, false);
		/**
		 * Проходим по списку управляемых слоев
		 */
		for(String l: layerSettings.getLayers()){
			/**
			 * Создаем чекбокс для слоя
			 */
			Button chk = new Button(layersPanel, SWT.CHECK);
			chk.setLayoutData(chkFormat);
			/**
			 * Чекбокс активен/неактивен в зависимости от активности слоя 
			 */
			chk.setSelection(layerSettings.isEnabled(l));
			GridData lblFormat = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			/**
			 * Название для чекбокса
			 */
			Label lbl = new Label(layersPanel, SWT.NONE);
			lbl.setLayoutData(lblFormat);
			lbl.setText(l);
			/**
			 * Запоминаем соответствие между слоем и чекбоксом
			 */
			layersCheckbox.put(l, chk);
		}	
		/**
		 * Обновляем раскладку элементов вкладки
		 */
		layersPanel.layout(true);
		layersScroll.layout(true);
		layersScroll.setMinHeight(layersPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

	}
	
	/**
	 * Заполняем вкладку настроек WMS Слоев
	 * @param tabFolder
	 */
	protected void showWms(TabFolder tabFolder){
		/**
		 * Получаем текущме настройки Wms проекта
		 */
		final WmsLayerSettings curWms = Workspace.getCurrent().currentProject.wmsLayer;
		
		/**
		 * Создаем и заполняем вкладку
		 */
		TabItem wmsTab = new TabItem(tabFolder, SWT.NONE);
		wmsTab.setText("WMS settings");
		wmsPanel = new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2,  false);
		wmsPanel.setLayout(gridLayout);
		wmsTab.setControl(wmsPanel);
		
		/**
		 * Метка для списка серверов
		 */
		GridData srvLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label srvLabel = new Label(wmsPanel, SWT.NONE);
		srvLabel.setLayoutData(srvLabelData);
		srvLabel.setText("Server");
		
		/**
		 * Список серверов
		 */
		GridData srvSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		srvSelData.widthHint = 150;
		srvSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		srvSel.setLayoutData(srvSelData);
		/**
		 * Первый пункт в списке - сервер не выбран (прочерк)
		 */		
		srvSel.add("-");
		/**
		 * Он же выбран по-умолчанию
		 */
		int srvIndex = 0;
		/**
		 * Проходим по списку зарегистрированных серверов
		 */
		for(String s: Workspace.getCurrent().getWmsService().getServers()){
			if(curWms!=null && s.equals(curWms.getServerName())){
				/**
				 * Если данные сервер является выбранным в текущих настройках проекта,
				 * то запоминаем его порядковый номер
				 */
				srvIndex = srvSel.getItemCount();
			}
			/**
			 * Добавляем пункт в выпадающий список для этого сервера
			 */			
			srvSel.add(s);
		}
		/**
		 * Ставим текущий выбранный пункт списка
		 */
		srvSel.select(srvIndex);
		/**
		 * Обработчик изменения выбранного пункта списка
		 */
		srvSel.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				/**
				 * Определяем номер выбранного пункта списка
				 */
				int index = srvSel.getSelectionIndex();
				if(index>0){
					/**
					 * Если выбран какой-то сервер,
					 * получаем его название
					 */
					String srvName = srvSel.getItem(index);
					String lrName = null;
					String stlName = null;
					if(curWms!=null && curWms.getServerName()==srvName){
						/**
						 * Если этот сервер выбран также в текущих WMS настройках проекта,
						 * берем из настроек проекта коды wms слоя и стиля 
						 */
						lrName = curWms.getLayerName();
						stlName = curWms.getStyleName();
					}
					/**
					 * Обновляем списки wms Слоев и стилей
					 */
					fillLrList(srvName, lrName);
					fillStyleList(srvName, lrName, stlName);
				}else{
					/**
					 * Если выбран прочерк(нет сервера)
					 * сбрасываем остальные списки
					 */
					fillLrList(null, null);
					fillStyleList(null, null, null);
				}				
			}
		});		

		/**
		 * Метка для списка wms Слоев
		 */
		GridData lrLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label lrLabel = new Label(wmsPanel, SWT.NONE);
		lrLabel.setLayoutData(lrLabelData);
		lrLabel.setText("Layer");
		
		/**
		 * Выпадающий список wms Слоев
		 */
		GridData lrSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		lrSelData.widthHint =150;
		lrSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		lrSel.setLayoutData(lrSelData);
		/**
		 * Заполняем список, используя настройки wms Слоя проекта, если они есть
		 */
		fillLrList(curWms==null?null:curWms.getServerName(), curWms==null?null:curWms.getLayerName());
		/**
		 * Ставим обработчик смена выбранного пункта
		 */
		lrSel.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				/**
				 * Определяем выбранный сервер
				 */
				String srvName = srvSel.getItem(srvSel.getSelectionIndex());
				/**
				 * Определяем номер выбранного пункта в списке
				 */
				int index = lrSel.getSelectionIndex();
				if(index>0){
					/**
					 * Если выбран слой
					 */
					String stlName = null;
					String lrName = lrOptions.get(index);
					if(curWms!=null && curWms.getLayerName().equals(lrName)){
						/**
						 * Если в текущих настройка проекта выбран тот же сервер и слой,
						 * берем код стиля из настроек проекта
						 */
						stlName = curWms.getStyleName();
					}
					/**
					 * Заполняем список wms стилей
					 */
					fillStyleList(srvName, lrName, stlName);
				}else{
					/**
					 * Если выбран прочерк, сбрасываем список стилей
					 */
					fillStyleList(null, null, null);
				}				
			}
		});
		
		/**
		 * Метка списка wms стилей
		 */
		GridData stlLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label stlLabel = new Label(wmsPanel, SWT.NONE);
		stlLabel.setLayoutData(stlLabelData);
		stlLabel.setText("Style");
		
		/**
		 * Список wms стилей
		 */
		GridData stlSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		stlSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		stlSelData.widthHint = 150;
		stlSel.setLayoutData(stlSelData);
		/**
		 * Заполняем список стилей, используя настройки wms Слоя проекта, если они есть
		 */
		fillStyleList(curWms==null?null:curWms.getServerName(), curWms==null?null:curWms.getLayerName(), curWms==null?null:curWms.getStyleName());
		
		/**
		 * Обновляем раскладку панели
		 */
		wmsPanel.pack();
	}
	
	/**
	 * Заполняет список wms слоев
	 * @param serverName
	 * @param layerName
	 */
	protected void fillLrList(String serverName, String layerName){
		/**
		 * Очищаем список
		 */
		lrSel.removeAll(); // выпадающий список
		lrOptions.clear(); // коды слоев
		lrSel.add("-");  // первый пункт списка
		lrOptions.add(null); // код слоя первого пункта		 
		int lrIndex = 0; //номер выбранного пункта
		
		/**
		 * Если выбран wms сервер
		 */
		if(serverName!=null){
			/**
			 * Получаем информацию о нем
			 */
			WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
 			/**
 			 * Проходим по списку доступных слоев(из этой инфы)
 			 */
			for(WMSLayerCapabilities layerCaps: caps.getNamedLayers()){
 				if(layerCaps.getName().equals(layerName)){
 					/**
 					 * Если код слоя совпадает с текущим выбранным,
 					 *  то запоминаем порядковый номер
 					 */
 					lrIndex = lrSel.getItemCount();
 				}
 				/**
 				 * Добавляем код слоя в список кодов
 				 */
 				lrOptions.add(layerCaps.getName());
 				/**
 				 * Добавляем название слоя в выпадающий список
 				 */
 				lrSel.add(layerCaps.getTitle());
 			}
		}
		/**
		 * Выставляем в вападающем списке текущий выбранный пункт
		 */
		lrSel.select(lrIndex);
		wmsPanel.layout(true);
	}
	
	/**
	 * Заполняет список wms стилей
	 * @param serverName
	 * @param layerName
	 * @param styleName
	 */
	protected void fillStyleList(String serverName, String layerName, String styleName){
		/**
		 * Очищаем списки
		 */
		stlSel.removeAll();  //выпадающий список
		stlOptions.clear();  //список кодов
		stlSel.add("-");    //добавляем первый пункт прочерк
		stlOptions.add(null); // код первого пункта
		int stlIndex = 0;  //текущий выбранный пункт
		
		/**
		 * Если выбран wms сервер и слой
		 */
		if(serverName!=null && layerName!=null){
			/**
			 * Получаем инфу о сервере
			 */
			WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
			/**
			 * Получаем из нее инфу о слое
			 */
			WMSLayerCapabilities layerCaps = caps.getLayerByName(layerName);
			/**
			 * Получаем список стилей слоя
			 */
			Set<WMSLayerStyle> lst =  layerCaps.getStyles();
			if(lst!=null){
				/**
				 * Проходим по всем стилям
				 */
				for(WMSLayerStyle style: lst){
					if(style.getName().equals(styleName)){
						/**
						 * Если код стиля совпадает с выбранным стилем в проекте,
						 * то запоминаем номер пункта
						 */
						stlIndex = stlSel.getItemCount();
					}
					/**
					 * Добавляем пункт списка для стиля
					 */
					stlSel.add(style.getTitle());
					/**
					 * Добавляем код стиля в список кодов
					 */
					stlOptions.add(style.getName());
				}				
			}
		}
		/**
		 * Ставим текущий выбранный пункт
		 */
		stlSel.select(stlIndex);
		wmsPanel.layout(true);
	}
	
	/**
	 * Сохраняет форму в настройки проекта
	 */
	public void save(){
		/**
		 * Собираем список выводимых слоев сцены
		 */
		Set<String> enabled = new HashSet<>();
		/**
		 * Проходим по всем чекбоксам
		 */
		for(Entry<String, Button> entry: layersCheckbox.entrySet()){
			Button chk = entry.getValue();
			if(chk.getSelection()){
				/**
				 * если чекбокс установлен, добавляем название слоя в список выводимых
				 */
				enabled.add(entry.getKey());
			}
		}
		/**
		 * Сохраняем в теккущий проект настройки выводимых слоев
		 */
		Workspace.getCurrent().currentProject.setLayerSettings(new LayersSettings(enabled));
				
		WmsLayerSettings wls = null;
		/**
		 * Получаем номер выбранного wms Сервера
		 */
		int srvIndex = srvSel.getSelectionIndex();
		if(srvIndex>0){
			/**
			 * Если выбран сервер,
			 * получаем номер слоя и стиля
			 */
			int lrIndex = lrSel.getSelectionIndex();
			int stlIndex = stlSel.getSelectionIndex();
			if(lrIndex>0 && (stlOptions.size()==1 || stlIndex>0)){
				/**
				 * если слой и стиль выбраны(или стилей нет),
				 * создаем новые настройки WMS
				 */
				wls = new WmsLayerSettings(srvSel.getItem(srvIndex), lrOptions.get(lrIndex), stlIndex>0?stlOptions.get(stlIndex):null);
			}
		}
		/**
		 * Сохраняем настройки WMS
		 */
		Workspace.getCurrent().currentProject.setWmsSettings(wls);
		
		/**
		 * Перерисовать сцену
		 */
		Workspace.getCurrent().getEditScene().redraw();
	}
	
}
