package protoevo.biology.genes;

import protoevo.biology.Protozoan;
import protoevo.core.Settings;
import protoevo.core.Simulation;

import java.io.Serializable;
import java.util.Arrays;

public class ProtozoaSpikesGene extends Gene<Protozoan.Spike[]> implements Serializable {

    public ProtozoaSpikesGene() {
        super();
    }

    public ProtozoaSpikesGene(Protozoan.Spike[] value) {
        super(value);
    }

    @Override
    public <G extends Gene<Protozoan.Spike[]>> G createNew(Protozoan.Spike[] value) {
        return (G) new ProtozoaSpikesGene(value);
    }

    public Protozoan.Spike[] getNewValue() {
        return new Protozoan.Spike[0];
    }

    @Override
    public String getTraitName() {
        return "Spikes";
    }

    @Override
    public String valueString() {
        Protozoan.Spike[] spikes = getValue();
        StringBuilder str = new StringBuilder(spikes.length + ";");
        for (Protozoan.Spike spike : spikes)
            str.append(spike.currentLength).append(";").append(spike.angle).append(";").append(spike.growthRate);
        return str.toString();
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

    private <G extends Gene<Protozoan.Spike[]>> G addSpike(Gene<?>[] genome) {
        Protozoan.Spike[] spikes = getValue();
        Protozoan.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length+1);

        Protozoan.Spike spike = new Protozoan.Spike();
        float radius = getProtozoaMaxRadius(genome);
        spike.length = randomSpikeLength(radius);
        spike.angle = randomAngle();
        spike.growthRate = randomSpikeGrowthRate();
        newSpikes[spikes.length] = spike;

        return createNew(newSpikes, getNumMutations() + 1);
    }

    private <G extends Gene<Protozoan.Spike[]>> G removeSpike() {
        Protozoan.Spike[] spikes = getValue();
        int idxRemove = Simulation.RANDOM.nextInt(spikes.length);
        Protozoan.Spike[] newSpikes = new Protozoan.Spike[spikes.length - 1];
        int j = 0;
        for (int i = 0; i < spikes.length; i++) {
            if (i == idxRemove)
                continue;
            newSpikes[j] = spikes[i];
            j++;
        }

        return createNew(newSpikes, getNumMutations() + 1);
    }

    private <G extends Gene<Protozoan.Spike[]>> G mutateRandomSpike(Gene<?>[] genome) {
        Protozoan.Spike[] spikes = getValue();
        Protozoan.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length);
        int idx = Simulation.RANDOM.nextInt(spikes.length);
        int nSpikeProperties = 3;
        float p = Simulation.RANDOM.nextFloat();

        Protozoan.Spike newSpike = new Protozoan.Spike();
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
    public <G extends Gene<Protozoan.Spike[]>> G mutate(Gene<?>[] genome) {
        float p = Simulation.RANDOM.nextFloat();

        Protozoan.Spike[] spikes = getValue();
        if (p > 3f / 4f || spikes.length == 0)
            return addSpike(genome);
        else if (p > 2f / 4f)
            return removeSpike();
        else if (p > 1f / 4f)
            return rotateSpikes();
        else
            return mutateRandomSpike(genome);
    }

    private <G extends Gene<Protozoan.Spike[]>> G rotateSpikes() {
        Protozoan.Spike[] spikes = getValue();
        Protozoan.Spike[] newSpikes = Arrays.copyOf(spikes, spikes.length);
        float theta = randomAngle();
        for (int i = 0; i < spikes.length; i++) {
            Protozoan.Spike newSpike = new Protozoan.Spike();
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
    public Protozoan.Spike[] disabledValue() {
        return new Protozoan.Spike[0];
    }
}