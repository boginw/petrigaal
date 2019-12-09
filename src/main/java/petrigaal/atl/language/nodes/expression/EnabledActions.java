package petrigaal.atl.language.nodes.expression;

import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.petri.Player;

import static petrigaal.petri.Player.Controller;
import static petrigaal.petri.Player.Environment;

public class EnabledActions implements Expression {
    private Player forPlayer;

    public EnabledActions(String literal) {
        if (literal.equals("d1")) {
            this.forPlayer = Controller;
        } else {
            this.forPlayer = Environment;
        }
    }

    @Override
    public String getLiteral() {
        return "d" + (forPlayer.ordinal() + 1);
    }

    @Override
    public <T> void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Player getForPlayer() {
        return forPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnabledActions that = (EnabledActions) o;
        return forPlayer == that.forPlayer;
    }
}
