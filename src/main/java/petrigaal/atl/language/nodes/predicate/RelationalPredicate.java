package petrigaal.atl.language.nodes.predicate;

import petrigaal.Configuration;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Binary;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Predicate;
import petrigaal.edg.DependencyGraphGenerator;

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
    public void visit(Configuration parent, DependencyGraphGenerator graph) {
        graph.visit(parent, this);
    }
}
