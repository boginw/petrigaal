package petrigaal.ctl.language.nodes.predicate;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Literal;
import petrigaal.ctl.language.nodes.Predicate;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.dg.DGTarget;

public class BooleanLiteral extends Literal<Boolean> implements Predicate {
    public BooleanLiteral(String literal) {
        setValue(Boolean.valueOf(literal));
    }

    public BooleanLiteral(boolean bool) {
        setValue(bool);
    }

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void visit(DGTarget parent, DependencyGraphGenerator graph) {
        graph.visit(parent, this);
    }
}
