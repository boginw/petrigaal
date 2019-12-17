package petrigaal.solver;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.Configuration;
import petrigaal.edg.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class EDGSolver {
    private List<Configuration> visited;
    private boolean edgeRemoved;
    private boolean edgeNegated;
    private boolean evaluateNegations;
    private BiConsumer<Long, Integer> consumer;
    private Configuration firstConfig;
    private ConcurrentLinkedQueue<Pair<Edge, Configuration>> queue;
    private int threads = 4;

    public String solve(Configuration c, BiConsumer<Long, Integer> consumer) {
        this.queue = new ConcurrentLinkedQueue<>();
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
        AtomicInteger report = new AtomicInteger(0);

        visited.clear();
        queue.clear();
        queue.add(start);
        CountDownLatch latch = new CountDownLatch(threads);

        visit(Objects.requireNonNull(queue.poll()));

        while (!queue.isEmpty() && queue.size() < threads) {
            visit(queue.poll());
        }

        System.out.println(queue.size());

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                Pair<Edge, Configuration> pair;

                while ((pair = queue.poll()) != null) {
                    if (report.incrementAndGet() % 10000 == 0) {
                        consumer.accept(latch.getCount(), visited.size());
                    }

                    visit(pair);
                }

                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
