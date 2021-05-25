package petrigaal.edg.dg;

import petrigaal.edg.Target;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.Objects;
import java.util.Set;

public class DGTarget implements Target<DGConfiguration, DGEdge, DGTarget> {
    private final DGConfiguration configuration;
    private final Transition transition;
    private final PetriGame game;

    public DGTarget(DGConfiguration configuration, Transition transition, PetriGame game) {
        this.configuration = configuration;
        this.transition = transition;
        this.game = game;
    }

    public DGTarget(DGConfiguration configuration) {
        this.configuration = configuration;
        this.transition = null;
        this.game = null;
    }

    @Override
    public DGConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public PetriGame getGame() {
        return game;
    }

    @Override
    public Set<Transition> getTransitions() {
        return transition != null ? Set.of(transition) : Set.of();
    }

    public DGTarget withConfiguration(DGConfiguration configuration) {
        return new DGTarget(configuration, transition, game);
    }

    @Override
    public String toString() {
        return "Target{"
                + "configuration=" + configuration
                + ", transitions=" + transition
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DGTarget target = (DGTarget) o;
        return Objects.equals(configuration, target.configuration)
                && Objects.equals(transition, target.transition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, transition);
    }
}
