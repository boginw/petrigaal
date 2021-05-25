package petrigaal.edg.mdg;

import petrigaal.edg.Edge;

import java.util.ArrayList;

public class MetaEdge extends ArrayList<MetaTarget> implements Edge<MetaConfiguration, MetaEdge, MetaTarget> {
    private final MetaConfiguration source;

    public MetaEdge(MetaConfiguration source) {
        this.source = source;
    }

    @Override
    public MetaConfiguration getSource() {
        return source;
    }
}
