package petrigaal.strategy.automata;

import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomataStrategyDeterminer {
    private final AutomataStrategy nondeterministic;

    public AutomataStrategyDeterminer(AutomataStrategy nondeterministic) {
        this.nondeterministic = nondeterministic;
    }

    public AutomataStrategy determine() {
        Queue<StatePath> paths = new PriorityQueue<>();
        StatePath start = new StatePath(
                null,
                null,
                new AutomataOutput(null, nondeterministic.getInitialState()),
                0
        );
        paths.add(start);
        StatePath smallest = new StatePath(null, null, null, Integer.MAX_VALUE);

        while (!paths.isEmpty()) {
            StatePath current = paths.poll();

            for (AutomataInput input : getInputsWithState(current.output().state())) {
                for (AutomataOutput output : nondeterministic.getStateTransitions().getOrDefault(input, Set.of())) {
                    if (nondeterministic.getFinalStates().contains(output.state())) {
                        StatePath next = new StatePath(current, input, output, current.length() + 1);

                        if (next.length() < smallest.length()) {
                            smallest = next;
                        } else if (!current.contains(output.state())) {
                            paths.add(next);
                        }
                    }
                }
            }
        }

        AutomataStrategy deterministic = new AutomataStrategy();

        StatePath current = smallest;
        while (current.parent() != null) {
            deterministic.addTransition(current.input(), current.output());
            if (nondeterministic.getFinalStates().contains(current.output().state())) {
                deterministic.addFinalState(current.output().state());
            }
            current = current.parent();
        }

        return deterministic;
    }

    private Set<AutomataInput> getInputsWithState(AutomataState nextState) {
        return nondeterministic.getStateTransitions().keySet()
                .stream()
                .filter(e -> e.state().equals(nextState))
                .collect(Collectors.toSet());
    }

    private static record StatePath(
            StatePath parent,
            AutomataInput input,
            AutomataOutput output,
            int length
    ) implements Comparable<StatePath> {
        @Override
        public int compareTo(StatePath o) {
            return Integer.compare(length, o.length());
        }

        public boolean contains(AutomataState state) {
            if (output.state().equals(state)) {
                return true;
            } else if (parent != null) {
                return parent.contains(state);
            } else {
                return false;
            }
        }
    }
}
