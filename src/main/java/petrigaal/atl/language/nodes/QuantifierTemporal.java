package petrigaal.atl.language.nodes;

import petrigaal.petri.Player;

public interface QuantifierTemporal extends Temporal {
    Player getPlayer();
    void setPlayer(Player player);
}
