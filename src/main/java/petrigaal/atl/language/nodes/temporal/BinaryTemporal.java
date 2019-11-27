package petrigaal.atl.language.nodes.temporal;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Binary;
import petrigaal.atl.language.nodes.Temporal;

public class BinaryTemporal extends Binary<Temporal> implements Temporal {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
