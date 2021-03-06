package petrigaal.strategy;

import petrigaal.edg.*;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class MetaDGGenerator {
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
            if (propagatesZero(closures)) continue;
            if (closures.isEmpty()) {
                target.configuration().getSuccessors().add(new MetaEdge(target.configuration()));
                continue;
            }

            /*MetaConfiguration conf = getOrCreateConf(new MetaConfiguration(getConfigurations(closures)));
            MetaEdge edge = new MetaEdge(target.configuration);
            edge.add(new MetaTarget(
                    conf,
                    null,
                    null
            ));
            target.configuration.successors.add(edge);*/

            Set<Closure> controllable = new HashSet<>();
            Set<Closure> uncontrollable = new HashSet<>();

            for (Closure closure : closures) {
                if (isControllable(closure)) {
                    controllable.add(closure);
                } else {
                    uncontrollable.add(closure);
                }
            }

            Set<Set<Transition>> transitions = controllable.stream()
                    .collect(groupingBy(c -> c.source().getGame(), toSet()))
                    .values()
                    .stream()
                    .map(c -> c.stream().map(s -> s.target().getTransition()).collect(toSet()))
                    .collect(toSet());

            if (transitions.stream().anyMatch(t -> t.size() > 1))
                continue;

            var uncontrollableClosureByTransition = uncontrollable.stream()
                    .collect(groupingBy(c -> new Label(c.source().getGame(), c.target().getTransition()), toSet()));
            var controllableClosureByTransition = controllable.stream()
                    .collect(groupingBy(c -> new Label(c.source().getGame(), c.target().getTransition()), toSet()));
            var combined = new HashSet<>(uncontrollableClosureByTransition.entrySet());
            combined.addAll(controllableClosureByTransition.entrySet());

            for (var combinedClosures : combined) {
                Set<DGConfiguration> configurations = getConfigurations(combinedClosures.getValue());
                MetaConfiguration conf = getOrCreateConf(new MetaConfiguration(configurations));
                MetaEdge edge = new MetaEdge(target.configuration);
                edge.add(new MetaTarget(
                        conf,
                        combinedClosures.getKey().transition(),
                        combinedClosures.getKey().game()
                ));
                target.configuration.successors.add(edge);
            }
        }
    }

    private boolean propagatesZero(Set<Closure> closures) {
        for (Closure closure : closures) {
            if (!propagationByConfiguration.get(closure.target().getConfiguration())) {
                return true;
            }
        }
        return false;
    }

    private Set<Set<Closure>> close(Set<DGConfiguration> configurations) {
        Set<Set<Closure>> successors = new HashSet<>();
        for (DGConfiguration configuration : configurations) {
            Set<Set<Closure>> success = new HashSet<>();
            for (DGEdge edge : configuration.getSuccessors()) {
                Set<Set<Closure>> succs = new HashSet<>();
                if (edge.isEmpty()) {
                    succs = Set.of(Collections.emptySet());
                } else {
                    for (DGTarget target : edge) {
                        Set<Set<Closure>> succ;
                        if (target.getTransition() == null) {
                            succ = close(Set.of(target.getConfiguration()));
                        } else {
                            succ = Set.of(Set.of(Closure.of(configuration, target)));
                        }

                        succs = combine(succs, succ);
                    }
                }
                success.addAll(succs);
            }
            successors = combine(successors, success);
        }

        return successors;
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

    private boolean isControllable(Closure closure) {
        return isControllable(closure.target().getTransition());
    }

    private boolean isControllable(Transition transition) {
        return root.getGame().getTransitions(Player.Controller).contains(transition);
    }

    private Set<Set<Closure>> combine(Set<Set<Closure>> s1, Set<Set<Closure>> s2) {
        if (s1.isEmpty()) {
            return s2;
        }
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

    public static class MetaConfiguration implements Configuration<MetaConfiguration, MetaEdge, MetaTarget> {
        private final Set<MetaEdge> successors = new HashSet<>();
        private final Set<DGConfiguration> configurations;

        public MetaConfiguration(Set<DGConfiguration> configurations) {
            this.configurations = configurations;
        }

        public Set<DGConfiguration> getConfigurations() {
            return configurations;
        }

        @Override
        public Set<MetaEdge> getSuccessors() {
            return successors;
        }

        @Override
        public String toString() {
            return "{" + configurations + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MetaConfiguration that = (MetaConfiguration) o;
            return Objects.equals(configurations, that.configurations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configurations);
        }
    }

    public static class MetaEdge extends HashSet<MetaTarget> implements Edge<MetaConfiguration, MetaEdge, MetaTarget> {
        private final MetaConfiguration source;

        public MetaEdge(MetaConfiguration source) {
            this.source = source;
        }

        @Override
        public MetaConfiguration getSource() {
            return source;
        }
    }

    public static record MetaTarget(
            MetaConfiguration configuration,
            Transition transition,
            PetriGame game
    ) implements Target<MetaConfiguration, MetaEdge, MetaTarget> {
        @Override
        public MetaConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        public Transition getTransition() {
            return transition;
        }

        @Override
        public PetriGame getGame() {
            return game;
        }
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

    private record Label(PetriGame game, Transition transition) {
    }
}
