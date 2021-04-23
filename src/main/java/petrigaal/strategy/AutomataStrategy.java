package petrigaal.strategy;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomataStrategy {
    private final Map<AutomataInput, Set<AutomataOutput>> stateTransitions = new HashMap<>();
    private final AutomataState initialState = new AutomataState("init");
    private final Set<AutomataState> finalStates = new HashSet<>();

    public AutomataState getInitialState() {
        return initialState;
    }

    public Map<AutomataInput, Set<AutomataOutput>> getStateTransitions() {
        return stateTransitions;
    }

    public void addTransition(
            AutomataState source,
            PetriGame game,
            Transition transition,
            AutomataState target
    ) {
        stateTransitions.computeIfAbsent(new AutomataInput(source, game), k -> new HashSet<>());
        stateTransitions.merge(
                new AutomataInput(source, game), Set.of(new AutomataOutput(transition, target)),
                this::union
        );
    }

    public void removeState(AutomataState state) {
        Set<AutomataInput> keys = stateTransitions.entrySet()
                .stream()
                .filter(e -> e.getValue().stream().anyMatch(p -> p.state().equals(state)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        keys.forEach(key -> stateTransitions.computeIfPresent(key, (k, v) -> {
            v.removeIf(s -> s.state().equals(state));
            return v;
        }));

        finalStates.remove(state);
        stateTransitions.entrySet().removeIf(e -> e.getKey().state().equals(state));
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
                    .filter(e -> e.getKey().state().equals(nextState))
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .map(AutomataOutput::state)
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
                .flatMap(e -> Stream.concat(Stream.of(e.getKey().state()), e.getValue().stream().map(p -> p.state())))
                .collect(Collectors.toSet());
    }

    public void addFinalState(AutomataState state) {
        finalStates.add(state);
    }

    public Set<AutomataState> getFinalStates() {
        return finalStates;
    }

    private <A> Set<A> union(Set<A> a, Set<A> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
    }

    public record AutomataState(String name) {

        @Override
        public String toString() {
            return "{name='" + name + "'}";
        }
    }
}
