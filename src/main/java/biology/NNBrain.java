package biology;

import core.Settings;
import neat.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NNBrain implements Brain {

    public final NeuralNetwork network;
    private float[] outputs;
    private final float[] inputs;
    private final float maxTurn = (float) Math.toRadians(Settings.maxTurnAngle);

    public NNBrain(NeuralNetwork network) {
        this.network = network;
        outputs = network.outputs();
        inputs = new float[network.getInputSize()];
    }

    @Override
    public void tick(Protozoa p)
    {
        int i = 0;
        for (Retina.Cell cell : p.getRetina()) {
            if (cell.entity != null) {
                inputs[i++] = -1 + 2 * cell.colour.getRed() / 255f;
                inputs[i++] = -1 + 2 * cell.colour.getGreen() / 255f;
                inputs[i++] = -1 + 2 * cell.colour.getBlue() / 255f;
            } else {
                inputs[i++] = 0f;
                inputs[i++] = 0f;
                inputs[i++] = 0f;
            }
        }
        inputs[i++] = p.getHealth() * 2 - 1;
        inputs[i++] = p.getRadius() / p.getGenome().getSplitRadius();
        inputs[i] = 2 * p.getCrowdingFactor() / 3 - 1;

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
                Settings.maxVel * outputs[1],
                Settings.maxVel
        );
    }

    @Override
    public boolean wantToAttack(Protozoa p) {
        return outputs[2] > 0;
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
