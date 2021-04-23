package petrigaal.strategy;

import petrigaal.edg.Configuration;
import petrigaal.petri.PetriGame;
import petrigaal.strategy.automata.AutomataStrategy;

import java.util.Map;
import java.util.function.Consumer;

public interface StrategySynthesiser {
    AutomataStrategy synthesize(
            PetriGame game,
            Configuration root,
            Map<Configuration, Boolean> propagationByConfiguration,
            Consumer<AutomataStrategy> consumer
    );
}
