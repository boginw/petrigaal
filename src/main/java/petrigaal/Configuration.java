package petrigaal;

import petrigaal.atl.language.ATLFormula;
import petrigaal.edg.Edge;
import petrigaal.petri.PetriGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Configuration {
    private final ATLFormula formula;
    private final PetriGame game;
    private final boolean mode;
    private final List<Edge> successors;
    private boolean propagates = false;

    public Configuration(
            ATLFormula formula,
            PetriGame game,
            List<Edge> successors,
            boolean mode
    ) {
        this.formula = formula;
        this.game = game;
        this.successors = successors;
        this.mode = mode;
    }

    public Configuration(ATLFormula formula, PetriGame game, boolean mode) {
        this(formula, game, new ArrayList<>(), mode);
    }

    public Configuration(ATLFormula formula, PetriGame game) {
        this(formula, game, new ArrayList<>(), false);
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

    public boolean getMode() {
        return mode;
    }

    public boolean isPropagates() {
        return propagates;
    }

    public void setPropagates(boolean propagates) {
        this.propagates = propagates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(formula, that.formula) &&
                Objects.equals(game, that.game) &&
                Objects.equals(mode, that.mode);
    }

    @Override
    public String toString() {
        return "{" + game + ", " + formula.getLiteral() + ", " + mode + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula, game, mode);
    }

    public Configuration copy() {
        List<Edge> copiedEdges = successors.stream().map(Edge::copy).collect(Collectors.toList());
        return new Configuration(formula, game, copiedEdges, mode);
    }
}
