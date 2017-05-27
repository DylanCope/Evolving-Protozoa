package core;

import java.awt.event.KeyEvent;

import utils.Input;
import utils.Vector2;
import biology.Entity;

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
		
//		if (input.getKey(KeyEvent.VK_UP))
//			renderer.setZoom(renderer.getZoom() * 2);
//		if (input.getKey(KeyEvent.VK_DOWN))
//			renderer.setZoom(renderer.getZoom() / 2.0);
//		else
			renderer.setZoom(1 - input.getMouseWheelRotation() / 7.0);
//		
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
			if (track == false)
				renderer.track(null);
		}
		
		if (input.isLeftMousePressed()) 
		{
			renderer.pan(input.getMouseDelta().mul(-2));
		}
	}
}
