package petrigaal.atl.language;

public interface ATLFormula {
    String getLiteral();

    void accept(Visitor visitor);

    <T> T visit(Visitor<T> visitor);
}
