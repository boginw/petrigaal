package petrigaal.petri;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransitionTest {
    private int weight;
    private String p;
    private String name;

    @BeforeEach
    void setUp() {
        weight = 1;
        name = "name";
        p = name;
    }

    @Test
    void whenArcsHaveSamePlaceAndSameWeight_theyAreEqual() {
        Transition.Arc arc1 = new Transition.Arc(p, weight);
        Transition.Arc arc2 = new Transition.Arc(p, weight);
        assertEquals(arc1, arc2);
    }

    @Test
    void shouldAddInputArc() {
        Transition t = new Transition(name).addInput(p, weight);
        Set<Transition.Arc> expectedArcs = Set.of(new Transition.Arc(p, weight));
        Set<Transition.Arc> inputArcs =  t.getInputsArcs();
        assertEquals(expectedArcs, inputArcs);
    }

    @Test
    void shouldAddOutputArc() {
        Transition t = new Transition(name).addOutput(p, weight);
        Set<Transition.Arc> expectedArcs = Set.of(new Transition.Arc(p, weight));
        Set<Transition.Arc> inputArcs =  t.getOutputArcs();
        assertEquals(expectedArcs, inputArcs);
    }
}