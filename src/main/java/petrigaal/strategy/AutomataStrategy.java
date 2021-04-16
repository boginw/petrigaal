package petrigaal.strategy;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomataStrategy {
    private final Map<Pair<AutomataState, PetriGame>, Set<Pair<Transition, AutomataState>>> stateTransitions = new HashMap<>();
    private final AutomataState initialState = new AutomataState("init");

    public AutomataState getInitialState() {
        return initialState;
    }

    public Map<Pair<AutomataState, PetriGame>, Set<Pair<Transition, AutomataState>>> getStateTransitions() {
        return stateTransitions;
    }

    public void addTransition(
            String source,
            PetriGame game,
            Transition transition,
            String target
    ) {
        addTransition(new AutomataState(source), game, transition, new AutomataState(target));
    }

    public void addTransition(
            AutomataState source,
            PetriGame game,
            Transition transition,
            AutomataState target
    ) {
        stateTransitions.computeIfAbsent(new Pair<>(source, game), k -> new HashSet<>());
        stateTransitions.merge(new Pair<>(source, game), Set.of(new Pair<>(transition, target)), this::union);
    }

    private <A> Set<A> union(Set<A> a, Set<A> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
    }

    public void removeState(AutomataState state) {
        Set<Pair<AutomataState, PetriGame>> keys = stateTransitions.entrySet()
                .stream()
                .filter(e -> e.getValue().stream().anyMatch(p -> p.b.equals(state)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        keys.forEach(key -> stateTransitions.computeIfPresent(key, (k, v) -> {
            v.removeIf(s -> s.b.equals(state));
            return v;
        }));

        stateTransitions.entrySet().removeIf(e -> e.getKey().a.equals(state));
    }

    public void removeEverythingThatIsNotConnectedTo(AutomataState state) {
        Set<AutomataState> visited = new HashSet<>();
        Queue<AutomataState> queue = new LinkedList<>();
        visited.add(state);
        queue.add(state);

        while (!queue.isEmpty()) {
            AutomataState nextState = queue.poll();

            Set<AutomataState> adjacentStates = stateTransitions.entrySet()
                    .stream()
                    .filter(e -> e.getKey().a.equals(nextState))
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .map(p -> p.b)
                    .collect(Collectors.toSet());
            for (AutomataState adjacentState : adjacentStates) {
                if (!visited.contains(adjacentState)) {
                    visited.add(adjacentState);
                    queue.add(adjacentState);
                }
            }
        }

        Set<AutomataState> statesToRemove = getAllStates();
        statesToRemove.removeAll(visited);

        for (AutomataState automataState : statesToRemove) {
            removeState(automataState);
        }
    }

    private Set<AutomataState> getAllStates() {
        return stateTransitions.entrySet().stream()
                .flatMap(e -> Stream.concat(Stream.of(e.getKey().a), e.getValue().stream().map(p -> p.b)))
                .collect(Collectors.toSet());
    }

    public static class AutomataState {
        private final String name;

        public AutomataState(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AutomataState state = (AutomataState) o;
            return Objects.equals(name, state.name);
        }

        @Override
        public String toString() {
            return "{name='" + name + "'}";
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
