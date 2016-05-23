package powerglobe.view;

import org.eclipse.swt.graphics.Image;

import powerglobe.project.Slide;

/**
 * Класс для передачи скриншотов по событию SLIDE_IMAGE
 * @author 1
 *
 */
public class SlideImage {
	public Slide slide;  // К какому слайду картинка
	public Image img;  // Картинка
	public SlideImage(Slide slide, Image img) {
		super();
		this.slide = slide;
		this.img = img;
	}
}
