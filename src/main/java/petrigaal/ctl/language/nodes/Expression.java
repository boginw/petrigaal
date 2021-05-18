package petrigaal.ctl.language.nodes;

import petrigaal.ctl.language.CTLNode;

public interface Expression extends CTLNode {
    default ATLType getType() {
        return ATLType.Evaluate;
    }
}
