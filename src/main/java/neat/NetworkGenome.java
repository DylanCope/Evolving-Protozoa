package neat;

import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetworkGenome
{
	private static int innovation = 0;

	
	private Set<NeuronGene> neuronGenes;
	private SortedSet<SynapseGene> synapseGenes;
	private Random random;
	private double mutationChance = 0.05;
	private double fitness;
	private Neuron.Activation defaultActivation;

	public NetworkGenome(long seed)
	{
		neuronGenes = new TreeSet<>();
		synapseGenes = new TreeSet<>();
		random = new Random(0);
	}

	public void setProperties(NetworkGenome other)
	{
		this.neuronGenes = other.neuronGenes;
		this.synapseGenes = other.synapseGenes;
		this.random = other.random;
		this.mutationChance = other.mutationChance;
	}

	public NetworkGenome(long seed, int numInputs, int numOutputs)
	{
		this(seed, numInputs, numOutputs, Neuron.Activation.SIGMOID);
	}

	public NetworkGenome(long seed, int numInputs, int numOutputs, Neuron.Activation defaultActivation)
	{
		neuronGenes = new TreeSet<>();
		for (int i = 0; i < numInputs; i++)
			neuronGenes.add(new NeuronGene(i, Neuron.Type.SENSOR, Neuron.Activation.LINEAR));
		for (int i = numInputs; i < numInputs + numOutputs; i++)
			neuronGenes.add(new NeuronGene(i, Neuron.Type.OUTPUT, defaultActivation));
		synapseGenes = new TreeSet<>();
		random = new Random(seed);
		this.defaultActivation = defaultActivation;
	}
	
	private void mutateSynapse(NeuronGene in, NeuronGene out)
	{
		if (random.nextDouble() <= mutationChance) {
			SynapseGene g = new SynapseGene();
			g.setInnovation(innovation++);
			g.setIn(in);
			g.setOut(out);
			g.setDisabled(false);
			g.setWeight(random.nextDouble());
			synapseGenes.add(g);
		}
	}
	
	private NetworkGenome mutateNeuron(NeuronGene in, NeuronGene out)
	{
		if (random.nextDouble() <= mutationChance)
			if (random.nextBoolean())
			{
				NeuronGene n = new NeuronGene(
						neuronGenes.size(), Neuron.Type.HIDDEN, defaultActivation);
				neuronGenes.add(n);
				mutateSynapse(in, n);
				mutateSynapse(n, out);

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
	
	private NetworkGenome mutate()
	{
		Streams.zip(neurons(Neuron.Type.SENSOR), neurons(Neuron.Type.HIDDEN), this::mutateNeuron);
		Streams.zip(neurons(Neuron.Type.SENSOR), neurons(Neuron.Type.OUTPUT), this::mutateNeuron);
		
		for (SynapseGene g : synapseGenes)
			mutateSynapse(g.getIn(), g.getOut());

		return this;
	}
	
	private NetworkGenome crossover(NetworkGenome other)
	{
		NetworkGenome G = new NetworkGenome(random.nextLong());

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

	protected NetworkGenome reproduce(NetworkGenome other)
	{
		return crossover(other).mutate();
	}

	public NeuralNetwork networkPhenotype()
	{
        Map<Integer, Neuron> neurons = neuronGenes.stream()
                .collect(Collectors.toMap(
                        NeuronGene::getId,
                        gene -> new Neuron(gene.getId(), gene.getType(), gene.getActivation())
                ));

        for (SynapseGene gene : synapseGenes) {
            Neuron postSynapticNeuron = neurons.get(gene.getOut().getId());
            Neuron preSynapticNeuron = neurons.get(gene.getIn().getId());
            postSynapticNeuron.addInput(preSynapticNeuron, gene.getWeight());
        }

		return new NeuralNetwork(neurons.values());
	}

	public double distance(NetworkGenome other)
	{
//		int excess = 0;
//		int disjoint = 0;
		return 0;
	}

	public void addSynapse(int in, int out, double w)
	{
		SynapseGene g = new SynapseGene();
		g.setInnovation(innovation++);
		for (NeuronGene gene : neuronGenes)
		{
			if (gene.getId() == in)
				g.setIn(gene);
			if (gene.getId() == out)
				g.setOut(gene);
		}
		g.setDisabled(false);
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
