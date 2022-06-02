package biology;

import neat.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NNBrain implements Brain {

    public final NeuralNetwork network;
    private List<Double> outputs;
    private final double maxTurn = Math.toRadians(15);
    private final double maxVel = 0.2;

    public NNBrain(NeuralNetwork network) {
        this.network = network;
        outputs = network.outputs();
    }

    @Override
    public void tick(Protozoa p)
    {
        ArrayList<Double> inputs = new ArrayList<>();
        for (Retina.Cell cell : p.getRetina()) {
            inputs.add(-1 + 2 * cell.colour.getRed() / 255.0);
            inputs.add(-1 + 2 * cell.colour.getGreen() / 255.0);
            inputs.add(-1 + 2 * cell.colour.getBlue() / 255.0);
        }
        network.setInput(inputs);
        network.tick();
        outputs = network.outputs();
    }

    @Override
    public double turn(Protozoa p)
    {
        double turn = outputs.get(0);
        return turn * maxTurn;
    }

    @Override
    public double speed(Protozoa p)
    {
        return Math.min(maxVel * outputs.get(1), maxVel);
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
    public double energyConsumption() {
        return 0;
    }
}
