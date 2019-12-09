package petrigaal.atl;

import petrigaal.atl.language.ATLNode;
import petrigaal.atl.language.visitor.OptimizeVisitor;

public class Optimizer {
    public ATLNode optimize(ATLNode tree) {
        return new OptimizeVisitor().visit(tree);
    }
}
