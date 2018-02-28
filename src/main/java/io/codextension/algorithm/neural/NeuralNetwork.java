package io.codextension.algorithm.neural;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NeuralNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(NeuralNetwork.class);
    private Brain brain;
    private double matchRange = 0.001;
    private boolean withBias=false;
    private double learningRate = 1;
    private int currentIteration = 0;
    private List<TrainingDataHolder> trainingDataHolders;
    private MethodType method;

    public NeuralNetwork() {
        trainingDataHolders = new LinkedList<>();
        method = MethodType.SGD;
    }

    public Brain getBrain() {
        return brain;
    }

    public void setBrain(Brain value) {
        this.brain = value;
        for (Integer key : brain.getLayers().keySet()) {
            List<Neuron> neurons = brain.getLayer(key);

            for (Neuron neuron : neurons) {
                for (Synapse synapse : neuron.getOutgoingSynapse()) {
                    synapse.setSource(neuron);
                    synapse.getTarget().getIncomingSynapse().add(synapse);
                }
            }
        }
    }

    public void initialize(List<HashMap<ValueType, Double[]>> trainingInstances, int numberOfHiddenLayers, int hiddenLayerSize) {
        LOG.debug("BRAIN - Initialising the brain of {} inputs, {} hidden layers of {} neurons and {} outputs",
                trainingInstances.get(0).get(ValueType.INPUT).length, numberOfHiddenLayers, hiddenLayerSize, trainingInstances.get(0).get(ValueType.OUTPUT).length);
        brain = new Brain(trainingInstances.get(0).get(ValueType.INPUT).length, trainingInstances.get(0).get(ValueType.OUTPUT).length, numberOfHiddenLayers, hiddenLayerSize, withBias);

        for (HashMap<ValueType, Double[]> trainingInstance : trainingInstances) {
            TrainingDataHolder dataHolder = new TrainingDataHolder();
            dataHolder.addInputs(Arrays.asList(trainingInstance.get(ValueType.INPUT)));
            dataHolder.getExpectedOutputs().addAll(Arrays.asList(trainingInstance.get(ValueType.OUTPUT)));
            dataHolder.setWeights(brain.actualWeights());
            trainingDataHolders.add(dataHolder);
        }
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double getMatchRange() {
        return matchRange;
    }

    public void setMatchRange(double matchRange) {
        this.matchRange = matchRange;
    }

    public int think() {
        return think(Long.MAX_VALUE);
    }

    public int think(long maxNbOfIteration) {
        LOG.debug("BRAIN - Training the neural network in {} mode", this.method.name());
        switch (method) {
            case BATCH:
                return thinkBatch(maxNbOfIteration);
            case SGD:
                return thinkSGD(maxNbOfIteration);
            case MINI_BATCH:
                return -1;
            default:
                return thinkSGD(maxNbOfIteration);
        }
    }

    private int thinkBatch(long maxNbOfIteration) {
        Map<Integer, Boolean> instanceState = new HashMap<>();

        for (int u = 1; u <= trainingDataHolders.size(); u++) {
            instanceState.put(u, false);
        }
        while (notAllTrue(instanceState) && currentIteration < maxNbOfIteration) {
            currentIteration++;
            if (currentIteration % 50000 == 0) {
                LOG.debug("=== Data after {} iterations", currentIteration);
            }
            LOG.trace("=== EPOCH [{}] for training instances ===", currentIteration);
            LOG.trace("Running forward propagation [{}]", currentIteration);
            for (int u = 0; u < trainingDataHolders.size(); u++) {
                brain.load(trainingDataHolders.get(u), true);
                evalForwardPropagation(brain);
                trainingDataHolders.set(u, brain.unload());
            }
            Map<String, Double> allDeltas = new HashMap<>();
            LOG.trace("Running back propagation [{}]", currentIteration);
            for (TrainingDataHolder trainingDataHolder : trainingDataHolders) {
                brain.load(trainingDataHolder, true);
                Map<String, Double> temp = calcDeltaWeights(brain, calcGradients(brain));
                if (allDeltas.size() == 0) {
                    allDeltas.putAll(temp);
                } else {
                    for (String key : allDeltas.keySet()) {
                        allDeltas.put(key, temp.get(key) + allDeltas.get(key));
                    }
                }
            }

            for (String key : allDeltas.keySet()) {
                allDeltas.put(key, allDeltas.get(key) / trainingDataHolders.size());
            }

            for (int u = 0; u < trainingDataHolders.size(); u++) {
                brain.load(trainingDataHolders.get(u), true);
                updateWeights(brain, allDeltas);
                trainingDataHolders.set(u, brain.unload());
                if (LOG.isDebugEnabled() && currentIteration % 50000 == 0) {
                    logIterationData(u);
                }
                instanceState.put(u + 1, areValuesMatching(brain.getOutputs(), brain.getExpectedOutputValues()));
            }
        }
        LOG.info("Training completed after {} iterations", currentIteration);
        return currentIteration;
    }

    private int thinkSGD(long maxNbOfIteration) {
        Map<Integer, Boolean> instanceState = new HashMap<>();
        for (int u = 1; u <= trainingDataHolders.size(); u++) {
            instanceState.put(u, false);
        }
        while (notAllTrue(instanceState) && currentIteration < maxNbOfIteration) {
            currentIteration++;
            if (currentIteration % 50000 == 0) {
                LOG.debug("=== Data after {} iterations", currentIteration);
            }
            for (int u = 0; u < trainingDataHolders.size(); u++) {
                brain.load(trainingDataHolders.get(u), true);
                LOG.trace("=== EPOCH [{}] for training instance {} ===", currentIteration, u + 1);
                LOG.trace("Running forward propagation [{}]", currentIteration);
                evalForwardPropagation(brain);
                LOG.trace("Running back propagation [{}]", currentIteration);
                Map<String, Double[]> gradients = calcGradients(brain);
                updateWeights(brain, calcDeltaWeights(brain, gradients));
                if (LOG.isTraceEnabled()) {
                    logWeights(brain);
                }
                if (LOG.isDebugEnabled() && currentIteration % 50000 == 0) {
                    logIterationData(u);
                }
                instanceState.put(u + 1, areValuesMatching(brain.getOutputs(), brain.getExpectedOutputValues()));
            }
        }
        LOG.info("Training completed after {} iterations", currentIteration);
        return currentIteration;
    }

    public void guess() {
        evalForwardPropagation(brain);
    }

    private boolean notAllTrue(Map<Integer, Boolean> instanceState) {
        boolean result = true;

        for (Boolean v : instanceState.values()) {
            result &= v;
        }
        return !result;
    }

    private boolean areValuesMatching(List<Neuron> neurons, List<Double> expectedValues) {
        boolean result = true;

        for (int i = 0; i < neurons.size(); i++) {
            Neuron neuron = neurons.get(i);
            result &= (Math.abs(expectedValues.get(i) - neuron.activationValue())) <= matchRange;
        }

        return result;
    }

    private void logIterationData(int u) {
        LOG.debug("Current state of training instance {}:", u + 1);
        logOutputWeights(trainingDataHolders.get(u).getExpectedOutputs(), trainingDataHolders.get(u).getPreviousOutputs());
        trainingDataHolders.get(u).getPreviousOutputs().clear();
        for (Neuron neuron : brain.getOutputs()) {
            trainingDataHolders.get(u).getPreviousOutputs().add(neuron.activationValue());
        }
    }

    private void evalForwardPropagation(Brain brain) {
        for (int i = 1; i < brain.getLayers().size(); i++) {
            LOG.trace("FWD - Reading layer {}", i + 1);
            for (int j = 0; j < brain.getLayer(i).size(); j++) {
                Neuron hiddenNeuron = brain.getLayer(i).get(j);
                double value = 0f;
                if (hiddenNeuron instanceof BiasNeuron) {
                    LOG.trace("FWD - Skipping {}", hiddenNeuron.getId());
                    continue;
                }
                LOG.trace("FWD - Reading {} in layer {}", hiddenNeuron.getId(), i + 1);

                for (Synapse synapse : hiddenNeuron.getIncomingSynapse()) {
                    LOG.trace("FWD - Incrementing {} value [v=v_old+w({})*a({})] --> v={}+{}*{}", hiddenNeuron.getId(), synapse.getId(), synapse.getSource().getId(), value, synapse.getWeight(), synapse.getSource().activationValue());
                    value += synapse.getWeight() * synapse.getSource().activationValue();
                }

                hiddenNeuron.setValue(value);
                LOG.trace("FWD - Final {} value is {} (a={})", hiddenNeuron.getId(), value, hiddenNeuron.activationValue());
            }
        }
    }

    private Map<String, Double> calcDeltaWeights(Brain brain, Map<String, Double[]> gradients) {
        Map<String, Double> deltasWeight = new HashMap<>();
        for (int i = brain.getLayers().size() - 2; i >= 0; i--) {
            for (int j = 0; j < brain.getLayer(i).size(); j++) {
                Neuron neuron = brain.getLayer(i).get(j);
                Double[] values = gradients.get("hidden_" + (i + 1));
                for (int s = 0; s < neuron.getOutgoingSynapse().size(); s++) {
                    Synapse synapse = neuron.getOutgoingSynapse().get(s);
                    double delta_weight = values[s] * neuron.activationValue();
                    deltasWeight.put(synapse.getId(), delta_weight);
                }
            }
        }
        return deltasWeight;
    }

    private void updateWeights(Brain brain, Map<String, Double> deltas) {
        // updating weights
        for (int i = brain.getLayers().size() - 2; i >= 0; i--) {
            LOG.trace("BACK - WEIGHT - Updating weights for layer {}", i + 1);
            for (int j = 0; j < brain.getLayer(i).size(); j++) {
                Neuron neuron = brain.getLayer(i).get(j);
                for (int s = 0; s < neuron.getOutgoingSynapse().size(); s++) {
                    Synapse synapse = neuron.getOutgoingSynapse().get(s);
                    double weight = synapse.getWeight() - (learningRate * deltas.get(synapse.getId()));
                    LOG.trace("BACK - DELTA_WEIGHT - DW[{}] = Delta[{}] * Activation{}[{}]", synapse.getId(), deltas.get(synapse.getId()), neuron.getId(), neuron.activationValue());
                    LOG.trace("BACK - WEIGHT - Synapse {} new weight is OLDW({})-(LR({})*DW({})) => {}", synapse.getId(), synapse.getWeight(), learningRate, deltas.get(synapse.getId()), weight);
                    synapse.setWeight(weight);
                }
            }
        }
    }

    private Map<String, Double[]> calcGradients(Brain brain) {
        LOG.trace("GRADIENT - Calculating");
        Map<String, Double[]> deltas = new HashMap<>();
        for (int i = brain.getLayers().size() - 1; i > 0; i--) {
            Double[] deltaHiddenSum = new Double[brain.getLayer(i).size()];
            for (int z = 0; z < brain.getLayer(i).size(); z++) {
                Neuron neuron = brain.getLayer(i).get(z);

                if (i == (brain.getLayers().size() - 1)) {
                    deltaHiddenSum[z] = (neuron.activationValue() - brain.getExpectedOutputValues().get(z)) * neuron.activationPrimeValue();
                } else {
                    Double dhs = 0d;
                    for (int j = 0; j < neuron.getOutgoingSynapse().size(); j++) {
                        Synapse synapse = neuron.getOutgoingSynapse().get(j);
                        dhs += deltas.get("hidden_" + (i + 1))[j] * synapse.getWeight();
                    }
                    deltaHiddenSum[z] = dhs * neuron.activationPrimeValue();
                }
            }
            String label = "hidden_" + i;
            deltas.put(label, deltaHiddenSum);
            LOG.trace("GRADIENT - Layer {} -> {}", i + 1, Arrays.toString(deltaHiddenSum));
        }
        return deltas;
    }

    private void logWeights(Brain brain) {
        for (Integer i = 0; i < brain.getLayers().keySet().size() - 1; i++) {
            List<Neuron> sources = brain.getLayers().get(i);
            LOG.trace("BRAIN - Synapses from layer {} --> {}", (i + 1), (i + 2));
            for (Neuron source : sources) {
                for (Synapse synapse : source.getOutgoingSynapse()) {
                    LOG.trace("BRAIN - {} ---{}---> {} a({})", source.getId(), synapse.getWeight(), synapse.getTarget().getId(), synapse.getTarget().activationValue());
                }
            }
        }
    }

    private void logOutputWeights(List<Double> expectedOutputs, List<Double> previousOutputs) {
        List<Neuron> sources = brain.getOutputs();
        int i = 0;
        if (previousOutputs.isEmpty()) {
            previousOutputs = expectedOutputs;
        }
        for (Neuron source : sources) {
            boolean matching = (Math.abs(expectedOutputs.get(i) - source.activationValue())) <= matchRange;
            LOG.debug("BRAIN - output {} {} with a({}), expected {} ==> Matching? {}", source.getId(), (source.activationValue() - previousOutputs.get(i)) <= 0 ? "(+)" : "(-)", source.activationValue(), expectedOutputs.get(i), matching);
            i++;
        }
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

	public boolean isWithBias() {
		return withBias;
	}

	public void setWithBias(boolean withBias) {
		this.withBias = withBias;
	}
}
