package petrigaal.loader;

import petrigaal.petri.PetriGame;

import java.io.InputStream;

public interface Loader {
    PetriGame load(InputStream file);
}
