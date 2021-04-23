package petrigaal.strategy.automata;

import petrigaal.petri.Transition;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

public record AutomataOutput(Transition transition, AutomataState state) {
}
