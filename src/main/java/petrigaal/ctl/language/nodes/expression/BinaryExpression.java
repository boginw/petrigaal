package petrigaal.ctl.language.nodes.expression;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Binary;
import petrigaal.ctl.language.nodes.Expression;

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
