package io.codextension.algorithm.neural;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Brain implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Brain.class);

    private Map<Integer, List<Neuron>> layers;
    private LinkedList<Double> expectedOutputValues;

    public Brain(int inputSize, int outputSize, int numberOfHiddenLayers, int hiddenLayerSize, boolean withBias) {
        layers = new LinkedHashMap<>();

        List<Neuron> neurons = new LinkedList<>();

        for (int j = 0; j < inputSize; j++) {
            neurons.add(new Neuron("N1." + (j + 1)));
        }
        layers.put(0, neurons);

        for (int i = 1; i < numberOfHiddenLayers + 1; i++) {
            neurons = new LinkedList<>();
            if (withBias) {
                neurons.add(new BiasNeuron("BN" + (i + 1) + ".1"));
            }
            for (int j = 0; j < hiddenLayerSize; j++) {
                neurons.add(new Neuron("N" + (i + 1) + "." + (j + (withBias ? 1 : 0) + 1)));
            }
            layers.put(i, neurons);
        }

        neurons = new LinkedList<>();
        for (int j = 0; j < outputSize; j++) {
            neurons.add(new Neuron("N" + (numberOfHiddenLayers + 2) + "." + (j + 1)));
        }
        layers.put(numberOfHiddenLayers + 1, neurons);

        createSynapses();
    }

	public void load(TrainingDataHolder dataHolder, boolean ignoreWeights) {
		expectedOutputValues = dataHolder.getExpectedOutputs();

		for (Integer i = 0; i < layers.keySet().size(); i++) {
			List<Neuron> sources = layers.get(i);
			for (Neuron source : sources) {
				if (dataHolder.getValues().containsKey(source.getId())) {
					source.setValue(dataHolder.getValues().get(source.getId()));
				}
				if (!ignoreWeights) {
					for (Synapse synapse : source.getOutgoingSynapse()) {
						if (dataHolder.getWeights().containsKey(synapse.getId())) {
							synapse.setWeight(dataHolder.getWeights().get(synapse.getId()));
						}
					}
				}
			}
		}

	}

    public TrainingDataHolder unload() {
        TrainingDataHolder dataHolder = new TrainingDataHolder();

        for (Integer i = 0; i < layers.keySet().size(); i++) {
            List<Neuron> sources = layers.get(i);
            for (Neuron source : sources) {
                dataHolder.getValues().put(source.getId(), source.getValue());
                for (Synapse synapse : source.getOutgoingSynapse()) {
                    dataHolder.getWeights().put(synapse.getId(), synapse.getWeight());
                }
            }
        }

        dataHolder.setExpectedOutputs(getExpectedOutputValues());

        return dataHolder;
    }

    private void createSynapses() {
        for (Integer i = 0; i < layers.keySet().size() - 1; i++) {
            List<Neuron> sources = layers.get(i);
            List<Neuron> targets = layers.get(i + 1);
            LOG.trace("BRAIN - Initializing synapses from layer {} --> {}", i + 1, i + 2);
            for (Neuron source : sources) {
                for (Neuron target : targets) {
                    Random random = new Random();
                    Double value = (random.nextDouble() * 78 + 20) / 100;
                    if (!(target instanceof BiasNeuron)) {
                        new Synapse(value, source, target);
                        LOG.trace("BRAIN - {} ---{}---> {}", source.getId(), value, target.getId());
                    }
                }
            }
        }
    }

    public LinkedList<Double> getExpectedOutputValues() {
        return expectedOutputValues;
    }

    public List<Neuron> getInputs() {
        return layers.get(0);
    }

    public List<Neuron> getOutputs() {
        return layers.get(layers.size() - 1);
    }

    public Map<Integer, List<Neuron>> getLayers() {
        return layers;
    }

    public void setLayers(Map<Integer, List<Neuron>> layers) {
        this.layers = layers;
    }

    /**
     * @param layerNumber the layer number starting with 0
     * @return The list of neurones
     */
    public List<Neuron> getLayer(int layerNumber) {
        return layers.get(layerNumber);
    }

    public Map<String, Double> actualWeights() {
        Map<String, Double> results = new HashMap<>();
        for (Integer i = 0; i < layers.keySet().size() - 1; i++) {
            List<Neuron> sources = layers.get(i);
            for (Neuron source : sources) {
                for (Synapse synapse : source.getOutgoingSynapse()) {
                    results.put(synapse.getId(), synapse.getWeight());
                }
            }
        }
        return results;
    }

    public void updateSynapseWeight(Double v, Neuron in, Neuron out) {
        for (Synapse synapse : in.getOutgoingSynapse()) {
            if (synapse.getTarget() == out) {
                synapse.setWeight(v);
                return;
            }
        }
    }
}
