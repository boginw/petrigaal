package petrigaal.strategy;

import petrigaal.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;
import petrigaal.strategy.AutomataStrategy.AutomataState;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TopDownStrategySynthesiser implements StrategySynthesiser {
    private final Set<ConfigurationSetStatePair> visited = new HashSet<>();
    private final Queue<ConfigurationSetStatePair> waiting = new LinkedList<>();
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
        this.strategy = new AutomataStrategy(game);

        visited.clear();
        waiting.clear();

        ConfigurationSetStatePair initialPair = ConfigurationSetStatePair.of(Set.of(root), strategy.getInitialState());
        waiting.add(initialPair);

        while (!waiting.isEmpty()) {
            ConfigurationSetStatePair pair = waiting.poll();
            Set<Set<Closure>> successors = close(pair.getConfigurations());
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
                        .collect(Collectors.toSet());

                if (transitions.size() > 1) {
                    continue;
                }

                //drawSet(closures);

                if (controllable.isEmpty()) {
                    for (Closure closure : uncontrollable) {
                        Optional<ConfigurationSetStatePair> foundPair = getPreviouslyVisitedPair(Set.of(closure));
                        AutomataState state;
                        if (foundPair.isEmpty()) {
                            state = new AutomataState("state" + counter++);
                            ConfigurationSetStatePair newPair = ConfigurationSetStatePair.of(
                                    getConfigurations(Set.of(closure)),
                                    state
                            );
                            visited.add(newPair);
                            waiting.add(newPair);
                        } else {
                            state = foundPair.get().getState();
                        }

                        strategy.addTransition(pair.getState(), closure.getSource().getGame(), null, state);
                    }
                } else {
                    for (Closure closure : uncontrollable) {
                        Optional<ConfigurationSetStatePair> foundPair = getPreviouslyVisitedPair(Set.of(closure));
                        if (foundPair.isEmpty()) {
                            ConfigurationSetStatePair newPair = ConfigurationSetStatePair.of(
                                    getConfigurations(Set.of(closure)),
                                    pair.getState()
                            );
                            waiting.add(newPair);
                        }
                    }

                    Optional<ConfigurationSetStatePair> foundPair = getPreviouslyVisitedPair(controllable);
                    AutomataState state;
                    if (foundPair.isEmpty()) {
                        state = new AutomataState("state" + counter++);
                        ConfigurationSetStatePair newPair = ConfigurationSetStatePair.of(
                                getConfigurations(controllable),
                                state
                        );
                        visited.add(newPair);
                        waiting.add(newPair);
                    } else {
                        state = foundPair.get().getState();
                    }
                    strategy.addTransition(
                            pair.getState(),
                            closures.iterator().next().getSource().getGame(),
                            closures.iterator().next().getTarget().getTransition(),
                            state
                    );
                }
            }
        }
        consumer.accept(strategy);
    }

    private void drawSet(Set<Closure> successors) {
        //Map<Configuration, Boolean> configurationsInSet = new HashMap<>();
        //successors.forEach(c -> configurationsInSet.put(c.getTarget().getConfiguration(), true));
        consumer.accept(strategy);
    }

    private boolean propagatesZero(Set<Closure> closures) {
        for (Closure closure : closures) {
            if (!propagationByConfiguration.get(closure.getTarget().getConfiguration())) {
                return true;
            }
        }
        return false;
    }

    private Optional<ConfigurationSetStatePair> getPreviouslyVisitedPair(Set<Closure> closures) {
        Set<Configuration> configurations = getConfigurations(closures);
        return visited.stream().filter(c -> c.getConfigurations().equals(configurations)).findAny();
    }

    private Set<Configuration> getConfigurations(Set<Closure> closures) {
        return closures.stream()
                .map(Closure::getTarget)
                .map(Target::getConfiguration)
                .collect(Collectors.toSet());
    }

    private boolean isControllable(Closure closure) {
        return game.getTransitions(Player.Controller).contains(closure.getTarget().getTransition());
    }

    private Set<Set<Closure>> close(Set<Configuration> configurations) {
        Set<Set<Closure>> successors = new HashSet<>();
        for (Configuration configuration : configurations) {
            Set<Set<Closure>> success = new HashSet<>();
            for (Edge edge : configuration.getSuccessors()) {
                Set<Set<Closure>> succs = new HashSet<>();
                for (Target target : edge) {
                    Set<Set<Closure>> succ;
                    if (target.getTransition() == null) {
                        succ = close(Set.of(target.getConfiguration()));
                    } else {
                        succ = Set.of(Set.of(Closure.of(configuration, target)));
                    }

                    succs = combine(succs, succ);
                }
                success.addAll(succs);
            }
            successors = combine(successors, success);
        }

        return successors;
    }

    private Set<Set<Closure>> combine(Set<Set<Closure>> s1, Set<Set<Closure>> s2) {
        if (s1.isEmpty()) {
            return s2;
        } /*else if (s2.isEmpty()) {
            return s1;
        }*/
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

    private static class ConfigurationSetStatePair {
        private final Set<Configuration> configuration;
        private final AutomataState state;

        private ConfigurationSetStatePair(Set<Configuration> configuration, AutomataState state) {
            this.configuration = configuration;
            this.state = state;
        }

        public static ConfigurationSetStatePair of(
                Set<Configuration> configuration,
                AutomataState automataStrategy
        ) {
            return new ConfigurationSetStatePair(configuration, automataStrategy);
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
            ConfigurationSetStatePair that = (ConfigurationSetStatePair) o;
            return Objects.equals(configuration, that.configuration)
                    && Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configuration, state);
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
