package petrigaal.strategy;

import petrigaal.edg.dg.DGConfiguration;
import petrigaal.edg.dg.DGEdge;
import petrigaal.edg.dg.DGTarget;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class TopDownStrategySynthesiser implements StrategySynthesiser<TopDownStrategySynthesiser.SynthesisState> {
    private final Set<ConfigurationSetStateLink> visited = new HashSet<>();
    private final Deque<ConfigurationSetStateLink> waiting = new LinkedList<>();
    private final Set<ConfigurationSetStateLink> deadPairs = new HashSet<>();
    private DGConfiguration root;
    private Map<DGConfiguration, Boolean> propagationByConfiguration;
    private Consumer<SynthesisState> consumer;
    private PetriGame game;
    private int counter = 0;
    private AutomataStrategy strategy;

    @Override
    public AutomataStrategy synthesize(
            DGConfiguration root,
            Map<DGConfiguration, Boolean> propagationByConfiguration,
            Consumer<SynthesisState> consumer
    ) {
        this.root = root;
        this.game = root.getGame();
        this.propagationByConfiguration = propagationByConfiguration;
        this.consumer = consumer;
        this.strategy = new AutomataStrategy();

        visited.clear();
        waiting.clear();
        deadPairs.clear();

        ConfigurationSetStateLink initialPair = ConfigurationSetStateLink.of(
                Collections.emptySet(),
                Set.of(root),
                strategy.getInitialState()
        );
        waiting.add(initialPair);

        System.out.println();
        while (!waiting.isEmpty()) {
            ConfigurationSetStateLink pair = waiting.pollLast();
            if (pair == null) continue;
            Set<Set<Closure>> successors = close(pair.getConfigurations());
            emit(successors);
            boolean dead = true;
            if (successors.contains(Collections.emptySet())) {
                strategy.addFinalState(pair.getState());
                continue;
            }
            for (Set<Closure> closures : successors) {
                if (propagatesZero(closures)) continue;

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

                Set<Set<Closure>> uncontrollableClosureByTransition = new HashSet<>(
                        uncontrollable.stream()
                                .collect(groupingBy(c -> c.target().getTransitions(), toSet()))
                                .values()
                );

                for (Set<Closure> uncontrollableClosures : uncontrollableClosureByTransition) {
                    ConfigurationSetStateLink newPair = getOrCreatePair(pair, uncontrollableClosures, pair::getState);
                    enqueuePair(newPair);
                    if (!deadPairs.contains(newPair)) dead = false;
                    if (controllable.isEmpty() && closures.stream().allMatch(this::controllableHasNotEnabledTransitions)) {
                        strategy.addTransition(
                                pair.getState(),
                                uncontrollableClosures.iterator().next().source().getGame(),
                                null,
                                newPair.getState()
                        );
                    }
                }

                if (!controllable.isEmpty()) {
                    Map<PetriGame, Set<Closure>> groups = controllable.stream()
                            .collect(groupingBy(c -> c.source().getGame(), toSet()));

                    for (Map.Entry<PetriGame, Set<Closure>> entry : groups.entrySet()) {
                        ConfigurationSetStateLink newPair = getOrCreatePair(pair, entry.getValue());
                        enqueuePair(newPair);
                        if (!deadPairs.contains(newPair)) dead = false;
                        strategy.addTransition(
                                pair.getState(),
                                entry.getKey(),
                                entry.getValue().iterator().next().target().getTransitions(),
                                newPair.getState()
                        );
                    }
                }
            }
            if (dead) {
                System.out.println("DEAD: " + pair);
                if (pair.equals(initialPair)) {
                    strategy = new AutomataStrategy();
                    break;
                }
                deadPairs.add(pair);
                strategy.removeState(pair.getState());
                for (ConfigurationSetStateLink parent : pair.getParents()) {
                    visited.remove(parent);
                    waiting.add(parent);
                }
            }
        }

        strategy.removeEverythingThatIsNotConnectedTo(initialPair.getState());

        if (consumer != null)
            consumer.accept(new SynthesisState(strategy, root, propagationByConfiguration, Set.of()));
        return strategy;
    }

    private void emit(Set<Set<Closure>> successors) {
        if (consumer == null) return;
        SynthesisState state = new SynthesisState(
                strategy.copy(),
                root,
                propagationByConfiguration,
                successors.stream().map(
                        s -> s.stream().map(c -> c.target().getConfiguration()).collect(Collectors.toSet())
                ).collect(Collectors.toSet())
        );
        consumer.accept(state);
    }

    private void enqueuePair(ConfigurationSetStateLink newPair) {
        if (!visited.contains(newPair)) {
            if (!waiting.contains(newPair)) {
                waiting.add(newPair);
            }
            visited.add(newPair);
        }
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

    private boolean controllableHasNotEnabledTransitions(Closure c) {
        return c.source().getGame().getEnabledTransitions(Player.Controller).isEmpty();
    }

    private boolean propagatesZero(Set<Closure> closures) {
        for (Closure closure : closures) {
            if (!propagationByConfiguration.get(closure.target().getConfiguration())) {
                return true;
            }
        }
        return false;
    }

    private ConfigurationSetStateLink getOrCreatePair(
            ConfigurationSetStateLink previous,
            Set<Closure> closures
    ) {
        return getOrCreatePair(previous, closures, () -> new AutomataState("state" + counter++));
    }

    private ConfigurationSetStateLink getOrCreatePair(
            ConfigurationSetStateLink previous,
            Set<Closure> closures,
            Supplier<AutomataState> supplier
    ) {
        Set<DGConfiguration> configurations = getConfigurations(closures);
        ConfigurationSetStateLink configurationSetStateLink = visited.stream()
                .filter(c -> c.getConfigurations().equals(configurations))
                .findAny()
                .orElseGet(() -> ConfigurationSetStateLink.of(
                        Set.of(previous),
                        configurations,
                        supplier.get()
                ));
        configurationSetStateLink.getParents().add(previous);
        return configurationSetStateLink;
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
        return game.getTransitions(Player.Controller).contains(transition);
    }

    private Set<Set<Closure>> combine(Set<Set<Closure>> s1, Set<Set<Closure>> s2) {
        if (s1.isEmpty()) {
            return s2;
        } else if (s2.isEmpty()) {
            return s1;
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

    private static class ConfigurationSetStateLink {
        private final Set<ConfigurationSetStateLink> parents;
        private final Set<DGConfiguration> configuration;
        private final AutomataState state;

        private ConfigurationSetStateLink(
                Set<ConfigurationSetStateLink> parents,
                Set<DGConfiguration> configuration,
                AutomataState state
        ) {
            this.parents = new HashSet<>(parents);
            this.configuration = configuration;
            this.state = state;
        }

        public static ConfigurationSetStateLink of(
                Set<ConfigurationSetStateLink> parents,
                Set<DGConfiguration> configuration,
                AutomataState automataStrategy
        ) {
            return new ConfigurationSetStateLink(parents, configuration, automataStrategy);
        }

        public Set<DGConfiguration> getConfigurations() {
            return configuration;
        }

        public AutomataState getState() {
            return state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigurationSetStateLink that = (ConfigurationSetStateLink) o;
            return Objects.equals(configuration, that.configuration)
                    && Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configuration, state);
        }

        @Override
        public String toString() {
            return "ConfigurationSetStatePair{"
                    + "configuration=" + configuration
                    + ", state=" + state
                    + '}';
        }

        public Set<ConfigurationSetStateLink> getParents() {
            return parents;
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

    public static record SynthesisState(
            AutomataStrategy strategy,
            DGConfiguration root,
            Map<DGConfiguration, Boolean> propagationByConfiguration,
            Set<Set<DGConfiguration>> close
    ) {
    }
}
