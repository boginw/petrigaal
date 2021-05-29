package petrigaal.draw;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.List;
import java.util.Map;

public class DGCytoscapeVisualizer<C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> extends DGVisualizer<C, E, T> {

    private DGCytoscapeVisualizer(C configuration, Map<C, Boolean> propagationByConfiguration) {
        super(configuration, propagationByConfiguration);
    }

    public static RootBuilder builder() {
        return new RootBuilder();
    }

    @Override
    protected String declareDependencyGraph(String vertices, String edges, String ranks) {
        return "{\"nodes\": [%s], \"edges\": [%s], \"ranks\": [%s]}".formatted(vertices, edges, ranks);
    }

    @Override
    protected String getVertexDelimiter() {
        return ",";
    }

    @Override
    protected String getEdgeDelimiter() {
        return ",";
    }

    @Override
    protected String getRankDelimiter() {
        return ",";
    }

    @Override
    protected String declareRank(List<String> nodeIds) {
        return '[' + String.join(",", nodeIds.stream().map(id -> '"' + id + '"').toList()) + ']';
    }

    @Override
    protected String declareConfiguration(C conf) {
        return "{\"data\": {\"id\": \"%d\", \"name\": \"%s\", \"propagates\": %s}}".formatted(
                conf.hashCode(),
                conf.toString(),
                propagationByConfiguration.getOrDefault(conf, false)
        );
    }

    @Override
    protected String declareJoint(int jointId) {
        return "{\"data\": {\"id\": \"joint%d\", \"joint\": true}}".formatted(jointId);
    }

    @Override
    protected String declareEmpty(int emptyId) {
        return "{\"data\": {\"id\": \"empty%d\", \"empty\": true}}".formatted(emptyId);
    }

    @Override
    protected String declareConfigurationToJointEdge(C conf, int jointId) {
        return "{\"data\": { \"source\": \"%d\", \"target\": \"joint%d\"}}".formatted(conf.hashCode(), jointId);
    }

    @Override
    protected String declareJointToTargetEdge(int jointId, T target) {
        return "{\"data\": {\"source\": \"joint%d\", \"target\": \"%d\", \"label\": \"%s\"}}".formatted(
                jointId,
                target.getConfiguration().hashCode(),
                getLabel(target)
        );
    }

    @Override
    protected String declareJointToEmptyEdge(int jointId, int emptyId) {
        return "{\"data\": {\"source\": \"joint%d\", \"target\": \"empty%d\"}}".formatted(
                jointId,
                emptyId
        );
    }

    public static class RootBuilder {
        public <C extends Configuration<C, E, T>, E extends Edge<C, E, T>, T extends Target<C, E, T>>
        Builder<C, E, T> forConfiguration(C configuration) {
            return new Builder<>(configuration);
        }
    }

    public static class Builder<C extends Configuration<C, E, T>, E extends Edge<C, E, T>, T extends Target<C, E, T>> {
        private final C configuration;
        private Map<C, Boolean> propagationByConfiguration = Map.of();
        private boolean displayOnlyConfigurationsWhichPropagateOne = false;

        public Builder(C configuration) {
            this.configuration = configuration;
        }

        public Builder<C, E, T> withPropagationMapping(Map<C, Boolean> propagationByConfiguration) {
            this.propagationByConfiguration = propagationByConfiguration;
            return this;
        }

        public Builder<C, E, T> withOnlyDisplayingPropagationOfOneBeing(
                boolean displayOnlyConfigurationsWhichPropagateOne
        ) {
            this.displayOnlyConfigurationsWhichPropagateOne = displayOnlyConfigurationsWhichPropagateOne;
            return this;
        }

        public String build() {
            var visualizer = new DGCytoscapeVisualizer<>(configuration, propagationByConfiguration);
            visualizer.displayOnlyConfigurationsWhichPropagateOne = displayOnlyConfigurationsWhichPropagateOne;
            return visualizer.draw();
        }
    }
}
