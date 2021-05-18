package petrigaal.petri;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Transition {
    private final HashSet<Arc> inputArcs;
    private final HashSet<Arc> outputArcs;
    private final String name;

    public Transition(String name) {
        this.name = name;
        inputArcs = new HashSet<>();
        outputArcs = new HashSet<>();
    }

    public Transition addInput(String p, int weight) {
        inputArcs.add(new Arc(p, weight));
        return this;
    }

    public Transition addOutput(String p, int weight) {
        outputArcs.add(new Arc(p, weight));
        return this;
    }

    public Transition addInput(String p) {
        inputArcs.add(new Arc(p, 1));
        return this;
    }

    public Transition addOutput(String p) {
        outputArcs.add(new Arc(p, 1));
        return this;
    }

    public Set<Arc> getInputsArcs() {
        return inputArcs;
    }

    public Set<Arc> getOutputArcs() {
        return outputArcs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return Objects.equals(inputArcs, that.inputArcs) &&
                Objects.equals(outputArcs, that.outputArcs) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputArcs, outputArcs, name);
    }

    @Override
    public String toString() {
        return name;
    }

    public record Arc(String place, int weight) {
    }
}
