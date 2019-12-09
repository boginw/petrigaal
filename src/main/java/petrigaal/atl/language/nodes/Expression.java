package petrigaal.atl.language.nodes;

import petrigaal.atl.language.ATLNode;

public interface Expression extends ATLNode {
    default ATLType getType() {
        return ATLType.Evaluate;
    }
}
