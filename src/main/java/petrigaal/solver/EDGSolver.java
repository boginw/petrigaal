package petrigaal.solver;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static petrigaal.solver.EDGSolver.AssignmentValue.*;

public class EDGSolver {
    private final int workers = Runtime.getRuntime().availableProcessors();
    private BiConsumer<Integer, Integer> consumer;

    private final Queue<Edge> waitingHyperEdges = new LinkedList<>();
    private final Set<Edge> waitingNegationEdges = new HashSet<>();
    private final Map<Configuration, AssignmentValue> assignment = new HashMap<>();
    private Configuration root;
    private boolean done = false;

    public Map<Configuration, Boolean> solve(Configuration c, BiConsumer<Integer, Integer> consumer) {
        root = c;
        done = false;

        explore(new Target(c, null));
        while (!done && (!waitingHyperEdges.isEmpty() || !waitingNegationEdges.isEmpty())) {
            if (!waitingHyperEdges.isEmpty()) {
                processHyperEdge(waitingHyperEdges.poll());
            } else {
                Optional<Edge> allowedNegationEdge = findAllowedNegationEdge();

                if (allowedNegationEdge.isPresent()) {
                    waitingNegationEdges.remove(allowedNegationEdge.get());
                    processNegationEdge(allowedNegationEdge.get());
                } else {
                    Optional<Edge> minDistance = waitingNegationEdges.stream()
                            .min(Comparator.comparing(p -> p.getSource().getNegationDistance()));

                    if (minDistance.isPresent()) {
                        waitingNegationEdges.remove(minDistance.get());
                        processNegationEdge(minDistance.get());
                    }
                }
            }
        }

        Map<Configuration, Boolean> propagationByConfiguration = new HashMap<>();
        assignment.forEach((k, v) -> propagationByConfiguration.put(k, v == TRUE));

        System.out.println("Can solve: " + propagationByConfiguration.get(c));
        return propagationByConfiguration;
    }

    private void processHyperEdge(Edge edge) {
        if (edge == null) return;

        if (edge.stream().allMatch(targetIsAssigned(TRUE))) {
            finalAssign(edge.getSource(), AssignmentValue.TRUE);
        } else if (edge.stream().anyMatch(targetIsAssigned(FALSE))) {
            deleteEdge(edge);
        } else {
            edge.stream().filter(targetIsAssigned(BOT, UNKNOWN))
                    .filter(targetIsAssigned(BOT))
                    .collect(Collectors.toSet())
                    .forEach(this::explore);
        }
    }

    private void processNegationEdge(Edge edge) {
        if (edge.stream().allMatch(targetIsAssigned(UNKNOWN, FALSE))) {
            finalAssign(edge.getSource(), AssignmentValue.TRUE);
        } else if (edge.stream().allMatch(targetIsAssigned(TRUE))) {
            deleteEdge(edge);
        } else if (edge.stream().allMatch(targetIsAssigned(BOT))) {
            waitingNegationEdges.add(edge);
            explore(edge.get(0));
        }
    }

    private void explore(Target target) {
        Configuration configuration = target.getConfiguration();

        assignment.put(configuration, UNKNOWN);

        if (configuration.getSuccessors().isEmpty()) {
            finalAssign(configuration, AssignmentValue.FALSE);
        } else {
            for (Edge successor : configuration.getSuccessors()) {
                if (successor.isNegated()) waitingNegationEdges.add(successor);
                else waitingHyperEdges.add(successor);
            }
        }
    }

    private void deleteEdge(Edge edge) {
        Configuration source = edge.getSource();
        source.getSuccessors().remove(edge);

        if (source.getSuccessors().isEmpty()) {
            finalAssign(source, FALSE);
        }

        if (edge.isNegated()) {
            waitingNegationEdges.remove(edge);
        } else {
            waitingHyperEdges.remove(edge);
        }

        edge.stream().flatMap(t -> getDependantsOf(t.getConfiguration()).stream())
                .forEach(e -> e.getSource().getSuccessors().remove(edge));
    }

    private void finalAssign(Configuration configuration, AssignmentValue value) {
        assignment.put(configuration, value);
        if (configuration.equals(root)) {
            done = true;
            return;
        }

        Set<Edge> dependants = getDependantsOf(configuration);

        for (Edge dependant : dependants) {
            if (dependant.isNegated()) waitingNegationEdges.add(dependant);
            else waitingHyperEdges.add(dependant);
        }
    }

    private Set<Edge> getDependantsOf(Configuration configuration) {
        return assignment.keySet().stream()
                .flatMap(c -> c.getSuccessors().stream())
                .filter(e -> e.stream().anyMatch(t -> t.getConfiguration().equals(configuration)))
                .collect(Collectors.toSet());
    }

    private Optional<Edge> findAllowedNegationEdge() {
        return waitingNegationEdges.stream()
                .filter(e -> targetIsAssigned(FALSE, TRUE, BOT).test(e.get(0)))
                .findFirst();
    }

    private Predicate<Target> targetIsAssigned(AssignmentValue... assignmentValues) {
        return t -> Arrays.asList(assignmentValues).contains(assignment.getOrDefault(t.getConfiguration(), BOT));
    }

    public enum AssignmentValue {
        BOT, UNKNOWN, TRUE, FALSE
    }
}
