package io.codextension.algorithm.neural;

import java.io.Serializable;
import java.util.*;

public class Neuron implements Serializable {

    private transient List<Synapse> incomingSynapse;
    private List<Synapse> outgoingSynapse;
    protected Double value;
    private String id;

    public Neuron() {
        incomingSynapse = new LinkedList<>();
        outgoingSynapse = new LinkedList<>();
        value = 0d;
        id = UUID.randomUUID().toString();
    }

    public Neuron(String id) {
        this();
        this.id = id;
    }

    public Double activationValue() {
        if (this.getIncomingSynapse().size() > 0) {
            return BrainUtils.sigmoid(value);
        } else {
            return value;
        }
    }

    public Double activationPrimeValue() {
        if (this.getIncomingSynapse().size() > 0) {
        return BrainUtils.derivativeSigmoid(value);
        } else {
            return value;
        }
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public List<Synapse> getIncomingSynapse() {
        if (incomingSynapse == null) {
            this.incomingSynapse = new ArrayList<>();
        }
        return incomingSynapse;
    }

    public void setIncomingSynapse(List<Synapse> incomingSynapse) {
        this.incomingSynapse = incomingSynapse;
    }

    public List<Synapse> getOutgoingSynapse() {
        return outgoingSynapse;
    }

    public void setOutgoingSynapse(List<Synapse> outgoingSynapse) {
        this.outgoingSynapse = outgoingSynapse;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neuron neuron = (Neuron) o;
        return Objects.equals(id, neuron.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
