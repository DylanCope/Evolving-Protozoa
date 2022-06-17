package neat;

import core.Settings;
import core.Simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class NetworkGenome implements Serializable
{
	public static final long serialVersionUID = 6145947068527764820L;
	private NeuronGene[] sensorNeuronGenes, outputNeuronGenes, hiddenNeuronGenes;
	private int nNeuronGenes;
	private SynapseGene[] synapseGenes;
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
		sensorNeuronGenes = other.sensorNeuronGenes;
		outputNeuronGenes = other.outputNeuronGenes;
		hiddenNeuronGenes = other.hiddenNeuronGenes;
		synapseGenes = other.synapseGenes;
		nNeuronGenes = other.nNeuronGenes;
		random = other.random;
		mutationChance = other.mutationChance;
		defaultActivation = other.defaultActivation;
		fitness = other.fitness;
		numMutations = other.numMutations;
		nSensors = other.nSensors;
		nOutputs = other.nOutputs;
	}

	public NetworkGenome(int numInputs, int numOutputs)
	{
		this(numInputs, numOutputs, Neuron.Activation.TANH);
	}

	public NetworkGenome(int numInputs, int numOutputs, Neuron.Activation defaultActivation)
	{
		this.nSensors = numInputs;
		this.nOutputs = numOutputs;

		nNeuronGenes = 0;
		sensorNeuronGenes = new NeuronGene[numInputs];
		for (int i = 0; i < numInputs; i++)
			sensorNeuronGenes[i] = new NeuronGene(nNeuronGenes++, Neuron.Type.SENSOR, Neuron.Activation.LINEAR);

		outputNeuronGenes = new NeuronGene[numOutputs];
		for (int i = 0; i < numOutputs; i++)
			outputNeuronGenes[i] = new NeuronGene(nNeuronGenes++, Neuron.Type.OUTPUT, defaultActivation);

		hiddenNeuronGenes = new NeuronGene[0];

		synapseGenes = new SynapseGene[numInputs * numOutputs];
		for (int i = 0; i < numInputs; i++)
			for (int j = 0; j < numOutputs; j++) {
				NeuronGene in = sensorNeuronGenes[i];
				NeuronGene out = outputNeuronGenes[j];
				synapseGenes[i*numOutputs + j] = new SynapseGene(in, out);
			}

		this.defaultActivation = defaultActivation;
	}

	public void addSensor() {
		NeuronGene n = new NeuronGene(
				nNeuronGenes++, Neuron.Type.SENSOR, Neuron.Activation.LINEAR
		);

		sensorNeuronGenes = Arrays.copyOf(sensorNeuronGenes, sensorNeuronGenes.length + 1);
		sensorNeuronGenes[sensorNeuronGenes.length - 1] = n;
		nSensors++;

		int originalLen = synapseGenes.length;
		synapseGenes = Arrays.copyOf(synapseGenes, originalLen + outputNeuronGenes.length);
		for (int i = 0; i < outputNeuronGenes.length; i++)
			synapseGenes[originalLen + i] = new SynapseGene(n, outputNeuronGenes[i]);
	}

	private void createHiddenBetween(SynapseGene g) {

		NeuronGene n = new NeuronGene(
			nNeuronGenes++, Neuron.Type.HIDDEN, defaultActivation
		);

		hiddenNeuronGenes = Arrays.copyOf(hiddenNeuronGenes, hiddenNeuronGenes.length + 1);
		hiddenNeuronGenes[hiddenNeuronGenes.length - 1] = n;

		SynapseGene inConnection = new SynapseGene(g.getIn(), n);
		SynapseGene outConnection = new SynapseGene(n, g.getOut());

		synapseGenes = Arrays.copyOf(synapseGenes, synapseGenes.length + 2);
		synapseGenes[synapseGenes.length - 2] = inConnection;
		synapseGenes[synapseGenes.length - 1] = outConnection;

		g.setDisabled(true);
	}

	private int getSynapseGeneIndex(NeuronGene in, NeuronGene out) {

		for (int i = 0; i < synapseGenes.length - 2; i++) {
			if (synapseGenes[i].getIn().equals(in) &&
					synapseGenes[i].getOut().equals(out) &&
					!synapseGenes[i].isDisabled()) {
				return i;
			}
		}
		return -1;
	}
	
	private void mutateConnection(NeuronGene in, NeuronGene out) {
		numMutations++;

		int geneIndex = getSynapseGeneIndex(in, out);

		if (geneIndex == -1) {
			synapseGenes = Arrays.copyOf(synapseGenes, synapseGenes.length + 1);
			synapseGenes[synapseGenes.length - 1] = new SynapseGene(in, out);
		} else {
			SynapseGene g = synapseGenes[geneIndex];
			if (random.nextBoolean())
				createHiddenBetween(g);
			else
				synapseGenes[geneIndex] = new SynapseGene(in, out, g.getInnovation());
		}
	}
	
	public void mutate()
	{
		int i = random.nextInt(sensorNeuronGenes.length + hiddenNeuronGenes.length);
		NeuronGene in, out;
		if (i < sensorNeuronGenes.length)
			in = sensorNeuronGenes[i];
		else in = hiddenNeuronGenes[i - sensorNeuronGenes.length];

		int j = random.nextInt(hiddenNeuronGenes.length + outputNeuronGenes.length);
		if (j < hiddenNeuronGenes.length)
			out = hiddenNeuronGenes[j];
		else out = outputNeuronGenes[j - hiddenNeuronGenes.length];

		mutateConnection(in, out);
	}
	
//	public NetworkGenome crossover(NetworkGenome other)
//	{
//		NetworkGenome G = new NetworkGenome();
//
////		G.synapseGenes = new TreeSet<>();
////
////		if (other.fitness > fitness) {
////			G.synapseGenes.addAll(other.synapseGenes);
////			G.synapseGenes.addAll(synapseGenes);
////		}
////		else {
////			G.synapseGenes.addAll(synapseGenes);
////			G.synapseGenes.addAll(other.synapseGenes);
////		}
////
////		G.neuronGenes = new HashSet<>();
////		for (SynapseGene s : G.synapseGenes) {
////			G.neuronGenes.add(s.getIn());
////			G.neuronGenes.add(s.getOut());
////		}
//
//		return G;
//	}
//
//	public NetworkGenome reproduce(NetworkGenome other)
//	{
//		NetworkGenome childGenome = crossover(other);
//		childGenome.mutate();
//		return childGenome;
//	}

	private int maxNeuronId() {
		int id = 0;
		for (NeuronGene g : sensorNeuronGenes)
			id = Math.max(g.getId(), id);
		for (NeuronGene g : hiddenNeuronGenes)
			id = Math.max(g.getId(), id);
		for (NeuronGene g : outputNeuronGenes)
			id = Math.max(g.getId(), id);
		return id;
	}

	public NeuralNetwork phenotype()
	{

		Neuron[] neurons = new Neuron[maxNeuronId() + 1];

		for (NeuronGene g : sensorNeuronGenes) {

			Neuron[] inputs = new Neuron[0];
			float[] weights = new float[0];

			neurons[g.getId()] = new Neuron(g.getId(), inputs, weights, g.getType(), g.getActivation());
		}

		int[] inputCounts = new int[neurons.length];
		Arrays.fill(inputCounts, 0);

		for (SynapseGene g : synapseGenes)
			inputCounts[g.getOut().getId()]++;

		for (int i = 0; i < hiddenNeuronGenes.length + outputNeuronGenes.length; i++) {
			NeuronGene g;
			if (i < hiddenNeuronGenes.length)
				g = hiddenNeuronGenes[i];
			else g = outputNeuronGenes[i - hiddenNeuronGenes.length];

			Neuron[] inputs = new Neuron[inputCounts[g.getId()]];
			float[] weights = new float[inputCounts[g.getId()]];

			neurons[g.getId()] = new Neuron(g.getId(), inputs, weights, g.getType(), g.getActivation());
		}

		Arrays.fill(inputCounts, 0);
		for (SynapseGene g : synapseGenes) {
			int i = inputCounts[g.getOut().getId()]++;
			neurons[g.getOut().getId()].getInputs()[i] = neurons[g.getIn().getId()];
			neurons[g.getOut().getId()].getWeights()[i] = g.getWeight();
		}

		return new NeuralNetwork(neurons, nSensors, nOutputs);
	}

	public float distance(NetworkGenome other)
	{
//		int excess = 0;
//		int disjoint = 0;
		return 0;
	}

	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for (NeuronGene gene : sensorNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (NeuronGene gene : hiddenNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (NeuronGene gene : outputNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (SynapseGene gene : synapseGenes)
			str.append(gene.toString()).append("\n");
		return str.toString();
	}

	public SynapseGene[] getSynapseGenes() {
		return synapseGenes;
	}

	public int getNumMutations() {
		return numMutations;
	}

	public int numberOfSensors() {
		return nSensors;
	}
}
