package petrigaal.strategy;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.strategy.automata.AutomataInput;
import petrigaal.strategy.automata.AutomataOutput;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MpsToInstanceConverter {
    private final Map<AutomataState, PathDistance> distance = new HashMap<>();
    private AutomataStrategy strategy;

    public AutomataStrategy convert(AutomataStrategy mps) {
        distance.clear();
        strategy = mps.copy();

        findDistances();

        strategy.removeEverythingThatIsNotConnectedTo(strategy.getInitialState());

        return strategy;
    }

    private void findDistances() {
        Queue<AutomataState> queue = new PriorityQueue<>(Comparator.comparing(distance::get));

        distance.putAll(strategy.getFinalStates().stream().collect(Collectors.toMap(
                s -> s,
                s -> PathDistance.MIN_VALUE)
        ));

        queue.addAll(strategy.getFinalStates());

        while (!queue.isEmpty()) {
            AutomataState state = queue.poll();

            var entries = getPredecessors(strategy, state);

            for (PathSegment entry : entries) {
                if (strategy.getFinalStates().contains(entry.input.state())) {
                    continue;
                }

                PathDistance pathDistance = distance.get(state);

                if (!isControllable(entry.input.game(), entry.output)) {
                    pathDistance = pathDistance.incrementEnvironment();
                } else {
                    pathDistance = pathDistance.incrementControllable();

                    Set<AutomataOutput> otherSuccessors = strategy.getStateTransitions().get(entry.input);
                    otherSuccessors.removeIf(s -> isControllable(entry.input.game(), s) && !s.equals(entry.output));
                }


                if (pathDistance.compareTo(distance.getOrDefault(entry.input().state(), PathDistance.MAX_VALUE)) < 0) {
                    distance.put(entry.input().state(), pathDistance);
                    queue.add(entry.input().state());
                }
            }
        }
    }

    private Set<PathSegment> getPredecessors(AutomataStrategy strategy, AutomataState state) {
        return strategy.getStateTransitions().entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(f -> f.state().equals(state)))
                .map(e -> new PathSegment(e.getKey(), getOutputFromEntryWithState(e, state)))
                .collect(Collectors.toSet());
    }

    private AutomataOutput getOutputFromEntryWithState(
            Entry<AutomataInput, Set<AutomataOutput>> e,
            AutomataState state
    ) {
        return e.getValue().stream().filter(f -> f.state().equals(state)).findFirst().orElseThrow();
    }

    private boolean isControllable(PetriGame game, AutomataOutput o) {
        return game.getEnabledTransitions(Player.Controller).contains(o.transition());
    }

    private static record PathSegment(AutomataInput input, AutomataOutput output) {
    }

    private static record PathDistance(int environment, int controllable) implements Comparable<PathDistance> {
        public static final PathDistance MAX_VALUE = new PathDistance(Integer.MAX_VALUE, Integer.MAX_VALUE);
        public static final PathDistance MIN_VALUE = new PathDistance(0, 0);

        @Override
        public int compareTo(PathDistance o) {
            int result = Integer.compare(environment, o.environment);
            return result != 0 ? result : Integer.compare(controllable, o.controllable);
        }

        public PathDistance incrementEnvironment() {
            return new PathDistance(environment + 1, controllable);
        }

        public PathDistance incrementControllable() {
            return new PathDistance(environment, controllable + 1);
        }
    }
}
