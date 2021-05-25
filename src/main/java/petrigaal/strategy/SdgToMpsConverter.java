package petrigaal.strategy;

import petrigaal.edg.dg.DGConfiguration;
import petrigaal.edg.mdg.MetaConfiguration;
import petrigaal.edg.mdg.MetaEdge;
import petrigaal.edg.mdg.MetaTarget;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategy.AutomataState;

import java.util.*;

public class SdgToMpsConverter {
    private final Queue<MetaConfiguration> queue = new LinkedList<>();
    private final Set<MetaConfiguration> visited = new HashSet<>();

    public AutomataStrategy convert(
            MetaConfiguration rootConf,
            Map<MetaConfiguration, Boolean> propagationByConfiguration
    ) {
        visited.clear();
        queue.clear();
        final AutomataStrategy strategy = new AutomataStrategy(new AutomataState(rootConf.toString()));

        queue.add(rootConf);

        while (!queue.isEmpty()) {
            MetaConfiguration metaConf = queue.poll();
            visited.add(metaConf);

            for (DGConfiguration conf : metaConf.getConfigurations()) {
                for (MetaEdge edge : metaConf.getSuccessors()) {
                    if (edge.isEmpty()) {
                        strategy.addFinalState(new AutomataState(metaConf.toString()));
                        continue;
                    }

                    for (MetaTarget target : edge) {
                        if (!Objects.equals(conf.getGame(), target.getGame())) {
                            continue;
                        }

                        if (!propagationByConfiguration.getOrDefault(target.configuration(), false)) {
                            continue;
                        }

                        strategy.addTransition(
                                new AutomataState(metaConf.toString()),
                                target.getGame(),
                                target.getTransitions().iterator().next(),
                                new AutomataState(target.configuration().toString())
                        );

                        if (!visited.contains(target.getConfiguration())) {
                            queue.add(target.configuration());
                        }
                    }
                }
            }
        }

        return strategy;
    }
}
