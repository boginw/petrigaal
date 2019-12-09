package petrigaal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.petri.PetriGame;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
    private PetriGame pg;
    private ATLFormula formula;
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        formula = new BooleanLiteral(true);
        pg = new PetriGame();
        configuration = new Configuration(formula, pg);
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