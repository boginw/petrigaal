package petrigaal.benchmark;

import java.util.List;

public record QSATProblem(List<String> variables, List<Clause> clauses) {
    public record Clause(Literal l1, Literal l2, Literal l3) {
    }

    public record Literal(String variable, boolean negated) {
    }
}
