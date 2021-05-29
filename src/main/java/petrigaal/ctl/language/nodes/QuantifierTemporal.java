package petrigaal.ctl.language.nodes;

import petrigaal.ctl.language.Path;

public interface QuantifierTemporal extends Temporal {
    Path getPath();
    void setPath(Path path);
}
