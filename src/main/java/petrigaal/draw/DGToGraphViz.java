package petrigaal.draw;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class DGToGraphViz<C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>>
        extends DGVisualizer<C, E, T> {

    public DGToGraphViz(C configuration, Map<C, Boolean> propagationByConfiguration) {
        super(configuration, propagationByConfiguration);
    }

    public void setDisplayOnlyConfigurationsWhichPropagateOne(boolean displayOnlyConfigurationsWhichPropagateOne) {
        this.displayOnlyConfigurationsWhichPropagateOne = displayOnlyConfigurationsWhichPropagateOne;
    }

    @Override
    protected String declareDependencyGraph(String vertices, String edges, String ranks) {
        return "digraph G {\n" +
                "graph [pad=\"2\", nodesep=\"2\", ranksep=\"1\", rankdir=\"TB\", splines=ortho];\n" +
                "node [shape=box]\n" +
                vertices + "\n" + edges + "\n" + ranks + "\n" +
                "}";
    }

    @Override
    protected String getVertexDelimiter() {
        return "\n";
    }

    @Override
    protected String getEdgeDelimiter() {
        return "\n";
    }

    @Override
    protected String getRankDelimiter() {
        return "\n";
    }

    @Override
    protected String declareRank(List<String> nodeIds) {
        StringJoiner joiner = new StringJoiner("; ", "{rank=same; ", "; }");

        for (String id : nodeIds) {
            joiner.add(id);
        }

        return joiner.toString();
    }

    @Override
    protected String declareConfiguration(C conf) {
        String suffix = "";
        if (propagationByConfiguration.getOrDefault(conf, false)) {
            suffix = ", color=green, penwidth=5";
        }

        return "\"%d\" [label=\"%s\" %s]".formatted(conf.hashCode(), conf, suffix);
    }

    @Override
    protected String declareJoint(int jointId) {
        return "joint" + jointId + " [shape=\"none\", label=\"\", width=0, height=0]";
    }

    @Override
    protected String declareEmpty(int emptyId) {
        return "empty" + emptyId + " [shape=\"none\", label=\"Ø\"]";
    }

    @Override
    protected String declareConfigurationToJointEdge(C conf, int jointId) {
        return "\"%s\" -> joint%d [arrowhead=\"none\"]".formatted(conf.hashCode(), jointId);
    }

    @Override
    protected String declareJointToTargetEdge(int jointId, T target) {
        // Insert negation logic here
        String suffix = "";
        return "joint%d -> \"%d\"  [xlabel=\"%s\" %s]".formatted(
                jointId,
                target.getConfiguration().hashCode(),
                getLabel(target),
                suffix
        );
    }

    @Override
    protected String declareJointToEmptyEdge(int jointId, int emptyId) {
        return "joint" + jointId + " -> empty" + emptyId;
    }
}
