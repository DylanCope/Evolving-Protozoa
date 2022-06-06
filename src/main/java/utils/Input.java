package utils;

import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;

public class Input implements KeyListener, FocusListener,
				MouseListener, MouseMotionListener, MouseWheelListener 
{	
	private Vector2 position 		= new Vector2(0, 0);
	private Vector2 mouseDelta		= new Vector2(0, 0);
	private boolean[] keys 			= new boolean[65536];
	private boolean[] mouseButtons 	= new boolean[4];
	private boolean[] mouseJustDown = new boolean[4];

	private final HashMap<Integer, Runnable> onPressHandlers = new HashMap<>();
	public Runnable onLeftMouseRelease;

	public void registerOnPressHandler(int key, Runnable handler) {
		onPressHandlers.put(key, handler);
	}

	public void unregisterOnPressHandler(int key) {
		onPressHandlers.remove(key);
	}
	
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
	
	public Vector2 getMousePosition() {
		return position;
	}
	
	public Vector2 getMouseDelta() {
		return mouseDelta;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			Vector2 newPosition = new Vector2(event.getX(), event.getY());
			mouseDelta = newPosition.sub(position);
		} else {
			mouseDelta = new Vector2(0, 0);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

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

		if (SwingUtilities.isLeftMouseButton(event))
			position = new Vector2(event.getX(), event.getY());

		if (0 < button && button < mouseButtons.length)
			mouseButtons[button] = true;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		
		int button = event.getButton();

		if (SwingUtilities.isLeftMouseButton(event)) {
			mouseDelta = new Vector2(0, 0);
			if (onLeftMouseRelease != null)
				onLeftMouseRelease.run();
		}
		
		if (0 < button && button < mouseButtons.length)
			mouseButtons[button] = false;
		
	}

	@Override
	public void focusGained(FocusEvent event) {
		
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		Arrays.fill(keys, false);
		Arrays.fill(mouseButtons, false);
	}

	@Override
	public void keyPressed(KeyEvent event) 
	{
		int key = event.getKeyCode();
		
		if (0 < key && key < keys.length) {
			if (!keys[key] & onPressHandlers.containsKey(key)) {
				try {
					onPressHandlers.get(key).run();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			keys[key] = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent event) 
	{	
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
