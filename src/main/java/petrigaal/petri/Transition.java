package petrigaal.petri;

import java.util.*;

public class Transition {
    private final HashSet<Arc> inputArcs;
    private final HashSet<Arc> outputArcs;

    public Transition(String name) {
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

    public static class Arc {
        private final String place;
        private final int weight;

        public Arc(String place, int weight) {
            this.place = place;
            this.weight = weight;
        }

        public String getPlace() {
            return this.place;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arc arc = (Arc) o;
            return weight == arc.weight &&
                    Objects.equals(place, arc.place);
        }
    }
}
