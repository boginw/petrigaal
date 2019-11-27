package petrigaal;

import petrigaal.atl.language.ATLFormula;
import petrigaal.petri.PetriGame;

public class Configuration {
    private ATLFormula formula;
    private PetriGame game;

    public Configuration(ATLFormula formula, PetriGame game) {
        this.formula = formula;
        this.game = game;
    }

    public ATLFormula getFormula() {
        return formula;
    }

    public PetriGame getGame() {
        return game;
    }
}
