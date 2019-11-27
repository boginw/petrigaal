package petrigaal.atl.language.nodes.expression;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Literal;

public class IntegerLiteralExpression extends Literal<Integer> implements Expression {
    public IntegerLiteralExpression(String text) {
        setValue(Integer.parseInt(text));
    }

    public IntegerLiteralExpression(int value) {
        setValue(value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
