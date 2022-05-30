package core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Text;
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
	private int infoTextSize;

	public UI(Window window, Simulation simulation)
	{
		this.window = window;
		this.simulation = simulation;

		title = new TextObject("Evolving Protozoa",
				TextStyle.fontName,
				window.getHeight() / 20, 
				new Vector2(window.getWidth() / 60f, window.getHeight() / 15f));
		title.setColor(Color.WHITE);
		
		info = new ArrayList<>();
		infoTextSize = window.getHeight() / 35;

		TextObject pelletText = new TextObject("Number of pellets: ", infoTextSize);
		pelletText.setColor(Color.WHITE.darker());

		TextObject protozoaText = new TextObject("Number of protozoa: ", infoTextSize);
		protozoaText.setColor(Color.WHITE.darker());

		TextObject trackingFitness = new TextObject("", infoTextSize);
		trackingFitness.setColor(Color.WHITE.darker());


		info.add(protozoaText);
		info.add(pelletText);
		info.add(trackingFitness);

		TextObject fpsText = new TextObject("FPS: ",
				infoTextSize,
				new Vector2(window.getWidth() * 0.9, window.getHeight() / 20f));
		fpsText.setColor(Color.YELLOW.darker());

		debugInfo = new ArrayList<>();
		debugInfo.add(fpsText);
	}

	public double getYPos(int i) {
		return (1.1*i + 3) * window.getHeight() / 20;
	}
	
	public void render(Graphics2D g, Renderer renderer)
	{
		title.render(g);

		int textAwayFromEdge = window.getWidth() / 60;

		int lineNumber;
		for (lineNumber = 0; lineNumber < info.size(); lineNumber++) {
			info.get(lineNumber).setPosition(new Vector2(textAwayFromEdge, getYPos(lineNumber)));
		}
		
		info.get(0).setText("Number of pellets: " + simulation.getTank().numberOfPellets());
		info.get(0).render(g);
		info.get(1).setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
		info.get(1).render(g);
		Entity tracked = renderer.getTracked();
		if (tracked != null) {
			lineNumber++;
			TextObject statsTitle = new TextObject(
					tracked.getPrettyName() + " Stats",
					(int) (infoTextSize * 1.1),
					new Vector2(textAwayFromEdge, getYPos(lineNumber))
			);
			statsTitle.setColor(Color.WHITE.darker());
			statsTitle.render(g);

			HashMap<String, Double> stats = tracked.getStats();
			if (tracked.isDead()) {
				stats.put("Alive", 0.0);
			}
			for (Map.Entry<String, Double> entityStat : stats.entrySet()) {
				lineNumber++;
				String text = entityStat.getKey() + ": " + TextStyle.toString(entityStat.getValue(), 2);
				TextObject statText = new TextObject(
						text, infoTextSize,
						new Vector2(textAwayFromEdge, getYPos(lineNumber))
				);
				statText.setColor(Color.WHITE.darker());
				statText.render(g);
			}
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
