package petrigaal.atl.language.nodes;

public interface Predicate extends Temporal {
    default ATLType getType() {
        return ATLType.Evaluate;
    }
}
