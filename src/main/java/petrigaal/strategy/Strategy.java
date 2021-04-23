package petrigaal.strategy;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.List;
import java.util.Set;

public interface Strategy {
    Set<Transition> out(List<PetriGame> gameList);
}
