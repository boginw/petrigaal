package petrigaal.strategy;

import petrigaal.Configuration;
import petrigaal.petri.PetriGame;

public interface StrategySynthesiser {
    void synthesize(PetriGame game, Configuration root);
}
