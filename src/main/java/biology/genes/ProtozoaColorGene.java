package biology.genes;


import core.Simulation;

import java.awt.*;

public class ProtozoaColorGene extends Gene<Color> {

    public ProtozoaColorGene() {
        super();
    }

    public ProtozoaColorGene(Color value) {
        super(value);
    }

    @Override
    public <G extends Gene<Color>> G createNew(Color value) {
        return (G) new ProtozoaColorGene(value);
    }

    @Override
    public Color getNewValue() {
        return new Color(
                80 + Simulation.RANDOM.nextInt(150),
                80 + Simulation.RANDOM.nextInt(150),
                80  + Simulation.RANDOM.nextInt(150)
        );
    }
}