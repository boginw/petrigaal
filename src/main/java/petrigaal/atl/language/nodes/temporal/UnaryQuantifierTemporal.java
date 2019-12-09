package petrigaal.atl.language.nodes.temporal;

import petrigaal.Configuration;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.QuantifierTemporal;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.petri.Player;

public class UnaryQuantifierTemporal extends UnaryTemporal implements QuantifierTemporal {
    private Player player;

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
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
                "{%d} %s (%s)",
                player.ordinal() + 1,
                getOperator(),
                getFirstOperand().getLiteral()
        );
    }
}
