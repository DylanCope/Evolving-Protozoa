package biology;

import core.ChemicalSolution;
import core.Settings;
import neat.NeuralNetwork;

import java.awt.*;

public class NNBrain implements Brain {

    public final NeuralNetwork network;
    private float[] outputs;
    private final float[] inputs;
    private final float maxTurn;

    public NNBrain(NeuralNetwork network, float maxTurn) {
        this.network = network;
        this.maxTurn = maxTurn;
        outputs = network.outputs();
        inputs = new float[network.getInputSize()];
    }

    public NNBrain(NeuralNetwork network) {
        this(network, (float) Math.toRadians(Settings.maxTurnAngle));
    }

    @Override
    public void tick(Protozoa p)
    {
        int i = 0;
        // ProtozoaGenome.nonVisualSensorSize
        inputs[i++] = 1; // bias term
        inputs[i++] = p.getHealth() * 2 - 1;
        inputs[i++] = 2 * p.getRadius() / p.getGenome().getSplitRadius() - 1;

        ChemicalSolution chemicalSolution = p.getTank().getChemicalSolution();
        int chemicalX = chemicalSolution.toChemicalGridX(p.getPos().getX());
        int chemicalY = chemicalSolution.toChemicalGridY(p.getPos().getY());
        inputs[i++] = chemicalSolution.getPlantPheromoneGradientX(chemicalX, chemicalY);
        inputs[i++] = chemicalSolution.getPlantPheromoneGradientY(chemicalX, chemicalY);

        for (Retina.Cell cell : p.getRetina()) {
            if (cell.anythingVisible()) {
                Color colour = cell.getColour();
                inputs[i++] = -1 + 2 * colour.getRed() / 255f;
                inputs[i++] = -1 + 2 * colour.getGreen() / 255f;
                inputs[i++] = -1 + 2 * colour.getBlue() / 255f;
            } else {
                inputs[i++] = 0f;
                inputs[i++] = 0f;
                inputs[i++] = 0f;
            }
        }

        network.setInput(inputs);
        network.tick();
        outputs = network.outputs();
    }

    @Override
    public float turn(Protozoa p)
    {
        float turn = outputs[0];
        return turn * maxTurn;
    }

    @Override
    public float speed(Protozoa p)
    {
        return Math.min(
                Settings.maxProtozoaSpeed * outputs[1],
                Settings.maxProtozoaSpeed
        );
    }

    @Override
    public boolean wantToMateWith(Protozoa p) {
        return outputs[3] > 0;
    }

    @Override
    public float energyConsumption() {
        return 0;
    }
}
