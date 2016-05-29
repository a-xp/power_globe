package powerglobe.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;
import powerglobe.wwd.AnnotationFactory;
import powerglobe.wwd.annotations.AbstractAnnotation;
import powerglobe.wwd.annotations.props.AbstractAnnotationProp;
import powerglobe.wwd.annotations.props.BooleanValue;
import powerglobe.wwd.annotations.props.DecimalValue;
import powerglobe.wwd.annotations.props.IntegerValue;
import powerglobe.wwd.annotations.props.SingleLine;
/**
 * ����� ���� �������������� ������
 * @author 1
 *
 */
public class SliderEdit {
	
	/**
	 * ID ������� ����� ������
	 */
	final static String F_TITLE = "1title"; 
	final static String F_LAT = "2latitude"; 
	final static String F_LON ="3longitude";
	final static String F_ELEVATION = "4elevation";
	final static String F_DELAY = "5delay";
	final static String F_MOVE = "6move";
	final static String F_TURN = "7turn";
	final static String F_PITCH = "8pitch";
	final static String F_PATH = "9path"; 
	
	protected Composite parent;
	protected Composite form;
	Composite annotationPanel;
	ScrolledComposite formScroll;	
	
	protected Map<String, AbstractAnnotationProp> curProps;
	
	/**
	 * ��-��������� ������ ������ �����
	 */
	protected Slide curSlide = emptySlide();
	
	/**
	 * ���� ���������
	 */
	protected List<Class<?>> annotationList;
	
	/**
	 * ������ �������� ����� ������
	 */
	protected Map<String, AbstractAnnotationProp> baseFields = new TreeMap<>();
	{
		baseFields.put(F_TITLE, new SingleLine("Slide title"));   // ��������
		baseFields.put(F_LAT, new DecimalValue("Latitude", -90.0, 90.0));  //  ������
		baseFields.put(F_LON, new DecimalValue("Longitude", -180.0, 180.0));  // �������
		baseFields.put(F_ELEVATION, new DecimalValue("Elevation (m)", 3e3, 50e6)); // ������
		baseFields.put(F_DELAY, new IntegerValue("Delay (ms)", 100, 10000));  // ��������
		baseFields.put(F_MOVE, new DecimalValue("Fly speed (deg per s)", 1.0, 30.0)); // �������� �����������
		baseFields.put(F_TURN, new DecimalValue("Turn speed (deg per s)", 30.0, 720.0)); // �������� ���������
		baseFields.put(F_PITCH, new DecimalValue("Pitch degree", -70.0 ,70.0 )); // ���� ������
		baseFields.put(F_PATH, new BooleanValue("Enable path")); // �������� ����
	}
		
	private Combo annotationType;
	
	/**
	 * ������� ������ �����
	 * @return
	 */
	protected Slide emptySlide(){
		Position pos = new Position(Angle.fromDegrees(53), Angle.fromDegrees(35), 3000000); // ������� ��-��������� �� ������
		return new Slide(pos, "����� �����", null, -1);
	}
	
	public void setUp(){
		/**
		 * ������� ������ ��� �����
		 */
		formScroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		GridData scrollData = new GridData(SWT.FILL, SWT.FILL, false, true);
		scrollData.widthHint = 230;
		formScroll.setLayoutData(scrollData);
		formScroll.setExpandHorizontal(true);
		formScroll.setExpandVertical(true);
		formScroll.setMinWidth(0);
		
		/**
		 * ������� �����
		 */
		form = new Composite(formScroll, SWT.NONE);		
		GridLayout formLayout = new GridLayout(1, false);
		formLayout.verticalSpacing = 3;
		form.setLayout(formLayout);
		formScroll.setContent(form);
		
		/**
		 * ������� ���� ����� �������� ������� ������
		 */
		for(AbstractAnnotationProp field: baseFields.values()){
			field.createControls(form);
		}
		
		/**
		 * ������ ������ ���� ���������
		 */
		GridData labelFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		
		/**
		 * ����� ��� ���������
		 */
		Label annotationLabel = new Label(form, SWT.NONE);
		annotationLabel.setText("Annotation type");
		annotationLabel.setLayoutData(labelFormat);
		
		/**
		 * �������� ������ ��������� �����->��������
		 */
		final Map<Class<?>, String> annotations = AnnotationFactory.getNames();
		annotationList = new ArrayList<>(annotations.keySet());
		annotationList.add(0, null); // ��������� ������ ��������� � ������ "���"
		/**
		 * �� ������ ����� ��������� �� ����� ������� ������ �������� ���������
		 */
		String[] annotationTitles = annotationList.stream().map(cls -> cls==null?"���":annotations.get(cls)).toArray(String[]::new);
		
		/**
		 * ������� ���������� ������ � ��������� ���������� ���������
		 */
		annotationType = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		annotationType.setItems(annotationTitles);
		annotationType.setLayoutData(inputFormat);
		annotationType.select(0);
		/**
		 * ���������� ������ ��������� 
		 */
		annotationType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				/**
				 * ���������� ����� ��������� �������
				 */
				int index = annotationType.getSelectionIndex(); // ����� � ������
				Class<?> cls = annotationList.get(index);  // �������� ��� ��������� (�����)
				showAnnotationTab(cls);
			}
		});
		
		/**
		 * ������� ������� � ������ ����� ��� ���������� ���� ���������
		 * 
		 * ������� ������ � ���������� ���� (���� ����������� � ������, �.�.
		 *  � ������ ������ ����� ������ ���� �������� �������)
		 */
		annotationPanel = new Composite(form, SWT.NONE);
		GridData annotationPanelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		annotationPanel.setLayoutData(annotationPanelData);
		//panelLayout = new StackLayout();
		//annotationPanel.setLayout(panelLayout);
		annotationPanel.setLayout(new FillLayout());
		
		/**
		 * ������� ������ ��������� � ��������
		 */
		Composite buttonGroup = new Composite(form, SWT.NONE);
		buttonGroup.setLayout(new RowLayout());
		
		/**
		 * ������ ���������� 
		 */	
		Button save = new Button(buttonGroup, SWT.PUSH);
		save.setText("Save");
		/**
		 * ���������� ������� Save 
		 */
		save.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return; // ���������, ��� ������ ����� ������
				onSave();
			}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		/**
		 * ������ ������
		 */
		Button reset = new Button(buttonGroup, SWT.PUSH);
		reset.setText("Reset");
		/**
		 * ���������� ������� ������
		 */
		reset.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return; // ���������, ��� ������ ����� ������
				onSelectSlide(curSlide);  // ��������� ����� ����� ������ - ������������� ���� ����� �� �������� ������
			}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		/**
		 * ���������� ������� ����� ������ - ������� ��������� ����� � �����
		 */
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Slide slide = (Slide)t;
				onSelectSlide(slide);
			}
		});
		
		/**
		 * ���������� ������� �������� ������ - ���� ������ ��������� �����, ���������� �����
		 */
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				if(curSlide.equals(t)){
					onSelectSlide(emptySlide());
				}
			}
		});
		/**
		 * ���������� ������� ����� ������� - ���������� �����
		 */
		Workspace.getCurrent().addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onSelectSlide(emptySlide());
			}
		});
		
		onSelectSlide(curSlide);
		
		/**
		 * ��������� ��������� �����
		 */
		formScroll.layout(true);
		form.layout(true);
		annotationPanel.pack(true);
		annotationPanel.layout(true);
		formScroll.setMinHeight(form.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);	
	}

	/**
	 * ������� ����� � ����� ��������������
	 * @param slide
	 */
	protected void onSelectSlide(Slide slide){
		curSlide = slide;
		/**
		 * ������ �������� ���� ������
		 */
		Position position = slide.getPosition();
		baseFields.get(F_TITLE).setValue(slide.getTitle());
		baseFields.get(F_LAT).setValue(position.latitude.degrees);
		baseFields.get(F_LON).setValue(position.longitude.degrees);
		baseFields.get(F_ELEVATION).setValue(position.elevation);
		baseFields.get(F_DELAY).setValue(slide.getDelay());
		baseFields.get(F_MOVE).setValue(slide.getMoveSpeed());
		baseFields.get(F_TURN).setValue(slide.getTurnSpeed());
		baseFields.get(F_PITCH).setValue(slide.getCameraPitch());
		baseFields.get(F_PATH).setValue(slide.isEnablePath());
		/**
		 * ������ ���������, ���� ��� ����
		 */
		AbstractAnnotation ann = slide.getAnnotation();
		int index = annotationList.indexOf(ann==null?null:ann.getClass());
		annotationType.select(index);

		showAnnotationTab(ann==null?null:ann.getClass());
	}
	
	/**
	 * ��������� ���������� �����
	 */
	protected void onSave(){
		boolean valid = true;
		/**
		 * ��������� �������� ���� ������
		 */
		for(AbstractAnnotationProp prop: baseFields.values()){
			valid = valid && prop.validate();
		}
		/**
		 * ��������� ���� ���������
		 */	
		int index = annotationType.getSelectionIndex();
		if(index>0){
			Class<?> cls = annotationList.get(index);
			for(AbstractAnnotationProp prop: curProps.values()){
				valid = valid && prop.validate();
			}	
		}
		if(!valid)return;
		
		Position pos = new Position(Angle.fromDegrees(baseFields.get(F_LAT).getDoubleValue()),
									Angle.fromDegrees(baseFields.get(F_LON).getDoubleValue()),
									baseFields.get(F_ELEVATION).getDoubleValue());
		AbstractAnnotation ann = null;
		/**
		 * �������� ��������� ���������
		 */
		if(index>0){
			Class<?> cls = annotationList.get(index);
			Map<String, String> params = new HashMap<>();
			for(Entry<String, AbstractAnnotationProp> entry: curProps.entrySet()){
				params.put(entry.getKey(), entry.getValue().getValue());
			}			
			try {
				ann = (AbstractAnnotation) cls.newInstance();
				ann.setState(params);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		/**
		 * ������� ����� �����, ���������� �� ������ ���������� ��� ������ ���������
		 */
		Slide newSlide = new Slide(-1, pos,
				baseFields.get(F_TITLE).getValue(),
				ann,
				baseFields.get(F_DELAY).getIntValue(),
				baseFields.get(F_MOVE).getDoubleValue(),
				baseFields.get(F_PATH).getBoolValue(),
				baseFields.get(F_PITCH).getDoubleValue(),
				baseFields.get(F_TURN).getDoubleValue()
			);
		if(curSlide.getIndex()==-1){
			Workspace.getCurrent().currentProject.add(newSlide);  // ����� �����
		}else{
			Workspace.getCurrent().currentProject.replace(curSlide, newSlide); // ����� ��� ���������� (�������� �����)
		}
	}
	
	protected void showAnnotationTab(Class cls){
		for(Control child : annotationPanel.getChildren()){
			child.dispose();
		}				
		curProps = null;
		if(cls!=null){
			try {
				/**
				 * �������� ������ ����� � ���������
				 */
				Method m = cls.getMethod("getControls");
				curProps = (Map<String, AbstractAnnotationProp>)m.invoke(new Object[0]);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(curProps!=null){
			/**
			 * ������� ������� � ������ �� ��� ���� ���������
			 */
			Composite tab = new Composite(annotationPanel, SWT.NONE);
			GridLayout tabLayout = new GridLayout(1, false);
			tabLayout.verticalSpacing = 0;
			tab.setLayout(tabLayout);
			for(AbstractAnnotationProp prop: curProps.values()){
				prop.createControls(tab);
			}
			
			/**
			 * ���������� ����� �������� ��������� ��� ��������� ���������
			 */
			Map<String, String> params = null;
			if(curSlide.getAnnotation()!=null && curSlide.getAnnotation().getClass()==cls){
				/* ���� ������ ��� ��������� ��� � �������������� ������ - ����� �� ������*/
				params = curSlide.getAnnotation().getState();
			}else{						
				/* ����� ����� ��������� ��-��������� ��� ����� ��������� */
				try {
					AbstractAnnotation ann = (AbstractAnnotation) cls.newInstance(); // ������� ����� ��������� ����� ����
					params = ann.getState();  // � ����� �� ���������
				} catch (InstantiationException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
			/**
			 * ���������� ���������� ��������� � �����
			 * 
			 * �������� �� ���� ����� ����� ��� ������� ���� ���������
			 */
			for(Entry<String, AbstractAnnotationProp> entry: curProps.entrySet()){
				/**
				 * ������ � ���� ����� �������� �� ����������
				 */
				entry.getValue().setValue(params.get(entry.getKey()));
			}				
		}	
		formScroll.setMinHeight(form.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
		annotationPanel.layout(true);
		form.layout(true);
	}
			
	public SliderEdit(Composite parent){
		this.parent = parent;
	}
}
