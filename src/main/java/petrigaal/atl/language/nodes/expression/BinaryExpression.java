package petrigaal.atl.language.nodes.expression;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Binary;
import petrigaal.atl.language.nodes.Expression;

public class BinaryExpression extends Binary<Expression> implements Expression {

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
