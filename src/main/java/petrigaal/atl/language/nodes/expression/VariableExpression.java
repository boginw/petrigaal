package petrigaal.atl.language.nodes.expression;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;

import java.util.Objects;

public class VariableExpression implements Expression {
    private String identifier;

    public VariableExpression(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getLiteral() {
        return getIdentifier();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableExpression that = (VariableExpression) o;
        return Objects.equals(identifier, that.identifier);
    }
}
