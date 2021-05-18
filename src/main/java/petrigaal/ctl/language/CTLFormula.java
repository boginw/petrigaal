package petrigaal.ctl.language;

import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.DGTarget;

public interface CTLFormula extends CTLNode {
    void visit(DGTarget parent, DependencyGraphGenerator graph);
}
