package petrigaal.edg;

import petrigaal.Configuration;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.QuantifierTemporal;
import petrigaal.atl.language.nodes.expression.*;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.atl.language.nodes.predicate.RelationalPredicate;
import petrigaal.atl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryTemporal;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static petrigaal.petri.Player.Controller;
import static petrigaal.petri.Player.Environment;

public class DependencyGraphGenerator {
    Map<Configuration, Configuration> configurations = new HashMap<>();
    Queue<Configuration> queue = new LinkedList<>();

    public int crawl(Configuration c) {
        queue.add(c);
        configurations.put(c, c);

        do {
            Configuration configuration = Objects.requireNonNull(queue.poll());
            configuration.getFormula().visit(configuration, this);
        } while (!queue.isEmpty());

        return configurations.size();
    }

    public void visitConjunction(Configuration c, BinaryTemporal formula) {
        Configuration c1 = createOrGet(formula.getFirstOperand(), c.getGame());
        Configuration c2 = createOrGet(formula.getSecondOperand(), c.getGame());

        List<Configuration> configurations = List.of(c1, c2);
        c.getSuccessors().add(new Edge(configurations));
    }

    public void visitDisjunction(Configuration c, BinaryTemporal formula) {
        Configuration c1 = createOrGet(formula.getFirstOperand(), c.getGame());
        Configuration c2 = createOrGet(formula.getSecondOperand(), c.getGame());

        c.getSuccessors().add(new Edge(c1));
        c.getSuccessors().add(new Edge(c2));
    }

    public void visitNegation(Configuration c, UnaryTemporal formula) {
        Configuration c1 = createOrGet(formula.getFirstOperand(), c.getGame());

        c.getSuccessors().add(new Edge(true, c1));
    }

    public void visitNext(Configuration c, UnaryQuantifierTemporal formula) {
        Player primary = formula.getPlayer();
        Player secondary = primary == Controller ? Environment : Controller;

        List<Configuration> primaryAfterTrans = fireAllEnabled(formula, c.getGame(), primary);
        List<Configuration> secondaryAfterTrans = fireAllEnabled(formula, c.getGame(), secondary);

        if (!primaryAfterTrans.isEmpty() && secondaryAfterTrans.isEmpty()) {
            List<Edge> primaryAfterTransEdges = primaryAfterTrans.stream()
                    .map(Edge::new)
                    .collect(Collectors.toList());

            c.getSuccessors().addAll(primaryAfterTransEdges);
        } else {
            List<Edge> primaryAfterTransEdges = secondaryAfterTrans.stream()
                    .map(cont -> addToList(primaryAfterTrans, cont))
                    .map(Edge::new)
                    .collect(Collectors.toList());

            c.getSuccessors().addAll(primaryAfterTransEdges);
        }
    }

    public void visitUntil(Configuration c, BinaryQuantifierTemporal formula) {
        Configuration end = createOrGet(formula.getSecondOperand(), c.getGame());
        Configuration now = createOrGet(formula.getFirstOperand(), c.getGame());
        Configuration next = createOrGet(nextTime(formula), c.getGame());

        c.getSuccessors().add(new Edge(next, now));
        c.getSuccessors().add(new Edge(end));
    }

    public void visitAlways(Configuration c, UnaryQuantifierTemporal formula) {
        /*Configuration end = createOrGet(deadlock(), c.getGame());
        Configuration now = createOrGet(formula.getFirstOperand(), c.getGame());
        Configuration next = createOrGet(nextTime(formula), c.getGame());

        c.getSuccessors().add(new Edge(now, next));
        c.getSuccessors().add(new Edge(now, end));*/

        UnaryTemporal notFormula = new UnaryTemporal();
        notFormula.setOperator("!");
        notFormula.setFirstOperand(formula.getFirstOperand());

        BinaryQuantifierTemporal bqt = new BinaryQuantifierTemporal();
        bqt.setPlayer(formula.getPlayer() == Controller ? Environment : Controller);
        bqt.setFirstOperand(new BooleanLiteral(true));
        bqt.setOperator("U");
        bqt.setSecondOperand(notFormula);

        UnaryTemporal ut = new UnaryTemporal();
        ut.setOperator("!");
        ut.setFirstOperand(bqt);

        ut.visit(c, this);
    }

    public void visit(Configuration parent, UnaryTemporal unaryTemporal) {
        visitNegation(parent, unaryTemporal);
    }

    public void visit(Configuration parent, BinaryQuantifierTemporal binaryQuantifierTemporal) {
        visitUntil(parent, binaryQuantifierTemporal);
    }

    public void visit(Configuration parent, UnaryQuantifierTemporal unaryQuantifierTemporal) {
        if (unaryQuantifierTemporal.getOperator().equals("@")) {
            visitNext(parent, unaryQuantifierTemporal);
        } else {
            visitAlways(parent, unaryQuantifierTemporal);
        }
    }

    public void visit(Configuration parent, BinaryTemporal binaryTemporal) {
        switch (binaryTemporal.getOperator()) {
            case "&":
                visitConjunction(parent, binaryTemporal);
                break;
            case "|":
                visitDisjunction(parent, binaryTemporal);
                break;
            default:
                throw new RuntimeException("Unexpected state");
        }
    }

    public void visit(Configuration c, BooleanLiteral booleanLiteral) {
        if (booleanLiteral.getValue()) {
            c.getSuccessors().add(new Edge());
        }
    }

    public void visit(Configuration c, RelationalPredicate relationalPredicate) {
        if (evaluatePredicate(relationalPredicate, c.getGame())) {
            c.getSuccessors().add(new Edge());
        }
    }

    private Configuration createOrGet(ATLFormula formula, PetriGame game) {
        Configuration config = new Configuration(formula, game);
        Configuration get = configurations.get(config);

        /*Optional<Configuration> opt = configurations.keySet()
                .stream()
                .filter(c -> c.equals(config))
                .findFirst();

        if (opt.isPresent()) {
            return opt.get();*/
        if (get != null) {
            return get;
        } else {
            configurations.put(config, config);
            queue.add(config);
            return config;
        }
    }

    private Edge addToList(List<Configuration> environmentAfterTrans, Configuration cont) {
        Edge e = new Edge(environmentAfterTrans);
        e.add(cont);
        return e;
    }

    private List<Configuration> fireAllEnabled(UnaryTemporal formula, PetriGame game, Player player) {
        List<Transition> transitions = game.getEnabledTransitions(player);

        return transitions.stream()
                .map(game::fire)
                .map(g -> createOrGet(formula.getFirstOperand(), g))
                .collect(Collectors.toList());
    }

    private boolean evaluatePredicate(RelationalPredicate predicate, PetriGame game) {
        int v1 = evaluateExpression(game, predicate.getFirstOperand());
        int v2 = evaluateExpression(game, predicate.getSecondOperand());
        switch (predicate.getOperator()) {
            case "<":
                return v1 < v2;
            case "<=":
                return v1 <= v2;
            case "=":
                return v1 == v2;
            case ">=":
                return v1 >= v2;
            case ">":
                return v1 > v2;
            default:
                throw new RuntimeException("Unsupported Operator: " + predicate.getOperator());
        }
    }

    private int evaluateExpression(PetriGame game, Expression expr) {
        if (expr instanceof IntegerLiteralExpression) {
            return ((IntegerLiteralExpression) expr).getValue();
        } else if (expr instanceof EnabledActions) {
            return (int) game.getTransitions(((EnabledActions) expr).getForPlayer())
                    .stream().filter(game::isEnabled).count() + 1;
        } else if (expr instanceof VariableExpression) {
            return game.getMarking(((VariableExpression) expr).getIdentifier());
        } else if (expr instanceof UnaryExpression) {
            int op1 = evaluateExpression(game, ((UnaryExpression) expr).getFirstOperand());
            return ((UnaryExpression) expr).getOperator().equals("-") ? -op1 : op1;
        } else if (expr instanceof BinaryExpression) {
            String operator = ((BinaryExpression) expr).getOperator();
            int op1 = evaluateExpression(game, ((BinaryExpression) expr).getFirstOperand());
            int op2 = evaluateExpression(game, ((BinaryExpression) expr).getSecondOperand());

            switch (operator) {
                case "+":
                    return op1 + op2;
                case "-":
                    return op1 - op2;
                case "*":
                    return op1 * op2;
                default:
                    throw new RuntimeException("Unsupported operator");
            }
        } else {
            throw new RuntimeException("Unsupported operation");
        }
    }

    private UnaryQuantifierTemporal nextTime(QuantifierTemporal formula) {
        UnaryQuantifierTemporal nextFormula = new UnaryQuantifierTemporal();
        nextFormula.setPlayer(formula.getPlayer());
        nextFormula.setOperator("@");
        nextFormula.setFirstOperand(formula);
        return nextFormula;
    }

    private BinaryTemporal deadlock() {
        RelationalPredicate rp1 = playerDeadlock("d1");
        RelationalPredicate rp2 = playerDeadlock("d2");

        return new BinaryTemporal(rp1, "&", rp2);
    }

    private RelationalPredicate playerDeadlock(String enabledActions) {
        RelationalPredicate playerDeadlock = new RelationalPredicate();
        playerDeadlock.setFirstOperand(new EnabledActions(enabledActions));
        playerDeadlock.setOperator("=");
        playerDeadlock.setSecondOperand(new IntegerLiteralExpression(1));
        return playerDeadlock;
    }
}
