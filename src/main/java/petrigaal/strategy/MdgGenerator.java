package petrigaal.strategy;

import petrigaal.edg.dg.DGConfiguration;
import petrigaal.edg.dg.DGEdge;
import petrigaal.edg.dg.DGTarget;
import petrigaal.edg.mdg.MetaConfiguration;
import petrigaal.edg.mdg.MetaEdge;
import petrigaal.edg.mdg.MetaTarget;
import petrigaal.petri.Transition;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class MdgGenerator {
    private final Map<MetaConfiguration, MetaConfiguration> configurations = new HashMap<>();
    private final Queue<MetaConfiguration> queue = new LinkedList<>();
    private DGConfiguration root;
    private Map<DGConfiguration, Boolean> propagationByConfiguration;

    public MetaConfiguration synthesize(
            DGConfiguration root,
            Map<DGConfiguration, Boolean> propagationByConfiguration
    ) {
        this.root = root;
        this.propagationByConfiguration = propagationByConfiguration;

        MetaConfiguration c = new MetaConfiguration(Set.of(root));

        queue.add(c);
        configurations.put(c, c);

        do {
            MetaConfiguration configuration = Objects.requireNonNull(queue.poll());
            visit(new MetaTarget(configuration, null, null));
        } while (!queue.isEmpty());

        return c;
    }

    private void visit(MetaTarget target) {
        Set<Set<Closure>> successors = close(target.getConfiguration().getConfigurations());
        for (Set<Closure> closures : successors) {
            Set<DGConfiguration> configurations = getConfigurations(closures);
            Set<Transition> transitions = getTransitions(closures);

            MetaConfiguration conf = getOrCreateConf(new MetaConfiguration(configurations));
            MetaTarget metaTarget = new MetaTarget(getOrCreateConf(conf), transitions);

            MetaEdge edge = new MetaEdge(target.getConfiguration());
            edge.add(metaTarget);

            target.getConfiguration().getSuccessors().add(edge);
        }
    }

    private MetaConfiguration getOrCreateConf(MetaConfiguration config) {
        MetaConfiguration get = configurations.get(config);

        if (get != null) {
            return get;
        } else {
            configurations.put(config, config);
            queue.add(config);
            return config;
        }
    }

    private Set<DGConfiguration> getConfigurations(Set<Closure> closures) {
        return closures.stream()
                .map(Closure::target)
                .map(DGTarget::getConfiguration)
                .collect(toSet());
    }

    private Set<Transition> getTransitions(Set<Closure> closures) {
        return closures.stream()
                .map(Closure::target)
                .flatMap(t -> t.getTransitions().stream())
                .collect(toSet());
    }

    private Set<Set<Closure>> close(Set<DGConfiguration> configurations) {
        Set<Set<Closure>> successors = new HashSet<>();
        for (DGConfiguration configuration : configurations) {
            Set<Set<Closure>> configSuccessors = new HashSet<>();
            for (DGEdge edge : configuration.getSuccessors()) {
                Set<Set<Closure>> edgeSuccessors = new HashSet<>();
                if (edge.isEmpty()) {
                    edgeSuccessors = Set.of(Collections.emptySet());
                } else {
                    for (DGTarget target : edge) {
                        Set<Set<Closure>> targetSuccessors;
                        if (target.getTransitions() == null) {
                            targetSuccessors = close(Set.of(target.getConfiguration()));
                        } else {
                            targetSuccessors = Set.of(Set.of(Closure.of(configuration, target)));
                        }
                        edgeSuccessors = combine(edgeSuccessors, targetSuccessors);
                    }
                }
                configSuccessors.addAll(edgeSuccessors);
            }
            successors = combine(successors, configSuccessors);
        }

        return successors;
    }

    private Set<Set<Closure>> combine(Set<Set<Closure>> s1, Set<Set<Closure>> s2) {
        if (s1.isEmpty()) return s2;
        if (s2.isEmpty()) return s1;

        Set<Set<Closure>> combined = new HashSet<>();
        for (Set<Closure> t : s2) {
            for (Set<Closure> k : s1) {
                Set<Closure> union = new HashSet<>();
                union.addAll(t);
                union.addAll(k);
                combined.addAll(Set.of(union));
            }
        }
        return combined;
    }

    private record Closure(DGConfiguration source, DGTarget target) {
        public static Closure of(DGConfiguration source, DGTarget target) {
            return new Closure(source, target);
        }

        @Override
        public String toString() {
            return "Closure{"
                    + "source=" + source
                    + ", target=" + target
                    + '}';
        }
    }
}
