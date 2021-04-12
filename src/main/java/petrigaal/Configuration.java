package petrigaal;

import petrigaal.atl.language.ATLFormula;
import petrigaal.edg.Edge;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class Configuration {
    private final ATLFormula formula;
    private final PetriGame game;
    private final Map<PetriGame, Transition> history;
    private final boolean mode;
    private final List<Edge> successors;

    public Configuration(
            ATLFormula formula,
            PetriGame game,
            Map<PetriGame, Transition> history,
            List<Edge> successors,
            boolean mode
    ) {
        this.formula = formula;
        this.game = game;
        this.successors = successors;
        this.history = history;
        this.mode = mode;
    }

    public Configuration(ATLFormula formula, PetriGame game, Map<PetriGame, Transition> history, boolean mode) {
        this(formula, game, history, new ArrayList<>(), mode);
    }

    public Configuration(ATLFormula formula, PetriGame game, Map<PetriGame, Transition> history) {
        this(formula, game, history, new ArrayList<>(), false);
    }

    public Configuration(ATLFormula formula, PetriGame game) {
        this(formula, game, new HashMap<>());
    }

    public ATLFormula getFormula() {
        return formula;
    }

    public PetriGame getGame() {
        return game;
    }

    public List<Edge> getSuccessors() {
        return successors;
    }

    public Map<PetriGame, Transition> getHistory() {
        return history;
    }

    public boolean getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(formula, that.formula) &&
                Objects.equals(game, that.game) &&
                Objects.equals(new HashMap<>(history), new HashMap<>(that.history)) &&
                Objects.equals(mode, that.mode);
    }

    @Override
    public String toString() {
        String hist = history.entrySet()
                .stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
        return "{" + game + ", {" + hist + "}, " + formula.getLiteral() + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula, game, history, mode);
    }
}
