package petrigaal.atl.language.nodes;

import petrigaal.atl.language.Path;

public interface QuantifierTemporal extends Temporal {
    Path getPath();
    void setPath(Path path);
}
