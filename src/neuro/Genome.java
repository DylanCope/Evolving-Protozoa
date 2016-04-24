package neuro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Genome 
{
	public static int innovation = 0;
	
	public enum NeuronType { SENSOR, HIDDEN, OUTPUT };
	
	public class NeuronGene 
	{
		int id;
		NeuronType type;
	}
	
	public class SynapseGene 
	{
		int innovation;
		NeuronGene in, out;
		double weight;
		boolean disabled;
	}
	
	private ArrayList<NeuronGene> neurons;
	private ArrayList<SynapseGene> synapses;
	private Random random;
	private double mutationChance = 0.05;
	private double fitness;
	
	public Genome()
	{
		neurons = new ArrayList<NeuronGene>();
		synapses = new ArrayList<SynapseGene>();
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
		int max = Math.max(other.synapses.size(), synapses.size());
		int min = Math.min(other.synapses.size(), synapses.size());
		Genome moreFit;
		if (fitness > other.fitness)
			moreFit = this;
		else moreFit = other;
		
		for (int i = 0; i < min; i++) {
			int i1 = synapses.get(i).innovation;
			int i2 = other.synapses.get(i).innovation;
			if (i1 == i2)
				G.addSynapse(moreFit.synapses.get(i));
			else if (i1 > i2)
				G.addSynapse(synapses.get(i));
			else
				G.addSynapse(other.synapses.get(i));
		}
		
		for (int i = min; i < max; i++) {
			if (max == synapses.size()) {
			}
		}
		
		return G;
	}
	
	public void addNeuron(NeuronGene n) {
		neurons.add(n);
	}
	
	public void addSynapse(SynapseGene s) {
		synapses.add(s);
	}
	
}
