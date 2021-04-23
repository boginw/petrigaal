package petrigaal.strategy.automata;

import petrigaal.petri.PetriGame;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

public record AutomataInput(AutomataState state, PetriGame game) {
}
