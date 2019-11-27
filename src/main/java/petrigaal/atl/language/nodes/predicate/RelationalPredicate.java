package petrigaal.atl.language.nodes.predicate;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Binary;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Predicate;

public class RelationalPredicate extends Binary<Expression> implements Predicate {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
