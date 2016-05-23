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
 * ���� ������ �������
 * @author 1
 *
 */
public class SliderList {	
	
	/**
	 * ������� ������
	 */
	public static final int slideWidth = 150;
	public static final int slideHeight = 120;
	/**
	 * 	����� SWT ��� �������� �����������
	 */
	private ResourceManager rm;
	/**
	 * ������ �������
	 */
	private Map<Slide, Label> buttons = new HashMap<>();
	
	/**
	 * �������� �������
	 */
	private Map<Slide, Image> images = new HashMap<>();
	
	/**
	 * ������� ��������� �����
	 */
	private Slide selectedSlide = null; 
	
	private ScrolledComposite sliderScroll;
	private Composite sliderContainer;
	
	/**
	 * ������������� ����� �� ������ �������
	 * 
	 * @param parent
	 */
	public SliderList(Composite parent) {
		Workspace.getCurrent().setSliderList(this);
		
		/**
		 * ��������� ����������� ��������� ������, ���� �� ������� �� ������
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
		 * ������������ ���� ��� ������ �������
		 */
		GridLayout listLayout = new GridLayout(1, false);
		listLayout.verticalSpacing = 5;
		sliderContainer = new Composite(sliderScroll, SWT.NONE);
		sliderContainer.setLayout(listLayout);
		sliderScroll.setContent(sliderContainer);
		
		/**
		 * ������������� ��� ��� �������� �������
		 */
		rm = new LocalResourceManager(JFaceResources.getResources(), sliderContainer);
		sliderContainer.setBackground(rm.createColor(new RGB(190, 190, 190)));
	
		/**
		 * ��������� ����������� �� �������, ��������� � ���������� ����������� ������ �������
		 */
		Workspace ws = Workspace.getCurrent();
		/**
		 * ��� �������� ������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_REMOVE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onRemoveSlide((Slide)t);
			}
		});	
		/**
		 *  ��� ����������� ������ 
		 */
		ws.addListener(Workspace.EVENT_SLIDE_MOVE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onMoveSlide((Slide)t);				
			}
		});
		
		/**
		 * ��� ���������� ������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_ADD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onAddSlide((Slide)t);
			}
		});		
		/**
		 * ��� �������� ������ �� �����
		 */
		ws.addListener(Workspace.EVENT_SLIDE_LOAD, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onLoadSlide((Slide)t);
			}
		});		
		/**
		 * ��� ��������� ��������� ��� ������
		 */
		ws.addListener(Workspace.EVENT_SLIDE_IMAGE, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				onImageSet((SlideImage)t);
			}
		});	
		/**
		 * ��� �������� �������� ������� - �������� ���� ������ � �� ����������
		 */
		ws.addListener(Workspace.EVENT_CLEAR, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				selectedSlide = null;
				/**
				 * �������� �� ���� �������
				 */
				for(Label label: buttons.values()){
					label.dispose(); //�������
				}
				/**
				 * �������� �� ���� ���������
				 */
				for(Image img: images.values()){
					img.dispose(); //�������
				}
				buttons.clear();
				images.clear();
				redrawContainer();
			}
		});	
				
		/**
		 * ��������� Drag&Drop - ����������� ����� ����������� ������ 
		 */
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		final DropTargetAdapter dragAdapter = new DropTargetAdapter()
		{
			/**
			 * ������������ ������ ��������� ����������� ������
			 */
			@Override
			public void drop(DropTargetEvent event) {
				final Label button = (Label) ((StructuredSelection)transfer.getSelection()).getFirstElement();
				if(buttons.containsValue(button)){
					/**
					 * ���������� ������������ �����
					 */
					Slide slide = null;
					/**
					 * �������� �� ���� �������, ��������� ������ ������ ������������� ������������
					 */
					for(Entry<Slide, Label> entry: buttons.entrySet()){
						if(entry.getValue()==button){
							slide = entry.getKey();
						}
					}
					/**
					 * ������ ����� ������� ������ �� ����������� �������
					 */
					Point p = sliderContainer.toControl(event.x, event.y); // �������� ���������� ���� ���������� ������ ������������ ����� ������ �������
					int newIndex = Math.min((int) Math.ceil(1.0*(p.y-5)/(slideHeight+5)), buttons.size())-1;  // ������� � ����� ������� ����� �������� �����
					if(slide.index!=newIndex){
						/*
						 * ���� ����� � ����� ������� �������, �� ���������� ����� � ������
						 */
						Workspace.getCurrent().currentProject.move(slide, newIndex);
					}
				}				
			}			
		};
		/**
		 * �������������� ����������� ������� ��� ����������� ������ ������� �� ����� �������
		 */
		final DropTarget dropTarget = new DropTarget(sliderContainer, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { transfer });
		dropTarget.addDropListener(dragAdapter);		
	}
	
	/**
	 * ������� ������ � �������� ������
	 * @param slide
	 */
	protected void onRemoveSlide(Slide slide){
		if(slide.equals(selectedSlide))selectedSlide = null;
		if(buttons.containsKey(slide)){
			/**
			 * ���� ���� ������ � ������
			 */
			buttons.get(slide).dispose();
			buttons.remove(slide);
		}
		if(images.containsKey(slide)){
			/**
			 * ���� ���� �������� � ������
			 */
			images.get(slide).dispose();
			images.remove(slide);
		}
		redrawContainer();
	}	
	
	/**
	 * �������� ������ ������
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
		 * ���������� ������� ����� �������(e.button=1) ����
		 */
		button.addMouseListener(new MouseListener() {					
			@Override
			public void mouseUp(MouseEvent e) {}					
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button!=1) return;
				setSelectedSlide(slide);  //������������� ������� ���������
				Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_SELECT, slide);					
			}					
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});	
		/**
		 * ������ ����� � ������� ������ ������
		 */
		button.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point rect = button.getSize();
				if(slide.equals(selectedSlide)){	
					/**
					 * ���� ������ �����, ������� ������,
					 * ��������� �����
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
		 * ��������� ����������� ���� (�������)
		 */
		Menu popupMenu = new Menu(button);
	    
		/**
		 * ����� ���� �������
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
	     * ������ ������ �� �� ������� ����� ������������
	     */
		int cnt = Workspace.getCurrent().currentProject.getSlidesCnt();
	    if(cnt>1 && slide.index!=cnt-1){
	    	Control[] list = sliderContainer.getChildren();
	    	button.moveAbove(list[slide.index]);
	    }
	    
	    /** 
	     * ��������� Drag&Drop - ����������� ������ ����������� ������
	     */
	    final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
	    final DragSourceAdapter drgSrcAdapter = new DragSourceAdapter(){
	    	@Override
			public void dragSetData(DragSourceEvent event) {
	    		/**
	    		 * ��������� �������, ������� ������������
	    		 */
	    		transfer.setSelection(new StructuredSelection(button));
			}
	    };
	    /**
	     * ����������� � ������ ���������� ������ �����������
	     */
	    final DragSource dragSource = new DragSource(button, DND.DROP_MOVE);
	    dragSource.setTransfer(new Transfer[]{transfer});
	    dragSource.addDragListener(drgSrcAdapter);
	    
	    redrawContainer();
	}
	
	/**
	 * ��� �������� ������ ������� ��� ������
	 * @param slide
	 */
	protected void onLoadSlide(Slide slide){
		createButton(slide);	
	}
	
	/**
	 * ��� ���������� ������ ������� ��� ������ � ������������� ��� �������
	 * @param slide
	 */
	protected void onAddSlide(final Slide slide){
		if(selectedSlide!=null){
			/**
			 * ���� �� ����� ��� ������ �����,
			 * �� �������������� ���, ����� ����� �����
			 */
			buttons.get(selectedSlide).redraw();
		}
		/**
		 * ������ ������� � ������� ������
		 */
		selectedSlide = slide;		
		createButton(slide);
	    Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_SELECT, slide);
    }

	/**
	 * �������������� ���� ������ �������
	 */
	protected void redrawContainer(){
	    sliderContainer.layout(true);
	    sliderScroll.layout(true);
	    sliderScroll.setMinHeight(sliderContainer.computeSize(slideWidth+10, SWT.DEFAULT).y);
	}
	
	/**
	 * ����� ����� �������� ������ ������ ��� �� ������ � ��������� �������� � ������
	 * @param img
	 */
	protected void onImageSet(SlideImage img){
		/**
		 * ����� ������ ��� ������
		 */	
		Label button = buttons.get(img.slide);
		button.setImage(img.img);  // ������ �� ��� ��������
		images.put(img.slide, img.img); // ��������� �������� � ������
		/**
		 * ��������� ��������� �����
		 */
		sliderContainer.layout(true);
		sliderScroll.layout(true);
	}
	
	/**
	 * ��� ������ ������ ����� ������������ ��� ������ (� ���������� � �������)
	 * @param slide
	 */
	public void setSelectedSlide(Slide slide){
		if(selectedSlide!=null){
			/**
			 * ���� ��� ������ ����� �� �����,
			 * �������������� ��� ������, ����� ����� �����
			 */
			Label prev = buttons.get(selectedSlide);
			prev.redraw();
		}
		/**
		 * ������ ����� ������� � �������������� ��� ������
		 */
		buttons.get(slide).redraw();
		selectedSlide = slide;	
	}
	
	/**
	 * ������ ������� ������ ��� ����������� ������
	 * @param slide
	 */
	protected void onMoveSlide(Slide slide){
		Label button = buttons.get(slide); // ����� ������ ������
		Control[] list = sliderContainer.getChildren(); // ������� ������ ������ �����
		int curIndex = 0;
		/**
		 * ���������� ���������� ������� ������ �� ��������� ������ � �����
		 */
		for(;curIndex<list.length;curIndex++){
			if(list[curIndex]==button)break;
		}
		if(slide.index==0){
			/**
			 * ���� ����� ������� - � ������,
			 * �� ������ ������� ������ ����� ������ ���������
			 */
			button.moveAbove(list[0]);
		}else{
			if(curIndex<slide.index){
				/**
				 * ���� ��������� �����
				 */
				button.moveBelow(list[slide.index]);	
			}else{
				/**
				 * ���� ��������� ����, ������ ���������� ���-�� ��������� �� ���
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
