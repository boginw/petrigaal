package petrigaal.draw;

import org.antlr.v4.runtime.misc.Pair;
import petrigaal.edg.dg.DGConfiguration;
import petrigaal.edg.dg.DGEdge;
import petrigaal.edg.dg.DGTarget;

import java.util.*;

public class EDGToGraphViz extends DGToGraphViz<DGConfiguration, DGEdge, DGTarget> {

    public EDGToGraphViz() {
        super(null, null);
    }

    @Override
    protected void visitJoint(String parent, DGEdge edge, int rank) {
        if (shouldSkipJoint(edge)) {
            return;
        }

        int joint = ++joints;
        String name = "joint" + joint;
        String nameWithoutArrow = name + " [arrowhead=\"none\"]";
        String suffix = "";

        if (edge.isNegated()) {
            nameWithoutArrow = name + " [arrowhead=\"none\", style=\"dashed\"]";
            suffix = ", style=\"dashed\"";
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

        for (DGTarget target : edge) {
            String label = "";
            if (target.getTransitions() != null) {
                label = target.getTransitions().toString();
            }
            children.add(nameOf(target.getConfiguration()) + " [xlabel=\"" + label + "\"" + suffix + "]");
            queue.add(new Pair<>(target.getConfiguration(), rank + 1));
        }
    }
}
