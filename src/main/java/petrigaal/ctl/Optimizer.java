package petrigaal.ctl;

import petrigaal.ctl.language.CTLNode;
import petrigaal.ctl.language.visitor.OptimizeVisitor;

public class Optimizer {
    public CTLNode optimize(CTLNode tree) {
        return new OptimizeVisitor().visit(tree);
    }
}
