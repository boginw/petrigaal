package petrigaal.ctl.language.nodes.predicate;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Binary;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.Predicate;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.DGTarget;

public class RelationalPredicate extends Binary<Expression> implements Predicate {
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
