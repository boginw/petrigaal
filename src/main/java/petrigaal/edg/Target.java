package petrigaal.edg;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.Set;

public interface Target<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>
        > {
    C getConfiguration();

    PetriGame getGame();

    Set<Transition> getTransitions();
}
