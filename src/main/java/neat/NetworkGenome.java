package neat;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import core.Settings;
import core.Simulation;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetworkGenome implements Serializable
{
	private static int innovation = 0;

	private Set<NeuronGene> neuronGenes = new TreeSet<>();
	private Set<SynapseGene> synapseGenes = new TreeSet<>();
	private Random random = Simulation.RANDOM;
	private double mutationChance = Settings.globalMutationChance;
	private Neuron.Activation defaultActivation = Neuron.Activation.LINEAR;
	private double fitness = 0.0;

	public NetworkGenome() {}

	public NetworkGenome(NetworkGenome other) {
		setProperties(other);
	}

	public void setProperties(NetworkGenome other)
	{
		this.neuronGenes = other.neuronGenes;
		this.synapseGenes = other.synapseGenes;
		this.random = other.random;
		this.mutationChance = other.mutationChance;
		this.defaultActivation = other.defaultActivation;
		this.fitness = other.fitness;
	}

	public NetworkGenome(int numInputs, int numOutputs)
	{
		this(numInputs, numOutputs, Neuron.Activation.SIGMOID);
	}

	public NetworkGenome(int numInputs, int numOutputs, Neuron.Activation defaultActivation)
	{
		for (int i = 0; i < numInputs; i++)
			neuronGenes.add(new NeuronGene(i, Neuron.Type.SENSOR, Neuron.Activation.LINEAR));

		for (int i = numInputs; i < numInputs + numOutputs; i++)
			neuronGenes.add(new NeuronGene(i, Neuron.Type.OUTPUT, defaultActivation));

		Set<List<NeuronGene>> neuronGenePairs = Sets.cartesianProduct(
				neurons(Neuron.Type.SENSOR).collect(Collectors.toSet()),
				neurons(Neuron.Type.OUTPUT).collect(Collectors.toSet())
		);

		synapseGenes = neuronGenePairs.stream()
				.map(pair -> new SynapseGene(pair.get(0), pair.get(1)))
				.collect(Collectors.toSet());

		this.defaultActivation = defaultActivation;
	}
	
	private Optional<SynapseGene> mutateSynapse(NeuronGene in, NeuronGene out)
	{
		if (random.nextDouble() <= mutationChance) {
			SynapseGene g = new SynapseGene(in, out);
			g.setInnovation(innovation++);
			return Optional.of(g);
		}
		return Optional.empty();
	}
	
	private NetworkGenome mutateNeuron(NeuronGene in, NeuronGene out)
	{
		if (random.nextDouble() <= mutationChance)
			if (random.nextBoolean())
			{
				NeuronGene n = new NeuronGene(
					neuronGenes.size(), Neuron.Type.HIDDEN, defaultActivation
				);
				neuronGenes.add(n);
				mutateSynapse(in, n).ifPresent(synapseGenes::add);
				mutateSynapse(n, out).ifPresent(synapseGenes::add);;

				for (SynapseGene g : synapseGenes)
					if (g.getIn().equals(in) && g.getOut().equals(out))
						g.setDisabled(true);
			}
			else
				addSynapse(in.getId(), out.getId(), random.nextDouble()*2 - 1);

		return this;
	}
	
	private Stream<NeuronGene> neurons(Neuron.Type type)
	{
		return neuronGenes.stream().filter(n -> n.getType().equals(type));
	}
	
	public NetworkGenome mutate()
	{
		Streams.zip(neurons(Neuron.Type.SENSOR), neurons(Neuron.Type.HIDDEN), this::mutateNeuron);
		Streams.zip(neurons(Neuron.Type.SENSOR), neurons(Neuron.Type.OUTPUT), this::mutateNeuron);

		Set<SynapseGene> newSynapseGenes = synapseGenes.stream()
				.map(g -> mutateSynapse(g.getIn(), g.getOut()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		synapseGenes.addAll(newSynapseGenes);

		return this;
	}
	
	public NetworkGenome crossover(NetworkGenome other)
	{
		NetworkGenome G = new NetworkGenome();

		G.synapseGenes = new TreeSet<>();
		
		if (other.fitness > fitness) {
			G.synapseGenes.addAll(other.synapseGenes);
			G.synapseGenes.addAll(synapseGenes);
		}
		else {
			G.synapseGenes.addAll(synapseGenes);
			G.synapseGenes.addAll(other.synapseGenes);
		}

		G.neuronGenes = new HashSet<>();
		for (SynapseGene s : G.synapseGenes) {
			G.neuronGenes.add(s.getIn());
			G.neuronGenes.add(s.getOut());
		}
		
		return G;
	}

	public NetworkGenome reproduce(NetworkGenome other)
	{
		return crossover(other).mutate();
	}

	public NeuralNetwork phenotype()
	{
        Map<Integer, Neuron> neurons = neuronGenes.stream()
                .collect(Collectors.toMap(
                        NeuronGene::getId,
                        gene -> new Neuron(gene.getId(), gene.getType(), gene.getActivation())
                ));

        for (SynapseGene gene : synapseGenes) {
			if (gene.isDisabled())
				continue;

            Neuron postSynapticNeuron = neurons.get(gene.getOut().getId());
            Neuron preSynapticNeuron = neurons.get(gene.getIn().getId());
            postSynapticNeuron.addInput(preSynapticNeuron, gene.getWeight());
        }

		return new NeuralNetwork(new ArrayList<>(neurons.values()));
	}

	public double distance(NetworkGenome other)
	{
//		int excess = 0;
//		int disjoint = 0;
		return 0;
	}

	public void addSynapse(int inID, int outID, double w)
	{
		NeuronGene inGene = null, outGene = null;
		for (NeuronGene gene : neuronGenes)
		{
			if (gene.getId() == inID)
				inGene = gene;
			if (gene.getId() == outID)
				outGene = gene;
		}
		if (inGene == null | outGene == null)
			throw new RuntimeException("Could not find neuron genes to initialise synapse...");

		SynapseGene g = new SynapseGene(inGene, outGene);
		g.setInnovation(innovation++);
		g.setWeight(w);
		synapseGenes.add(g);
	}

	public String toString()
	{
		String str = "";
		for (NeuronGene gene : neuronGenes)
			str += gene.toString() + "\n";
		for (SynapseGene gene : synapseGenes)
			str += gene.toString() + "\n";
		return str;
	}

}
