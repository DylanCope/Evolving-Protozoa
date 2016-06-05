package core;

import java.awt.event.KeyEvent;

import utils.Input;
import utils.Vector2;
import biology.Entity;
import physics.Particle;

public class Controller
{
	private Input input;
	
	public Controller(Input input)
	{
		this.input = input;
	}

	public void update(Simulation simulation, Renderer renderer) 
	{
		if (input.getKey(KeyEvent.VK_ESCAPE)) 
			Application.exit();
		
		renderer.setZoom(1 - input.getMouseWheelRotation() / 7.0);

		if (input.isLeftMouseJustPressed()) 
		{
			Vector2 pos = input.getMousePosition();
			boolean track = false;
			for (Particle p : simulation.getTank().getParticles()) 
			{
				Entity e = null;
				if (p instanceof Entity)
					e = (Entity) p;
				else continue;
				
				Vector2 s = renderer.toRenderSpace(e);
				double r = renderer.toRenderSpace(e.getRadius());
				if (s.sub(pos).len2() < r*r) 
				{
					renderer.track(e);
					track = true;
					break;
				}
			}
			if (track == false)
				renderer.track(null);
		}
		
		if (input.isLeftMousePressed()) 
		{
			renderer.pan(input.getMouseDelta().mul(-2));
		}
	}
}
