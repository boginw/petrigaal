package petrigaal.app;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import petrigaal.draw.AutomataStrategyCytoscapeVisualizer;
import petrigaal.draw.AutomataStrategyGraphVizVisualizer;
import petrigaal.draw.DGCytoscapeVisualizer;
import petrigaal.draw.DGGraphVizVisualizer;

import java.io.IOException;

import static petrigaal.app.PetriGAALApplication.FORMAT;

public class SynthesisRender {

    public Result render(Synthesizer.Result synthesisState, Synthesizer.Options options) throws IllegalAccessException {
        String dg;
        String mdg;
        String mps;
        String instance;

        if (options.legacyRender()) {
            var dgVis = new DGGraphVizVisualizer<>(synthesisState.dg(), synthesisState.propagationByDGConfiguration());
            dgVis.setDisplayOnlyConfigurationsWhichPropagateOne(options.displayOnlyOne());
            dg = renderViz(dgVis.draw());

            var mdgVis = new DGGraphVizVisualizer<>(synthesisState.mdg(), synthesisState.propagationByMetaConfiguration());
            mdg = renderViz(mdgVis.draw());

            mps = renderViz(new AutomataStrategyGraphVizVisualizer().draw(synthesisState.mps()));
            instance = renderViz(new AutomataStrategyGraphVizVisualizer().draw(synthesisState.instance()));
        } else {
            dg = DGCytoscapeVisualizer.builder()
                    .forConfiguration(synthesisState.dg())
                    .withPropagationMapping(synthesisState.propagationByDGConfiguration())
                    .withOnlyDisplayingPropagationOfOneBeing(options.displayOnlyOne())
                    .build();
            mdg = DGCytoscapeVisualizer.builder()
                    .forConfiguration(synthesisState.mdg())
                    .withPropagationMapping(synthesisState.propagationByMetaConfiguration())
                    .withOnlyDisplayingPropagationOfOneBeing(options.displayOnlyOne())
                    .build();

            mps = new AutomataStrategyCytoscapeVisualizer().draw(synthesisState.mps());
            instance = new AutomataStrategyCytoscapeVisualizer().draw(synthesisState.instance());
        }

        return new Result(dg, mdg, mps, instance);
    }

    private String renderViz(String graph) {
        try {
            MutableGraph g = new Parser().read(graph);
            return Graphviz.fromGraph(g).totalMemory(480000000).render(FORMAT).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static record Result(
            String dgSvg,
            String mdgSvg,
            String strategySvg,
            String instanceSvg
    ) {
    }
}
