package petrigaal.atl.language;

import petrigaal.Configuration;
import petrigaal.edg.DependencyGraphGenerator;

public interface ATLFormula extends ATLNode {
    void visit(Configuration parent, DependencyGraphGenerator graph);
}
