package petrigaal.solver;

import petrigaal.Configuration;
import petrigaal.edg.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EDGSolver {
    private List<Configuration> visited;
    private boolean edgeRemoved;
    private boolean edgeNegated;
    private boolean evaluateNegations;
    private Consumer<Configuration> consumer;
    private Configuration firstConfig;

    public String solve(Configuration c, Consumer<Configuration> consumer) {
        this.consumer = consumer;
        firstConfig = c;
        Edge e = new Edge(c);

        do {
            edgeNegated = false;
            evaluateNegations = false;

            do {
                consumer.accept(firstConfig);
                edgeRemoved = false;
                visited = new ArrayList<>();
                visit(null, e, c);
            } while (edgeRemoved && e.contains(c));

            visited = new ArrayList<>();
            evaluateNegations = true;

            visit(null, e, c);
        } while (edgeNegated);

        return "Can solve: " + !e.contains(c);
    }

    private void visit(Configuration parent, Edge in, Configuration c) {
        if (visited.contains(c)) {
            return;
        }

        visited.add(c);

        List<Edge> successors = new ArrayList<>(c.getSuccessors());

        if (successors.isEmpty() && in.isNegated()) {
            in.setNegated(false);
            in.clear();
            return;
        }

        for (Edge edge : successors) {
            if (edge.isNegated() && edge.isEmpty()) {
                c.getSuccessors().remove(edge);
                consumer.accept(firstConfig);
            } else if (evaluateNegations && edge.isNegated() && !edge.isEmpty()) {
                edgeNegated = true;
                in.setNegated(false);
                edge.setNegated(false);
                edge.clear();
                propagateEmptySet(in, c, edge);
                consumer.accept(firstConfig);
                break;
            } else if (edge.isEmpty() && !edge.isNegated()) {
                propagateEmptySet(in, c, edge);

                consumer.accept(firstConfig);
                break;
            } else {
                visitEdge(c, edge);
            }
        }
    }

    private void visitEdge(Configuration parent, Edge e) {
        List<Configuration> configurations = new ArrayList<>(e);
        for (Configuration c : configurations) {
            visit(parent, e, c);
        }
    }

    private void propagateEmptySet(Edge in, Configuration c, Edge edge) {
        edgeRemoved = true;
        in.remove(c);
        c.getSuccessors().retainAll(List.of(edge));
    }
}
