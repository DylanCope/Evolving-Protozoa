package biology.genes;

import biology.Protozoa;
import core.Settings;
import core.Simulation;

import java.util.Arrays;

public class ProtozoaSpikesGene extends Gene<Protozoa.Spike[]> {

    public ProtozoaSpikesGene() {
        super();
    }

    public ProtozoaSpikesGene(Protozoa.Spike[] value) {
        super(value);
    }

    @Override
    public <G extends Gene<Protozoa.Spike[]>> G createNew(Protozoa.Spike[] value) {
        return (G) new ProtozoaSpikesGene(value);
    }

    public Protozoa.Spike[] getNewValue() {
        return new Protozoa.Spike[0];
    }

    private float getProtozoaMaxRadius(Gene<?>[] genome) {
        float radius = -1;
        for (Gene<?> gene : genome)
            if (gene instanceof ProtozoaRadiusGene)
                radius = (float) gene.getValue();
        return radius;
    }

    private float randomSpikeLength(float maxProtozoaRadius) {
        return (0.3f + 0.5f * Simulation.RANDOM.nextFloat()) * maxProtozoaRadius;
    }

    private float randomAngle() {
        return (float) (2 * Math.PI * Simulation.RANDOM.nextFloat());
    }

    private float randomSpikeGrowthRate() {
        return Settings.maxSpikeGrowth * Simulation.RANDOM.nextFloat();
    }

    private <G extends Gene<Protozoa.Spike[]>> G addSpike(Gene<?>[] genome) {
        Protozoa.Spike[] spikes = getValue();
        Protozoa.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length+1);

        Protozoa.Spike spike = new Protozoa.Spike();
        float radius = getProtozoaMaxRadius(genome);
        spike.length = randomSpikeLength(radius);
        spike.angle = randomAngle();
        spike.growthRate = randomSpikeGrowthRate();
        newSpikes[spikes.length] = spike;

        return createNew(newSpikes);
    }

    private <G extends Gene<Protozoa.Spike[]>> G removeSpike() {
        Protozoa.Spike[] spikes = getValue();
        int idxRemove = Simulation.RANDOM.nextInt(spikes.length);
        Protozoa.Spike[] newSpikes = new Protozoa.Spike[spikes.length - 1];
        int j = 0;
        for (int i = 0; i < spikes.length; i++) {
            if (i == idxRemove)
                continue;
            newSpikes[j] = spikes[i];
            j++;
        }
        return createNew(newSpikes);
    }

    private <G extends Gene<Protozoa.Spike[]>> G mutateRandomSpike(Gene<?>[] genome) {
        Protozoa.Spike[] spikes = getValue();
        Protozoa.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length);
        int idx = Simulation.RANDOM.nextInt(spikes.length);
        int nSpikeProperties = 3;
        float p = Simulation.RANDOM.nextFloat();

        if (p < 1f / nSpikeProperties) {
            spikes[idx].angle = randomAngle();
        } else if (p < 2f / nSpikeProperties) {
            float radius = getProtozoaMaxRadius(genome);
            spikes[idx].length = randomSpikeLength(radius);
        } else {
            spikes[idx].growthRate = randomSpikeGrowthRate();
        }

        return createNew(newSpikes);
    }

    @Override
    public <G extends Gene<Protozoa.Spike[]>> G mutate(Gene<?>[] genome) {
        float p = Simulation.RANDOM.nextFloat();

        Protozoa.Spike[] spikes = getValue();
        if (p < 1f / 3f || spikes.length == 0)
            return addSpike(genome);
        else if (p < 1f / 2f)
            return removeSpike();
        else
            return mutateRandomSpike(genome);
    }
}