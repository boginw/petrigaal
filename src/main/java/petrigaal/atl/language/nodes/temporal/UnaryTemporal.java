package petrigaal.atl.language.nodes.temporal;

import petrigaal.Configuration;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.ATLType;
import petrigaal.atl.language.nodes.Temporal;
import petrigaal.atl.language.nodes.Unary;
import petrigaal.edg.DependencyGraphGenerator;

public class UnaryTemporal extends Unary<Temporal> implements Temporal {
    public UnaryTemporal() {
    }

    public UnaryTemporal(String operator, Temporal firstOperand) {
        setOperator(operator);
        setFirstOperand(firstOperand);
    }

    @Override
    public ATLType getType() {
        return getOperator().equals("!") ? ATLType.Evaluate : ATLType.PathQuery;
    }

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public void visit(Configuration parent, DependencyGraphGenerator graph) {
        graph.visit(parent, this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
