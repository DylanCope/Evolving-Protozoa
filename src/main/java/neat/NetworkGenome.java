package neat;

import core.Settings;
import core.Simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class NetworkGenome implements Serializable
{

	private NeuronGene[] neuronGenes;
	private SynapseGene[][] synapseGenes;
	private Random random = Simulation.RANDOM;
	private float mutationChance = Settings.globalMutationChance;
	private Neuron.Activation defaultActivation = Neuron.Activation.LINEAR;
	private float fitness = 0.0f;
	private int numMutations = 0, nSensors, nOutputs;

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
		this.numMutations = other.numMutations;
		this.nSensors = other.nSensors;
		this.nOutputs = other.nOutputs;
	}

	public NetworkGenome(int numInputs, int numOutputs)
	{
		this(numInputs, numOutputs, Neuron.Activation.TANH);
	}

	public NetworkGenome(int numInputs, int numOutputs, Neuron.Activation defaultActivation)
	{
		this.nSensors = numInputs;
		this.nOutputs = numOutputs;

		neuronGenes = new NeuronGene[numInputs + numOutputs];
		for (int i = 0; i < numInputs; i++)
			neuronGenes[i] = new NeuronGene(i, Neuron.Type.SENSOR, Neuron.Activation.LINEAR);

		for (int i = numInputs; i < numInputs + numOutputs; i++)
			neuronGenes[i] = new NeuronGene(i, Neuron.Type.OUTPUT, defaultActivation);

		synapseGenes = new SynapseGene[neuronGenes.length][neuronGenes.length];
		for (int i = 0; i < numInputs; i++)
			for (int j = 0; j < numOutputs; j++) {
				NeuronGene in = neuronGenes[i];
				NeuronGene out = neuronGenes[numInputs + j];
				synapseGenes[in.getId()][out.getId()] = new SynapseGene(in, out);
			}
		this.defaultActivation = defaultActivation;

//		mutationChance = 1f;
//		for (int i = 0; i < 2; i++)
//			mutate();
	}

	private void resizeSynapseMatrix() {
		SynapseGene[][] newSynapseGenes = new SynapseGene[neuronGenes.length][neuronGenes.length];
		for (int i = 0; i < synapseGenes.length; i++)
			System.arraycopy(synapseGenes[i], 0, newSynapseGenes[i], 0, synapseGenes[i].length);
		synapseGenes = newSynapseGenes;
	}

	private void createHiddenBetween(NeuronGene in, NeuronGene out) {

		NeuronGene n = new NeuronGene(
			neuronGenes.length, Neuron.Type.HIDDEN, defaultActivation
		);

		neuronGenes = Arrays.copyOf(neuronGenes, neuronGenes.length + 1);
		neuronGenes[neuronGenes.length - 1] = n;
		resizeSynapseMatrix();

		SynapseGene inConnection = new SynapseGene(in, n);
		SynapseGene outConnection = new SynapseGene(n, out);
		synapseGenes[in.getId()][n.getId()] = inConnection;
		synapseGenes[n.getId()][out.getId()] = outConnection;
		synapseGenes[in.getId()][out.getId()].setDisabled(true);
	}
	
	private void mutateConnection(NeuronGene in, NeuronGene out) {
		if (random.nextDouble() <= mutationChance) {
			numMutations++;

			SynapseGene g = synapseGenes[in.getId()][out.getId()];
			if (g == null)
				synapseGenes[in.getId()][out.getId()] = new SynapseGene(in, out);
			else {
				if (random.nextBoolean())
					createHiddenBetween(in, out);
				else
					g.setWeight((float) (random.nextDouble()*2 - 1));
			}
		}
	}
	
	public NetworkGenome mutate()
	{
		for (int n = 0; n < 10; n++) {
			int i = random.nextInt(neuronGenes.length - nOutputs);
			if (i >= nSensors)
				i += nOutputs;
			int j = nSensors + random.nextInt(neuronGenes.length - nSensors);
			mutateConnection(neuronGenes[i], neuronGenes[j]);
		}
		return this;
	}
	
	public NetworkGenome crossover(NetworkGenome other)
	{
		NetworkGenome G = new NetworkGenome();

//		G.synapseGenes = new TreeSet<>();
//
//		if (other.fitness > fitness) {
//			G.synapseGenes.addAll(other.synapseGenes);
//			G.synapseGenes.addAll(synapseGenes);
//		}
//		else {
//			G.synapseGenes.addAll(synapseGenes);
//			G.synapseGenes.addAll(other.synapseGenes);
//		}
//
//		G.neuronGenes = new HashSet<>();
//		for (SynapseGene s : G.synapseGenes) {
//			G.neuronGenes.add(s.getIn());
//			G.neuronGenes.add(s.getOut());
//		}
		
		return G;
	}

	public NetworkGenome reproduce(NetworkGenome other)
	{
		return crossover(other).mutate();
	}

	public NeuralNetwork phenotype()
	{
		int nNeurons = neuronGenes.length;
		Neuron[] neurons = new Neuron[nNeurons];

		for (NeuronGene g : neuronGenes) {
			int nInputs = 0;
			for (SynapseGene[] outGenes : synapseGenes) {
				SynapseGene synapseGene = outGenes[g.getId()];
				if (synapseGene != null && !synapseGene.isDisabled())
					nInputs++;
			}

			Neuron[] inputs = new Neuron[nInputs];
			float[] weights = new float[nInputs];

			neurons[g.getId()] = new Neuron(g.getId(), inputs, weights, g.getType(), g.getActivation());
		}

		for (NeuronGene g : neuronGenes) {

			float[] weights = neurons[g.getId()].getWeights();
			Neuron[] inputs = neurons[g.getId()].getInputs();

			int i = 0;
			for (SynapseGene[] outGenes : synapseGenes) {
				SynapseGene synapseGene = outGenes[g.getId()];
				if (synapseGene != null && !synapseGene.isDisabled()) {
					weights[i] = synapseGene.getWeight();
					inputs[i] = neurons[synapseGene.getIn().getId()];
					i++;
				}
			}
		}

		return new NeuralNetwork(neurons, nSensors, nOutputs);
	}

	public float distance(NetworkGenome other)
	{
//		int excess = 0;
//		int disjoint = 0;
		return 0;
	}

	public void addSynapse(int inID, int outID, float w)
	{
//		NeuronGene inGene = null, outGene = null;
//		for (NeuronGene gene : neuronGenes)
//		{
//			if (gene.getId() == inID)
//				inGene = gene;
//			if (gene.getId() == outID)
//				outGene = gene;
//		}
//		if (inGene == null | outGene == null)
//			throw new RuntimeException("Could not find neuron genes to initialise synapse...");
//
//		for (SynapseGene g : synapseGenes) {
//			if (g.getIn().getId() == inID && g.getOut().getId() == outID && !g.isDisabled()) {
//				g.setWeight(w);
//				return;
//			}
//		}
//
//		SynapseGene g = new SynapseGene(inGene, outGene);
//		g.setInnovation(innovation++);
//		g.setWeight(w);
//		synapseGenes.add(g);

	}

	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for (NeuronGene gene : neuronGenes)
			str.append(gene.toString()).append("\n");
		for (SynapseGene[] geneRow : synapseGenes)
			for (SynapseGene gene : geneRow)
				str.append(gene.toString()).append("\n");
		return str.toString();
	}

	public SynapseGene[][] getSynapseGenes() {
		return synapseGenes;
	}

	public NeuronGene[] getNeuronGenes() {
		return neuronGenes;
	}

	public int getNumMutations() {
		return numMutations;
	}
}
