package core;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import biology.NNBrain;
import neat.NeuralNetwork;
import neat.Neuron;
import utils.TextObject;
import utils.Vector2;
import utils.Window;
import utils.TextStyle;
import biology.Entity;
import biology.Protozoa;

public class UI 
{
	private final Window window;
	private final Simulation simulation;
	private final Renderer renderer;
	private final TextObject title;
	private final ArrayList<TextObject> info;
	private final ArrayList<TextObject> debugInfo;
	private final int infoTextSize;

	public UI(Window window, Simulation simulation, Renderer renderer)
	{
		this.window = window;
		this.simulation = simulation;
		this.renderer = renderer;

		title = new TextObject("Evolving Protozoa",
				TextStyle.fontName,
				window.getHeight() / 20, 
				new Vector2(window.getWidth() / 60f, window.getHeight() / 15f));
		title.setColor(Color.WHITE);
		
		info = new ArrayList<>();
		infoTextSize = window.getHeight() / 45;

		TextObject pelletText = new TextObject("Number of pellets: ", infoTextSize);
		pelletText.setColor(Color.WHITE.darker());

		TextObject protozoaText = new TextObject("Number of protozoa: ", infoTextSize);
		protozoaText.setColor(Color.WHITE.darker());

		TextObject trackingFitness = new TextObject("Generation", infoTextSize);
		trackingFitness.setColor(Color.WHITE.darker());


		info.add(protozoaText);
		info.add(pelletText);
		info.add(trackingFitness);

		TextObject fpsText = new TextObject("FPS: ",
				infoTextSize,
				new Vector2(window.getWidth() * 0.9f, window.getHeight() / 20f));
		fpsText.setColor(Color.YELLOW.darker());

		debugInfo = new ArrayList<>();
		debugInfo.add(fpsText);
	}

	public float getYPosLHS(int i) {
		return 1.3f*infoTextSize*i + 3 * window.getHeight() / 20f;
	}

	public float getYPosRHS(int i) {
		return 1.3f*infoTextSize*i + window.getHeight() / 20f;
	}
	
	public void render(Graphics2D g)
	{
		title.render(g);

		int textAwayFromEdge = window.getWidth() / 60;

		int lineNumber;
		for (lineNumber = 0; lineNumber < info.size(); lineNumber++)
			info.get(lineNumber).setPosition(new Vector2(textAwayFromEdge, getYPosLHS(lineNumber)));

		
		info.get(0).setText("Number of pellets: " + simulation.getTank().numberOfPellets());
		info.get(0).render(g);
		info.get(1).setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
		info.get(1).render(g);
		info.get(2).setText("Generation: " + simulation.getGeneration());
		info.get(2).render(g);
		lineNumber++;

		Entity tracked = renderer.getTracked();
		if (tracked != null) {
			if (tracked.isDead() && !tracked.getChildren().isEmpty()) {
				renderer.track(tracked.getChildren().iterator().next());
				tracked = renderer.getTracked();
			}

			TextObject statsTitle = new TextObject(
					tracked.getPrettyName() + " Stats",
					(int) (infoTextSize * 1.1),
					new Vector2(textAwayFromEdge, getYPosLHS(lineNumber))
			);
			statsTitle.setColor(Color.WHITE.darker());
			statsTitle.render(g);

			HashMap<String, Float> stats = tracked.getStats();
			for (Map.Entry<String, Float> entityStat : stats.entrySet()) {
				lineNumber++;
				String text = entityStat.getKey() + ": " + TextStyle.toString(entityStat.getValue(), 2);
				TextObject statText = new TextObject(
						text, infoTextSize,
						new Vector2(textAwayFromEdge, getYPosLHS(lineNumber))
				);
				statText.setColor(Color.WHITE.darker());
				statText.render(g);
			}

			if (tracked instanceof Protozoa && ((Protozoa) tracked).getBrain() instanceof NNBrain) {
				NNBrain brain = (NNBrain) ((Protozoa) tracked).getBrain();
				renderBrainNetwork(brain.network, g);
			}
		}
		else
			info.get(2).setText("");


		if (simulation.inDebugMode())
			renderDebugStats(g);
	}

	private void renderDebugStats(Graphics2D g) {
		int lineNumber = 0;
		HashMap<String, Integer> stats = renderer.getStats();

		TextObject[] statTexts = new TextObject[stats.keySet().size()];
		int maxWidth = 0;
		for (Map.Entry<String, Integer> entityStat : stats.entrySet()) {
			String text = entityStat.getKey() + ": " + TextStyle.toString(entityStat.getValue(), 2);
			TextObject statText = new TextObject(
					text, infoTextSize,
					new Vector2(0, getYPosRHS(lineNumber))
			);
			maxWidth = Math.max(maxWidth, statText.getWidth());
			statText.setColor(Color.YELLOW.darker());
			statTexts[lineNumber] = statText;
			lineNumber++;
		}

		int x = (int) (0.98 * window.getWidth() - maxWidth);
		for (TextObject statText : statTexts) {
			int y = (int) statText.getPosition().getY();
			statText.setPosition(new Vector2(x, y));
			statText.render(g);
		}
	}

	private void renderBrainNetwork(NeuralNetwork nn, Graphics2D g) {
		int networkDepth = nn.getDepth();
		int boxWidth = (int) (window.getWidth() / 2.0 - 1.2 * renderer.getTrackingScopeRadius());
		int boxHeight = 3 * window.getHeight() / 4;

		int boxXStart = window.getWidth() - (int) (boxWidth * 1.1);
		int boxYStart = (window.getHeight() - boxHeight) / 2;

		if (simulation.inDebugMode()) {
			g.setColor(Color.YELLOW.darker());
			g.drawRect(boxXStart, boxYStart, boxWidth, boxHeight);
			for (int y = boxYStart; y < boxYStart + boxHeight; y += boxHeight / networkDepth)
				g.drawLine(boxXStart, y, boxXStart + boxWidth, y);
		}

		if (!nn.hasComputedGraphicsPositions())
			precomputeGraphicsPositions(nn, boxXStart, boxYStart, boxWidth, boxHeight);


		int r = nn.getGraphicsNodeSpacing() / 8;
		for (Neuron neuron : nn.getNeurons()) {
			if (!neuron.getType().equals(Neuron.Type.SENSOR)) {
				g.setColor(Color.WHITE.darker());
				Stroke s = g.getStroke();

				for (int i = 0; i < neuron.getInputs().length; i++) {
					Neuron inputNeuron = neuron.getInputs()[i];
					float weight = neuron.getWeights()[i];
					g.setStroke(new BasicStroke((int) (0.3 * r * Math.abs(weight))));

					if (neuron == inputNeuron) {
						g.drawOval(
								neuron.getGraphicsX(),
								neuron.getGraphicsY() - 2*r,
								3*r,
								3*r);
					} else if (inputNeuron.getDepth() == neuron.getDepth()) {
						int width = boxWidth / (2 * networkDepth);
						int height = Math.abs(neuron.getGraphicsY() - inputNeuron.getGraphicsY());
						int x = neuron.getGraphicsX() - width / 2;
						int y = Math.min(neuron.getGraphicsY(), inputNeuron.getGraphicsY());
						g.drawArc(x, y, width, height,-90, 180);
					} else {
						g.drawLine(neuron.getGraphicsX(), neuron.getGraphicsY(),
								inputNeuron.getGraphicsX(), inputNeuron.getGraphicsY());
					}
				}
				g.setStroke(s);
			}
		}

		for (Neuron neuron : nn.getNeurons()) {
			Color colour;
			double state = neuron.getLastState();
			if (state > 0) {
				colour = new Color(
						30, (int) (50 + state * 150), 30
				);
			} else {
				colour = new Color(
						(int) (50 - state * 150), 30, 30
				);
			}

			g.setColor(colour);
			g.fillOval(
					neuron.getGraphicsX() - r,
					neuron.getGraphicsY() - r,
					2*r,
					2*r);


			if (simulation.inDebugMode())
				if (neuron.getType().equals(Neuron.Type.HIDDEN))
					g.setColor(Color.YELLOW.darker());
				else if (neuron.getType().equals(Neuron.Type.SENSOR))
					g.setColor(Color.BLUE.brighter());
				else
					g.setColor(Color.WHITE.darker());
			else {
				g.setColor(Color.WHITE.darker());
			}
			if (neuron.getDepth() == networkDepth && neuron.getType().equals(Neuron.Type.HIDDEN))
				g.setColor(new Color(150, 30, 150));

			Stroke s = g.getStroke();
			g.setStroke(new BasicStroke((int) (0.3*r)));

			g.drawOval(
					neuron.getGraphicsX() - r,
					neuron.getGraphicsY() - r,
					2*r,
					2*r);
			g.setStroke(s);
		}
	}

	private void precomputeGraphicsPositions(NeuralNetwork nn,
											 int boxXStart,
											 int boxYStart,
											 int boxWidth,
											 int boxHeight) {

		Neuron[] neurons = nn.getNeurons();
		int networkDepth = nn.getDepth();

		int[] depthWidthValues = new int[networkDepth + 1];
		Arrays.fill(depthWidthValues, 0);
		for (Neuron n : neurons)
			depthWidthValues[n.getDepth()]++;

		int maxWidth = 0;
		for (int width : depthWidthValues)
			maxWidth = Math.max(maxWidth, width);

		int nodeSpacing = boxHeight / maxWidth;
		nn.setGraphicsNodeSpacing(nodeSpacing);

		for (int depth = 0; depth <= networkDepth; depth++) {
			int x = boxXStart + depth * boxWidth / networkDepth;
			int nNodes = depthWidthValues[depth];

			int i = 0;
			for (Neuron n : neurons) {
				if (n.getDepth() == depth) {
					int y = boxYStart + boxHeight / 2 - (nNodes / 2 - i) * nodeSpacing;
					n.setGraphicsPosition(x, y);
					i++;
				}
			}
		}
		nn.setComputedGraphicsPositions(true);
	}
}
