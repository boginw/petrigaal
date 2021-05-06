package petrigaal.strategy;

import petrigaal.edg.Configuration;
import petrigaal.petri.PetriGame;
import petrigaal.strategy.automata.AutomataStrategy;

import java.util.Map;
import java.util.function.Consumer;

public interface StrategySynthesiser<T> {
    AutomataStrategy synthesize(
            Configuration root,
            Map<Configuration, Boolean> propagationByConfiguration,
            Consumer<T> consumer
    );
}
