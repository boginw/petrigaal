package petrigaal.edg;

import java.util.*;
import java.util.stream.Collectors;

public class Edge extends ArrayList<Target> {
    private final Configuration source;
    private boolean isNegated;

    public Edge(Configuration source) {
        this(source, Collections.emptyList());
    }

    public Edge(Configuration source, boolean isNegated) {
        this(source, Collections.emptyList(), isNegated);
    }

    public Edge(Configuration source, Collection<Target> outgoing) {
        this(source, outgoing, false);
    }

    public Edge(Configuration source, Configuration... elements) {
        this(source, false, elements);
    }

    public Edge(Configuration source, Target... elements) {
        this(source, Arrays.asList(elements));
    }

    public Edge(Configuration source, boolean isNegated, Configuration... elements) {
        this(source, Arrays.stream(elements).map(Target::new).toList(), isNegated);
    }

    public Edge(Configuration source, Collection<Target> outgoing, boolean isNegated) {
        super(outgoing);
        this.source = source;
        this.isNegated = isNegated;
    }

    public Configuration getSource() {
        return source;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public Edge setNegated(boolean negated) {
        isNegated = negated;
        return this;
    }

    public Edge copy() {
        List<Target> copy = this.stream().map(Target::copy).toList();
        return new Edge(source, new ArrayList<>(copy), isNegated);
    }
}
