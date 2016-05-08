package utils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Input implements KeyListener, FocusListener,
				MouseListener, MouseMotionListener, MouseWheelListener 
{	
	private Vector2f position 		= new Vector2f(0, 0);
	private Vector2f mouseDelta		= new Vector2f(0, 0);
	private boolean[] keys 			= new boolean[65536];
	private boolean[] mouseButtons 	= new boolean[4];
	private boolean[] mouseJustDown = new boolean[4];
	
	private int mouseWheelRotation = 0;
	
	public boolean getKey(int key) {
		return keys[key];
	}
	
	public boolean getMouse(int button) {
		return mouseButtons[button];
	}
	
	private boolean mouseButtonJustDown(int button)
	{
		if (getMouse(button) && !mouseJustDown[button])
		{
			mouseJustDown[button] = true;
			return true;
		}
		else if (!getMouse(button))
		{
			mouseJustDown[button] = false;
		}
		return false;
	}
	
	public boolean isLeftMouseJustPressed()  { return mouseButtonJustDown(1); }
	public boolean isRightMouseJustPressed() { return mouseButtonJustDown(0); }
	public boolean isLeftMousePressed()  	 { return getMouse(1); 			  }
	public boolean isRightMousePressed() 	 { return getMouse(0); 			  }
	
	public Vector2f getMousePosition() {
		return position;
	}
	
	public Vector2f getMouseDelta() {
		return mouseDelta;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		Vector2f pos = new Vector2f(event.getX(), event.getY());
		mouseDelta = pos.sub(position);
		position = pos;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		position = new Vector2f(event.getX(), event.getY());
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		
	}

	@Override
	public void mouseExited(MouseEvent event) {
		
	}

	@Override
	public void mousePressed(MouseEvent event) {
		int button = event.getButton();

		if (0 < button && button < mouseButtons.length)
			mouseButtons[button] = true;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		
		int button = event.getButton();
		
		if (0 < button && button < mouseButtons.length)
			mouseButtons[button] = false;
		
	}

	@Override
	public void focusGained(FocusEvent event) {
		
	}

	@Override
	public void focusLost(FocusEvent event) {
		
		for (int i = 0; i < keys.length; i++)
			keys[i] = false;
		
		for (int i = 0; i < mouseButtons.length; i++)
			mouseButtons[i] = false;
		
	}

	@Override
	public void keyPressed(KeyEvent event) {
		
		int key = event.getKeyCode();
		
		if (0 < key && key < keys.length)
			keys[key] = true;
		
	}

	@Override
	public void keyReleased(KeyEvent event) {
		
		int key = event.getKeyCode();
		
		if (0 < key && key < keys.length)
			keys[key] = false;
		
	}

	@Override
	public void keyTyped(KeyEvent event) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		mouseWheelRotation += arg0.getWheelRotation();
	}
	
	public int getMouseWheelRotation() {
		return mouseWheelRotation;
	}
	
	
	
}
