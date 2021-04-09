package petrigaal.edg;

import petrigaal.Configuration;
import petrigaal.petri.Transition;

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
}
