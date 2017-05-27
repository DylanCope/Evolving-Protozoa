package neat;

import java.util.*;

public class NetworkGenome
{
	public static int innovation = 0;
	
	public enum NeuronType { SENSOR, HIDDEN, OUTPUT };
	
	public class NeuronGene implements Comparable<NeuronGene>
	{
		int id;
		NeuronType type;

		public NeuronGene(int id, NeuronType type) { this.id = id; this.type = type; }

		@Override
		public int compareTo(NeuronGene o) {
			return id - o.id;
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
	}
	
	private Set<NeuronGene> neurons;
	private SortedSet<SynapseGene> synapses;
	private Random random;
	private double mutationChance = 0.05;
	private double fitness;
	
	public NetworkGenome()
	{	
		neurons = new HashSet<NeuronGene>();
		synapses = new TreeSet<SynapseGene>();
		random = new Random();
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
		NeuronGene n = new NeuronGene(neurons.size(), NeuronType.HIDDEN);
		n.type = NeuronType.HIDDEN;
		n.id = neurons.size();
		neurons.add(n);
		mutateSynapse(in, n);
		mutateSynapse(n, out);
		
		for (SynapseGene g : synapses)
			if (g.in.id == in.id && g.out.id == out.id)
				g.disabled = true;
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
		NetworkGenome G = new NetworkGenome();

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

	protected NeuralNetwork networkPhenotype()
	{
		Neuron[] ns = new Neuron[neurons.size()];
		List<Neuron> outputs = new ArrayList<>();
		for (int i = 0; i < ns.length; i++)
		{
			ns[i].id = i;
			List<Neuron> inputs = new ArrayList<>();
			List<Double> ws = new ArrayList<>();
			NeuronGene neuronGene = null;
			for (SynapseGene syn : synapses)
				if (!syn.disabled && syn.out.id == i) {
					neuronGene = syn.out;
					inputs.add(ns[syn.in.id]);
					ws.add(syn.weight);
				}

			if (neuronGene != null && neuronGene.type == NeuronType.OUTPUT)
				outputs.add(ns[neuronGene.id]);
			else if (neuronGene.type == NeuronType.SENSOR)
				ns[i].setActivation(Neuron.LINEAR);

			ns[i].inputs = (Neuron[]) inputs.toArray();
			ns[i].weights = new double[ws.size()];
			for (int j = 0; j < ws.size(); j++)
				ns[i].weights[j] = ws.get(j);
		}
		return new NeuralNetwork((Neuron[]) outputs.toArray(), ns);
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
}
