package petrigaal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.edg.DGConfiguration;
import petrigaal.petri.PetriGame;

import static org.junit.jupiter.api.Assertions.*;

class DGConfigurationTest {
    private PetriGame pg;
    private ATLFormula formula;
    private DGConfiguration configuration;

    @BeforeEach
    void setUp() {
        formula = new BooleanLiteral(true);
        pg = new PetriGame();
        configuration = new DGConfiguration(formula, pg);
    }

    @Test
    void shouldGetFormula() {
        assertEquals(formula, configuration.getFormula());
    }

    @Test
    void shouldGetGame() {
        assertEquals(pg, configuration.getGame());
    }
}
