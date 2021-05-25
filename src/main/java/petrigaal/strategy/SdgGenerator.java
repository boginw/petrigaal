package petrigaal.strategy;

import javafx.util.Pair;
import petrigaal.edg.dg.DGConfiguration;
import petrigaal.edg.dg.DGEdge;
import petrigaal.edg.dg.DGTarget;
import petrigaal.edg.mdg.MetaConfiguration;
import petrigaal.edg.mdg.MetaEdge;
import petrigaal.edg.mdg.MetaTarget;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class SdgGenerator {
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
                    .map(c -> c.stream().map(s -> s.target().getTransitions()).collect(toSet()))
                    .collect(toSet());

            if (transitions.stream().anyMatch(t -> t.size() > 1))
                continue;

            var uncontrollableClosureByTransition = uncontrollable.stream()
                    .collect(groupingBy(
                            c -> new Pair<>(c.target().getTransitions(), c.source().getGame()), toSet()
                    ));

            for (var uncontrollableClosures : uncontrollableClosureByTransition.entrySet()) {
                Set<DGConfiguration> configurations = getConfigurations(uncontrollableClosures.getValue());
                MetaConfiguration conf = getOrCreateConf(new MetaConfiguration(configurations));
                MetaEdge edge = new MetaEdge(target.configuration);
                edge.add(new MetaTarget(
                        conf,
                        uncontrollableClosures.getKey().getKey(),
                        uncontrollableClosures.getKey().getValue()
                ));
                target.configuration.successors.add(edge);
            }

            if (!controllable.isEmpty()) {
                Map<PetriGame, Set<Closure>> groups = controllable.stream()
                        .collect(groupingBy(c -> c.source().getGame(), toSet()));

                for (Map.Entry<PetriGame, Set<Closure>> entry : groups.entrySet()) {
                    Set<DGConfiguration> configurations = getConfigurations(entry.getValue());
                    MetaConfiguration conf = getOrCreateConf(new MetaConfiguration(configurations));
                    MetaEdge edge = new MetaEdge(target.configuration);
                    edge.add(new MetaTarget(
                            conf,
                            entry.getValue().iterator().next().target().getTransitions(),
                            entry.getKey()
                    ));
                    target.configuration.successors.add(edge);
                }
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
                        if (target.getTransitions() == null) {
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
        return isControllable(closure.target().getTransitions());
    }

    private boolean isControllable(Transition transition) {
        return root.getGame().getTransitions(Player.Controller).contains(transition);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Closure closure = (Closure) o;
            return Objects.equals(source, closure.source)
                    && Objects.equals(target, closure.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }
}
