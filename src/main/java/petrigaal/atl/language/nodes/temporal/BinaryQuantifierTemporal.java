package petrigaal.atl.language.nodes.temporal;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.QuantifierTemporal;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.atl.language.Path;
import petrigaal.edg.DGTarget;

public class BinaryQuantifierTemporal extends BinaryTemporal implements QuantifierTemporal {
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
    public void visit(DGTarget parent, DependencyGraphGenerator graph) {
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
                "%s (%s %s %s)",
                path.toString(),
                getFirstOperand().getLiteral(),
                getOperator(),
                getSecondOperand().getLiteral()
        );
    }
}
