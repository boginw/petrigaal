package petrigaal.strategy;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomataStrategy {
    private final PetriGame game;
    private Map<Pair<AutomataState, PetriGame>, Set<Pair<Transition, AutomataState>>> stateTransitions = new HashMap<>();
    private AutomataState initialState = new AutomataState("init");

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

    public static class AutomataState {
        private final String name;

        public AutomataState(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
