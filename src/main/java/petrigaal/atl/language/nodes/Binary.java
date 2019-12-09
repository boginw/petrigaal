package petrigaal.atl.language.nodes;

import petrigaal.atl.language.ATLNode;

import java.util.Objects;

public abstract class Binary<T extends ATLNode> extends Unary<T> {
    private T secondOperand;

    public T getSecondOperand() {
        return this.secondOperand;
    }

    public void setSecondOperand(T secondOperand) {
        this.secondOperand = secondOperand;
    }

    @Override
    public String getLiteral() {
        return getFirstOperand().getLiteral() + " " +
                getOperator() + " " +
                getSecondOperand().getLiteral();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Binary<?> binary = (Binary<?>) o;
        return Objects.equals(secondOperand, binary.secondOperand);
    }
}
