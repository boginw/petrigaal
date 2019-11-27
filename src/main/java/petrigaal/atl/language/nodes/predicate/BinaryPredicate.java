package petrigaal.atl.language.nodes.predicate;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Binary;
import petrigaal.atl.language.nodes.Predicate;

public class BinaryPredicate extends Binary<Predicate> implements Predicate {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
