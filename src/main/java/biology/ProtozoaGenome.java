package biology;

import core.Simulation;
import neat.NetworkGenome;
import neat.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan on 28/05/2017.
 */
public class ProtozoaGenome extends NetworkGenome
{
    int retinaSize;
    double radius;

    public ProtozoaGenome(int retinaSize, double radius)
    {
        super(0, 3*retinaSize, 4);
        this.retinaSize = retinaSize;
        this.radius = radius;
    }

    public Brain brain()
    {
        return new Brain()
        {
            private NeuralNetwork network = networkPhenotype();
            private List<Double> outputs;
            private double maxTurn = Math.toRadians(35);
            private double maxVel = 0.1;

            @Override
            public void tick(Protozoa p)
            {
                ArrayList<Double> inputs = new ArrayList<>();
                for (Retina.Cell cell : p.getRetina()) {
                    inputs.add(cell.colour.getRed() / 255.0);
                    inputs.add(cell.colour.getGreen() / 255.0);
                    inputs.add(cell.colour.getBlue() / 255.0);
                }
                network.setInput(inputs);
                network.tick();
                outputs = network.outputs();
            }

            @Override
            public double turn(Protozoa p)
            {
                double x = outputs.get(0);
                return x < maxTurn ? x : maxTurn;
            }

            @Override
            public double speed(Protozoa p)
            {
                double x = outputs.get(1);
                return x < maxVel ? x : maxVel;
            }

            @Override
            public boolean wantToAttack(Protozoa p) {
                return Simulation.RANDOM.nextBoolean();
            }

            @Override
            public boolean wantToMateWith(Protozoa p) {
                return Simulation.RANDOM.nextBoolean();
            }

            @Override
            public double energyConsumption() {
                return 0;
            }

        };
    }

    public Retina retina()
    {
        return new Retina(this.retinaSize);
    }

    public double getRadius()
    {
        return radius;
    }

    public Protozoa phenotype()
    {
        return new Protozoa(this);
    }

    public Protozoa reproduce(Protozoa a, Protozoa b)
    {
        NetworkGenome ng = super.reproduce((NetworkGenome) b.getGenome());
        ProtozoaGenome pg = new ProtozoaGenome(this.retinaSize, this.radius);
        pg.setProperties(ng);
        Protozoa offspring = new Protozoa(pg);
        offspring.setPos(a.getPos().add(b.getPos()).mul(0.5));
        return offspring;
    }

    @Override
    public String toString() {
        String str = super.toString();
        return str;
    }
}
