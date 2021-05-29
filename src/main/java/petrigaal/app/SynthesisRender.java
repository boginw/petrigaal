package petrigaal.app;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import petrigaal.draw.AutomataStrategyGraphVizVisualizer;
import petrigaal.draw.DGCytoscapeVisualizer;
import petrigaal.draw.DGToGraphViz;

import java.io.IOException;

import static petrigaal.app.PetriGAALApplication.FORMAT;

public class SynthesisRender {

    public Result render(Synthesizer.Result synthesisState, Synthesizer.Options options) throws IllegalAccessException {
        String dg;
        String mdg;

        if (options.legacyRender()) {
            var dgVis = new DGToGraphViz<>(synthesisState.dg(), synthesisState.propagationByDGConfiguration());
            dgVis.setDisplayOnlyConfigurationsWhichPropagateOne(options.displayOnlyOne());
            dg = renderViz(dgVis.draw());

            var mdgVis = new DGToGraphViz<>(synthesisState.mdg(), synthesisState.propagationByMetaConfiguration());
            mdg = renderViz(mdgVis.draw());
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
        }

        String mps = new AutomataStrategyGraphVizVisualizer().draw(synthesisState.mps());
        String instance = new AutomataStrategyGraphVizVisualizer().draw(synthesisState.instance());

        String strategySvg = renderViz(mps);
        String instanceSvg = renderViz(instance);

        return new Result(dg, mdg, strategySvg, instanceSvg);
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
