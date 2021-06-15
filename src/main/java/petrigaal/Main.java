package petrigaal;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import petrigaal.app.PetriGAALApplication;
import petrigaal.app.Synthesizer;
import petrigaal.benchmark.QSATProblem;
import petrigaal.benchmark.QSATProblemGenerator;
import petrigaal.benchmark.QSATSolver;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-qsat")) {
            QSATProblem.Literal l1 = new QSATProblem.Literal("x1", false);
            QSATProblem.Literal l2 = new QSATProblem.Literal("x2", true);
            QSATProblem.Literal l3 = new QSATProblem.Literal("x3", false);
            QSATProblem.Clause clause = new QSATProblem.Clause(l1, l2, l3);
            ArrayList<String> variableString = new ArrayList<>(List.of("x1", "x2", "x3"));
            QSATProblem problem = new QSATProblem(variableString, List.of(clause));
            Synthesizer.Result result = new QSATSolver().solve(problem);

            System.out.printf(
                    "Variables: 3, Clauses: 1 >>> Time: %5d ms, Memory (Bytes): %10d\n",
                    result.time(),
                    result.bytes()
            );

            // for (int variables = 3; variables < 100; variables++) {
            //     int clauses = variables;
            //     QSATProblem problem = new QSATProblemGenerator().generate(variables, clauses);
            //     Synthesizer.Result result = new QSATSolver().solve(problem);

            //     System.out.printf(
            //             "Variables %5d, Clauses: %5d >>> Time: %5d ms, Memory (Bytes): %10d\n",
            //             variables,
            //             clauses,
            //             result.time(),
            //             result.bytes()
            //     );
            // }
        } else {
            Application.launch(PetriGAALApplication.class);
        }
    }
}
