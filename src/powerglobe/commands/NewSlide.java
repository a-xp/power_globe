package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import powerglobe.project.Workspace;

/**
 * Обработчик действия Создать слайд
 * @author 1
 *
 */
public class NewSlide implements IHandler {

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
		/**
		 * Вызываем у текущего проекта метод "создать слайд"
		 */
		Workspace.getCurrent().currentProject.newSlide();
		return null;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}

	@Override
	public boolean isHandled() {

		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
