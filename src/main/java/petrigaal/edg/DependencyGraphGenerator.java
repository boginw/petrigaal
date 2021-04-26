package petrigaal.edg;

import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.expression.BinaryExpression;
import petrigaal.atl.language.nodes.expression.IntegerLiteralExpression;
import petrigaal.atl.language.nodes.expression.UnaryExpression;
import petrigaal.atl.language.nodes.expression.VariableExpression;
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

import static petrigaal.atl.language.Path.A;
import static petrigaal.atl.language.Path.E;

public class DependencyGraphGenerator {
    Map<Configuration, Configuration> configurations = new HashMap<>();
    Queue<Configuration> queue = new LinkedList<>();

    public int crawl(Configuration c) {
        queue.add(c);
        configurations.put(c, c);

        do {
            Configuration configuration = Objects.requireNonNull(queue.poll());
            configuration.getFormula().visit(new Target(configuration), this);
        } while (!queue.isEmpty());

        return configurations.size();
    }

    public void visitConjunction(Target target, BinaryTemporal formula) {
        Configuration c = target.getConfiguration();
        Configuration c1 = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        Configuration c2 = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        List<Configuration> configurations = List.of(c1, c2);
        c.getSuccessors().add(new Edge(c, configurations.stream().map(Target::new).toList()));
    }

    public void visitDisjunction(Target target, BinaryTemporal formula) {
        Configuration c = target.getConfiguration();
        Configuration c1 = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        Configuration c2 = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        c.getSuccessors().add(new Edge(c, c1));
        c.getSuccessors().add(new Edge(c, c2));
    }

    public void visitNegation(Target target, UnaryTemporal formula) {
        Configuration c = target.getConfiguration();
        Configuration c1 = createOrGet(new Configuration(
                formula.getFirstOperand(),
                c.getGame(),
                c.getMode()
        ));

        c1.setNegationDistance(Math.min(c.getNegationDistance() - 1, c1.getNegationDistance()));
        c.getSuccessors().add(new Edge(c, true, c1));
    }

    public void visitNext(Target target, UnaryQuantifierTemporal formula) {
        Configuration c = target.getConfiguration();

        if (!c.getMode()) {
            if (formula.getPath() == E) {
                nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new Target(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition()))
                        .map(t -> new Edge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new Target(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition()))
                        .map(t -> new Edge(c, t))
                        .forEach(c.getSuccessors()::add);

                Set<Target> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new Target(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition()))
                        .collect(Collectors.toSet());
                if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                    c.getSuccessors().add(new Edge(c));
                }
                uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
            }
        } else {
            List<Edge> primaryAfterTransEdges;
            List<Target> primaryAfterTrans = fireAllEnabled(formula, c.getGame(), Player.Controller, c.getMode());
            List<Target> secondaryAfterTrans = fireAllEnabled(formula, c.getGame(), Player.Environment, c.getMode());

            if (!primaryAfterTrans.isEmpty() && secondaryAfterTrans.isEmpty()) {
                primaryAfterTransEdges = primaryAfterTrans.stream()
                        .map(t -> new Edge(c, t))
                        .toList();
            } else {
                primaryAfterTransEdges = secondaryAfterTrans.stream()
                        .map(cont -> addToList(c, primaryAfterTrans, cont))
                        .map(t -> new Edge(c, t))
                        .toList();
            }
            c.getSuccessors().addAll(primaryAfterTransEdges);
        }
    }

    public void visitUntil(Target target, BinaryQuantifierTemporal formula) {
        Configuration c = target.getConfiguration();
        Configuration now = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        Configuration end = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        if (formula.getPath() == E) {
            nextMarkingsWithTransitions(c)
                    .stream()
                    .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                    .map(t -> new Edge(c, t, new Target(now)))
                    .forEach(c.getSuccessors()::add);
        } else {
            nextMarkingsWithTransitions(c, Player.Controller)
                    .stream()
                    .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                    .map(t -> new Edge(c, t, new Target(now)))
                    .forEach(c.getSuccessors()::add);

            Set<Target> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                    .stream()
                    .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                    .collect(Collectors.toSet());
            if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                c.getSuccessors().add(new Edge(c, new Target(now)));
            }
            uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
        }

        c.getSuccessors().add(new Edge(c, end));
    }

    public void visitFinally(Target target, UnaryQuantifierTemporal formula) {
        Configuration c = target.getConfiguration();
        Configuration now = createOrGet(new Configuration(formula.getFirstOperand(), c.getGame(), c.getMode()));

        if (!c.getMode()) {
            if (formula.getPath() == E) {
                nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .map(t -> new Edge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .map(t -> new Edge(c, t))
                        .forEach(c.getSuccessors()::add);

                Set<Target> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .collect(Collectors.toSet());
                if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                    c.getSuccessors().add(new Edge(c));
                }
                uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
            }
        } else {
            List<Target> targets;
            if (formula.getPath() == E) {
                targets = nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .toList();
                if (!targets.isEmpty()) c.getSuccessors().add(new Edge(c, targets));

                nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .map(t -> new Edge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                targets = nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new Target(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition()))
                        .toList();
                if (!targets.isEmpty()) c.getSuccessors().add(new Edge(c, targets));
            }
        }
        c.getSuccessors().add(new Edge(c, now));
    }

    public void visitAlways(Target target, UnaryQuantifierTemporal formula) {
        Configuration c = target.getConfiguration();

        UnaryTemporal notFormula = new UnaryTemporal();
        notFormula.setOperator("!");
        notFormula.setFirstOperand(formula.getFirstOperand());

        UnaryQuantifierTemporal bqt = new UnaryQuantifierTemporal();
        bqt.setPath(formula.getPath() == E ? A : E);
        bqt.setFirstOperand(notFormula);
        bqt.setOperator("F");

        UnaryTemporal ut = new UnaryTemporal();
        ut.setOperator("!");
        ut.setFirstOperand(bqt);

        ut.visit(target.withConfiguration(c), this);
    }

    public void visit(Target parent, UnaryTemporal unaryTemporal) {
        visitNegation(parent, unaryTemporal);
    }

    public void visit(Target parent, BinaryQuantifierTemporal binaryQuantifierTemporal) {
        visitUntil(parent, binaryQuantifierTemporal);
    }

    public void visit(Target parent, UnaryQuantifierTemporal unaryQuantifierTemporal) {
        String operator = unaryQuantifierTemporal.getOperator();
        switch (operator) {
            case "X" -> visitNext(parent, unaryQuantifierTemporal);
            case "G" -> visitAlways(parent, unaryQuantifierTemporal);
            case "F" -> visitFinally(parent, unaryQuantifierTemporal);
        }
    }

    public void visit(Target parent, BinaryTemporal binaryTemporal) {
        switch (binaryTemporal.getOperator()) {
            case "&" -> visitConjunction(parent, binaryTemporal);
            case "|" -> visitDisjunction(parent, binaryTemporal);
            default -> throw new RuntimeException("Unexpected state");
        }
    }

    public void visit(Target target, BooleanLiteral booleanLiteral) {
        Configuration c = target.getConfiguration();

        if (booleanLiteral.getValue()) {
            c.getSuccessors().add(new Edge(null));
        }
    }

    public void visit(Target target, RelationalPredicate relationalPredicate) {
        Configuration c = target.getConfiguration();

        if (evaluatePredicate(relationalPredicate, c.getGame())) {
            c.getSuccessors().add(new Edge(c));
        }
    }

    private Configuration createOrGet(ATLFormula formula, PetriGame game, boolean mode) {
        Configuration config = new Configuration(formula, game, mode);
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

    private Edge addToList(Configuration c, List<Target> environmentAfterTrans, Target cont) {
        Edge e = new Edge(c, environmentAfterTrans);
        e.add(cont);
        return e;
    }

    private List<Target> fireAllEnabled(UnaryTemporal formula, PetriGame game, Player player, boolean mode) {
        return nextMarkingsWithTransitions(game, player).stream()
                .map(g -> new Target(createOrGet(formula.getFirstOperand(), g.getGame(), mode), g.getTransition()))
                .toList();
    }

    private List<TransitionMarkingPair> nextMarkingsWithTransitions(Configuration c) {
        List<Transition> transitions = c.getGame().getEnabledTransitions();
        return transitions.stream().map(t -> TransitionMarkingPair.of(t, c.getGame().fire(t))).toList();
    }

    private List<TransitionMarkingPair> nextMarkingsWithTransitions(Configuration c, Player player) {
        List<Transition> transitions = c.getGame().getEnabledTransitions(player);
        return transitions.stream().map(t -> TransitionMarkingPair.of(t, c.getGame().fire(t))).toList();
    }

    private List<TransitionMarkingPair> nextMarkingsWithTransitions(PetriGame game, Player player) {
        List<Transition> transitions = game.getEnabledTransitions(player);
        return transitions.stream().map(t -> TransitionMarkingPair.of(t, game.fire(t))).toList();
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

    private static class TransitionMarkingPair {
        private final Transition key;
        private final PetriGame value;

        private TransitionMarkingPair(Transition transition, PetriGame game) {
            this.key = transition;
            this.value = game;
        }

        public static TransitionMarkingPair of(Transition transition, PetriGame game) {
            return new TransitionMarkingPair(transition, game);
        }

        public Transition getTransition() {
            return key;
        }

        public PetriGame getGame() {
            return value;
        }
    }
}
