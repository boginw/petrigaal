package petrigaal.ctl.language;

import petrigaal.ctl.language.nodes.ATLType;

public interface CTLNode {
    String getLiteral();

    ATLType getType();

    <T> void accept(Visitor<T> visitor);

    <T> T visit(Visitor<T> visitor);

    enum Quantifier {
        And, Or, Not
    }
}
