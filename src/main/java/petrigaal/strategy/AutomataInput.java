package petrigaal.strategy;

import petrigaal.petri.PetriGame;
import petrigaal.strategy.AutomataStrategy.AutomataState;

public record AutomataInput(AutomataState state, PetriGame game) {
}
