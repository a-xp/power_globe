package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Обработчик действия Удалить слайд
 * @author 1
 *
 */
public class RemoveSlide implements IHandler {

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
		Workspace ws = Workspace.getCurrent();
		/**
		 * Получаем текущий выбранный слайд
		 */
		Slide slide = ws.getSliderList().getSelectedSlide();
		if(slide!=null){
			/**
			 * Если он есть, удаляем его из проекта
			 */
			ws.currentProject.remove(slide);
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
