package petrigaal;

import petrigaal.atl.language.ATLFormula;
import petrigaal.edg.Edge;
import petrigaal.petri.PetriGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Configuration {
    private ATLFormula formula;
    private PetriGame game;
    private List<Edge> successors;

    public Configuration(ATLFormula formula, PetriGame game, List<Edge> successors) {
        this.formula = formula;
        this.game = game;
        this.successors = successors;
    }

    public Configuration(ATLFormula formula, PetriGame game) {
        this(formula, game, new ArrayList<>());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        if (Objects.equals(formula, that.formula) &&
                Objects.equals(game, that.game)) {
            return true;
        }
        return Objects.equals(formula, that.formula) &&
                Objects.equals(game, that.game);
    }

    @Override
    public String toString() {
        return "{" + game + ", " + formula.getLiteral() + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula, game);
    }
}
