package petrigaal.edg;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

public interface Target<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>
        > {
    C getConfiguration();

    PetriGame getGame();

    Transition getTransition();
}
