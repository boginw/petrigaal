package petrigaal.solver;

import petrigaal.edg.Configuration;
import petrigaal.edg.Edge;
import petrigaal.edg.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class NonModifyingDGSolver<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> {
    private Map<C, Boolean> propagationByConfiguration = new HashMap<>();

    public NonModifyingDGSolver(C c) {
        propagationByConfiguration.put(c, false);
    }

    public Map<C, Boolean> solve() {
        while (true) {
            Map<C, Boolean> updated = getNextIteration();
            if (updated.equals(propagationByConfiguration)) {
                break;
            }
            propagationByConfiguration = updated;
        }

        return propagationByConfiguration;
    }

    private Map<C, Boolean> getNextIteration() {
        Map<C, Boolean> updated = new HashMap<>();

        for (C configuration : propagationByConfiguration.keySet()) {
            boolean configurationPropagation = false;
            for (E successor : configuration.getSuccessors()) {
                if (successor.isEmpty()) {
                    configurationPropagation = true;
                } else {
                    boolean edgePropagation = true;
                    for (T target : successor) {
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
}
