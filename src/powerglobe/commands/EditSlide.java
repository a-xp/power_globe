package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Обработчик действия редактирование слайда
 * @author 1
 *
 */
public class EditSlide implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		 * Запрашиваем текущий выбранный слайд у списка слайдов
		 */
		Slide curSlide = Workspace.getCurrent().getSliderList().getSelectedSlide();
		
		if(curSlide!=null){
			/**
			 * Если он есть, то отправляем событием EVENT_SLIDE_EDIT во все части программы
			 */
			Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_EDIT, curSlide);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
