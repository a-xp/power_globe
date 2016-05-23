package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import powerglobe.project.Slide;
import powerglobe.project.Workspace;
/**
 * Обработчик события "Переход на следующий слайд"
 * @author 1
 *
 */
public class NextSlide implements IHandler {

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
		 * Запрашиваем у проекта следующий(за текущим выбранным) слайд
		 */
		Slide slide = ws.currentProject.getNextSlide(ws.getSliderList().getSelectedSlide());
		if(slide!=null){
			/**
			 * Если такой слайд есть, ставим его текущим в списке сладов и передаем событием SLIDE_SELECT во все части программы
			 */
			ws.getSliderList().setSelectedSlide(slide);
			Workspace.getCurrent().fireEvent(Workspace.EVENT_SLIDE_SELECT, slide);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {

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
