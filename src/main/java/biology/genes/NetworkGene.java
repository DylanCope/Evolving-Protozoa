package biology.genes;

import neat.NetworkGenome;

public class NetworkGene extends Gene<NetworkGenome> {

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
        NetworkGenome newNetworkGenome = new NetworkGenome(networkGenome);
        newNetworkGenome.mutate();
        return newNetworkGenome;
    }
}
