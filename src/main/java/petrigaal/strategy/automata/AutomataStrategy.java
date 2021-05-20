package petrigaal.strategy.automata;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;
import petrigaal.strategy.Strategy;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class AutomataStrategy implements Strategy {
    private final Map<AutomataInput, Set<AutomataOutput>> stateTransitions = new HashMap<>();
    private final AutomataState initialState;
    private final Set<AutomataState> finalStates = new HashSet<>();

    public AutomataStrategy() {
        initialState = new AutomataState("init");
    }

    public AutomataStrategy(AutomataState initialState) {
        this.initialState = initialState;
    }

    public AutomataStrategy copy() {
        AutomataStrategy strategy = new AutomataStrategy(initialState);
        for (Map.Entry<AutomataInput, Set<AutomataOutput>> entry : stateTransitions.entrySet()) {
            strategy.stateTransitions.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        strategy.finalStates.addAll(finalStates);
        return strategy;
    }

    public AutomataState getInitialState() {
        return initialState;
    }

    public Map<AutomataInput, Set<AutomataOutput>> getStateTransitions() {
        return stateTransitions;
    }

    public void addTransition(AutomataState source, PetriGame game, Transition transition, AutomataState target) {
        addTransition(new AutomataInput(source, game), new AutomataOutput(transition, target));
    }

    public void addTransition(AutomataInput input, AutomataOutput output) {
        stateTransitions.computeIfAbsent(input, k -> new HashSet<>());
        stateTransitions.merge(input, Set.of(output), this::union);
    }

    @Override
    public Set<Transition> out(final List<PetriGame> gameList) {
        if (gameList.isEmpty()) {
            throw new IllegalArgumentException("Unknown state");
        }

        Queue<PetriGame> games = new LinkedList<>(gameList);

        Set<AutomataState> currentStates = new HashSet<>();
        currentStates.add(initialState);
        Set<Transition> transitions = new HashSet<>();

        while (!games.isEmpty()) {
            PetriGame game = games.poll();
            Set<AutomataOutput> outputs = stateTransitions.entrySet().stream()
                    .filter(e -> currentStates.contains(e.getKey().state()))
                    .filter(e -> e.getKey().game().equals(game))
                    .flatMap(e -> e.getValue().stream())
                    .collect(Collectors.toSet());
            transitions = outputs.stream().map(AutomataOutput::transition).collect(Collectors.toSet());
            currentStates.clear();
            currentStates.addAll(outputs.stream().map(AutomataOutput::state).collect(Collectors.toSet()));
        }

        return transitions;
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

            for (AutomataState adjacentState : getAdjacentStates(nextState)) {
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

    public Set<AutomataState> getFinalStates() {
        return finalStates;
    }

    public void addFinalState(AutomataState state) {
        addTransition(state, null, null, state);
        finalStates.add(state);
    }

    private Set<AutomataState> getAdjacentStates(AutomataState state) {
        return stateTransitions.entrySet()
                .stream()
                .filter(e -> e.getKey().state().equals(state))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(AutomataOutput::state)
                .collect(Collectors.toSet());
    }

    private Set<AutomataState> getAllStates() {
        return stateTransitions.entrySet().stream()
                .flatMap(e -> concat(Stream.of(e.getKey().state()), e.getValue().stream().map(AutomataOutput::state)))
                .collect(Collectors.toSet());
    }

    private <A> Set<A> union(Set<A> a, Set<A> b) {
        return concat(a.stream(), b.stream()).collect(Collectors.toSet());
    }

    public record AutomataState(String name) {

        @Override
        public String toString() {
            return "{name='" + name + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AutomataState that = (AutomataState) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
