package petrigaal.draw;

import petrigaal.petri.Player;
import petrigaal.strategy.automata.AutomataInput;
import petrigaal.strategy.automata.AutomataOutput;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

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

        states.forEach(s -> sb.append('"').append(s.name()).append('"').append("\n"));

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
                    if (input.game().getEnabledTransitions(Player.Environment).contains(output.transition())) {
                        addTransition(
                                sb,
                                input.state().name(),
                                input.game(),
                                output.transition(),
                                output.state().name(),
                                true
                        );
                    } else {
                        addTransition(
                                sb,
                                input.state().name(),
                                input.game(),
                                output.transition(),
                                output.state().name()
                        );
                    }
                } else {
                    throw new IllegalArgumentException("Game is null");
                }
            }
        });

        for (AutomataState finalState : strategy.getFinalStates()) {
            sb.append('"').append(finalState.name()).append('"')
                    .append(" [color=green, fillcolor=black]\n");
        }

        if (sb.isEmpty()) {
            sb.append("label=\" There does not exist a strategy\"");
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
        addTransition(sb, start, game, transition, end, false);
    }

    private void addTransition(
            StringBuilder sb,
            Object start,
            Object game,
            Object transition,
            Object end,
            boolean dashed
    ) {
        sb.append('"').append(start).append('"')
                .append(" -> ")
                .append('"').append(end).append('"')
                .append(" [label=\"")
                .append(game)
                .append(" / ")
                .append(transition)
                .append("\"");
        if (dashed) {
            sb.append(", style=\"dashed\"");
        }
        sb.append(']').append("\n");
    }
}
