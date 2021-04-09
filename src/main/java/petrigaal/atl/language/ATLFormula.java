package petrigaal.atl.language;

import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.Target;

public interface ATLFormula extends ATLNode {
    void visit(Target parent, DependencyGraphGenerator graph);
}
