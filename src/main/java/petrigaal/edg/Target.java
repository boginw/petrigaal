package petrigaal.edg;

import petrigaal.petri.Transition;

import java.util.Objects;

public class Target {
    private final Configuration configuration;
    private final Transition transition;

    public Target(Configuration configuration, Transition transition) {
        this.configuration = configuration;
        this.transition = transition;
    }

    public Target(Configuration configuration) {
        this.configuration = configuration;
        this.transition = null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Transition getTransition() {
        return transition;
    }

    public Target withConfiguration(Configuration configuration) {
        return new Target(configuration, getTransition());
    }

    public Target copy() {
        return new Target(configuration.copy(), transition);
    }

    @Override
    public String toString() {
        return "Target{"
                + "configuration=" + configuration
                + ", transition=" + transition
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(configuration, target.configuration)
                && Objects.equals(transition, target.transition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, transition);
    }
}
