package petrigaal.edg;

import java.util.*;

public class DGEdge extends HashSet<DGTarget> implements Edge<DGConfiguration, DGEdge, DGTarget> {
    private final DGConfiguration source;
    private boolean isNegated;

    public DGEdge(DGConfiguration source) {
        this(source, Collections.emptyList());
    }

    public DGEdge(DGConfiguration source, boolean isNegated) {
        this(source, Collections.emptyList(), isNegated);
    }

    public DGEdge(DGConfiguration source, Collection<DGTarget> outgoing) {
        this(source, outgoing, false);
    }

    public DGEdge(DGConfiguration source, DGConfiguration... elements) {
        this(source, false, elements);
    }

    public DGEdge(DGConfiguration source, DGTarget... elements) {
        this(source, Arrays.asList(elements));
    }

    public DGEdge(DGConfiguration source, boolean isNegated, DGConfiguration... elements) {
        this(source, Arrays.stream(elements).map(DGTarget::new).toList(), isNegated);
    }

    public DGEdge(DGConfiguration source, Collection<DGTarget> outgoing, boolean isNegated) {
        super(outgoing);
        this.source = source;
        this.isNegated = isNegated;
    }

    @Override
    public DGConfiguration getSource() {
        return source;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public DGEdge setNegated(boolean negated) {
        isNegated = negated;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DGEdge dgTargets = (DGEdge) o;
        return isNegated == dgTargets.isNegated
                && Objects.equals(source, dgTargets.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source, isNegated);
    }
}
