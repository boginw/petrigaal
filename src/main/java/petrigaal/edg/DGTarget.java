package petrigaal.edg;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Transition;

import java.util.Objects;

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
    public Transition getTransition() {
        return transition;
    }

    public DGTarget withConfiguration(DGConfiguration configuration) {
        return new DGTarget(configuration, getTransition(), getGame());
    }

    @Override
    public String toString() {
        return "DGTarget{" +
                "configuration=" + configuration +
                ", transition=" + transition +
                ", game=" + game +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DGTarget dgTarget = (DGTarget) o;
        return Objects.equals(configuration, dgTarget.configuration)
                && Objects.equals(transition, dgTarget.transition)
                && Objects.equals(game, dgTarget.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, transition, game);
    }
}
