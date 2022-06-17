package biology.genes;

import neat.NetworkGenome;

import java.io.Serializable;

public class NetworkGene extends Gene<NetworkGenome> implements Serializable {
    public static final long serialVersionUID = -1259753801126730417L;

    public NetworkGene(NetworkGenome value) {
        super(value);
    }

    @Override
    public <G extends Gene<NetworkGenome>> G createNew(NetworkGenome value) {
        return (G) new NetworkGene(value);
    }

    @Override
    public NetworkGenome getNewValue() {
        NetworkGenome networkGenome = new NetworkGenome(getValue());
        networkGenome.mutate();
        return networkGenome;
    }

    @Override
    public int getNumMutations() {
        return getValue().getNumMutations();
    }
}
