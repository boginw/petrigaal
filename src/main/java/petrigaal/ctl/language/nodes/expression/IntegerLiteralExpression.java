package petrigaal.ctl.language.nodes.expression;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.Literal;

public class IntegerLiteralExpression extends Literal<Integer> implements Expression {
    public IntegerLiteralExpression(String text) {
        setValue(Integer.parseInt(text));
    }

    public IntegerLiteralExpression(int value) {
        setValue(value);
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
