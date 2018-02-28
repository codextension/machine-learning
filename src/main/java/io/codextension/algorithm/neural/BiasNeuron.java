package io.codextension.algorithm.neural;

import java.util.ArrayList;
import java.util.List;

public class BiasNeuron extends Neuron {

    public BiasNeuron(String id) {
        super(id);
        value = 1.0d;
    }

    @Override
    public List<Synapse> getIncomingSynapse() {
        return new ArrayList<>(); // a bias has no incoming synapse
    }

    public Double activationValue() {
        return getValue();
    }

    @Override
    public Double activationPrimeValue() {
        return getValue();
    }

    @Override
    public void setIncomingSynapse(List<Synapse> incomingSynapse) {
        // Do nothing
    }

    @Override
    public void setValue(Double value) {
        // Do nothing
    }
}
