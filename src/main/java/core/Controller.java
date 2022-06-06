package core;

import java.awt.event.KeyEvent;

import utils.Input;
import utils.Vector2;
import biology.Entity;

public class Controller
{
	private final Input input;
	private final Simulation simulation;
	private final Renderer renderer;
	
	public Controller(Input input, Simulation simulation, Renderer renderer)
	{
		this.input = input;
		this.simulation = simulation;
		this.renderer = renderer;

		input.registerOnPressHandler(KeyEvent.VK_F3, simulation::toggleDebug);
		input.registerOnPressHandler(KeyEvent.VK_R, renderer::resetCamera);
	}



	public void update()
	{
		if (input.getKey(KeyEvent.VK_ESCAPE)) {
			simulation.close();
			Application.exit();
		}

		renderer.setZoom(1 - input.getMouseWheelRotation() / 7.0f);

		if (input.isLeftMouseJustPressed())
		{
			Vector2 pos = input.getMousePosition();
			boolean track = false;
			for (Entity e : simulation.getTank().getEntities()) 
			{
				Vector2 s = renderer.toRenderSpace(e.getPos());
				double r = renderer.toRenderSpace(e.getRadius());
				if (s.sub(pos).len2() < r*r) 
				{
					renderer.track(e);
					track = true;
					break;
				}
			}
			if (!track)
				renderer.track(null);
		}

		renderer.setPan(input.getMouseDelta());

	}
}
