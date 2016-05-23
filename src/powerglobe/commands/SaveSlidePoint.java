package powerglobe.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import gov.nasa.worldwind.geom.Position;
import powerglobe.project.Slide;
import powerglobe.project.Workspace;

/**
 * Обработчик действия Сохранить позицию слайда
 * @author 1
 *
 */
public class SaveSlidePoint implements IHandler {

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
		 * Получаем текущий выбранный слайд
		 */
		Slide slide= Workspace.getCurrent().getSliderList().getSelectedSlide();
		if(slide!=null){
			/**
			 * Получаем текущую позицию камеры
			 */
			Position position = Workspace.getCurrent().getEditScene().getEyePosition();
			/**
			 * Обновляем слайд с этой позицией камеры
			 */
			Slide newSlide = new Slide(slide.getIndex(), position, slide.getTitle(), slide.getAnnotation(), slide.getDelay(), slide.getMoveSpeed(), slide.isEnablePath(), slide.getCameraPitch(), slide.getTurnSpeed());
			Workspace.getCurrent().currentProject.replace(slide, newSlide);
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
