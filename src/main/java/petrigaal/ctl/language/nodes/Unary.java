package petrigaal.ctl.language.nodes;

import petrigaal.ctl.language.CTLNode;

import java.util.Objects;

public abstract class Unary<T extends CTLNode> implements CTLNode {
    private String operator;
    private T firstOperand;

    public T getFirstOperand() {
        return this.firstOperand;
    }

    public void setFirstOperand(T firstOperand) {
        this.firstOperand = firstOperand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String getLiteral() {
        if (getOperator().equals("!")) {
            return getOperator() + getFirstOperand().getLiteral();
        } else {
            return getOperator() + '(' + getFirstOperand().getLiteral() + ')';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unary<?> unary = (Unary<?>) o;
        return Objects.equals(operator, unary.operator) &&
                Objects.equals(firstOperand, unary.firstOperand);
    }
}
