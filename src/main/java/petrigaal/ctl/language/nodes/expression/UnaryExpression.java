package petrigaal.ctl.language.nodes.expression;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.Unary;

public class UnaryExpression extends Unary<Expression> implements Expression {
    public UnaryExpression(String operator, Expression operand) {
        setOperator(operator);
        setFirstOperand(operand);
    }

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
