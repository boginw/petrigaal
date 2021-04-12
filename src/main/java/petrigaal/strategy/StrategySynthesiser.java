package petrigaal.strategy;

import petrigaal.Configuration;
import petrigaal.petri.PetriGame;

import java.util.Map;

public interface StrategySynthesiser {
    void synthesize(PetriGame game, Configuration root, Map<Configuration, Boolean> propagationByConfiguration);
}
