package petrigaal.atl;

import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.visitor.OptimizeVisitor;

public class Optimizer {
    public ATLFormula optimize(ATLFormula tree) {
        return new OptimizeVisitor().visit(tree);
    }
}
