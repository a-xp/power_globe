package powerglobe.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import powerglobe.project.Slide;
import powerglobe.project.Workspace;


/**
 * Блок списка слайдов
 * @author 1
 *
 */
public class SliderList {	
	
	/**
	 * размеры слайда
	 */
	public static final int slideWidth = 150;
	public static final int slideHeight = 120;
	/**
	 * 	Класс SWT для загрузки изображений
	 */
	private ResourceManager rm;
	/**
	 * Кнопки слайдов
	 */
	private Map<Slide, Label> buttons = new HashMap<>();
	
	/**
	 * Картинки слайдов
	 */
	private Map<Slide, Image> images = new HashMap<>();
	
	/**
	 * Текущий выбранный слайд
	 */
	private Slide selectedSlide = null; 
	
	private ScrolledComposite sliderScroll;
	private Composite sliderContainer;
	
	/**
	 * Инициализация блока со список слайдов
	 * 
	 * @param parent
	 */
	public SliderList(Composite parent) {
		Workspace.getCurrent().setSliderList(this);
		
		/**
		 * Добавляем возможность прокрутки списка, если не влезает по высоте
		 */
		sliderScroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		gridData.widthHint = SliderList.slideWidth+20;
		gridData.minimumWidth = SliderList.slideHeight+20;
		sliderScroll.setLayoutData(gridData);
		sliderScroll.setExpandVertical(true);
		sliderScroll.setExpandHorizontal(true);
		sliderScroll.setMinWidth(0);
		
		/**
		 * Родительский блок для кнопок слайдов
		 */
		GridLayout listLayout = new GridLayout(1, false);
		listLayout.verticalSpacing = 5;
		sliderContainer = new Composite(sliderScroll, SWT.NONE);
		sliderContainer.setLayout(listLayout);
		sliderScroll.setContent(sliderContainer);
		
		/**
		 * Устанавливаем фон под кнопками слайдов
		 */
		rm = new LocalResourceManager(JFaceResources.getResources(), sliderContainer);
		sliderContainer.setBackground(rm.createColor(new RGB(190, 190, 190)));
	
		/**
		 * Добавляем обработчики на события, связанные с изменением глобального списка слайдов
		 */
		Workspace ws = Workspace.getCurrent();
		/**
		 * при удалении слайда
		 */
		ws.addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onRemoveSlide((Slide)t);
			}
		});	
		/**
		 *  при перемещении слайда 
		 */
		ws.addListener(Workspace.EVENT_SLIDE_MOVE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onMoveSlide((Slide)t);				
			}
		});
		
		/**
		 * при добавлении слайда
		 */
		ws.addListener(Workspace.EVENT_SLIDE_ADD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onAddSlide((Slide)t);
			}
		});		
		/**
		 * при загрузке слайда из файла
		 */
		ws.addListener(Workspace.EVENT_SLIDE_LOAD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onLoadSlide((Slide)t);
			}
		});		
		/**
		 * при получении скриншота для слайда
		 */
		ws.addListener(Workspace.EVENT_SLIDE_IMAGE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onImageSet((SlideImage)t);
			}
		});	
		/**
		 * при закрытии текущего проекта - удаление всех кнопок с их картинками
		 */
		ws.addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				selectedSlide = null;
				/**
				 * Проходим по всем кнопкам
				 */
				for(Label label: buttons.values()){
					label.dispose(); //удаляем
				}
				/**
				 * Проходим по всем картинкам
				 */
				for(Image img: images.values()){
					img.dispose(); //удаляем
				}
				buttons.clear();
				images.clear();
				redrawContainer();
			}
		});	
				
		/**
		 * Настройка Drag&Drop - отслеживаем конец перемещения кнопки 
		 */
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		final DropTargetAdapter dragAdapter = new DropTargetAdapter()
		{
			/**
			 * Обрабатываем момент оканчания перемещения слайда
			 */
			@Override
			public void drop(DropTargetEvent event) {
				final Label button = (Label) ((StructuredSelection)transfer.getSelection()).getFirstElement();
				if(buttons.containsValue(button)){
					/**
					 * Определяем перемещаемый слайд
					 */
					Slide slide = null;
					/**
					 * Проходим по всем кнопкам, проверяем какому слайду соответствует перемещаемая
					 */
					for(Entry<Slide, Label> entry: buttons.entrySet()){
						if(entry.getValue()==button){
							slide = entry.getKey();
						}
					}
					/**
					 * Расчет новой позиции слайда по координатам события
					 */
					Point p = sliderContainer.toControl(event.x, event.y); // получаем координаты куда перемещена кнопка относительно блока списка слайдов
					int newIndex = Math.min((int) Math.ceil(1.0*(p.y-5)/(slideHeight+5)), buttons.size())-1;  // считаем в какую позицию нужно поставть слайд
					if(slide.index!=newIndex){
						/*
						 * если слайд в итоге поменял позицию, то перемещаем слайд в списке
						 */
						Workspace.getCurrent().currentProject.move(slide, newIndex);
					}
				}				
			}			
		};
		/**
		 * Инициализируем принимающую область для перемещения кнопок сладйов на блоке слайдов
		 */
		final DropTarget dropTarget = new DropTarget(sliderContainer, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { transfer });
		dropTarget.addDropListener(dragAdapter);		
	}
	
	/**
	 * удаляем кнопку и картинку слайда
	 * @param slide
	 */
	protected void onRemoveSlide(Slide slide){
		if(slide.equals(selectedSlide))selectedSlide = null;
		if(buttons.containsKey(slide)){
			/**
			 * если есть кнопка у слайда
			 */
			buttons.get(slide).dispose();
			buttons.remove(slide);
		}
		if(images.containsKey(slide)){
			/**
			 * если есть картинка у слайда
			 */
			images.get(slide).dispose();
			images.remove(slide);
		}
		redrawContainer();
	}	
	
	/**
	 * Создание кнопки слайда
	 * @param slide
	 */
	protected void createButton(final Slide slide){
		final Label button = new Label(sliderContainer, SWT.NONE);
		GridData data = new GridData(SWT.CENTER,SWT.CENTER,true,false);
		data.heightHint = slideHeight;
		data.widthHint = slideWidth;
		button.setLayoutData(data);
		buttons.put(slide, button);
		
		/** 
		 * обработчик нажатия левой кнопкой(e.button=1) мыши
		 */
		button.addMouseListener(new MouseListener() {					
			@Override
			public void mouseUp(MouseEvent e) {}					
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1) return;
				setSelectedSlide(slide);  //устанавливаем текущим выбранным
				Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_SELECT, slide);					
			}					
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});	
		/**
		 * Рисуем рамку и подпись кнопки слайда
		 */
		button.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point rect = button.getSize();
				if(slide.equals(selectedSlide)){	
					/**
					 * Если рисуем слайд, который выбран,
					 * добавляем рамку
					 */
					e.gc.setForeground(rm.createColor(new RGB(255, 255, 150)));
					e.gc.setLineWidth(5);
					e.gc.drawRectangle(0, 0, rect.x, rect.y);
				}
				e.gc.setForeground(rm.createColor(new RGB(200, 255, 255)));
				int textWidth = e.gc.stringExtent(slide.getTitle()).x;
				e.gc.drawText(slide.getTitle(), rect.x/2-textWidth/2, rect.y-30, true);
			}
		});
		
		/**
		 * добавляем всплывающее меню (Удалить)
		 */
		Menu popupMenu = new Menu(button);
	    
		/**
		 * Пункт меню Удалить
		 */
	    MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
	    deleteItem.setText("Delete");
	    deleteItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Workspace.getCurrent().currentProject.remove(slide);				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		}); 
	    button.setMenu(popupMenu);	
	    
	    /**
	     * Ставим кнопку на ее позицию среди существующих
	     */
		int cnt = Workspace.getCurrent().currentProject.getSlidesCnt();
	    if(cnt>1 && slide.index!=cnt-1){
	    	Control[] list = sliderContainer.getChildren();
	    	button.moveAbove(list[slide.index]);
	    }
	    
	    /** 
	     * Настройка Drag&Drop - отслеживаем начало перемещения кнопки
	     */
	    final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
	    final DragSourceAdapter drgSrcAdapter = new DragSourceAdapter(){
	    	@Override
			public void dragSetData(DragSourceEvent event) {
	    		/**
	    		 * Сохраняем элемент, который перемещается
	    		 */
	    		transfer.setSelection(new StructuredSelection(button));
			}
	    };
	    /**
	     * Привязываем к кнопке обработчик начала перемещения
	     */
	    final DragSource dragSource = new DragSource(button, DND.DROP_MOVE);
	    dragSource.setTransfer(new Transfer[]{transfer});
	    dragSource.addDragListener(drgSrcAdapter);
	    
	    redrawContainer();
	}
	
	/**
	 * При загрузке слайда создаем ему кнопку
	 * @param slide
	 */
	protected void onLoadSlide(Slide slide){
		createButton(slide);	
	}
	
	/**
	 * При добавлении слайда создаем ему кнопку и устанавливаем его текущим
	 * @param slide
	 */
	protected void onAddSlide(final Slide slide){
		if(selectedSlide!=null){
			/**
			 * Если до этого был выбран слайд,
			 * то перерисовываем его, чтобы снять рамку
			 */
			buttons.get(selectedSlide).redraw();
		}
		/**
		 * Ставим текущим и создаем кнопку
		 */
		selectedSlide = slide;		
		createButton(slide);
	    Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_SELECT, slide);
    }

	/**
	 * Перерисовываем блок списка слайдов
	 */
	protected void redrawContainer(){
	    sliderContainer.layout(true);
	    sliderScroll.layout(true);
	    sliderScroll.setMinHeight(sliderContainer.computeSize(slideWidth+10, SWT.DEFAULT).y);
	}
	
	/**
	 * Когда готов скриншот слайда ставим его на кнопку и сохраняем картинку в список
	 * @param img
	 */
	protected void onImageSet(SlideImage img){
		/**
		 * Берем кнопку для слайда
		 */	
		Label button = buttons.get(img.slide);
		button.setImage(img.img);  // ставим на нее картинку
		images.put(img.slide, img.img); // сохраняем картинку в список
		/**
		 * Обновляем раскладку блока
		 */
		sliderContainer.layout(true);
		sliderScroll.layout(true);
	}
	
	/**
	 * При выборе слайда нужно перерисовать обе кнопки (и предыдущую и текущую)
	 * @param slide
	 */
	public void setSelectedSlide(Slide slide){
		if(selectedSlide!=null){
			/**
			 * Если был выбран слайд до этого,
			 * перерисовываем его кнопку, чтобы снять рамку
			 */
			Label prev = buttons.get(selectedSlide);
			prev.redraw();
		}
		/**
		 * Ставим слайд текущим и перерисовываем его кнопку
		 */
		buttons.get(slide).redraw();
		selectedSlide = slide;	
	}
	
	/**
	 * Меняем порядок кнопок при перемещении слайда
	 * @param slide
	 */
	protected void onMoveSlide(Slide slide){
		Label button = buttons.get(slide); // берем кнопку слайда
		Control[] list = sliderContainer.getChildren(); // текущий список кнопок блока
		int curIndex = 0;
		/**
		 * Определяем предыдущую позицию слайда по положению кнопки в блоке
		 */
		for(;curIndex<list.length;curIndex++){
			if(list[curIndex]==button)break;
		}
		if(slide.index==0){
			/**
			 * Если новая позиция - в начале,
			 * то просто двигаем кнопку перед первым элементои
			 */
			button.moveAbove(list[0]);
		}else{
			if(curIndex<slide.index){
				/**
				 * если двигается вверх
				 */
				button.moveBelow(list[slide.index]);	
			}else{
				/**
				 * если двигается вниз, учесть уменьшение кол-ва элементов на ним
				 */
				button.moveBelow(list[slide.index-1]);	
			}
		}
		sliderContainer.layout(true);
		sliderScroll.layout(true);		
	}
	
	public Slide getSelectedSlide(){
		return selectedSlide;
	}
}
