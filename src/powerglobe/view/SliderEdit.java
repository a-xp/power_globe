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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
 * Класс окна редактирования слайда
 * @author 1
 *
 */
public class SliderEdit {
	
	/**
	 * ID базовых полей слайда
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
	StackLayout panelLayout;
	Composite annotationPanel;
	ScrolledComposite formScroll;	
	
	/**
	 * По-умолчанию выбран пустой слайд
	 */
	protected Slide curSlide = emptySlide();
	
	/**
	 * Типы аннотаций
	 */
	protected List<Class<?>> annotationList;
	/**
	 * Поля ввода для каждого типа аннотации
	 */
	protected Map<Class<?>, Map<String, AbstractAnnotationProp>> annotationProps = new HashMap<>();
	/**
	 * Вкладки для каждого типа аннотации
	 */
	protected Map<Class<?>, Composite> annotationTabs = new HashMap<>();
	
	/**
	 * Список основных полей слайда
	 */
	protected Map<String, AbstractAnnotationProp> baseFields = new TreeMap<>();
	{
		baseFields.put(F_TITLE, new SingleLine("Slide title"));   // Название
		baseFields.put(F_LAT, new DecimalValue("Latitude"));  //  Широта
		baseFields.put(F_LON, new DecimalValue("Longitude"));  // Долгота
		baseFields.put(F_ELEVATION, new DecimalValue("Elevation (m)")); // Высота
		baseFields.put(F_DELAY, new IntegerValue("Delay (ms)"));  // Задержка
		baseFields.put(F_MOVE, new DecimalValue("Fly speed (deg per s)")); // Скорость перемещения
		baseFields.put(F_TURN, new DecimalValue("Turn speed (deg per s)")); // Скорость разворота
		baseFields.put(F_PITCH, new DecimalValue("Pitch degree")); // Угол камеры
		baseFields.put(F_PATH, new BooleanValue("Enable path")); // включить пути
	}
		
	private Combo annotationType;
	
	/**
	 * Генерит пустой слайд
	 * @return
	 */
	protected Slide emptySlide(){
		Position pos = new Position(Angle.fromDegrees(53), Angle.fromDegrees(35), 3000000); // Позиция по-умолчанию на Москву
		return new Slide(pos, "Новый слайд", null, -1);
	}
	
	public void setUp(){
		/**
		 * Создаем скролл для формы
		 */
		formScroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		GridData scrollData = new GridData(SWT.FILL, SWT.FILL, false, true);
		scrollData.widthHint = 230;
		formScroll.setLayoutData(scrollData);
		formScroll.setExpandHorizontal(true);
		formScroll.setExpandVertical(true);
		formScroll.setMinWidth(0);
		
		/**
		 * Создаем форму
		 */
		form = new Composite(formScroll, SWT.NONE);		
		GridLayout formLayout = new GridLayout(1, false);
		formLayout.verticalSpacing = 3;
		form.setLayout(formLayout);
		formScroll.setContent(form);
		
		/**
		 * Создаем поля ввода основных свойств слайда
		 */
		for(AbstractAnnotationProp field: baseFields.values()){
			field.createControls(form);
		}
		
		/**
		 * Список выбора типа аннотации
		 */
		GridData labelFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		GridData inputFormat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		
		/**
		 * Метка тип аннотации
		 */
		Label annotationLabel = new Label(form, SWT.NONE);
		annotationLabel.setText("Annotation type");
		annotationLabel.setLayoutData(labelFormat);
		
		/**
		 * Получаем список аннотаций Класс->Название
		 */
		final Map<Class<?>, String> annotations = AnnotationFactory.getNames();
		annotationList = new ArrayList<>(annotations.keySet());
		annotationList.add(0, null); // Добавляем первым элементом в список "Нет"
		/**
		 * Из списка типов аннотаций по карте создаем массив названий аннотаций
		 */
		String[] annotationTitles = annotationList.stream().map(cls -> cls==null?"нет":annotations.get(cls)).toArray(String[]::new);
		
		/**
		 * Создаем выпадающий список и заполняем названийми аннотаций
		 */
		annotationType = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		annotationType.setItems(annotationTitles);
		annotationType.setLayoutData(inputFormat);
		annotationType.select(0);
		/**
		 * Обработчик выбора аннотации 
		 */
		annotationType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				/**
				 * Определяем какая аннотация выбрана
				 */
				int index = annotationType.getSelectionIndex(); // номер в списке
				Class<?> cls = annotationList.get(index);  // получаем тип аннотации (класс)
				if(cls==null){
					/**
					 * Если выбран "Нет", скрываем панель настроек аннотации
					 */
					panelLayout.topControl = null;
					annotationPanel.setSize(0, 0);					
				}else{
					/**
					 * Выводим вкладку, которая ей соответсвует
					 */
					Composite tab = annotationTabs.get(cls);
					panelLayout.topControl = tab;
					tab.layout(true);
					/**
					 * Определяем какие выводить параметры для выбранной аннотации
					 */
					Map<String, String> params = null;
					if(curSlide.getAnnotation()!=null && curSlide.getAnnotation().getClass()==cls){
						/* если выбран тип аннотации как у редактируемого слайда - берем из слайда*/
						params = curSlide.getAnnotation().getState();
					}else{						
						/* иначе берем параметры по-умолчанию для новой аннотации */
						try {
							AbstractAnnotation ann = (AbstractAnnotation) cls.newInstance(); // Создаем новую аннотацию этого типа
							params = ann.getState();  // и берем ее параметры
						} catch (InstantiationException | IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
					/**
					 * Выставляем полученные параметры в форму
					 * 
					 * Проходим по всем полям ввода для данного типа аннотации
					 */
					for(Entry<String, AbstractAnnotationProp> entry: annotationProps.get(cls).entrySet()){
						/**
						 * Ставим в поле ввода значение из параметров
						 */
						entry.getValue().setValue(params.get(entry.getKey()));
					}
				}								
				annotationPanel.layout(true);
				form.layout(true);
			}
		});
		
		/**
		 * генерим вкладки с полями ввода для параметров всех аннотаций
		 * 
		 * Создаем панель с раскладкой Стэк (дети расположены в стопку, т.е.
		 *  в каждый момент видно только один дочерний элемент)
		 */
		annotationPanel = new Composite(form, SWT.NONE);
		GridData annotationPanelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		annotationPanel.setLayoutData(annotationPanelData);
		panelLayout = new StackLayout();
		annotationPanel.setLayout(panelLayout);
		
		/**
		 * проходим по все аннотациям
		 */
		for(Class<?> cls: annotations.keySet()){
			Map<String, AbstractAnnotationProp> props = null;
			try {
				/**
				 * Забираем список полей у аннотации
				 */
				Method m = cls.getMethod("getControls");
				props = (Map<String, AbstractAnnotationProp>)m.invoke(new Object[0]);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				continue;
			}
			if(props==null) continue;
			/**
			 * Создаем вкладку и ставим на нее поля аннотации
			 */
			Composite tab = new Composite(annotationPanel, SWT.NONE);
			GridLayout tabLayout = new GridLayout(1, false);
			tabLayout.verticalSpacing = 0;
			tab.setLayout(tabLayout);
			for(AbstractAnnotationProp prop: props.values()){
				prop.createControls(tab);
			}
			annotationProps.put(cls, props);
			annotationTabs.put(cls, tab);			
		}
		
		/**
		 * Создаем кнопки сохранить и сбросить
		 */
		Composite buttonGroup = new Composite(form, SWT.NONE);
		buttonGroup.setLayout(new RowLayout());
		
		/**
		 * Кнопка сохранения 
		 */	
		Button save = new Button(buttonGroup, SWT.PUSH);
		save.setText("Save");
		/**
		 * Обработчик нажатия Save 
		 */
		save.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return; // проверяем, что нажата левая кнопка
				onSave();
			}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		/**
		 * Кнопка сброса
		 */
		Button reset = new Button(buttonGroup, SWT.PUSH);
		reset.setText("Reset");
		/**
		 * Обработчик нажатия сброса
		 */
		reset.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1)return; // проверяем, что нажата левая кнопка
				onSelectSlide(curSlide);  // запускаем метод Выбор слайда - устанавливает поля формы из текущего слайда
			}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		/**
		 * Обработчик события Выбор слайда - выводим выбранный слайд в форму
		 */
		Workspace.getCurrent().addListener(Workspace.EVENT_SLIDE_SELECT, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Slide slide = (Slide)t;
				onSelectSlide(slide);
			}
		});
		
		/**
		 * Обработчик события Удаление слайда - если выбран удаленный слайд, сбрасываем форму
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
		 * Обработчик события Сброс проекта - сбрасываем форму
		 */
		Workspace.getCurrent().addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onSelectSlide(emptySlide());
			}
		});
		
		onSelectSlide(curSlide);
		
		/**
		 * Обновляем раскладку форму
		 */
		formScroll.layout(true);
		form.layout(true);
		annotationPanel.layout(true);
		formScroll.setMinHeight(form.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);	
	}

	/**
	 * Выводит слайд в форму редактирования
	 * @param slide
	 */
	protected void onSelectSlide(Slide slide){
		curSlide = slide;
		/**
		 * Ставим основные поля слайда
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
		 * Ставим аннотацию, если она есть
		 */
		AbstractAnnotation ann = slide.getAnnotation();
		int index = annotationList.indexOf(ann==null?null:ann.getClass());
		annotationType.select(index);
		if(ann==null){
			panelLayout.topControl=null;
		}else{
			/**
			 * Выводим вкладку редактирования для выбранного типа аннотации
			 */
			Class<?> cls = ann.getClass();
			/**
			 * Получаем значения свойств из слайда
			 */
			Map<String, String> params = slide.getAnnotation().getState();
			/**
			 * Выводим панель для текущего типа аннотации
			 */
			panelLayout.topControl = annotationTabs.get(cls);
			/**
			 * Проходим по всем полям для данного типа и ставим значение из параметров слайда
			 */
			for(Entry<String, AbstractAnnotationProp> entry: annotationProps.get(cls).entrySet()){
				entry.getValue().setValue(params.get(entry.getKey()));
			}
		}
		form.layout(true);
	}
	
	/**
	 * Сохраняет содержимое формы
	 */
	protected void onSave(){
		boolean valid = true;
		/**
		 * Проверяем основные поля слайда
		 */
		for(AbstractAnnotationProp prop: baseFields.values()){
			valid = valid && prop.validate();
		}
		/**
		 * Проверяем поля аннотации
		 */	
		int index = annotationType.getSelectionIndex();
		if(index>0){
			Class<?> cls = annotationList.get(index);
			for(AbstractAnnotationProp prop: annotationProps.get(cls).values()){
				valid = valid && prop.validate();
			}	
		}
		if(!valid)return;
		
		Position pos = new Position(Angle.fromDegrees(baseFields.get(F_LAT).getDoubleValue()),
									Angle.fromDegrees(baseFields.get(F_LON).getDoubleValue()),
									baseFields.get(F_ELEVATION).getDoubleValue());
		AbstractAnnotation ann = null;
		/**
		 * Собираем параметры аннотации
		 */
		if(index>0){
			Class<?> cls = annotationList.get(index);
			Map<String, String> params = new HashMap<>();
			for(Entry<String, AbstractAnnotationProp> entry: annotationProps.get(cls).entrySet()){
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
		 * Создаем новый слайд, записываем на вместо выбранного или просто добавляем
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
			Workspace.getCurrent().currentProject.add(newSlide);  // слайд новый
		}else{
			Workspace.getCurrent().currentProject.replace(curSlide, newSlide); // слайд уже существует (заполнен номер)
		}
	}
		
	public SliderEdit(Composite parent){
		this.parent = parent;
	}
}
