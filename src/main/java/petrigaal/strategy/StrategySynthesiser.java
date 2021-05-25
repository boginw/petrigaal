package petrigaal.strategy;

import petrigaal.edg.dg.DGConfiguration;
import petrigaal.strategy.automata.AutomataStrategy;

import java.util.Map;
import java.util.function.Consumer;

public interface StrategySynthesiser<T> {
    AutomataStrategy synthesize(
            DGConfiguration root,
            Map<DGConfiguration, Boolean> propagationByConfiguration,
            Consumer<T> consumer
    );
}
