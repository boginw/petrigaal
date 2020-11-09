package petrigaal.atl.language.nodes;

import petrigaal.petri.Path;

public interface QuantifierTemporal extends Temporal {
    Path getPath();
    void setPath(Path path);
}
