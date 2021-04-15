package petrigaal.edg;

import petrigaal.atl.language.ATLFormula;
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
        return "{" + game + ", " + formula.getLiteral() + (mode ? ", true" : "") + '}';
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
