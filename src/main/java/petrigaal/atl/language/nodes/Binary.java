package petrigaal.atl.language.nodes;

import petrigaal.atl.language.ATLNode;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;

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
        return parenthesizeIfBinary(getFirstOperand())
                + ' ' + getOperator() + ' '
                + parenthesizeIfBinary(getSecondOperand());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Binary<?> binary = (Binary<?>) o;
        return Objects.equals(secondOperand, binary.secondOperand);
    }

    private String parenthesizeIfBinary(T node) {
        if (node instanceof BinaryTemporal && !((BinaryTemporal) node).getOperator().equals(getOperator())) {
            return "(" + node.getLiteral() + ")";
        } else {
            return node.getLiteral();
        }
    }
}
