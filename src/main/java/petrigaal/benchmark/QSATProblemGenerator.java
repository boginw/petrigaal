package petrigaal.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import petrigaal.benchmark.QSATProblem.Clause;
import petrigaal.benchmark.QSATProblem.Literal;

import java.security.SecureRandom;
import java.util.Arrays;

public class QSATProblemGenerator {
    private final SecureRandom random = new SecureRandom();

    public QSATProblem generate(int numberOfVariables, int numberOfClauses) {
        String[] variables = new String[numberOfVariables];
        Clause[] clauses = new Clause[numberOfClauses];

        for (int i = 0; i < numberOfVariables; i++) {
            variables[i] = "v" + numberOfVariables;
        }

        for (int i = 0; i < numberOfClauses; i++) {
            String variable1 = variables[getNumberBetweenZeroAnd(numberOfVariables)];
            String variable2 = variables[getNumberBetweenZeroAnd(numberOfVariables)];
            String variable3 = variables[getNumberBetweenZeroAnd(numberOfVariables)];
            Literal literal1 = new Literal(variable1, random.nextBoolean());
            Literal literal2 = new Literal(variable2, random.nextBoolean());
            Literal literal3 = new Literal(variable3, random.nextBoolean());
            clauses[i] = new Clause(literal1, literal2, literal3);
        }

        return new QSATProblem(Arrays.asList(variables), Arrays.asList(clauses));
    }

    private int getNumberBetweenZeroAnd(int max) {
        return random.nextInt(max);
    }
}
