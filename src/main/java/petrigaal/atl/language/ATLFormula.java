package petrigaal.atl.language;

import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.DGTarget;

public interface ATLFormula extends ATLNode {
    void visit(DGTarget parent, DependencyGraphGenerator graph);
}
