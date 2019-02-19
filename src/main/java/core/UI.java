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

	public UI(Window window, Simulation simulation)
	{
		this.window = window;
		this.simulation = simulation;
		
		title = new TextObject("Microbial Evolution", 
				"bubble sharp", 
				window.getHeight() / 20, 
				new Vector2(10, window.getHeight() / 20));
		title.setColor(Color.WHITE);
		
		info = new ArrayList<TextObject>();

		TextObject pelletText = new TextObject("Number of pellets: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 3*window.getHeight() / 20));
		pelletText.setColor(Color.WHITE.darker());

		TextObject protozoaText = new TextObject("Number of protozoa: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 4.1*window.getHeight() / 20));
		protozoaText.setColor(Color.WHITE.darker());

		TextObject trackingFitness = new TextObject("", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 4.1*window.getHeight() / 20));
		trackingFitness.setColor(Color.WHITE.darker());
		
		info.add(protozoaText);
		info.add(pelletText);
		info.add(trackingFitness);
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
		if (tracked != null && tracked instanceof Protozoa) {
			String fit = TextStyle.toString(((Protozoa) tracked).getFitness(), 2);
			info.get(2).setText("Tracked's fitness: " + fit);
			info.get(2).render(g);
		}
		else {
			info.get(2).setText("");
		}
	}
}
