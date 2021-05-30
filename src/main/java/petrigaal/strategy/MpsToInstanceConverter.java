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
            Set<PathSegment> segments = getPredecessors(strategy, state);
            PathDistance oldPathDistance = distance.get(state);

            for (PathSegment segment : segments) {
                if (strategy.getFinalStates().contains(segment.input.state())) {
                    continue;
                }

                PathDistance pathDistance = oldPathDistance;

                if (!isControllable(segment.input.game(), segment.output)) {
                    pathDistance = pathDistance.incrementEnvironment();
                } else {
                    pathDistance = pathDistance.incrementControllable();

                    strategy.getStateTransitions().entrySet().stream()
                            .filter(e -> e.getKey().state().equals(segment.input().state()))
                            .forEach(e -> e.getValue().removeIf(v -> isControllableAndNotSegment(segment, e, v)));
                }

                if (pathDistanceHasLowerDistanceThanSegment(pathDistance, segment)) {
                    distance.put(segment.input().state(), pathDistance);
                    queue.add(segment.input().state());
                }
            }
        }
    }

    private boolean pathDistanceHasLowerDistanceThanSegment(PathDistance pathDistance, PathSegment segment) {
        return pathDistance.compareTo(distance.getOrDefault(segment.input().state(), PathDistance.MAX_VALUE)) < 0;
    }

    private boolean isControllableAndNotSegment(
            PathSegment segment,
            Entry<AutomataInput, Set<AutomataOutput>> entry,
            AutomataOutput output
    ) {
        return isControllable(segment.input.game(), output) && !new PathSegment(entry.getKey(), output).equals(segment);
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
