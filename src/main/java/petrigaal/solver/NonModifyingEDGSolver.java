package petrigaal.solver;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class NonModifyingEDGSolver {
    private Map<Configuration, Boolean> propagationByConfiguration = new HashMap<>();
    private BiConsumer<Integer, Integer> consumer;

    public Map<Configuration, Boolean> solve(Configuration c, BiConsumer<Integer, Integer> consumer) {
        this.consumer = consumer;
        propagationByConfiguration.clear();

        propagationByConfiguration.put(c, false);
        while (true) {
            Map<Configuration, Boolean> updated = getNextIteration();
            if (updated.equals(propagationByConfiguration)) {
                break;
            }
            propagationByConfiguration = updated;
        }

        System.out.println("Can solve: " + propagationByConfiguration.get(c));
        return propagationByConfiguration;
    }

    private Map<Configuration, Boolean> getNextIteration() {
        Map<Configuration, Boolean> updated = new HashMap<>();

        for (Configuration configuration : propagationByConfiguration.keySet()) {
            boolean configurationPropagation = false;
            for (Edge successor : configuration.getSuccessors()) {
                if (successor.isEmpty()) {
                    configurationPropagation = true;
                } else {
                    boolean edgePropagation = true;
                    for (Target target : successor) {
                        if (!propagationByConfiguration.getOrDefault(target.getConfiguration(), false)) {
                            edgePropagation = false;
                        }
                        updated.putIfAbsent(
                                target.getConfiguration(),
                                propagationByConfiguration.getOrDefault(target.getConfiguration(), false)
                        );
                    }
                    if (edgePropagation) {
                        configurationPropagation = true;
                    }
                }
            }
            updated.put(configuration, configurationPropagation);
        }

        return updated;
    }

    private static boolean isConfigurationTargetOfEdge(Configuration c, Edge e) {
        return e.stream().anyMatch(t -> t.getConfiguration().equals(c));
    }
}
