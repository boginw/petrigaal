package petrigaal.strategy;

import petrigaal.petri.Transition;
import petrigaal.strategy.AutomataStrategy.AutomataState;

public record AutomataOutput(Transition transition, AutomataState state) {
}
