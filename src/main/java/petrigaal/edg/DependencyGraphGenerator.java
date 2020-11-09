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
import petrigaal.petri.Path;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static petrigaal.petri.Path.E;
import static petrigaal.petri.Path.A;

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
        Path primary = formula.getPath();
        Path secondary = primary == E ? A : E;

        List<Configuration> primaryAfterTrans = fireAllEnabled(formula, c.getGame(), primary);
        List<Configuration> secondaryAfterTrans = fireAllEnabled(formula, c.getGame(), secondary);

        List<Edge> primaryAfterTransEdges;
        if (!primaryAfterTrans.isEmpty() && secondaryAfterTrans.isEmpty()) {
            primaryAfterTransEdges = primaryAfterTrans.stream()
                    .map(Edge::new)
                    .collect(Collectors.toList());

        } else {
            primaryAfterTransEdges = secondaryAfterTrans.stream()
                    .map(cont -> addToList(primaryAfterTrans, cont))
                    .map(Edge::new)
                    .collect(Collectors.toList());
        }
        c.getSuccessors().addAll(primaryAfterTransEdges);
    }

    public void visitUntil(Configuration c, BinaryQuantifierTemporal formula) {
        Configuration end = createOrGet(formula.getSecondOperand(), c.getGame());

        List<Transition> transitions = c.getGame().getEnabledTransitions(formula.getPath());

        for (Transition transition : transitions) {
            PetriGame nextState = c.getGame().fire(transition);
            Configuration conf = createOrGet(formula, nextState);
            Configuration now = createOrGet(
                    new Configuration(formula.getFirstOperand(), c.getGame(), transition)
            );
            c.getSuccessors().add(new Edge(conf, now));
        }

        c.getSuccessors().add(new Edge(end));
    }

    public void visitFinally(Configuration c, UnaryQuantifierTemporal formula) {
        Configuration now = createOrGet(new Configuration(formula.getFirstOperand(), c.getGame(), c.getGenerator()));
        nextMarkings(c, formula.getPath())
                .stream()
                .map(m -> createOrGet(formula, m))
                .map(Edge::new)
                .forEach(c.getSuccessors()::add);
        c.getSuccessors().add(new Edge(now));
    }

    public void visitAlways(Configuration c, UnaryQuantifierTemporal formula) {
        UnaryTemporal notFormula = new UnaryTemporal();
        notFormula.setOperator("!");
        notFormula.setFirstOperand(formula.getFirstOperand());

        BinaryQuantifierTemporal bqt = new BinaryQuantifierTemporal();
        bqt.setPath(formula.getPath() == E ? A : E);
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
        String operator = unaryQuantifierTemporal.getOperator();
        switch (operator) {
            case "X" -> visitNext(parent, unaryQuantifierTemporal);
            case "G" -> visitAlways(parent, unaryQuantifierTemporal);
            case "F" -> visitFinally(parent, unaryQuantifierTemporal);
        }
    }

    public void visit(Configuration parent, BinaryTemporal binaryTemporal) {
        switch (binaryTemporal.getOperator()) {
            case "&" -> visitConjunction(parent, binaryTemporal);
            case "|" -> visitDisjunction(parent, binaryTemporal);
            default -> throw new RuntimeException("Unexpected state");
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
        return createOrGet(config);
    }

    private Configuration createOrGet(Configuration config) {
        Configuration get = configurations.get(config);

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

    private List<Configuration> fireAllEnabled(UnaryTemporal formula, PetriGame game, Path path) {
        return nextMarkings(game, path).stream()
                .map(g -> createOrGet(formula.getFirstOperand(), g))
                .collect(Collectors.toList());
    }

    private List<PetriGame> nextMarkings(Configuration c, Path path) {
        List<Transition> transitions = c.getGame().getEnabledTransitions(path);
        if (c.getGenerator() != null) {
            transitions = List.of(c.getGenerator());
            if (!c.getGame().isEnabled(c.getGenerator())) {
                return List.of();
            }
        }
        return transitions.stream().map(c.getGame()::fire).collect(Collectors.toList());
    }

    private List<PetriGame> nextMarkings(PetriGame game, Path path) {
        List<Transition> transitions = game.getEnabledTransitions(path);
        return transitions.stream().map(game::fire).collect(Collectors.toList());
    }

    private boolean evaluatePredicate(RelationalPredicate predicate, PetriGame game) {
        int v1 = evaluateExpression(game, predicate.getFirstOperand());
        int v2 = evaluateExpression(game, predicate.getSecondOperand());
        return switch (predicate.getOperator()) {
            case "<" -> v1 < v2;
            case "<=" -> v1 <= v2;
            case "=" -> v1 == v2;
            case ">=" -> v1 >= v2;
            case ">" -> v1 > v2;
            default -> throw new RuntimeException("Unsupported Operator: " + predicate.getOperator());
        };
    }

    private int evaluateExpression(PetriGame game, Expression expr) {
        if (expr instanceof IntegerLiteralExpression) {
            return ((IntegerLiteralExpression) expr).getValue();
        } else if (expr instanceof VariableExpression) {
            return game.getMarking(((VariableExpression) expr).getIdentifier());
        } else if (expr instanceof UnaryExpression) {
            int op1 = evaluateExpression(game, ((UnaryExpression) expr).getFirstOperand());
            return ((UnaryExpression) expr).getOperator().equals("-") ? -op1 : op1;
        } else if (expr instanceof BinaryExpression) {
            String operator = ((BinaryExpression) expr).getOperator();
            int op1 = evaluateExpression(game, ((BinaryExpression) expr).getFirstOperand());
            int op2 = evaluateExpression(game, ((BinaryExpression) expr).getSecondOperand());

            return switch (operator) {
                case "+" -> op1 + op2;
                case "-" -> op1 - op2;
                case "*" -> op1 * op2;
                default -> throw new RuntimeException("Unsupported operator");
            };
        } else {
            throw new RuntimeException("Unsupported operation");
        }
    }
}
