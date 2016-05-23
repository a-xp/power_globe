package powerglobe.wwd.annotations.props;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.BeforeClass;
import org.junit.Test;

public class SingleLineTest {
	static Composite parent;
	static SingleLine sl;
	
	@BeforeClass
	public static void setUp(){
		Display display = Display.getDefault();
		Shell shell = new Shell(display);		
		sl = new SingleLine("Название поля");
		parent = new Composite(shell, SWT.NONE);
		sl.createControls(parent);		
	}
	
	@Test
	public void controlsCreated(){		
		Control[] list = parent.getChildren();
		assertTrue(list!=null);
		assertEquals(2, list.length);
		assertTrue(list[0] instanceof Label);	
		assertTrue(list[1] instanceof Text);
	}
	
	@Test
	public void getValue(){
		String text ="Название объекта";
		Text input = (Text)(parent.getChildren())[1];
		input.setText(text);		
		assertEquals(text, sl.getValue());
		input.setText("7");
		assertEquals(7, sl.getIntValue());
		input.setText("0.8");
		assertEquals(0.8, sl.getDoubleValue(), 0.01);
	}
	@Test
	public void setValue(){
		Text input = (Text)(parent.getChildren())[1];
		String text ="Название объекта";
		sl.setValue(text);
		assertEquals(text, input.getText());
		assertEquals(text, sl.getValue());
		sl.setValue(4);
		assertEquals("4", input.getText());
		assertEquals(4, sl.getIntValue());
		sl.setValue(0.3);
		assertEquals("0.30", input.getText());
		assertEquals(0.3, sl.getDoubleValue(), 0.01);
	}
	
	@Test
	public void validateValue(){
		sl.setValue("Название города");
		assertTrue(sl.validate());
		sl.setValue("");
		assertTrue(sl.validate());
		sl.setValue(81);
		assertTrue(sl.validate());
	}

}
