package petrigaal.atl.language.nodes.temporal;

import petrigaal.Configuration;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.QuantifierTemporal;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.petri.Path;

public class UnaryQuantifierTemporal extends UnaryTemporal implements QuantifierTemporal {
    private Path path;

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public void visit(Configuration parent, DependencyGraphGenerator graph) {
        graph.visit(parent, this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getLiteral() {
        return toString();
    }

    @Override
    public String toString() {
        return String.format(
                "%s %s (%s)",
                path.toString(),
                getOperator(),
                getFirstOperand().getLiteral()
        );
    }
}
