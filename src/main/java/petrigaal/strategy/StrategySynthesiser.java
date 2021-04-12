package petrigaal.strategy;

import petrigaal.Configuration;
import petrigaal.petri.PetriGame;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StrategySynthesiser {
    void synthesize(
            PetriGame game,
            Configuration root,
            Map<Configuration, Boolean> propagationByConfiguration,
            Consumer<AutomataStrategy> consumer
    );
}
