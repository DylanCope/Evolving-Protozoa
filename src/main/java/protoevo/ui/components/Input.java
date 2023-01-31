package protoevo.ui.components;

import protoevo.utils.Vector2;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Input implements KeyListener, FocusListener,
		MouseListener, MouseMotionListener, MouseWheelListener
{
	private Vector2 mousePosition = new Vector2(0, 0);
	private Vector2 positionOnLeftClickDown = new Vector2(0, 0);
	private Vector2 mouseLeftClickDelta = new Vector2(0, 0);
	private boolean[] keys 			= new boolean[65536];
	private boolean[] mouseButtons 	= new boolean[4];
	private boolean[] mouseJustDown = new boolean[4];

	private final HashMap<Integer, Runnable> onPressHandlers = new HashMap<>();
	private final List<Clickable> uiClickHandlers = new ArrayList<>();
	private final List<Runnable> onLeftClickCallbacks = new ArrayList<>();
	private final List<Runnable> onRightClickCallbacks = new ArrayList<>();
	public Runnable onLeftMouseRelease;

	public void registerOnPressHandler(int key, Runnable handler) {
		onPressHandlers.put(key, handler);
	}

	public void registerUIClickable(Clickable clickable) {
		uiClickHandlers.add(clickable);
	}

	public void registerLeftClickCallback(Runnable callback) {
		onLeftClickCallbacks.add(callback);
	}

	public void registerRightClickCallback(Runnable callback) {
		onRightClickCallbacks.add(callback);
	}

	public void unregisterOnPressHandler(int key) {
		onPressHandlers.remove(key);
	}

	private int mouseWheelRotation = 0;

	public void reset() {
		mouseWheelRotation = 0;
		mousePosition = new Vector2(0, 0);
		mouseLeftClickDelta = new Vector2(0, 0);
		positionOnLeftClickDown = new Vector2(0, 0);
	}

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

	// awful behaviour - can only be called once per click and return true
	// needs to be fixed so that it can be called multiple times per click
	public boolean isLeftMouseJustPressed()  { return mouseButtonJustDown(1); }
	public boolean isRightMouseJustPressed() { return mouseButtonJustDown(3); }
	public boolean isLeftMousePressed()  	 { return getMouse(1); 			  }
	public boolean isRightMousePressed() 	 { return getMouse(3); 			  }

	public Vector2 getMousePosition() {
		return mousePosition;
	}

	public Vector2 getMouseLeftClickDelta() {
		return mouseLeftClickDelta;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		mousePosition = new Vector2(event.getX(), event.getY());
		if (SwingUtilities.isLeftMouseButton(event)) {
			Vector2 newPosition = new Vector2(event.getX(), event.getY());
			mouseLeftClickDelta = newPosition.sub(positionOnLeftClickDown);
		} else {
			mouseLeftClickDelta = new Vector2(0, 0);
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		mousePosition = new Vector2(event.getX(), event.getY());
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		mousePosition = new Vector2(event.getX(), event.getY());
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		mousePosition = new Vector2(event.getX(), event.getY());
	}

	@Override
	public void mouseExited(MouseEvent event) {
		mousePosition = new Vector2(event.getX(), event.getY());
	}

	@Override
	public void mousePressed(MouseEvent event) {
		int button = event.getButton();
		mousePosition = new Vector2(event.getX(), event.getY());

		if (SwingUtilities.isLeftMouseButton(event))
			positionOnLeftClickDown = mousePosition;

		if (0 < button && button < mouseButtons.length)
			mouseButtons[button] = true;
	}

	@Override
	public void mouseReleased(MouseEvent event) {

		int button = event.getButton();

		if (SwingUtilities.isLeftMouseButton(event)) {
			mouseLeftClickDelta = new Vector2(0, 0);
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


	public void update() {
		for (Clickable clickable : uiClickHandlers) {
			if (clickable.isActive()
					&& clickable.isInClickRegion((int) mousePosition.getX(), (int) mousePosition.getY())
					&& isLeftMouseJustPressed()) {
					clickable.onClick();
					return; // UI clicks supersede simulation
			}
		}

		for (Runnable callback : onLeftClickCallbacks)
			callback.run();

		if (isRightMousePressed())
			for (Runnable callback : onRightClickCallbacks)
				callback.run();

	}
}
