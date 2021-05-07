package petrigaal.solver;

import petrigaal.edg.DGConfiguration;
import petrigaal.edg.DGEdge;
import petrigaal.edg.DGTarget;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class NonModifyingDGSolver {
    private Map<DGConfiguration, Boolean> propagationByConfiguration = new HashMap<>();
    private BiConsumer<Integer, Integer> consumer;

    public Map<DGConfiguration, Boolean> solve(DGConfiguration c, BiConsumer<Integer, Integer> consumer) {
        this.consumer = consumer;
        propagationByConfiguration.clear();

        propagationByConfiguration.put(c, false);
        while (true) {
            Map<DGConfiguration, Boolean> updated = getNextIteration();
            if (updated.equals(propagationByConfiguration)) {
                break;
            }
            propagationByConfiguration = updated;
        }

        System.out.println("Can solve: " + propagationByConfiguration.get(c));
        return propagationByConfiguration;
    }

    private Map<DGConfiguration, Boolean> getNextIteration() {
        Map<DGConfiguration, Boolean> updated = new HashMap<>();

        for (DGConfiguration configuration : propagationByConfiguration.keySet()) {
            boolean configurationPropagation = false;
            for (DGEdge successor : configuration.getSuccessors()) {
                if (successor.isEmpty()) {
                    configurationPropagation = true;
                } else {
                    boolean edgePropagation = true;
                    for (DGTarget target : successor) {
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

    private static boolean isConfigurationTargetOfEdge(DGConfiguration c, DGEdge e) {
        return e.stream().anyMatch(t -> t.getConfiguration().equals(c));
    }
}
