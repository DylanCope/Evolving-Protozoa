package protoevo.core;

import java.awt.*;
import java.util.*;

import protoevo.biology.NNBrain;
import protoevo.neat.NeuralNetwork;
import protoevo.neat.Neuron;
import protoevo.biology.Cell;
import protoevo.utils.TextObject;
import protoevo.utils.Vector2;
import protoevo.utils.Window;
import protoevo.utils.TextStyle;
import protoevo.biology.Protozoan;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UI implements ChangeListener {
	private final Window window;
	private final Simulation simulation;
	private final protoevo.core.Renderer renderer;
	private final TextObject title, creatingTank;
	private final ArrayList<TextObject> info;
	private final ArrayList<TextObject> debugInfo;
	private final int infoTextSize, textAwayFromEdge;
	private boolean showFPS = Settings.showFPS;

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
		infoTextSize = window.getHeight() / 50;

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
		textAwayFromEdge = window.getWidth() / 60;

		creatingTank = new TextObject("Generating Initial Tank...", infoTextSize,
				new Vector2(textAwayFromEdge, getYPosLHS(1)));
		creatingTank.setColor(Color.WHITE.darker());

		//Create the slider
		JSlider framesPerSecond = new JSlider(JSlider.VERTICAL,
				10, 60, 30);
		framesPerSecond.addChangeListener(this);
		framesPerSecond.setMajorTickSpacing(10);
		framesPerSecond.setPaintTicks(true);

		//Create the label table
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>(3);
		labelTable.put(0, new JLabel("Stop") );
		labelTable.put(60/10, new JLabel("Slow") );
		labelTable.put(60, new JLabel("Fast") );
		framesPerSecond.setLabelTable( labelTable );

		framesPerSecond.setPaintLabels(true);
	}

	public float getYPosLHS(int i) {
		return 1.3f*infoTextSize*i + 3 * window.getHeight() / 20f;
	}

	public float getYPosRHS(int i) {
		return 1.3f*infoTextSize*i + window.getHeight() / 20f;
	}

	private int renderStats(Graphics2D g, int lineNumber, Map<String, Float> stats) {

		for (Map.Entry<String, Float> entityStat : stats.entrySet()) {
			String text = entityStat.getKey() + ": " + TextStyle.toString(entityStat.getValue(), 2);
			TextObject statText = new TextObject(
					text, infoTextSize,
					new Vector2(textAwayFromEdge, getYPosLHS(lineNumber))
			);
			statText.setColor(Color.WHITE.darker());
			statText.render(g);
			lineNumber++;
		}
		return lineNumber;
	}

	public void render(Graphics2D g)
	{
		title.render(g);

		int lineNumber = 0;

		if (!simulation.getTank().hasBeenInitialised()) {
			creatingTank.render(g);
			return;
		}

		Cell tracked = renderer.getTracked();
		if (tracked == null) {
			Map<String, Float> tankStats = simulation.getTank().getStats();
			lineNumber = renderStats(g, lineNumber, tankStats);

		} else {
			for (lineNumber = 0; lineNumber < info.size(); lineNumber++)
				info.get(lineNumber).setPosition(new Vector2(textAwayFromEdge, getYPosLHS(lineNumber)));


			info.get(0).setText("Number of pellets: " + simulation.getTank().numberOfPellets());
			info.get(0).render(g);
			info.get(1).setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
			info.get(1).render(g);
//			lineNumber++;

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

			lineNumber++;

			renderStats(g, lineNumber++, tracked.getStats());

			if (tracked instanceof Protozoan && ((Protozoan) tracked).getBrain() instanceof NNBrain) {
				NNBrain brain = (NNBrain) ((Protozoan) tracked).getBrain();
				renderBrainNetwork(brain.network, g);
			}
		}

		renderDebugStats(g);
	}

	public void toggleShowFPS() {
		showFPS = !showFPS;
	}

	private void renderDebugStats(Graphics2D g) {
		if (!simulation.inDebugMode() && !showFPS)
			return;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
			if (simulation.inDebugMode() || (showFPS && statText.getText().contains("FPS")))
				statText.render(g);
		}

		if (renderer.antiAliasing)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
			if (!neuron.getType().equals(Neuron.Type.SENSOR) && neuron.isConnectedToOutput()) {
				Stroke s = g.getStroke();

				for (int i = 0; i < neuron.getInputs().length; i++) {
					Neuron inputNeuron = neuron.getInputs()[i];

					float weight = inputNeuron.getLastState() * neuron.getWeights()[i];
					if (Math.abs(weight) <= 1e-4)
						continue;

					if (weight > 0) {
						float p = weight > 1 ? 1 : weight;
						g.setColor(new Color(
								(int) (240 - 100 * p), 240, (int) (255 - 100 * p)
						));
					} else if (weight < 0) {
						float p = weight < -1 ? 1 : -weight;
						g.setColor(new Color(
								240, (int) (240 - 100 * p), (int) (255 - 100 * p)
						));
					} else {
						g.setColor(new Color(240, 240, 240));
					}
					g.setStroke(new BasicStroke((int) (r * Math.abs(weight))));

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
			if (!neuron.isConnectedToOutput())
				continue;

			Color colour;
			double state = neuron.getLastState();
			if (state > 0) {
				state = state > 1 ? 1 : state;
				colour = new Color(
						30, (int) (50 + state * 150), 30
				);
			} else if (state < 0) {
				state = state < -1 ? -1 : state;
				colour = new Color(
						(int) (50 - state * 150), 30, 30
				);
			} else {
				colour = new Color(10, 10, 10);
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
		int networkDepth = nn.calculateDepth();

		int[] depthWidthValues = new int[networkDepth + 1];
		Arrays.fill(depthWidthValues, 0);
		for (Neuron n : neurons)
			if (n.isConnectedToOutput())
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
				if (n.getDepth() == depth && n.isConnectedToOutput()) {
					int y = (int) (boxYStart + nodeSpacing / 2f + boxHeight / 2f - (nNodes / 2f - i) * nodeSpacing);
					n.setGraphicsPosition(x, y);
					i++;
				}
			}
		}
		nn.setComputedGraphicsPositions(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		System.out.println("change event");
	}
}
