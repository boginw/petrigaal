package petrigaal.draw;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;
import petrigaal.strategy.automata.AutomataInput;
import petrigaal.strategy.automata.AutomataOutput;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomataStrategyCytoscapeVisualizer implements Visualizer<AutomataStrategy> {
    private AutomataStrategy strategy;

    @Override
    public String draw(AutomataStrategy strategy) {
        if (strategy.getStateTransitions().isEmpty()) {
            return null;
        }

        this.strategy = strategy;
        Set<AutomataState> states = strategy.getStateTransitions()
                .keySet()
                .stream()
                .map(AutomataInput::state)
                .collect(Collectors.toSet());

        List<String> vertices = new ArrayList<>(states.stream().map(this::declareState).toList());
        List<String> edges = new ArrayList<>();

        strategy.getStateTransitions().forEach((input, v) -> {
            for (AutomataOutput output : v) {
                String edge;

                if (input.game() == null && output.transition() == null) {
                    edge = declareStarTransition(input.state(), output.state());
                } else if (input.game() != null) {
                    if (input.game().getEnabledTransitions(Player.Environment).contains(output.transition())) {
                        edge = declareUncontrollableTransition(
                                input.state(),
                                input.game(),
                                output.transition(),
                                output.state()
                        );
                    } else {
                        edge = declareTransition(input.state(), input.game(), output.transition(), output.state());
                    }
                } else {
                    throw new IllegalArgumentException("Game is null");
                }

                edges.add(edge);
            }
        });

        return declareStrategy(String.join(getVertexDelimiter(), vertices), String.join(getEdgeDelimiter(), edges));
    }

    protected String getVertexDelimiter() {
        return ",";
    }

    protected String getEdgeDelimiter() {
        return getVertexDelimiter();
    }

    protected String declareStrategy(String states, String edges) {
        return "{\"nodes\": [%s], \"edges\": [%s], \"ranks\": [], \"strategy\": true}".formatted(states, edges);
    }

    private String declareState(AutomataState state) {
        return "{\"data\": {\"id\": \"%d\", \"name\": \"%s\", \"propagates\": %s}}".formatted(
                state.hashCode(),
                state.name(),
                strategy.getFinalStates().contains(state)
        );
    }

    private String declareUncontrollableTransition(
            AutomataState start,
            PetriGame game,
            Transition transition,
            AutomataState end
    ) {
        return ("{\"data\": { \"source\": \"%d\", \"target\": \"%d\", " +
                "\"label\": \"%s / %s\", \"uncontrollable\": true}, \"classes\": \"%s\"}").formatted(
                start.hashCode(),
                end.hashCode(),
                String.valueOf(game),
                String.valueOf(transition),
                start.equals(end) ? "loop" : ""
        );
    }

    private String declareTransition(
            AutomataState start,
            PetriGame game,
            Transition transition,
            AutomataState end
    ) {
        return ("{\"data\": { \"source\": \"%d\", \"target\": \"%d\", " +
                "\"label\": \"%s / %s\"}, \"classes\": \"%s\"}").formatted(
                start.hashCode(),
                end.hashCode(),
                String.valueOf(game),
                String.valueOf(transition),
                start.equals(end) ? "loop" : ""
        );
    }

    private String declareStarTransition(
            AutomataState start,
            AutomataState end
    ) {
        return ("{\"data\": { \"source\": \"%d\", \"target\": \"%d\", " +
                "\"label\": \"* / *\"}, \"classes\": \"%s\"}").formatted(
                start.hashCode(),
                end.hashCode(),
                start.equals(end) ? "loop" : ""
        );
    }
}
