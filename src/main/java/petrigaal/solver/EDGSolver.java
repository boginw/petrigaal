package petrigaal.solver;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;

public class EDGSolver {
    private static final int REPORT_LIMIT = 10;
    private List<Configuration> visited;
    private boolean edgeRemoved;
    private boolean edgeNegated;
    private boolean evaluateNegations;
    private BiConsumer<Integer, Integer> consumer;
    private Queue<Pair<Edge, Configuration>> queue;
    private final int threads = 4;
    private int report = 0;

    public String solve(Configuration c, BiConsumer<Integer, Integer> consumer) {
        this.queue = new LinkedList<>();
        this.visited = new ArrayList<>();
        this.consumer = consumer;
        Edge e = new Edge(c);

        do {
            edgeNegated = false;
            evaluateNegations = false;

            do {
                edgeRemoved = false;
                visitQueue(new Pair<>(e, c));
            } while (edgeRemoved && isConfigurationTargetOfEdge(c, e));

            evaluateNegations = true;

            System.out.println("Negations");
            visitQueue(new Pair<>(e, c));
        } while (edgeNegated);

        return "Can solve: " + !isConfigurationTargetOfEdge(c, e);
    }

    private void visitQueue(Pair<Edge, Configuration> start) {
        visited.clear();
        queue.clear();
        queue.add(start);

        Pair<Edge, Configuration> pair;
        while ((pair = queue.poll()) != null) {
            if (report++ % REPORT_LIMIT == 0) {
                consumer.accept(queue.size(), visited.size());
            }

            visit(pair);
        }
    }

    private void visit(Pair<Edge, Configuration> pair) {
        visit(pair.a, pair.b);
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
            } else if (evaluateNegations && edge.isNegated() && !edge.isEmpty()) {
                edgeNegated = true;
                in.setNegated(false);
                edge.setNegated(false);
                edge.clear();
                propagateEmptySet(in, c, edge);
                break;
            } else if (edge.isEmpty() && !edge.isNegated()) {
                propagateEmptySet(in, c, edge);
                break;
            } else {
                visitEdge(edge);
            }
        }
    }

    private void visitEdge(Edge e) {
        List<Target> targets = new ArrayList<>(e);

        for (Target t : targets) {
            queue.add(new Pair<>(e, t.getConfiguration()));
            // visit(e, c);
        }
    }

    private void propagateEmptySet(Edge in, Configuration c, Edge edge) {
        edgeRemoved = true;
        in.removeIf(t -> t.getConfiguration().equals(c));
        c.getSuccessors().retainAll(List.of(edge));
    }

    private static boolean isConfigurationTargetOfEdge(Configuration c, Edge e) {
        return e.stream().anyMatch(t -> t.getConfiguration().equals(c));
    }
}
