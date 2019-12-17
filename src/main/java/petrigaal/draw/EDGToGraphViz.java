package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.Configuration;
import petrigaal.edg.Edge;

import java.util.*;

public class EDGToGraphViz {
    private List<String> nodesOrder = new ArrayList<>();
    private Map<String, List<String>> nodes = new HashMap<>();
    private Map<Integer, List<String>> ranks = new HashMap<>();
    private Queue<Pair<Configuration, Integer>> queue;
    private int empties = 0;
    private int joints = 0;

    public String draw(Configuration configuration) {
        queue = new LinkedList<>();

        StringBuilder sb = new StringBuilder();

        queue.add(new Pair<>(configuration, 0));

        while(!queue.isEmpty()) {
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
                "graph [rankdir=\"TB\", splines=ortho];\n" +
                "node [shape=box]\n" +
                sb.toString() +
                "}";
    }

    private void visit(Pair<Configuration, Integer> pair) {
        visit(pair.a, pair.b);
    }

    private void visit(Configuration c, int rank) {
        String name = nameOf(c);

        if (nodes.containsKey(name)) {
            return;
        }

        nodesOrder.add(name);
        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        nodes.computeIfAbsent(name, n -> new ArrayList<>());
        ranks.get(rank).add(name);

        for (Edge edge : c.getSuccessors()) {
            visitJoint(name, edge, rank + 1);
        }
    }

    private void visitJoint(String parent, Edge edge, int rank) {
        int joint = ++joints;
        String name = "joint" + joint;
        String nameWithoutArrow = name + " [arrowhead=\"none\"]";
        String suffix = "";

        if (edge.isNegated()) {
            nameWithoutArrow = name + " [arrowhead=\"none\", style=\"dashed\"]";
            suffix = "[style=\"dashed\"]";
        }

        ranks.computeIfAbsent(rank, n -> new ArrayList<>());
        ranks.get(rank).add(name);

        nodes.get(parent).add(nameWithoutArrow);
        nodes.computeIfAbsent(name, n -> new ArrayList<>());
        List<String> children = nodes.get(name);

        if (edge.isEmpty()) {
            children.add("empty" + empties++);
            return;
        }

        for (Configuration config : edge) {
            children.add(nameOf(config) + suffix);
            queue.add(new Pair<>(config, rank + 1));
        }
    }

    private String nameOf(Configuration c) {
        return '"' + Objects.toString(c) + '"';
    }
}
