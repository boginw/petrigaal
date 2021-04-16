package petrigaal.strategy;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomataStrategy {
    private final PetriGame game;
    private final Map<Pair<AutomataState, PetriGame>, Set<Pair<Transition, AutomataState>>> stateTransitions = new HashMap<>();
    private final AutomataState initialState = new AutomataState("init");

    public AutomataStrategy(PetriGame game) {
        this.game = game;
    }

    public AutomataState getInitialState() {
        return initialState;
    }

    public Map<Pair<AutomataState, PetriGame>, Set<Pair<Transition, AutomataState>>> getStateTransitions() {
        return stateTransitions;
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
        Set<Pair<AutomataState, PetriGame>> foundKey = stateTransitions.entrySet()
                .stream()
                .filter(e -> e.getValue().stream().anyMatch(p -> p.b.equals(state)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        foundKey.forEach(key -> stateTransitions.computeIfPresent(key, (k, v) -> {
            v.removeIf(s -> s.b.equals(state));
            return v;
        }));

        stateTransitions.entrySet().removeIf(e -> e.getKey().a.equals(state));
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
            return "AutomataState{name='" + name + "'}";
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
