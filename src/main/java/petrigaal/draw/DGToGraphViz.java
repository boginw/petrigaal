package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.*;

public class DGToGraphViz<C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> {
    protected final Map<String, List<String>> nodes = new HashMap<>();
    protected final Map<Integer, List<String>> ranks = new HashMap<>();
    protected final Queue<Pair<C, Integer>> queue = new LinkedList<>();
    protected int empties = 0;
    protected int joints = 0;
    private final List<String> nodesOrder = new ArrayList<>();
    private Map<C, Boolean> propagationByConfiguration;
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

    protected void visitJoint(String parent, E edge, int rank) {
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
            String label = getLabel(target);
            children.add(nameOf(target.getConfiguration()) + " [xlabel=\"" + label + "\"" + suffix + "]");
            queue.add(new Pair<>(target.getConfiguration(), rank + 1));
        }
    }

    protected boolean shouldSkipJoint(E edge) {
        return edge.stream().anyMatch(t -> shouldSkipConfiguration(t.getConfiguration()));
    }

    private void visit(Pair<C, Integer> pair) {
        visit(pair.a, pair.b);
    }

    protected String nameOf(C c) {
        return '"' + Objects.toString(c) + '"';
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

    private String getLabel(T target) {
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

    private boolean shouldSkipConfiguration(C c) {
        return displayOnlyConfigurationsWhichPropagateOne
                && !propagationByConfiguration.getOrDefault(c, false);
    }
}
