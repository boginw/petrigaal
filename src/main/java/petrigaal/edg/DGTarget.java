package petrigaal.edg;

import petrigaal.petri.Transition;

import java.util.Objects;

public class DGTarget implements Target<DGConfiguration, DGEdge, DGTarget> {
    private final DGConfiguration configuration;
    private final Transition transition;

    public DGTarget(DGConfiguration configuration, Transition transition) {
        this.configuration = configuration;
        this.transition = transition;
    }

    public DGTarget(DGConfiguration configuration) {
        this.configuration = configuration;
        this.transition = null;
    }

    @Override
    public DGConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Transition getTransition() {
        return transition;
    }

    public DGTarget withConfiguration(DGConfiguration configuration) {
        return new DGTarget(configuration, getTransition());
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
        DGTarget target = (DGTarget) o;
        return Objects.equals(configuration, target.configuration)
                && Objects.equals(transition, target.transition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, transition);
    }
}
