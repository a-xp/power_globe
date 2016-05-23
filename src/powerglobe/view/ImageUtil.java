package powerglobe.view;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Функции для работы с изображениями
 * @author 1
 *
 */
public class ImageUtil {
	
	/**
	 * Конвертирует картинку из awt(world wind) в swt(eclipse)
	 * Копипаст
	 * @param bufferedImage
	 * @return
	 */
    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel
                    = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(),
                    colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0],
                            pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();
            //ASSUMES: 3 BYTE BGR IMAGE TYPE
            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    colorModel.getPixelSize(), palette);
            //This is valid because we are using a 3-byte Data model with no transparent pixels
            data.transparentPixel = -1;
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
           
            return data;
        }
        return null;
    }
    /**
     * Ресайз картинки
     * @param image
     * @param width
     * @param height
     * @return
     */
	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, 
		image.getBounds().width, image.getBounds().height, 
		0, 0, width, height);
		gc.dispose();
		image.dispose(); 
		return scaled;
	}
	
	/**
	 * Кадрирование картинки
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image crop(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, 
		width, height, 
		0, 0, width, height);
		gc.dispose();
		image.dispose(); 
		return scaled;
	}
	
	/**
	 * Кадрирование картинки с масштабом
	 * @param image
	 * @param width
	 * @param height
	 * @param scale
	 * @return
	 */
	
	public static Image makeSlideImage(BufferedImage bi, int width, int height, double maxScale){
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int srcW = bi.getWidth();
		int srcH = bi.getHeight();
		double scale = 1.0;
		if(1.0*srcW/srcH<1.0*width/height){
			scale = Math.min(maxScale, 1.0*srcW/width);
		}else{
			scale = Math.min(maxScale,  1.0*srcH/height);
		}
		int cropW = (int)Math.round(width*scale);
		int cropH = (int)Math.round(height*scale);
		int centerW = srcW/2;
		int centerH = srcH/2;
		Graphics at = scaled.getGraphics();
		at.drawImage(bi, 0, 0, width, height, Math.max(0, centerW-cropW/2), Math.max(0, centerH-cropH/2), Math.min(srcW-1, centerW+cropW/2), Math.min(srcH-1, centerH+cropH/2), null);
		
		at.dispose();
		return new Image(Display.getDefault(), convertToSWT(scaled));
	}
	
}
