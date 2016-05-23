package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import powerglobe.project.Slide;
import powerglobe.project.Workspace;
/**
 * Обработчик действия Предыдущий слайд
 * @author 1
 *
 */
public class PreviousSlide implements IHandler {

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
		 * Получаем предыдущий слайд(для текущего выбранного) у проекта
		 */
		Slide slide = ws.currentProject.getPrevSlide(ws.getSliderList().getSelectedSlide());
		if(slide!=null){
			/**
			 * Если такой слайд есть, ставим его текущим в списке слайдов и отправляем событием SLIDE_SELECT во все части приложения
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
