package petrigaal.solver;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.Configuration;
import petrigaal.edg.Edge;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class EDGSolver {
    private List<Configuration> visited;
    private boolean edgeRemoved;
    private boolean edgeNegated;
    private boolean evaluateNegations;
    private BiConsumer<Integer, Integer> consumer;
    private Configuration firstConfig;
    private Queue<Pair<Edge, Configuration>> queue;
    private int threads = 4;

    public String solve(Configuration c, BiConsumer<Integer, Integer> consumer) {
        this.queue = new LinkedList<>();
        this.visited = new ArrayList<>();
        this.consumer = consumer;
        firstConfig = c;
        Edge e = new Edge(c);

        do {
            edgeNegated = false;
            evaluateNegations = false;

            do {
                edgeRemoved = false;
                visitQueue(new Pair<>(e, c));
            } while (edgeRemoved && e.contains(c));

            evaluateNegations = true;

            System.out.println("Negations");
            visitQueue(new Pair<>(e, c));
        } while (edgeNegated);

        return "Can solve: " + !e.contains(c);
    }

    private void visitQueue(Pair<Edge, Configuration> start) {
        int report = 0;

        visited.clear();
        queue.clear();
        queue.add(start);

        Pair<Edge, Configuration> pair;
        while ((pair = queue.poll()) != null) {
            if (report++ % 10000 == 0) {
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
        List<Configuration> configurations = new ArrayList<>(e);

        for (Configuration c : configurations) {
            queue.add(new Pair<>(e, c));
            // visit(e, c);
        }
    }

    private void propagateEmptySet(Edge in, Configuration c, Edge edge) {
        edgeRemoved = true;
        in.remove(c);
        c.getSuccessors().retainAll(List.of(edge));
    }
}
