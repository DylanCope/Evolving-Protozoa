package neat;

import core.Settings;
import core.Simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class NetworkGenome implements Serializable
{

	private NeuronGene[] sensorNeuronGenes, outputNeuronGenes, hiddenNeuronGenes;
	private int nNeuronGenes;
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
		this.sensorNeuronGenes = other.sensorNeuronGenes;
		this.outputNeuronGenes = other.outputNeuronGenes;
		this.hiddenNeuronGenes = other.hiddenNeuronGenes;
		this.nNeuronGenes = other.nNeuronGenes;
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

		nNeuronGenes = 0;
		sensorNeuronGenes = new NeuronGene[numInputs];
		for (int i = 0; i < numInputs; i++)
			sensorNeuronGenes[i] = new NeuronGene(nNeuronGenes++, Neuron.Type.SENSOR, Neuron.Activation.LINEAR);

		outputNeuronGenes = new NeuronGene[numOutputs];
		for (int i = 0; i < numOutputs; i++)
			outputNeuronGenes[i] = new NeuronGene(nNeuronGenes++, Neuron.Type.OUTPUT, defaultActivation);

		hiddenNeuronGenes = new NeuronGene[0];


		synapseGenes = new SynapseGene[nNeuronGenes][nNeuronGenes];
		for (int i = 0; i < numInputs; i++)
			for (int j = 0; j < numOutputs; j++) {
				NeuronGene in = sensorNeuronGenes[i];
				NeuronGene out = outputNeuronGenes[j];
				synapseGenes[in.getId()][out.getId()] = new SynapseGene(in, out);
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

		resizeSynapseMatrix();
		for (int i = 0; i < outputNeuronGenes.length; i++)
			synapseGenes[n.getId()][outputNeuronGenes[i].getId()] = new SynapseGene(n, outputNeuronGenes[i]);
	}

	private void resizeSynapseMatrix() {
		SynapseGene[][] newSynapseGenes = new SynapseGene[nNeuronGenes][nNeuronGenes];
		for (int i = 0; i < synapseGenes.length; i++)
			System.arraycopy(synapseGenes[i], 0, newSynapseGenes[i], 0, synapseGenes[i].length);
		synapseGenes = newSynapseGenes;
	}

	private void createHiddenBetween(NeuronGene in, NeuronGene out) {

		NeuronGene n = new NeuronGene(
			nNeuronGenes++, Neuron.Type.HIDDEN, defaultActivation
		);

		hiddenNeuronGenes = Arrays.copyOf(hiddenNeuronGenes, hiddenNeuronGenes.length + 1);
		hiddenNeuronGenes[hiddenNeuronGenes.length - 1] = n;
		resizeSynapseMatrix();

		SynapseGene inConnection = new SynapseGene(in, n);
		SynapseGene outConnection = new SynapseGene(n, out);
		synapseGenes[in.getId()][n.getId()] = inConnection;
		synapseGenes[n.getId()][out.getId()] = outConnection;
		synapseGenes[in.getId()][out.getId()].setDisabled(true);
	}
	
	private void mutateConnection(NeuronGene in, NeuronGene out) {
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
	
	public void mutate()
	{
		if (random.nextDouble() <= mutationChance) {
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
		NetworkGenome childGenome = crossover(other);
		childGenome.mutate();
		return childGenome;
	}

	public NeuralNetwork phenotype()
	{
		Neuron[] neurons = new Neuron[nNeuronGenes];

		for (NeuronGene g : sensorNeuronGenes) {

			Neuron[] inputs = new Neuron[0];
			float[] weights = new float[0];

			neurons[g.getId()] = new Neuron(g.getId(), inputs, weights, g.getType(), g.getActivation());
		}

		for (int i = 0; i < hiddenNeuronGenes.length + outputNeuronGenes.length; i++) {
			NeuronGene g;
			if (i < hiddenNeuronGenes.length)
				g = hiddenNeuronGenes[i];
			else g = outputNeuronGenes[i - hiddenNeuronGenes.length];

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

		for (int i = 0; i < hiddenNeuronGenes.length + outputNeuronGenes.length; i++) {
			NeuronGene g;
			if (i < hiddenNeuronGenes.length)
				g = hiddenNeuronGenes[i];
			else g = outputNeuronGenes[i - hiddenNeuronGenes.length];

			float[] weights = neurons[g.getId()].getWeights();
			Neuron[] inputs = neurons[g.getId()].getInputs();

			int j = 0;
			for (SynapseGene[] outGenes : synapseGenes) {
				SynapseGene synapseGene = outGenes[g.getId()];
				if (synapseGene != null && !synapseGene.isDisabled()) {
					weights[j] = synapseGene.getWeight();
					inputs[j] = neurons[synapseGene.getIn().getId()];
					j++;
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

	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for (NeuronGene gene : sensorNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (NeuronGene gene : hiddenNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (NeuronGene gene : outputNeuronGenes)
			str.append(gene.toString()).append("\n");
		for (SynapseGene[] geneRow : synapseGenes)
			for (SynapseGene gene : geneRow)
				str.append(gene.toString()).append("\n");
		return str.toString();
	}

	public SynapseGene[][] getSynapseGenes() {
		return synapseGenes;
	}

	public int getNumMutations() {
		return numMutations;
	}
}
