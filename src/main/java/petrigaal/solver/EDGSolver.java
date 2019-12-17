package petrigaal.solver;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.Configuration;
import petrigaal.edg.Edge;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class EDGSolver {
    private List<Configuration> visited;
    private boolean edgeRemoved;
    private boolean edgeNegated;
    private boolean evaluateNegations;
    private Consumer<Configuration> consumer;
    private Configuration firstConfig;
    private Queue<Pair<Edge, Configuration>> queue;

    public String solve(Configuration c, Consumer<Configuration> consumer) {
        queue = new LinkedList<>();
        
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
                visit(e, c);
            } while (edgeRemoved && e.contains(c));

            visited = new ArrayList<>();
            evaluateNegations = true;

            visit(e, c);
        } while (edgeNegated);

        return "Can solve: " + !e.contains(c);
    }

    private void visit(Edge in, Configuration c) {
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
                visitEdge(edge);
            }
        }
    }

    private void visitEdge(Edge e) {
        List<Configuration> configurations = new ArrayList<>(e);
        for (Configuration c : configurations) {
            visit(e, c);
        }
    }

    private void propagateEmptySet(Edge in, Configuration c, Edge edge) {
        edgeRemoved = true;
        in.remove(c);
        c.getSuccessors().retainAll(List.of(edge));
    }
}
