package petrigaal.edg;

import java.util.*;
import java.util.stream.Collectors;

public class Edge extends ArrayList<Target> {
    boolean isNegated;

    public Edge() {
        this(Collections.emptyList());
    }

    public Edge(boolean isNegated) {
        this(Collections.emptyList(), isNegated);
    }

    public Edge(Collection<Target> outgoing) {
        this(outgoing, false);
    }

    public Edge(Configuration... elements) {
        this(false, elements);
    }

    public Edge(Target... elements) {
        this(Arrays.asList(elements));
    }

    public Edge(boolean isNegated, Configuration... elements) {
        this(Arrays.stream(elements).map(Target::new).collect(Collectors.toList()), isNegated);
    }

    public Edge(Collection<Target> outgoing, boolean isNegated) {
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

    public Edge copy() {
        List<Target> copy = this.stream().map(Target::copy).collect(Collectors.toList());
        return new Edge(new ArrayList<>(copy), isNegated);
    }
}
