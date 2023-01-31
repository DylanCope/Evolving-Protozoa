package protoevo.ui.main;

import protoevo.core.Application;
import protoevo.ui.Controller;
import protoevo.ui.Window;
import protoevo.ui.components.Input;

import java.awt.event.KeyEvent;

public class MainScreenController implements Controller
{
	private final Input input;
	private final MainScreenRenderer renderer;
	
	public MainScreenController(Window window, MainScreenRenderer renderer)
	{
		this.renderer = renderer;
		this.input = window.getInput();
	}

	public void update()
	{
//		if (input.getKey(KeyEvent.VK_ESCAPE)) {
//			Application.exit();
//		}

//		renderer.setZoom(1 - input.getMouseWheelRotation() / 7.0f);

	}
}
