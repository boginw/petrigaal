package petrigaal;

import petrigaal.atl.language.ATLFormula;
import petrigaal.edg.Edge;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Configuration {
    private final ATLFormula formula;
    private final PetriGame game;
    private final Transition generator;
    private final List<Edge> successors;

    public Configuration(
            ATLFormula formula,
            PetriGame game,
            Transition generator,
            List<Edge> successors
    ) {
        this.formula = formula;
        this.game = game;
        this.successors = successors;
        this.generator = generator;
    }

    public Configuration(ATLFormula formula, PetriGame game, Transition generator) {
        this(formula, game, generator, new ArrayList<>());
    }

    public Configuration(ATLFormula formula, PetriGame game) {
        this(formula, game, null);
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

    public Transition getGenerator() {
        return generator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(formula, that.formula) &&
                Objects.equals(game, that.game) &&
                Objects.equals(generator, that.generator);
    }

    @Override
    public String toString() {
        return "{" + game + ", " + generator + ", " + formula.getLiteral() + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula, game, generator);
    }
}
