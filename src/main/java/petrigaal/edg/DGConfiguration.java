package petrigaal.edg;

import petrigaal.atl.language.ATLFormula;
import petrigaal.petri.PetriGame;

import java.util.*;

public class DGConfiguration implements Configuration<DGConfiguration, DGEdge, DGTarget> {
    private final ATLFormula formula;
    private final PetriGame game;
    private final boolean mode;
    private final Set<DGEdge> successors;
    private int negationDistance = Integer.MAX_VALUE;

    public DGConfiguration(
            ATLFormula formula,
            PetriGame game,
            Set<DGEdge> successors,
            boolean mode
    ) {
        this.formula = formula;
        this.game = game;
        this.successors = successors;
        this.mode = mode;
    }

    public DGConfiguration(ATLFormula formula, PetriGame game, boolean mode) {
        this(formula, game, new HashSet<>(), mode);
    }

    public DGConfiguration(ATLFormula formula, PetriGame game) {
        this(formula, game, new HashSet<>(), false);
    }

    public int getNegationDistance() {
        return negationDistance;
    }

    public void setNegationDistance(int negationDistance) {
        this.negationDistance = negationDistance;
    }

    public ATLFormula getFormula() {
        return formula;
    }

    public PetriGame getGame() {
        return game;
    }

    @Override
    public Set<DGEdge> getSuccessors() {
        return successors;
    }

    public boolean getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DGConfiguration that = (DGConfiguration) o;
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
}
