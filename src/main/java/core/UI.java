package core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import utils.TextObject;
import utils.Vector2;
import utils.Window;
import utils.TextStyle;
import biology.Entity;
import biology.Protozoa;

public class UI 
{
	private Window window;
	private Simulation simulation;
	private TextObject title;
	private ArrayList<TextObject> info;
	private ArrayList<TextObject> debugInfo;

	public UI(Window window, Simulation simulation)
	{
		this.window = window;
		this.simulation = simulation;
		
		title = new TextObject("Microbial Evolution", 
				"bubble sharp", 
				window.getHeight() / 20, 
				new Vector2(10, window.getHeight() / 20f));
		title.setColor(Color.WHITE);
		
		info = new ArrayList<>();

		TextObject pelletText = new TextObject("Number of pellets: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 3*window.getHeight() / 20f));
		pelletText.setColor(Color.WHITE.darker());

		TextObject protozoaText = new TextObject("Number of protozoa: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 4.1*window.getHeight() / 20f));
		protozoaText.setColor(Color.WHITE.darker());

		TextObject trackingFitness = new TextObject("", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 4.1*window.getHeight() / 20f));
		trackingFitness.setColor(Color.WHITE.darker());


		info.add(protozoaText);
		info.add(pelletText);
		info.add(trackingFitness);

		TextObject fpsText = new TextObject("FPS: ",
				"bubble sharp",
				window.getHeight() / 30,
				new Vector2(window.getWidth() * 0.9, window.getHeight() / 20f));
		fpsText.setColor(Color.YELLOW.darker());

		debugInfo = new ArrayList<>();
		debugInfo.add(fpsText);
	}
	
	public void render(Graphics2D g, Renderer renderer)
	{
		title.render(g);
		
		for (int i = 0; i < info.size(); i++) {
			info.get(i).setPosition(new Vector2(10, (1.1*i + 3)*window.getHeight() / 20));
		}
		
		info.get(0).setText("Number of pellets: " + simulation.getTank().numberOfPellets());
		info.get(0).render(g);
		info.get(1).setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
		info.get(1).render(g);
		Entity tracked = renderer.getTracked();
		if (tracked instanceof Protozoa) {
			String fit = TextStyle.toString(((Protozoa) tracked).getFitness(), 2);
			info.get(2).setText("Tracked entity's fitness: " + fit);
			info.get(2).render(g);
		}
		else {
			info.get(2).setText("");
		}

		if (simulation.inDebugMode()) {
			debugInfo.get(0).setText("FPS: " + (int) renderer.getFPS());
			debugInfo.get(0).render(g);
		}
	}
}
