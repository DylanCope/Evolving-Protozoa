package protoevo.biology.genes;

import protoevo.neat.NetworkGenome;
import protoevo.neat.NeuralNetwork;

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
    public boolean canDisable() {
        return true;
    }

    @Override
    public NetworkGenome disabledValue() {
        return null;
    }

    @Override
    public NetworkGenome getNewValue() {
        NetworkGenome networkGenome = new NetworkGenome(getValue());
        networkGenome.mutate();
        return networkGenome;
    }

    @Override
    public String getTraitName() {
        return "Control Network";
    }

//    @Override
//    public Gene<?> crossover(Gene<?> other) {
//        if (other instanceof  NetworkGene) {
//            NetworkGenome otherNG = ((NetworkGene) other).getValue();
//            return createNew(getValue().crossover(otherNG));
//        }
//        return super.crossover(other);
//    }

    @Override
    public int getNumMutations() {
        return getValue().getNumMutations();
    }

    @Override
    public String valueString() {
        return getValue().hashCode() + "";
    }

}
