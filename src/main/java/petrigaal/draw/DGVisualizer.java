package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.*;
import java.util.function.Predicate;

public abstract class DGVisualizer<C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> {
    protected final Map<C, Boolean> propagationByConfiguration;
    private final Map<C, Set<Integer>> configToJointIdEdges = new HashMap<>();
    private final Map<Integer, Set<T>> jointIdToTargetEdges = new HashMap<>();
    private final Map<Integer, Integer> jointIdToEmptyIdEdges = new HashMap<>();
    private final Map<Integer, List<String>> ranks = new HashMap<>();
    private final Set<C> visitedConfigs = new LinkedHashSet<>();
    private final Queue<Pair<C, Integer>> queue = new LinkedList<>();
    private final C configuration;
    protected boolean displayOnlyConfigurationsWhichPropagateOne = false;
    private int empties = 0;
    private int joints = 0;

    public DGVisualizer(C configuration, Map<C, Boolean> propagationByConfiguration) {
        this.propagationByConfiguration = propagationByConfiguration;
        this.configuration = configuration;
    }

    public String draw() {
        queue.clear();
        queue.add(new Pair<>(configuration, 0));

        while (!queue.isEmpty()) {
            visit(queue.poll());
        }

        List<String> vertices = new ArrayList<>();
        List<String> edges = new ArrayList<>();

        List<String> configs = visitedConfigs.stream()
                .filter(Predicate.not(this::shouldSkipConfiguration))
                .map(this::declareConfiguration)
                .toList();

        vertices.addAll(configs);

        for (int i = 1; i <= joints; i++) {
            vertices.add(declareJoint(i));
        }

        for (int i = 0; i < empties; i++) {
            vertices.add(declareEmpty(i));
        }

        List<String> configEdges = configToJointIdEdges.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(j -> declareConfigurationToJointEdge(e.getKey(), j)))
                .toList();

        List<String> jointEdges = jointIdToTargetEdges.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(j -> declareJointToTargetEdge(e.getKey(), j)))
                .toList();

        List<String> emptyEdges = jointIdToEmptyIdEdges.entrySet()
                .stream()
                .map(e -> declareJointToEmptyEdge(e.getKey(), e.getValue()))
                .toList();

        edges.addAll(configEdges);
        edges.addAll(jointEdges);
        edges.addAll(emptyEdges);

        List<String> rankList = ranks.values()
                .stream()
                .map(this::declareRank)
                .toList();

        return declareDependencyGraph(
                String.join(getVertexDelimiter(), vertices),
                String.join(getEdgeDelimiter(), edges),
                String.join(getRankDelimiter(), rankList)
        );
    }

    protected abstract String declareDependencyGraph(String vertices, String edges, String ranks);

    protected abstract String getVertexDelimiter();

    protected abstract String getEdgeDelimiter();

    protected abstract String getRankDelimiter();

    protected abstract String declareRank(List<String> nodeIds);

    protected abstract String declareConfiguration(C conf);

    protected abstract String declareJoint(int jointId);

    protected abstract String declareEmpty(int emptyId);

    protected abstract String declareConfigurationToJointEdge(C conf, int jointId);

    protected abstract String declareJointToTargetEdge(int jointId, T target);

    protected abstract String declareJointToEmptyEdge(int jointId, int emptyId);

    private void visit(Pair<C, Integer> pair) {
        visit(pair.a, pair.b);
    }

    private void visit(C c, int rank) {
        if (shouldSkipConfiguration(c)) {
            return;
        }

        visitedConfigs.add(c);
        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        ranks.get(rank).add(String.valueOf(c.hashCode()));

        for (E edge : c.getSuccessors()) {
            visitJoint(c, edge, rank + 1);
        }
    }

    protected void visitJoint(C parent, E edge, int rank) {
        if (shouldSkipJoint(edge)) {
            return;
        }

        int joint = ++joints;
        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        ranks.get(rank).add("joint" + joint);

        configToJointIdEdges.computeIfAbsent(parent, n -> new LinkedHashSet<>());
        configToJointIdEdges.get(parent).add(joint);

        jointIdToTargetEdges.computeIfAbsent(joint, n -> new LinkedHashSet<>());
        Set<T> children = jointIdToTargetEdges.get(joint);

        if (edge.isEmpty()) {
            int empty = empties++;
            jointIdToEmptyIdEdges.put(joint, empty);
            ranks.computeIfAbsent(rank + 1, n -> new ArrayList<>());
            ranks.get(rank + 1).add("empty" + empty);
            return;
        }

        for (T target : edge) {
            children.add(target);

            if (!visitedConfigs.contains(target.getConfiguration())) {
                queue.add(new Pair<>(target.getConfiguration(), rank + 1));
            }
        }
    }

    protected String getLabel(T target) {
        if (target.getGame() != null && target.getTransition() != null) {
            return target.getGame() + " / " + target.getTransition();
        } else if (target.getGame() != null) {
            return target.getGame().toString();
        } else if (target.getTransition() != null) {
            return target.getTransition().toString();
        } else {
            return "";
        }
    }

    protected boolean shouldSkipJoint(E edge) {
        return edge.stream().anyMatch(t -> shouldSkipConfiguration(t.getConfiguration()));
    }

    private boolean shouldSkipConfiguration(C c) {
        return displayOnlyConfigurationsWhichPropagateOne
                && !propagationByConfiguration.getOrDefault(c, false);
    }
}
