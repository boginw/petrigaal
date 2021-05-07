package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.*;

public class DGToGraphViz<C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> {
    private final List<String> nodesOrder = new ArrayList<>();
    private final Map<String, List<String>> nodes = new HashMap<>();
    private final Map<Integer, List<String>> ranks = new HashMap<>();
    private final Queue<Pair<C, Integer>> queue = new LinkedList<>();
    private Map<C, Boolean> propagationByConfiguration;
    private int empties = 0;
    private int joints = 0;
    private boolean displayOnlyConfigurationsWhichPropagateOne = false;

    public DGToGraphViz(C configuration, Map<C, Boolean> propagationByConfiguration) {
        this.propagationByConfiguration = propagationByConfiguration;
    }

    public void setDisplayOnlyConfigurationsWhichPropagateOne(boolean displayOnlyConfigurationsWhichPropagateOne) {
        this.displayOnlyConfigurationsWhichPropagateOne = displayOnlyConfigurationsWhichPropagateOne;
    }

    public String draw(C configuration) {
        return draw(configuration, propagationByConfiguration);
    }

    public String draw(C configuration, Map<C, Boolean> propagationByConfiguration) {
        this.propagationByConfiguration = propagationByConfiguration;
        queue.clear();

        StringBuilder sb = new StringBuilder();

        queue.add(new Pair<>(configuration, 0));

        while (!queue.isEmpty()) {
            visit(queue.poll());
        }

        nodesOrder.forEach(k -> sb.append(k).append("\n"));

        for (int i = 1; i <= joints; i++) {
            sb.append("joint")
                    .append(i)
                    .append(" [shape=\"none\", label=\"\", width=0, height=0]\n");
        }

        for (int i = 0; i < empties; i++) {
            sb.append("empty").append(i).append(" [shape=\"none\", label=\"Ø\"]\n");
        }

        nodes.forEach((k, v) -> v.forEach(
                s -> sb.append(k).append(" -> ").append(s).append("\n")
        ));

        for (List<String> values : ranks.values()) {
            sb.append("{rank=same; ");

            for (String value : values) {
                sb.append(value).append("; ");
            }

            sb.append("}\n");
        }

        return "digraph G {\n" +
                "graph [pad=\"2\", nodesep=\"2\", ranksep=\"1\", rankdir=\"TB\", splines=ortho];\n" +
                "node [shape=box]\n" +
                sb +
                "}";
    }

    private void visit(Pair<C, Integer> pair) {
        visit(pair.a, pair.b);
    }

    private void visit(C c, int rank) {
        if (shouldSkipConfiguration(c)) {
            return;
        }
        String name = nameOf(c);

        String suffix = "";
        if (propagationByConfiguration.getOrDefault(c, false)) {
            suffix = " [color=green, penwidth=5]";
        }

        if (nodes.containsKey(name)) {
            return;
        }

        nodesOrder.add(name + suffix);
        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        nodes.computeIfAbsent(name, n -> new ArrayList<>());
        ranks.get(rank).add(name);

        for (E edge : c.getSuccessors()) {
            visitJoint(name, edge, rank + 1);
        }
    }

    private void visitJoint(String parent, E edge, int rank) {
        if (shouldSkipJoint(edge)) {
            return;
        }

        int joint = ++joints;
        String name = "joint" + joint;
        String nameWithoutArrow = name + " [arrowhead=\"none\"]";
        String suffix = "";

        /*if (edge.isNegated()) {
            nameWithoutArrow = name + " [arrowhead=\"none\", style=\"dashed\"]";
            suffix = ", style=\"dashed\"";
        }*/

        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        ranks.get(rank).add(name);

        nodes.get(parent).add(nameWithoutArrow);
        nodes.computeIfAbsent(name, n -> new ArrayList<>());
        List<String> children = nodes.get(name);

        if (edge.isEmpty()) {
            children.add("empty" + empties++);
            return;
        }

        for (T target : edge) {
            String label = "";
            if (target.getGame() != null) {
                label += target.getGame().toString();
            }
            if (target.getTransition() != null) {
                label += target.getTransition().toString();
            }
            children.add(nameOf(target.getConfiguration()) + " [xlabel=\"" + label + "\"" + suffix + "]");
            queue.add(new Pair<>(target.getConfiguration(), rank + 1));
        }<
    }

    private boolean shouldSkipConfiguration(C c) {
        return displayOnlyConfigurationsWhichPropagateOne
                && !propagationByConfiguration.getOrDefault(c, false);
    }

    private boolean shouldSkipJoint(E edge) {
        return edge.stream().anyMatch(t -> shouldSkipConfiguration(t.getConfiguration()));
    }

    private String nameOf(C c) {
        return '"' + Objects.toString(c) + '"';
    }
}
