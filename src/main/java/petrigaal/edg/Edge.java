package petrigaal.edg;

import petrigaal.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Edge extends ArrayList<Configuration> {
    boolean isNegated;

    public Edge() {
        this(Collections.emptyList());
    }

    public Edge(boolean isNegated) {
        this(Collections.emptyList(), isNegated);
    }

    public Edge(Collection<Configuration> outgoing) {
        this(outgoing, false);
    }

    public Edge(Configuration... elements) {
        this(Arrays.asList(elements));
    }

    public Edge(boolean isNegated, Configuration... elements) {
        this(Arrays.asList(elements), isNegated);
    }

    public Edge(Collection<Configuration> outgoing, boolean isNegated) {
        super(outgoing);
        this.isNegated = isNegated;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public Edge setNegated(boolean negated) {
        isNegated = negated;
        return this;
    }
}
