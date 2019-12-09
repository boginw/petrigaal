package petrigaal.atl.language;

import petrigaal.atl.language.nodes.ATLType;

public interface ATLNode {
    String getLiteral();

    ATLType getType();

    <T> void accept(Visitor<T> visitor);

    <T> T visit(Visitor<T> visitor);

    enum Quantifier {
        And, Or, Not
    }
}
