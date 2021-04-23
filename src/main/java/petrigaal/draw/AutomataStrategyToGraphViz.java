package petrigaal.draw;

import petrigaal.strategy.AutomataInput;
import petrigaal.strategy.AutomataOutput;
import petrigaal.strategy.AutomataStrategy;
import petrigaal.strategy.AutomataStrategy.AutomataState;

import java.util.Set;
import java.util.stream.Collectors;

public class AutomataStrategyToGraphViz {
    public String draw(AutomataStrategy strategy) {
        Set<AutomataState> states = strategy.getStateTransitions()
                .keySet()
                .stream()
                .map(AutomataInput::state)
                .collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();

        states.forEach(s -> sb.append(s.name()).append("\n"));

        strategy.getStateTransitions().forEach((input, v) -> {
            for (AutomataOutput output : v) {
                if (input.game() != null && output.transition() == null) {
                    addTransition(
                            sb,
                            input.state().name(),
                            input.game(),
                            "⊥",
                            output.state().name()
                    );
                } else if (input.game() == null && output.transition() == null) {
                    addTransition(
                            sb,
                            input.state().name(),
                            "*",
                            "*",
                            output.state().name()
                    );
                } else if (input.game() != null) {
                    addTransition(
                            sb,
                            input.state().name(),
                            input.game(),
                            output.transition(),
                            output.state().name()
                    );
                } else {
                    throw new IllegalArgumentException("Game is null");
                }
            }
        });

        for (AutomataState finalState : strategy.getFinalStates()) {
            sb.append(finalState.name())
                    .append(" [color=green, fillcolor=black]\n");
        }

        if (sb.isEmpty()) {
            sb.append("label=\" There does not exist a strategy. \"");
        }

        return "digraph G {\n" +
                "graph [pad=\"2\", nodesep=\"2\", ranksep=\"1\", rankdir=\"TB\"];\n" +
                "node [shape=oval]\n" +
                sb +
                "}";
    }

    private void addTransition(
            StringBuilder sb,
            Object start,
            Object game,
            Object transition,
            Object end
    ) {
        sb.append(start)
                .append(" -> ")
                .append(end)
                .append(" [xlabel=\"")
                .append(game)
                .append(" / ")
                .append(transition)
                .append("\"]")
                .append("\n");
    }
}
