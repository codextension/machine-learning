package io.codextension.algorithm.neural;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TrainingDataHolder {
    private Map<String, Double> weights;
    private Map<String, Double> values;
    private LinkedList<Double> expectedOutputs;
    private List<Double> previousOutputs; // for debugging purposes

    public TrainingDataHolder() {
        weights = new HashMap<>();
        values = new HashMap<>();
        previousOutputs = new LinkedList<>();
        expectedOutputs = new LinkedList<>();
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Double> weights) {
        this.weights = weights;
    }

    public List<Double> getPreviousOutputs() {
        return previousOutputs;
    }

    public void setPreviousOutputs(List<Double> previousOutputs) {
        this.previousOutputs = previousOutputs;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
    }

    public LinkedList<Double> getExpectedOutputs() {
        return expectedOutputs;
    }

    public void setExpectedOutputs(LinkedList<Double> expectedOutputs) {
        this.expectedOutputs = expectedOutputs;
    }

    public void addInputs(List<Double> inputs) {
        int i = 1;
        for (Double input : inputs) {
            values.put("N1." + i++, input);
        }
    }
}
