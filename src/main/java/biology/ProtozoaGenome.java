package biology;

import neat.NetworkGenome;
import neat.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        return new NNBrain(networkPhenotype());
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

    public Stream<Protozoa> reproduce(Protozoa a, Protozoa b)
    {
        NetworkGenome ng = super.reproduce(b.getGenome());
        ProtozoaGenome pg = new ProtozoaGenome(this.retinaSize, this.radius);
        pg.setProperties(ng);
        Protozoa offspring = new Protozoa(pg);
        offspring.setPos(a.getPos().add(b.getPos()).mul(0.5));
        return Stream.of(offspring);
    }

    @Override
    public String toString() {
        String str = super.toString();
        return str;
    }
}
