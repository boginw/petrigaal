package petrigaal.atl.language.nodes.expression;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.petri.Player;

public class EnabledActions implements Expression {
    private Player forPlayer;

    public EnabledActions(String literal) {
        if (literal.equals("d1")) {
            this.forPlayer = Player.Controller;
        } else {
            this.forPlayer = Player.Environment;
        }
    }

    @Override
    public String getLiteral() {
        return null;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Player getForPlayer() {
        return forPlayer;
    }
}
