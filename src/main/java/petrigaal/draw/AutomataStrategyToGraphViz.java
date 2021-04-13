package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.petri.Transition;
import petrigaal.strategy.AutomataStrategy;
import petrigaal.strategy.AutomataStrategy.AutomataState;

import java.util.Set;
import java.util.stream.Collectors;

public class AutomataStrategyToGraphViz {
    public String draw(AutomataStrategy strategy) {
        Set<AutomataState> states = strategy.getStateTransitions()
                .keySet()
                .stream()
                .map(p -> p.a)
                .collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();

        states.forEach(s -> sb.append(s.getName()).append("\n"));

        strategy.getStateTransitions().forEach((k, v) -> {
            for (Pair<Transition, AutomataState> transitionAutomataStatePair : v) {
                sb.append(k.a.getName())
                        .append(" -> ")
                        .append(transitionAutomataStatePair.b.getName())
                        .append(" [xlabel=\"")
                        .append(k.b)
                        .append(" / ")
                        .append(transitionAutomataStatePair.a)
                        .append("\"]")
                        .append("\n");
            }
        });

        return "digraph G {\n" +
                "graph [pad=\"2\", nodesep=\"2\", ranksep=\"1\", rankdir=\"TB\"];\n" +
                "node [shape=oval]\n" +
                sb +
                "}";
    }
}
