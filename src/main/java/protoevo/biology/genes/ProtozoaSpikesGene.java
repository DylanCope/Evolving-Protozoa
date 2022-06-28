package protoevo.biology.genes;

import protoevo.biology.Protozoa;
import protoevo.core.Settings;
import protoevo.core.Simulation;

import java.io.Serializable;
import java.util.Arrays;

public class ProtozoaSpikesGene extends Gene<Protozoa.Spike[]> implements Serializable {

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

        return createNew(newSpikes, getNumMutations() + 1);
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

        return createNew(newSpikes, getNumMutations() + 1);
    }

    private <G extends Gene<Protozoa.Spike[]>> G mutateRandomSpike(Gene<?>[] genome) {
        Protozoa.Spike[] spikes = getValue();
        Protozoa.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length);
        int idx = Simulation.RANDOM.nextInt(spikes.length);
        int nSpikeProperties = 3;
        float p = Simulation.RANDOM.nextFloat();

        Protozoa.Spike newSpike = new Protozoa.Spike();
        if (p < 1f / nSpikeProperties) {
            newSpike.angle = randomAngle();
        } else if (p < 2f / nSpikeProperties) {
            float radius = getProtozoaMaxRadius(genome);
            newSpike.length = randomSpikeLength(radius);
        } else {
            newSpike.growthRate = randomSpikeGrowthRate();
        }
        newSpikes[idx] = newSpike;

        return createNew(newSpikes, getNumMutations() + 1);
    }

    @Override
    public <G extends Gene<Protozoa.Spike[]>> G mutate(Gene<?>[] genome) {
        float p = Simulation.RANDOM.nextFloat();

        Protozoa.Spike[] spikes = getValue();
        if (p > 3f / 4f || spikes.length == 0)
            return addSpike(genome);
        else if (p > 2f / 4f)
            return removeSpike();
        else if (p > 1f / 4f)
            return rotateSpikes();
        else
            return mutateRandomSpike(genome);
    }

    private <G extends Gene<Protozoa.Spike[]>> G rotateSpikes() {
        Protozoa.Spike[] spikes = getValue();
        Protozoa.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length);
        float theta = randomAngle();
        for (int i = 0; i < spikes.length; i++) {
            Protozoa.Spike newSpike = new Protozoa.Spike();
            newSpike.angle = spikes[i].angle + theta;
            newSpike.length = spikes[i].length;
            newSpike.growthRate = spikes[i].growthRate;
            newSpikes[i] = newSpike;
        }

        return createNew(newSpikes, getNumMutations() + 1);
    }

    @Override
    public boolean canDisable() {
        return true;
    }

    @Override
    public Protozoa.Spike[] disabledValue() {
        return new Protozoa.Spike[0];
    }
}