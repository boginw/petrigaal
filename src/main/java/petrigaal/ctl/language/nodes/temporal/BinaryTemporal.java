package petrigaal.ctl.language.nodes.temporal;

import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.ATLType;
import petrigaal.ctl.language.nodes.Binary;
import petrigaal.ctl.language.nodes.Temporal;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.edg.DGTarget;

public class BinaryTemporal extends Binary<Temporal> implements Temporal {
    public BinaryTemporal() {
    }

    public BinaryTemporal(Temporal firstOperand, String operator, Temporal secondOperand) {
        setFirstOperand(firstOperand);
        setOperator(operator);
        setSecondOperand(secondOperand);
    }

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void visit(DGTarget parent, DependencyGraphGenerator graph) {
        graph.visit(parent, this);
    }

    @Override
    public ATLType getType() {
        return getOperator().equals("U") ? ATLType.PathQuery : ATLType.Evaluate;
    }
}
