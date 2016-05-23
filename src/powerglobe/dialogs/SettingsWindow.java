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
 * ���� ������� �������
 * @author 1
 *
 */
public class SettingsWindow {
	
	/**
	 * �������� ����
	 */
	protected Shell shell;  //����
	protected Combo srvSel; //���������� ������ wms ��������
	protected Combo lrSel;  // ���������� ������ wms �����
	protected List<String> lrOptions = new ArrayList<>();  //���� wms ����� � ������
	protected Combo stlSel; // ���������� ������ wms ������
	protected List<String> stlOptions = new ArrayList<>(); //���� wms ������ � ������
	protected Composite wmsPanel;
	
	protected Map<String, Button> layersCheckbox = new HashMap<>();  //�������� ����� �����
	
	public SettingsWindow(){
	}
	
	/**
	 * ������������� ����
	 */
	public void open(){
		/**
		 * ������� ����
		 */
		shell = new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());
		Composite frame = new Composite(shell, SWT.NONE);
		GridLayout frameLayout = new GridLayout(1,true);
		frame.setLayout(frameLayout);		
		
		/**
		 * ������� �������
		 */
		TabFolder tabFolder = new TabFolder(frame, SWT.BORDER);
		GridData tabsFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tabsFormat.heightHint = 220;
		tabFolder.setLayoutData(tabsFormat);
		
		/**
		 * ��������� �������
		 */
		showLayers(tabFolder);
		showWms(tabFolder);
		
		tabFolder.setSelection(0);
		
		/**
		 * ������� ������ 
		 */
		Composite btnGroup = new Composite(frame, SWT.NONE);
		btnGroup.setLayout(new RowLayout());
		GridData btnGroupFormat = new GridData(SWT.FILL, SWT.RIGHT, true, false);
		btnGroup.setLayoutData(btnGroupFormat);
		
		/**
		 * ������ Save
		 */
		Button save = new Button(btnGroup, SWT.PUSH);
		save.setText("Save");
		/**
		 * ���������� ������� - ��������� ��������� � ��������� ����
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
		 * ������ Cancel
		 */
		Button cancel = new Button(btnGroup, SWT.PUSH);
		cancel.setText("Cancel");
		/**
		 * ���������� ������� - ������ ��������� ����
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
		 * ������ ���� ���������� � ���������
		 */
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Point size = parent.getSize();
		shell.setBounds(Math.max(0, size.x/2-150), Math.max(0, size.y/2-125), 300, 300);
		shell.layout(true);
		shell.open();		
	}

	/**
	 * ��������� ������� ����� �����
	 * @param tabFolder
	 */
	protected void showLayers(TabFolder tabFolder){
		/**
		 * �������� ��������� ����� �������
		 */
		LayersSettings layerSettings = Workspace.getCurrent().currentProject.getLayers();
		/**
		 * ��������� ������� ����������
		 */
		TabItem layersTab = new TabItem(tabFolder, SWT.NONE);
		layersTab.setText("Layers settings");
		/**
		 * ������
		 */
		ScrolledComposite layersScroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.BORDER);
		layersScroll.setExpandVertical(true);
		layersScroll.setExpandHorizontal(true);
		layersScroll.setMinWidth(0);
		/**
		 * ���� ������ � ����������
		 */
		Composite layersPanel = new Composite(layersScroll, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		layersPanel.setLayout(gridLayout);
		layersTab.setControl(layersScroll);
		layersScroll.setContent(layersPanel);
		GridData chkFormat = new GridData(SWT.CENTER, SWT.CENTER,false, false);
		/**
		 * �������� �� ������ ����������� �����
		 */
		for(String l: layerSettings.getLayers()){
			/**
			 * ������� ������� ��� ����
			 */
			Button chk = new Button(layersPanel, SWT.CHECK);
			chk.setLayoutData(chkFormat);
			/**
			 * ������� �������/��������� � ����������� �� ���������� ���� 
			 */
			chk.setSelection(layerSettings.isEnabled(l));
			GridData lblFormat = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			/**
			 * �������� ��� ��������
			 */
			Label lbl = new Label(layersPanel, SWT.NONE);
			lbl.setLayoutData(lblFormat);
			lbl.setText(l);
			/**
			 * ���������� ������������ ����� ����� � ���������
			 */
			layersCheckbox.put(l, chk);
		}	
		/**
		 * ��������� ��������� ��������� �������
		 */
		layersPanel.layout(true);
		layersScroll.layout(true);
		layersScroll.setMinHeight(layersPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

	}
	
	/**
	 * ��������� ������� �������� WMS �����
	 * @param tabFolder
	 */
	protected void showWms(TabFolder tabFolder){
		/**
		 * �������� ������� ��������� Wms �������
		 */
		final WmsLayerSettings curWms = Workspace.getCurrent().currentProject.wmsLayer;
		
		/**
		 * ������� � ��������� �������
		 */
		TabItem wmsTab = new TabItem(tabFolder, SWT.NONE);
		wmsTab.setText("WMS settings");
		wmsPanel = new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2,  false);
		wmsPanel.setLayout(gridLayout);
		wmsTab.setControl(wmsPanel);
		
		/**
		 * ����� ��� ������ ��������
		 */
		GridData srvLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label srvLabel = new Label(wmsPanel, SWT.NONE);
		srvLabel.setLayoutData(srvLabelData);
		srvLabel.setText("Server");
		
		/**
		 * ������ ��������
		 */
		GridData srvSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		srvSelData.widthHint = 150;
		srvSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		srvSel.setLayoutData(srvSelData);
		/**
		 * ������ ����� � ������ - ������ �� ������ (�������)
		 */		
		srvSel.add("-");
		/**
		 * �� �� ������ ��-���������
		 */
		int srvIndex = 0;
		/**
		 * �������� �� ������ ������������������ ��������
		 */
		for(String s: Workspace.getCurrent().getWmsService().getServers()){
			if(curWms!=null && s.equals(curWms.getServerName())){
				/**
				 * ���� ������ ������ �������� ��������� � ������� ���������� �������,
				 * �� ���������� ��� ���������� �����
				 */
				srvIndex = srvSel.getItemCount();
			}
			/**
			 * ��������� ����� � ���������� ������ ��� ����� �������
			 */			
			srvSel.add(s);
		}
		/**
		 * ������ ������� ��������� ����� ������
		 */
		srvSel.select(srvIndex);
		/**
		 * ���������� ��������� ���������� ������ ������
		 */
		srvSel.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				/**
				 * ���������� ����� ���������� ������ ������
				 */
				int index = srvSel.getSelectionIndex();
				if(index>0){
					/**
					 * ���� ������ �����-�� ������,
					 * �������� ��� ��������
					 */
					String srvName = srvSel.getItem(index);
					String lrName = null;
					String stlName = null;
					if(curWms!=null && curWms.getServerName()==srvName){
						/**
						 * ���� ���� ������ ������ ����� � ������� WMS ���������� �������,
						 * ����� �� �������� ������� ���� wms ���� � ����� 
						 */
						lrName = curWms.getLayerName();
						stlName = curWms.getStyleName();
					}
					/**
					 * ��������� ������ wms ����� � ������
					 */
					fillLrList(srvName, lrName);
					fillStyleList(srvName, lrName, stlName);
				}else{
					/**
					 * ���� ������ �������(��� �������)
					 * ���������� ��������� ������
					 */
					fillLrList(null, null);
					fillStyleList(null, null, null);
				}				
			}
		});		

		/**
		 * ����� ��� ������ wms �����
		 */
		GridData lrLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label lrLabel = new Label(wmsPanel, SWT.NONE);
		lrLabel.setLayoutData(lrLabelData);
		lrLabel.setText("Layer");
		
		/**
		 * ���������� ������ wms �����
		 */
		GridData lrSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		lrSelData.widthHint =150;
		lrSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		lrSel.setLayoutData(lrSelData);
		/**
		 * ��������� ������, ��������� ��������� wms ���� �������, ���� ��� ����
		 */
		fillLrList(curWms==null?null:curWms.getServerName(), curWms==null?null:curWms.getLayerName());
		/**
		 * ������ ���������� ����� ���������� ������
		 */
		lrSel.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				/**
				 * ���������� ��������� ������
				 */
				String srvName = srvSel.getItem(srvSel.getSelectionIndex());
				/**
				 * ���������� ����� ���������� ������ � ������
				 */
				int index = lrSel.getSelectionIndex();
				if(index>0){
					/**
					 * ���� ������ ����
					 */
					String stlName = null;
					String lrName = lrOptions.get(index);
					if(curWms!=null && curWms.getLayerName().equals(lrName)){
						/**
						 * ���� � ������� ��������� ������� ������ ��� �� ������ � ����,
						 * ����� ��� ����� �� �������� �������
						 */
						stlName = curWms.getStyleName();
					}
					/**
					 * ��������� ������ wms ������
					 */
					fillStyleList(srvName, lrName, stlName);
				}else{
					/**
					 * ���� ������ �������, ���������� ������ ������
					 */
					fillStyleList(null, null, null);
				}				
			}
		});
		
		/**
		 * ����� ������ wms ������
		 */
		GridData stlLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		Label stlLabel = new Label(wmsPanel, SWT.NONE);
		stlLabel.setLayoutData(stlLabelData);
		stlLabel.setText("Style");
		
		/**
		 * ������ wms ������
		 */
		GridData stlSelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		stlSel = new Combo(wmsPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		stlSelData.widthHint = 150;
		stlSel.setLayoutData(stlSelData);
		/**
		 * ��������� ������ ������, ��������� ��������� wms ���� �������, ���� ��� ����
		 */
		fillStyleList(curWms==null?null:curWms.getServerName(), curWms==null?null:curWms.getLayerName(), curWms==null?null:curWms.getStyleName());
		
		/**
		 * ��������� ��������� ������
		 */
		wmsPanel.pack();
	}
	
	/**
	 * ��������� ������ wms �����
	 * @param serverName
	 * @param layerName
	 */
	protected void fillLrList(String serverName, String layerName){
		/**
		 * ������� ������
		 */
		lrSel.removeAll(); // ���������� ������
		lrOptions.clear(); // ���� �����
		lrSel.add("-");  // ������ ����� ������
		lrOptions.add(null); // ��� ���� ������� ������		 
		int lrIndex = 0; //����� ���������� ������
		
		/**
		 * ���� ������ wms ������
		 */
		if(serverName!=null){
			/**
			 * �������� ���������� � ���
			 */
			WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
 			/**
 			 * �������� �� ������ ��������� �����(�� ���� ����)
 			 */
			for(WMSLayerCapabilities layerCaps: caps.getNamedLayers()){
 				if(layerCaps.getName().equals(layerName)){
 					/**
 					 * ���� ��� ���� ��������� � ������� ���������,
 					 *  �� ���������� ���������� �����
 					 */
 					lrIndex = lrSel.getItemCount();
 				}
 				/**
 				 * ��������� ��� ���� � ������ �����
 				 */
 				lrOptions.add(layerCaps.getName());
 				/**
 				 * ��������� �������� ���� � ���������� ������
 				 */
 				lrSel.add(layerCaps.getTitle());
 			}
		}
		/**
		 * ���������� � ���������� ������ ������� ��������� �����
		 */
		lrSel.select(lrIndex);
		wmsPanel.layout(true);
	}
	
	/**
	 * ��������� ������ wms ������
	 * @param serverName
	 * @param layerName
	 * @param styleName
	 */
	protected void fillStyleList(String serverName, String layerName, String styleName){
		/**
		 * ������� ������
		 */
		stlSel.removeAll();  //���������� ������
		stlOptions.clear();  //������ �����
		stlSel.add("-");    //��������� ������ ����� �������
		stlOptions.add(null); // ��� ������� ������
		int stlIndex = 0;  //������� ��������� �����
		
		/**
		 * ���� ������ wms ������ � ����
		 */
		if(serverName!=null && layerName!=null){
			/**
			 * �������� ���� � �������
			 */
			WMSCapabilities caps = Workspace.getCurrent().getWmsService().getByServerName(serverName);
			/**
			 * �������� �� ��� ���� � ����
			 */
			WMSLayerCapabilities layerCaps = caps.getLayerByName(layerName);
			/**
			 * �������� ������ ������ ����
			 */
			Set<WMSLayerStyle> lst =  layerCaps.getStyles();
			if(lst!=null){
				/**
				 * �������� �� ���� ������
				 */
				for(WMSLayerStyle style: lst){
					if(style.getName().equals(styleName)){
						/**
						 * ���� ��� ����� ��������� � ��������� ������ � �������,
						 * �� ���������� ����� ������
						 */
						stlIndex = stlSel.getItemCount();
					}
					/**
					 * ��������� ����� ������ ��� �����
					 */
					stlSel.add(style.getTitle());
					/**
					 * ��������� ��� ����� � ������ �����
					 */
					stlOptions.add(style.getName());
				}				
			}
		}
		/**
		 * ������ ������� ��������� �����
		 */
		stlSel.select(stlIndex);
		wmsPanel.layout(true);
	}
	
	/**
	 * ��������� ����� � ��������� �������
	 */
	public void save(){
		/**
		 * �������� ������ ��������� ����� �����
		 */
		Set<String> enabled = new HashSet<>();
		/**
		 * �������� �� ���� ���������
		 */
		for(Entry<String, Button> entry: layersCheckbox.entrySet()){
			Button chk = entry.getValue();
			if(chk.getSelection()){
				/**
				 * ���� ������� ����������, ��������� �������� ���� � ������ ���������
				 */
				enabled.add(entry.getKey());
			}
		}
		/**
		 * ��������� � �������� ������ ��������� ��������� �����
		 */
		Workspace.getCurrent().currentProject.setLayerSettings(new LayersSettings(enabled));
				
		WmsLayerSettings wls = null;
		/**
		 * �������� ����� ���������� wms �������
		 */
		int srvIndex = srvSel.getSelectionIndex();
		if(srvIndex>0){
			/**
			 * ���� ������ ������,
			 * �������� ����� ���� � �����
			 */
			int lrIndex = lrSel.getSelectionIndex();
			int stlIndex = stlSel.getSelectionIndex();
			if(lrIndex>0 && (stlOptions.size()==1 || stlIndex>0)){
				/**
				 * ���� ���� � ����� �������(��� ������ ���),
				 * ������� ����� ��������� WMS
				 */
				wls = new WmsLayerSettings(srvSel.getItem(srvIndex), lrOptions.get(lrIndex), stlIndex>0?stlOptions.get(stlIndex):null);
			}
		}
		/**
		 * ��������� ��������� WMS
		 */
		Workspace.getCurrent().currentProject.setWmsSettings(wls);
		
		/**
		 * ������������ �����
		 */
		Workspace.getCurrent().getEditScene().redraw();
	}
	
}
