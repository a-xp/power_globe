/*!
 * @file
 * Copyright (c) jdknight. All rights reserved.
 *
 * The MIT License (MIT).
 */

package powerglobe.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import powerglobe.project.Workspace;
import powerglobe.wwd.Scene;

/**
 * Sample view which uses a <tt>WorldWindowGLCanvas</tt>.
 */
public class View extends ViewPart
{
	/**
	 * Identifier for view.
	 */
	public static final String ID = "powerglobe.view.Earth"; //$NON-NLS-1$


	/**
	 * AWT container for view.
	 */
	private Composite _embeddedContainer;
	
	/**
	 * Список слайдов
	 */
	private SliderList sliderList;
	/**
	 * Сцена редактирования
	 */
	private Scene editScene;
	/**
	 * ФОрма редактирования
	 */
	private SliderEdit editForm;
	

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @param parent The parent control.
	 */
	public void createPartControl(Composite parent)
	{	
		/**
		 * Минимальный размер окна 800x500
		 */
		parent.getShell().addControlListener(new ControlListener() {			
			@Override
			public void controlResized(ControlEvent e) {
				/**
				 * Обрабатываем событие изменение размеров окна
				 */
				Shell sh = (Shell)e.widget;
				Point size = sh.getSize();
				if(size.x<800 || size.y<500){
					/**
					 * Если меньше, чем 800х500, корректируем
					 */
					sh.setSize(Math.max(800, size.x), Math.max(500, size.y));
				}
			}			
			@Override
			public void controlMoved(ControlEvent e) {				
			}
		});
		/**
		 * Разбиваем окно на 3 области
		 * список слайдов, окно WW, форма редактирования
		 */
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 5;
		parent.setLayout(layout);
		
		sliderList = new SliderList(parent);
		
		// Setup AWT container.
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		_embeddedContainer = new Composite(parent, SWT.EMBEDDED);
		_embeddedContainer.setLayoutData(gridData);
		java.awt.Frame frame = SWT_AWT.new_Frame(_embeddedContainer);
		java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
		frame.add(panel);				
		
		editScene = new Scene(panel);		
		editScene.setUp();
		Workspace.getCurrent().setEditScene(editScene);
		
		editForm = new SliderEdit(parent);
		editForm.setUp();
	}
	
	/**
	 * Invoked when this part takes focus in the workbench.
	 */
	public void setFocus()
	{
		_embeddedContainer.setFocus();
	}
	
}
