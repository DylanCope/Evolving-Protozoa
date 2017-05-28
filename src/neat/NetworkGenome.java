package neat;

import java.util.*;

public class NetworkGenome
{
	private static int innovation = 0;
	
	public enum NeuronType { SENSOR, HIDDEN, OUTPUT };
	
	public class NeuronGene implements Comparable<NeuronGene>
	{
		int id;
		NeuronType type;
		Neuron.Activation activation;

		public NeuronGene(int id, NeuronType type, Neuron.Activation activation)
		{
			this.id = id;
			this.type = type;
			this.activation = activation;
		}

		@Override
		public int compareTo(NeuronGene o) {
			return id - o.id;
		}

		@Override
		public String toString()
		{
			String typeStr;
			switch (type)
			{
				case HIDDEN: typeStr = "HIDDEN"; break;
				case SENSOR: typeStr = "SENSOR"; break;
				case OUTPUT: typeStr = "OUTPUT"; break;
				default: typeStr = "UNKNOWN";
			}
			return String.format("Neuron, id:%d, type:%s", id, typeStr);
		}
	}
	
	public class SynapseGene implements Comparable<SynapseGene>
	{
		int id;
		int innovation;
		NeuronGene in, out;
		double weight;
		boolean disabled;
		
		@Override
		public int compareTo(SynapseGene g) {
			return innovation - g.innovation;
		}

		@Override
		public String toString()
		{
			return String.format("Synapse, innov:%d, in:%d, out:%d, w:%.2f, disabled:%b",
					innovation, in.id, out.id, weight, disabled);
		}
	}
	
	private Set<NeuronGene> neurons;
	private SortedSet<SynapseGene> synapses;
	private Random random;
	private double mutationChance = 0.05;
	private double fitness;
	private Neuron.Activation defaultActivation;

	public NetworkGenome(long seed)
	{
		neurons = new TreeSet<NeuronGene>();
		synapses = new TreeSet<SynapseGene>();
		random = new Random(0);
	}

	public void setProperties(NetworkGenome other)
	{
		this.neurons = other.neurons;
		this.synapses = other.synapses;
		this.random = other.random;
		this.mutationChance = other.mutationChance;
	}

	public NetworkGenome(long seed, int numInputs, int numOutputs)
	{
		this(seed, numInputs, numOutputs, Neuron.SIGMOID);
	}

	public NetworkGenome(long seed, int numInputs,
						 int numOutputs, Neuron.Activation defaultActivation)
	{
		neurons = new TreeSet<NeuronGene>();
		for (int i = 0; i < numInputs; i++)
			neurons.add(new NeuronGene(i, NeuronType.SENSOR, Neuron.LINEAR));
		for (int i = numInputs; i < numInputs + numOutputs; i++)
			neurons.add(new NeuronGene(i, NeuronType.OUTPUT, defaultActivation));
		synapses = new TreeSet<SynapseGene>();
		random = new Random(seed);
		this.defaultActivation = defaultActivation;
	}
	
	private void mutateSynapse(NeuronGene in, NeuronGene out)
	{
		SynapseGene g = new SynapseGene();
		g.innovation = innovation++;
		g.in = in;
		g.out = out;
		g.disabled = false;
		g.weight = random.nextDouble();
		synapses.add(g);
	}
	
	private void mutateNeuron(NeuronGene in, NeuronGene out)
	{
		if (random.nextBoolean())
		{
			NeuronGene n = new NeuronGene(neurons.size(),
					NeuronType.HIDDEN, defaultActivation);
			n.type = NeuronType.HIDDEN;
			n.id = neurons.size();
			neurons.add(n);
			mutateSynapse(in, n);
			mutateSynapse(n, out);

			for (SynapseGene g : synapses)
				if (g.in.id == in.id && g.out.id == out.id)
					g.disabled = true;
		}
		else
		{
			addSynapse(in.id, out.id, random.nextDouble()*2 - 1);
		}
	}
	
	private Iterable<NeuronGene> neurons(NeuronType type)
	{
		return new Iterable<NeuronGene>() {

			@Override
			public Iterator<NeuronGene> iterator() {
				return new Iterator<NeuronGene>() {

					Iterator<NeuronGene> iter = neurons.iterator();
					NeuronGene next;
					
					@Override
					public boolean hasNext() {
						while (iter.hasNext()) {
							next = iter.next();
							if (next.type == type)
								return true;
						}
						return false;
					}

					@Override
					public NeuronGene next() {
						return next;
					}
				};
			}	
		};
	}
	
	private NetworkGenome mutate()
	{
		for (NeuronGene in : neurons(NeuronType.SENSOR)) {
			for (NeuronGene out : neurons(NeuronType.HIDDEN))
				if (random.nextDouble() <= mutationChance)
					mutateNeuron(in, out);
			for (NeuronGene out : neurons(NeuronType.OUTPUT))
				if (random.nextDouble() <= mutationChance)
					mutateNeuron(in, out);
		}
		
		for (SynapseGene g : synapses)
			if (random.nextDouble() <= mutationChance)
				 mutateSynapse(g.in, g.out);

		return this;
	}
	
	private NetworkGenome crossover(NetworkGenome other)
	{
		NetworkGenome G = new NetworkGenome(random.nextLong());

		G.synapses = new TreeSet<>();
		
		if (other.fitness > fitness) {
			G.synapses.addAll(other.synapses);
			G.synapses.addAll(synapses);
		}
		else {
			G.synapses.addAll(synapses);
			G.synapses.addAll(other.synapses);
		}

		G.neurons = new HashSet<>();
		for (SynapseGene s : G.synapses) {
			G.neurons.add(s.in);
			G.neurons.add(s.out);
		}
		
		return G;
	}

	protected NetworkGenome reproduce(NetworkGenome other)
	{
		return crossover(other).mutate();
	}

	public NeuralNetwork networkPhenotype()
	{
		Neuron[] ns = new Neuron[neurons.size()];
		List<Neuron> outputs = new ArrayList<>();
		for (int i = 0; i < ns.length; i++)
		{
			ns[i] = new Neuron(i);
			List<Neuron> inputs = new ArrayList<>();
			List<Double> ws = new ArrayList<>();
			for (SynapseGene syn : synapses)
				if (!syn.disabled && syn.out.id == i)
				{
					inputs.add(ns[syn.in.id]);
					ws.add(syn.weight);
				}

			NeuronGene neuronGene = null;
			for (NeuronGene gene : neurons)
				if (gene.id == i) {
					neuronGene = gene;
					if (neuronGene.type == NeuronType.OUTPUT)
						outputs.add(ns[neuronGene.id]);
					ns[neuronGene.id].setActivation(neuronGene.activation);
					break;
				}

			ns[i].inputs = new Neuron[inputs.size()];
			for (int j = 0; j < inputs.size(); j++)
				ns[i].inputs[j] = inputs.get(j);
			ns[i].weights = new double[ws.size()];
			for (int j = 0; j < ws.size(); j++)
				ns[i].weights[j] = ws.get(j);
		}

		Neuron[] out = new Neuron[outputs.size()];
		for (int j = 0; j < outputs.size(); j++)
			out[j] = outputs.get(j);
		return new NeuralNetwork(out, ns);
	}
	
	public int getInnovation()
	{
		int i = 0;
		for (SynapseGene synapse : synapses)
			if (synapse.innovation > i)
				i = synapse.innovation;
		return i;		
	}
	
	public double distance(NetworkGenome other)
	{
//		int excess = 0;
//		int disjoint = 0;
		return 0;
	}

	public void addNeuron(NeuronGene n) {
		neurons.add(n);
	}
	
	public void addSynapse(SynapseGene s) {
		synapses.add(s);
	}

	public void addSynapse(int in, int out, double w)
	{
		SynapseGene g = new SynapseGene();
		g.innovation = innovation++;
		for (NeuronGene gene : neurons)
		{
			if (gene.id == in)
				g.in = gene;
			if (gene.id == out)
				g.out = gene;
		}
		g.disabled = false;
		g.weight = w;
		synapses.add(g);
	}

	public String toString()
	{
		String str = "";
		for (NeuronGene gene : neurons)
			str += gene.toString() + "\n";
		for (SynapseGene gene : synapses)
			str += gene.toString() + "\n";
		return str;
	}
}
