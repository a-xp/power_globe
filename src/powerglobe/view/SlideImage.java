package powerglobe.view;

import org.eclipse.swt.graphics.Image;

import powerglobe.project.Slide;

/**
 * ����� ��� �������� ���������� �� ������� SLIDE_IMAGE
 * @author 1
 *
 */
public class SlideImage {
	public Slide slide;  // � ������ ������ ��������
	public Image img;  // ��������
	public SlideImage(Slide slide, Image img) {
		super();
		this.slide = slide;
		this.img = img;
	}
}
