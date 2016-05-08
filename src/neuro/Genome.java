package neuro;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Genome 
{
	public static int innovation = 0;
	
	public enum NeuronType { SENSOR, HIDDEN, OUTPUT };
	
	public class NeuronGene implements Comparable<NeuronGene>
	{
		int id;
		NeuronType type;
		
		@Override
		public int compareTo(NeuronGene o) {
			return id - o.id;
		}
	}
	
	public class SynapseGene implements Comparable<SynapseGene>
	{
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
	
	public Genome()
	{	
		neurons = new HashSet<NeuronGene>();
		synapses = new TreeSet<SynapseGene>();
		random = new Random();
	}
	
	public void mutateSynapse(NeuronGene in, NeuronGene out) 
	{
		SynapseGene g = new SynapseGene();
		g.innovation = innovation++;
		g.in = in;
		g.out = out;
		g.disabled = false;
		g.weight = random.nextDouble();
		synapses.add(g);
	}
	
	public void mutateNeuron(NeuronGene in, NeuronGene out) 
	{
		NeuronGene n = new NeuronGene();
		n.type = NeuronType.HIDDEN;
		n.id = neurons.size();
		neurons.add(n);
		mutateSynapse(in, n);
		mutateSynapse(n, out);
		
		for (SynapseGene g : synapses)
			if (g.in.id == in.id && g.out.id == out.id)
				g.disabled = true;
	}
	
	public Iterable<NeuronGene> neurons(NeuronType type)
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
	
	public void mutate()
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
	}
	
	public Genome crossover(Genome other) 
	{
		Genome G = new Genome();

		G.synapses = new TreeSet<SynapseGene>();
		
		if (other.fitness > fitness) {
			G.synapses.addAll(other.synapses);
			G.synapses.addAll(synapses);
		}
		else {
			G.synapses.addAll(synapses);
			G.synapses.addAll(other.synapses);
		}

		G.neurons = new HashSet<NeuronGene>();
		for (SynapseGene s : G.synapses) {
			G.neurons.add(s.in);
			G.neurons.add(s.out);
		}
		
		return G;
	}
	
	public int getInnovation() {
		int i = 0;
		for (SynapseGene synapse : synapses)
			if (synapse.innovation > i)
				i = synapse.innovation;
		return i;		
	}
	
	public double distance(Genome other) 
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
