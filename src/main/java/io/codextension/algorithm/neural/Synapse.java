package io.codextension.algorithm.neural;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Synapse implements Serializable {
    private String id;
    private Double weight;
    private Neuron target;
    private transient Neuron source;

    public Synapse() {
        id = UUID.randomUUID().toString();
        weight = 0d;
    }

    public Synapse(Double weight, Neuron source, Neuron target) {
        this.weight = weight;
        this.source = source;
        this.source.getOutgoingSynapse().add(this);
        this.target = target;
        this.target.getIncomingSynapse().add(this);
        id = source.getId() + "-" + target.getId();
    }

    public String getId() {
        return id;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Neuron getTarget() {
        return target;
    }

    public void setTarget(Neuron target) {
        this.target = target;
    }

    public Neuron getSource() {
        return source;
    }

    public void setSource(Neuron source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Synapse synapse = (Synapse) o;
        return Objects.equals(id, synapse.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
