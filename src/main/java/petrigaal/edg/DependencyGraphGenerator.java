package petrigaal.edg;

import petrigaal.ctl.language.CTLFormula;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.expression.BinaryExpression;
import petrigaal.ctl.language.nodes.expression.IntegerLiteralExpression;
import petrigaal.ctl.language.nodes.expression.UnaryExpression;
import petrigaal.ctl.language.nodes.expression.VariableExpression;
import petrigaal.ctl.language.nodes.predicate.BooleanLiteral;
import petrigaal.ctl.language.nodes.predicate.RelationalPredicate;
import petrigaal.ctl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.BinaryTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryTemporal;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static petrigaal.ctl.language.Path.A;
import static petrigaal.ctl.language.Path.E;

public class DependencyGraphGenerator {
    Map<DGConfiguration, DGConfiguration> configurations = new HashMap<>();
    Queue<DGConfiguration> queue = new LinkedList<>();

    public int crawl(DGConfiguration c) {
        queue.add(c);
        configurations.put(c, c);

        do {
            DGConfiguration configuration = Objects.requireNonNull(queue.poll());
            configuration.getFormula().visit(new DGTarget(configuration), this);
        } while (!queue.isEmpty());

        clean();
        return configurations.size();
    }

    private void clean() {
        for (DGConfiguration conf : configurations.values()) {
            Set<DGEdge> edges = new HashSet<>(conf.getSuccessors().stream().toList());
            conf.getSuccessors().clear();
            conf.getSuccessors().addAll(edges);
        }
    }

    public void visitConjunction(DGTarget target, BinaryTemporal formula) {
        DGConfiguration c = target.getConfiguration();
        DGConfiguration c1 = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        DGConfiguration c2 = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        List<DGConfiguration> configurations = List.of(c1, c2);
        c.getSuccessors().add(new DGEdge(c, configurations.stream().map(DGTarget::new).toList()));
    }

    public void visitDisjunction(DGTarget target, BinaryTemporal formula) {
        DGConfiguration c = target.getConfiguration();
        DGConfiguration c1 = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        DGConfiguration c2 = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        c.getSuccessors().add(new DGEdge(c, c1));
        c.getSuccessors().add(new DGEdge(c, c2));
    }

    public void visitNegation(DGTarget target, UnaryTemporal formula) {
        DGConfiguration c = target.getConfiguration();
        DGConfiguration c1 = createOrGet(new DGConfiguration(
                formula.getFirstOperand(),
                c.getGame(),
                c.getMode()
        ));

        c1.setNegationDistance(Math.min(c.getNegationDistance() - 1, c1.getNegationDistance()));
        c.getSuccessors().add(new DGEdge(c, true, c1));
    }

    public void visitNext(DGTarget target, UnaryQuantifierTemporal formula) {
        DGConfiguration c = target.getConfiguration();

        if (!c.getMode()) {
            if (formula.getPath() == E) {
                nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .map(t -> new DGEdge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .map(t -> new DGEdge(c, t))
                        .forEach(c.getSuccessors()::add);

                Set<DGTarget> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula.getFirstOperand(), m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .collect(Collectors.toSet());
                if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                    c.getSuccessors().add(new DGEdge(c));
                }
                uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
            }
        } else {
            List<DGEdge> primaryAfterTransEdges;
            List<DGTarget> primaryAfterTrans = fireAllEnabled(formula, c.getGame(), Player.Controller, c.getMode());
            List<DGTarget> secondaryAfterTrans = fireAllEnabled(formula, c.getGame(), Player.Environment, c.getMode());

            if (!primaryAfterTrans.isEmpty() && secondaryAfterTrans.isEmpty()) {
                primaryAfterTransEdges = primaryAfterTrans.stream()
                        .map(t -> new DGEdge(c, t))
                        .toList();
            } else {
                primaryAfterTransEdges = secondaryAfterTrans.stream()
                        .map(cont -> addToList(c, primaryAfterTrans, cont))
                        .map(t -> new DGEdge(c, t))
                        .toList();
            }
            c.getSuccessors().addAll(primaryAfterTransEdges);
        }
    }

    public void visitUntil(DGTarget target, BinaryQuantifierTemporal formula) {
        DGConfiguration c = target.getConfiguration();
        DGConfiguration now = createOrGet(formula.getFirstOperand(), c.getGame(), c.getMode());
        DGConfiguration end = createOrGet(formula.getSecondOperand(), c.getGame(), c.getMode());

        if (formula.getPath() == E) {
            nextMarkingsWithTransitions(c)
                    .stream()
                    .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                    .map(t -> new DGEdge(c, t, new DGTarget(now)))
                    .forEach(c.getSuccessors()::add);
        } else {
            nextMarkingsWithTransitions(c, Player.Controller)
                    .stream()
                    .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                    .map(t -> new DGEdge(c, t, new DGTarget(now)))
                    .forEach(c.getSuccessors()::add);

            Set<DGTarget> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                    .stream()
                    .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                    .collect(Collectors.toSet());
            if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                c.getSuccessors().add(new DGEdge(c, new DGTarget(now)));
            }
            uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
        }

        c.getSuccessors().add(new DGEdge(c, end));
    }

    public void visitFinally(DGTarget target, UnaryQuantifierTemporal formula) {
        DGConfiguration c = target.getConfiguration();
        DGConfiguration now = createOrGet(new DGConfiguration(formula.getFirstOperand(), c.getGame(), c.getMode()));

        if (!c.getMode()) {
            if (formula.getPath() == E) {
                nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .map(t -> new DGEdge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .map(t -> new DGEdge(c, t))
                        .forEach(c.getSuccessors()::add);

                Set<DGTarget> uncontrollableTargets = nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .collect(Collectors.toSet());
                if (!uncontrollableTargets.isEmpty() && c.getSuccessors().isEmpty()) {
                    c.getSuccessors().add(new DGEdge(c));
                }
                uncontrollableTargets.forEach(t -> c.getSuccessors().forEach(e -> e.add(t)));
            }
        } else {
            List<DGTarget> targets;
            if (formula.getPath() == E) {
                targets = nextMarkingsWithTransitions(c, Player.Controller)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .toList();
                if (!targets.isEmpty()) c.getSuccessors().add(new DGEdge(c, targets));

                nextMarkingsWithTransitions(c, Player.Environment)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .map(t -> new DGEdge(c, t))
                        .forEach(c.getSuccessors()::add);
            } else {
                targets = nextMarkingsWithTransitions(c)
                        .stream()
                        .map(m -> new DGTarget(createOrGet(formula, m.getGame(), c.getMode()), m.getTransition(), c.getGame()))
                        .toList();
                if (!targets.isEmpty()) c.getSuccessors().add(new DGEdge(c, targets));
            }
        }
        c.getSuccessors().add(new DGEdge(c, now));
    }

    public void visitAlways(DGTarget target, UnaryQuantifierTemporal formula) {
        DGConfiguration c = target.getConfiguration();

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

    public void visit(DGTarget parent, UnaryTemporal unaryTemporal) {
        visitNegation(parent, unaryTemporal);
    }

    public void visit(DGTarget parent, BinaryQuantifierTemporal binaryQuantifierTemporal) {
        visitUntil(parent, binaryQuantifierTemporal);
    }

    public void visit(DGTarget parent, UnaryQuantifierTemporal unaryQuantifierTemporal) {
        String operator = unaryQuantifierTemporal.getOperator();
        switch (operator) {
            case "X" -> visitNext(parent, unaryQuantifierTemporal);
            case "G" -> visitAlways(parent, unaryQuantifierTemporal);
            case "F" -> visitFinally(parent, unaryQuantifierTemporal);
        }
    }

    public void visit(DGTarget parent, BinaryTemporal binaryTemporal) {
        switch (binaryTemporal.getOperator()) {
            case "&" -> visitConjunction(parent, binaryTemporal);
            case "|" -> visitDisjunction(parent, binaryTemporal);
            default -> throw new RuntimeException("Unexpected state");
        }
    }

    public void visit(DGTarget target, BooleanLiteral booleanLiteral) {
        DGConfiguration c = target.getConfiguration();

        if (booleanLiteral.getValue()) {
            c.getSuccessors().add(new DGEdge(null));
        }
    }

    public void visit(DGTarget target, RelationalPredicate relationalPredicate) {
        DGConfiguration c = target.getConfiguration();

        if (evaluatePredicate(relationalPredicate, c.getGame())) {
            c.getSuccessors().add(new DGEdge(c));
        }
    }

    private DGConfiguration createOrGet(CTLFormula formula, PetriGame game, boolean mode) {
        DGConfiguration config = new DGConfiguration(formula, game, mode);
        return createOrGet(config);
    }

    private DGConfiguration createOrGet(DGConfiguration config) {
        DGConfiguration get = configurations.get(config);

        if (get != null) {
            return get;
        } else {
            configurations.put(config, config);
            queue.add(config);
            return config;
        }
    }

    private DGEdge addToList(DGConfiguration c, List<DGTarget> environmentAfterTrans, DGTarget cont) {
        DGEdge e = new DGEdge(c, environmentAfterTrans);
        e.add(cont);
        return e;
    }

    private List<DGTarget> fireAllEnabled(UnaryTemporal formula, PetriGame game, Player player, boolean mode) {
        return nextMarkingsWithTransitions(game, player).stream()
                .map(g -> new DGTarget(createOrGet(formula.getFirstOperand(), g.getGame(), mode), g.getTransition(), game))
                .toList();
    }

    private List<TransitionMarkingPair> nextMarkingsWithTransitions(DGConfiguration c) {
        List<Transition> transitions = c.getGame().getEnabledTransitions();
        return transitions.stream().map(t -> TransitionMarkingPair.of(t, c.getGame().fire(t))).toList();
    }

    private List<TransitionMarkingPair> nextMarkingsWithTransitions(DGConfiguration c, Player player) {
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
