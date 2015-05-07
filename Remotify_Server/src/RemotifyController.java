import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class RemotifyController {

	public static final String PLAY = "PLAY/PAUSE";
	public static final String NEXT = "NEXT";
	public static final String PREVIOUS = "PREVIOUS";
	private final Robot r;

	public RemotifyController() throws AWTException {
		r = new Robot();
	}

	public void click() {
		r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public void rightClick() {
		r.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}

	public void move(int x, int y) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int)Math.round(screenSize.getWidth());
		int h = (int)Math.round(screenSize.getHeight());
		
		int curX = MouseInfo.getPointerInfo().getLocation().x;
		int curY = MouseInfo.getPointerInfo().getLocation().y;
		
		int newX = curX + x;
		int newY = curY + y;
		
		if(newX < 0) newX = 0;
		if(newX > w) newX = w-1;
		if(newY < 0) newY = 0;
		if(newY > h) newY = h-1;
		
		r.mouseMove(newX, newY);
		
	}
	
	public void scroll(int x, int y) {
		r.mouseWheel(y/3);
	}

}
