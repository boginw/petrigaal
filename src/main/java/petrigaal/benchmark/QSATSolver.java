package petrigaal.benchmark;

import petrigaal.app.Synthesizer;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class QSATSolver {
    public Synthesizer.Result solve(QSATProblem problem) {
        PetriGame game = getGame(problem);
        String formula = getFormula(problem);

        Synthesizer synthesizer = new Synthesizer(
                new Synthesizer.Options(null, formula, true, false)
        );

        try {
            return synthesizer.synthesize(game);
        } catch (IllegalAccessException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFormula(QSATProblem problem) {
        List<String> clauses = new ArrayList<>();

        for (QSATProblem.Clause clause : problem.clauses()) {
            String clauseFormula = "(A F (%s | %s | %s))".formatted(
                    getFormulaFromLiteral(clause.l1()),
                    getFormulaFromLiteral(clause.l2()),
                    getFormulaFromLiteral(clause.l3())
            );
            clauses.add(clauseFormula);
        }
        return String.join(" & ", clauses);
    }

    private PetriGame getGame(QSATProblem problem) {
        PetriGame game = new PetriGame();

        String start = "start";
        String firstValueTrue = getPlaceName(problem.variables().get(0), true);
        String firstValueFalse = getPlaceName(problem.variables().get(0), false);

        game.setMarking(start, 1);
        game.setMarking(firstValueTrue, 0);
        game.setMarking(firstValueFalse, 0);

        Transition firstTransitionTrue = new Transition("T0_true");
        firstTransitionTrue.addInput(start);
        firstTransitionTrue.addOutput(firstValueTrue);

        Transition firstTransitionFalse = new Transition("T0_false");
        firstTransitionFalse.addInput(start);
        firstTransitionFalse.addOutput(firstValueFalse);

        for (int i = 1; i < problem.variables().size(); i++) {
            for (int truthValue = 0; truthValue < 2; truthValue++) {
                boolean truth = truthValue == 1;
                String place = getPlaceName(problem.variables().get(i), truth);

                game.setMarking(place, 0);

                Transition transition1 = new Transition("T" + i + "_1_" + truthValue);
                transition1.addInput(getPlaceName(problem.variables().get(i - 1), truth));
                transition1.addOutput(place);

                Transition transition2 = new Transition("T" + i + "_2_" + truthValue);
                transition2.addInput(getPlaceName(problem.variables().get(i - 1), !truth));
                transition2.addOutput(place);

                Player player = i % 2 == 0 ? Player.Controller : Player.Environment;
                game.addTransition(player, transition1);
                game.addTransition(player, transition2);
            }
        }

        return game;
    }

    private String getFormulaFromLiteral(QSATProblem.Literal literal) {
        return "(%s = 1)".formatted(getPlaceName(literal.variable(), !literal.negated()));
    }

    private String getPlaceName(String variable, boolean truth) {
        return "P" + variable + "_" + truth;
    }
}
