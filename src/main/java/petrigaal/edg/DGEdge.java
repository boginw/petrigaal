package petrigaal.edg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DGEdge extends ArrayList<DGTarget> implements Edge<DGConfiguration, DGEdge, DGTarget> {
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
}
