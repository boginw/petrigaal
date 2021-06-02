package petrigaal;

import javafx.application.Application;
import petrigaal.app.PetriGAALApplication;
import petrigaal.app.Synthesizer;
import petrigaal.benchmark.QSATProblem;
import petrigaal.benchmark.QSATProblemGenerator;
import petrigaal.benchmark.QSATSolver;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-qsat")) {
            for (int variables = 3; variables < 100; variables++) {
                int clauses = variables;
                QSATProblem problem = new QSATProblemGenerator().generate(variables, clauses);
                Synthesizer.Result result = new QSATSolver().solve(problem);

                System.out.printf(
                        "Variables %5d, Clauses: %5d >>> Time: %5d ms, Memory (Bytes): %10d\n",
                        variables,
                        clauses,
                        result.time(),
                        result.bytes()
                );
            }
        } else {
            Application.launch(PetriGAALApplication.class);
        }

    }
}
