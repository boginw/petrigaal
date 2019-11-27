package petrigaal.petri;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static petrigaal.petri.Player.Controller;
import static petrigaal.petri.Player.Environment;

class PetriGameTest {
    private PetriGame pg;
    private String p1;
    private String p2;
    private String p3;
    private Transition t;
    private List<Transition> expectedTransitions;

    @BeforeEach
    void setUp() {
        pg = new PetriGame();
        p1 = "p1";
        p2 = "p2";
        p3 = "p3";
        t = new Transition("t")
                .addInput(p1, 1)
                .addOutput(p2, 1);
        expectedTransitions = List.of(t);
    }

    @Test
    void shouldAddTransitionForController() {
        pg.addTransition(Controller, t);
        assertEquals(expectedTransitions, pg.getTransitions(Controller));
        assertEquals(Set.of(p1, p2), pg.getPlaces());
    }

    @Test
    void shouldAddTransitionForEnvironment() {
        pg.addTransition(Environment, t);
        assertEquals(expectedTransitions, pg.getTransitions(Environment));
        assertEquals(Set.of(p1, p2), pg.getPlaces());
    }

    @Test
    void shouldSetMarkingForPlace() {
        int marking = 1;
        pg.setMarking(p1, marking);
        assertEquals(marking, pg.getMarking(p1));
    }

    @Test
    void shouldReturn0MarkingsIfNotSet() {
        assertEquals(0, pg.getMarking(p1));
    }

    @Test
    void whenAddingNegativeMarking_expectFailure() {
        int marking = -1;
        assertThrows(IllegalArgumentException.class, () -> pg.setMarking(p1, marking));
    }

    @Test
    void shouldAddToMarking() {
        int marking = 10;
        pg.setMarking(p1, marking);
        pg.addMarkings(p1, marking);
        assertEquals(marking + marking, pg.getMarking(p1));
    }

    @Test
    void shouldSubtractMarkings() {
        int marking = 10;
        pg.setMarking(p1, marking);
        pg.subtractMarkings(p1, marking / 2);
        assertEquals(marking / 2, pg.getMarking(p1));
    }

    @Test
    void whenInputArsCanAffordToFire_transitionIsEnabled() {
        Transition t = new Transition("name")
                .addInput(p1, pg.getMarking(p1))
                .addInput(p2, pg.getMarking(p2));

        pg.addTransition(Controller, t);
        assertTrue(pg.isEnabled(t));
    }

    @Test
    void whenInputArsCannotAffordToFire_transitionIsNotEnabled() {
        Transition t = new Transition("name")
                .addInput(p1, pg.getMarking(p1))
                .addInput(p2, pg.getMarking(p2) + 1);

        pg.addTransition(Controller, t);
        assertFalse(pg.isEnabled(t));
    }

    @Test
    void whenFire_shouldNotChangeOriginalPetriGame() {
        Transition t1 = new Transition("t1")
                .addInput(p1)
                .addOutput(p2)
                .addOutput(p3);

        pg.addTransition(Controller, t1);
        pg.setMarking(p1, 1);
        pg.setMarking(p2, 0);

        pg.fire(t1);
        assertEquals(1, pg.getMarking(p1));
        assertEquals(0, pg.getMarking(p2));
    }

    @Test
    void whenFire_shouldCycleMarkings() {
        Transition t1 = new Transition("t1").addInput(p1).addOutput(p2);
        Transition t2 = new Transition("t2").addInput(p2).addOutput(p1);

        pg.addTransition(Controller, t1);
        pg.addTransition(Environment, t2);

        pg.setMarking(p1, 1);
        pg.setMarking(p2, 0);

        pg = pg.fire(t1);
        assertEquals(0, pg.getMarking(p1));
        assertEquals(1, pg.getMarking(p2));
        pg = pg.fire(t2);
        assertEquals(1, pg.getMarking(p1));
        assertEquals(0, pg.getMarking(p2));
    }

    @Test
    void whenFire_shouldAddToAllOutputPlaces() {
        Transition t1 = new Transition("t1")
                .addInput(p1)
                .addOutput(p2)
                .addOutput(p3);

        pg.addTransition(Controller, t1);
        pg.setMarking(p1, 1);
        pg.setMarking(p2, 0);
        pg.setMarking(p3, 0);

        pg = pg.fire(t1);
        assertEquals(0, pg.getMarking(p1));
        assertEquals(1, pg.getMarking(p2));
        assertEquals(1, pg.getMarking(p3));
    }

    @Test
    void whenFire_shouldSubtractFromAllInputPlaces() {
        Transition t1 = new Transition("t1")
                .addInput(p1)
                .addInput(p2)
                .addOutput(p3);

        pg.addTransition(Controller, t1);
        pg.setMarking(p1, 1);
        pg.setMarking(p2, 1);
        pg.setMarking(p3, 0);

        pg = pg.fire(t1);
        assertEquals(0, pg.getMarking(p1));
        assertEquals(0, pg.getMarking(p2));
        assertEquals(1, pg.getMarking(p3));
    }
}