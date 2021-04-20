package petrigaal.strategy;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;
import petrigaal.strategy.AutomataStrategy.AutomataState;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class TopDownStrategySynthesiser implements StrategySynthesiser {
    private final Set<ConfigurationSetStateLink> visited = new HashSet<>();
    private final Deque<ConfigurationSetStateLink> waiting = new LinkedList<>();
    private final Set<ConfigurationSetStateLink> deadPairs = new HashSet<>();
    private Map<Configuration, Boolean> propagationByConfiguration;
    private Consumer<AutomataStrategy> consumer;
    private PetriGame game;
    private int counter = 0;
    private AutomataStrategy strategy;

    @Override
    public void synthesize(
            PetriGame game,
            Configuration root,
            Map<Configuration, Boolean> propagationByConfiguration,
            Consumer<AutomataStrategy> consumer
    ) {
        this.game = game;
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
            boolean dead = true;
            if (successors.contains(Collections.emptySet())) {
                strategy.addTransition(
                        pair.getState(),
                        null,
                        null,
                        pair.getState()
                );
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

                Set<Transition> transitions = controllable.stream()
                        .map(Closure::getTarget)
                        .map(Target::getTransition)
                        .collect(toSet());

                if (transitions.size() > 1) continue;

                if (controllable.isEmpty() && closures.stream().allMatch(this::controllableHasNotEnabledTransitions)) {
                    for (Closure closure : uncontrollable) {
                        ConfigurationSetStateLink newPair = getOrCreatePair(pair, Set.of(closure), pair::getState);
                        enqueuePair(newPair);
                        if (!deadPairs.contains(newPair)) dead = false;
                        strategy.addTransition(
                                pair.getState(),
                                closure.getSource().getGame(),
                                null,
                                newPair.getState()
                        );
                    }
                } else {
                    Set<Set<Closure>> uncontrollableClosureByTransition = new HashSet<>(
                            uncontrollable.stream()
                                    .collect(groupingBy(c -> c.getTarget().getTransition(), toSet()))
                                    .values()
                    );
                    for (Set<Closure> uncontrollableClosures : uncontrollableClosureByTransition) {
                        ConfigurationSetStateLink newPair = getOrCreatePair(pair, uncontrollableClosures, pair::getState);
                        enqueuePair(newPair);
                        if (!deadPairs.contains(newPair)) dead = false;
                    }

                    if (!controllable.isEmpty()) {
                        ConfigurationSetStateLink newPair = getOrCreatePair(pair, controllable);
                        enqueuePair(newPair);
                        if (!deadPairs.contains(newPair)) dead = false;
                        strategy.addTransition(
                                pair.getState(),
                                controllable.iterator().next().getSource().getGame(),
                                controllable.iterator().next().getTarget().getTransition(),
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

        consumer.accept(strategy);
    }

    private void enqueuePair(ConfigurationSetStateLink newPair) {
        if (!visited.contains(newPair)) {
            if (!waiting.contains(newPair)) {
                waiting.add(newPair);
            }
            visited.add(newPair);
        }
    }

    private Set<Set<Closure>> close(Set<Configuration> configurations) {
        Set<Set<Closure>> successors = new HashSet<>();
        for (Configuration configuration : configurations) {
            Set<Set<Closure>> success = new HashSet<>();
            for (Edge edge : configuration.getSuccessors()) {
                Set<Set<Closure>> succs = new HashSet<>();
                if (edge.isEmpty()) {
                    succs = Set.of(Collections.emptySet());
                } else {
                    for (Target target : edge) {
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

    private boolean controllableHasNotEnabledTransitions(Closure c) {
        return c.getSource().getGame().getEnabledTransitions(Player.Controller).isEmpty();
    }

    private boolean propagatesZero(Set<Closure> closures) {
        for (Closure closure : closures) {
            if (!propagationByConfiguration.get(closure.getTarget().getConfiguration())) {
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
        Set<Configuration> configurations = getConfigurations(closures);
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

    private Set<Configuration> getConfigurations(Set<Closure> closures) {
        return closures.stream()
                .map(Closure::getTarget)
                .map(Target::getConfiguration)
                .collect(toSet());
    }

    private boolean isControllable(Closure closure) {
        return isControllable(closure.getTarget().getTransition());
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
        private final Set<Configuration> configuration;
        private final AutomataState state;

        private ConfigurationSetStateLink(
                Set<ConfigurationSetStateLink> parents,
                Set<Configuration> configuration,
                AutomataState state
        ) {
            this.parents = new HashSet<>(parents);
            this.configuration = configuration;
            this.state = state;
        }

        public static ConfigurationSetStateLink of(
                Set<ConfigurationSetStateLink> parents,
                Set<Configuration> configuration,
                AutomataState automataStrategy
        ) {
            return new ConfigurationSetStateLink(parents, configuration, automataStrategy);
        }

        public Set<Configuration> getConfigurations() {
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

    private static class Closure {
        private final Configuration source;
        private final Target target;

        private Closure(Configuration source, Target target) {
            this.source = source;
            this.target = target;
        }

        public static Closure of(Configuration source, Target target) {
            return new Closure(source, target);
        }

        public Configuration getSource() {
            return source;
        }

        public Target getTarget() {
            return target;
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
