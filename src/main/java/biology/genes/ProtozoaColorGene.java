package biology.genes;


import core.Simulation;

import java.awt.*;
import java.io.Serializable;

public class ProtozoaColorGene extends Gene<Color> implements Serializable {

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
        Color color = getValue();
        int minVal = 80;
        int maxVal = 150;
        if (color == null)
            return new Color(
                minVal + Simulation.RANDOM.nextInt(maxVal),
                minVal + Simulation.RANDOM.nextInt(maxVal),
                minVal + Simulation.RANDOM.nextInt(maxVal)
            );

        float p = Simulation.RANDOM.nextFloat();
        int valChange = -15 + Simulation.RANDOM.nextInt(30);

        if (p < 1 / 3f) {
            int v = Math.max(Math.min(color.getRed() + valChange, maxVal), minVal);
            return new Color(v, color.getGreen(), color.getBlue());
        } else if (p < 2 / 3f) {
            int v = Math.max(Math.min(color.getGreen() + valChange, maxVal), minVal);
            return new Color(color.getRed(), v, color.getBlue());
        } else {
            int v = Math.max(Math.min(color.getBlue() + valChange, maxVal), minVal);
            return new Color(color.getRed(), color.getGreen(), v);
        }
    }
}