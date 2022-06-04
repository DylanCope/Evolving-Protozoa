package biology;

import core.Settings;
import neat.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NNBrain implements Brain {

    public final NeuralNetwork network;
    private List<Float> outputs;
    private final float maxTurn = (float) Math.toRadians(45);

    public NNBrain(NeuralNetwork network) {
        this.network = network;
        outputs = network.outputs();
    }

    @Override
    public void tick(Protozoa p)
    {
        ArrayList<Float> inputs = new ArrayList<>();
        for (Retina.Cell cell : p.getRetina()) {
            inputs.add(-1 + 2 * cell.colour.getRed() / 255f);
            inputs.add(-1 + 2 * cell.colour.getGreen() / 255f);
            inputs.add(-1 + 2 * cell.colour.getBlue() / 255f);
        }
        inputs.add(p.getHealth());
        network.setInput(inputs);
        network.tick();
        outputs = network.outputs();
    }

    @Override
    public float turn(Protozoa p)
    {
        float turn = outputs.get(0);
        return turn * maxTurn;
    }

    @Override
    public float speed(Protozoa p)
    {
        return Math.min(
                Settings.maxVel * outputs.get(1),
                Settings.maxVel
        );
    }

    @Override
    public boolean wantToAttack(Protozoa p) {
        return outputs.get(2) > 0.5;
    }

    @Override
    public boolean wantToMateWith(Protozoa p) {
        return outputs.get(3) > 0.5;
    }

    @Override
    public float energyConsumption() {
        return 0;
    }
}
