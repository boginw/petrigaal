package petrigaal.edg.mdg;

import petrigaal.edg.Target;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.Set;

public record MetaTarget(
        MetaConfiguration configuration,
        Set<Transition> transitions,
        PetriGame game
) implements Target<MetaConfiguration, MetaEdge, MetaTarget> {
    @Override
    public MetaConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Set<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public PetriGame getGame() {
        return game;
    }
}
