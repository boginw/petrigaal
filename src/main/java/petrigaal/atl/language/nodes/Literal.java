package petrigaal.atl.language.nodes;

import petrigaal.atl.language.ATLNode;

import java.util.Objects;

public abstract class Literal<T> implements ATLNode {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String getLiteral() {
        return getValue().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal<?> literal = (Literal<?>) o;
        return Objects.equals(value, literal.value);
    }

    @Override
    public ATLType getType() {
        return ATLType.Evaluate;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
