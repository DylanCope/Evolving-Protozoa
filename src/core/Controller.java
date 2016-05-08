package core;

import utils.Input;
import utils.Vector2f;
import biology.Entity;

public class Controller
{
	Renderer renderer;
	Input input;
	
	public Controller(Input input)
	{
		this.input = input;
	}

	public void update(Simulation simulation, Renderer renderer) 
	{
		renderer.setZoom(1 - input.getMouseWheelRotation() / 10.0);
		if (input.isLeftMouseJustPressed()) {
			Vector2f pos = input.getMousePosition();
			boolean track = false;
			for (Entity e : simulation.getTank().getEntities()) {
				Vector2f s = renderer.toRenderSpace(e.getPos());
				double r = renderer.toRenderSpace(e.getRadius());
				if (s.sub(pos).len2() < r*r) {
					renderer.track(e);
					track = true;
					break;
				}
			}
			if (track == false)
				renderer.track(null);
		}
		if (input.isLeftMousePressed()) {
			renderer.pan(input.getMouseDelta().mul(-1));
		}
	}
}
