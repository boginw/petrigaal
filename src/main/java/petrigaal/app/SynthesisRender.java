package petrigaal.app;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.DGToGraphViz;
import petrigaal.draw.EDGToGraphViz;

import java.io.IOException;

import static petrigaal.app.PetriGAALApplication.FORMAT;

public class SynthesisRender {

    public Result render(Synthesizer.Result synthesisState, Synthesizer.Options options) throws IllegalAccessException {
        EDGToGraphViz edgToGraphViz = new EDGToGraphViz();
        edgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(options.displayOnlyOne());
        var dgToGraphViz = new DGToGraphViz<>(
                synthesisState.mdg(),
                synthesisState.propagationByMetaConfiguration()
        );
        dgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(options.displayOnlyOne());
        String dg = edgToGraphViz.draw(synthesisState.dg(), synthesisState.propagationByDGConfiguration());
        String mdg = dgToGraphViz.draw(synthesisState.mdg());
        String mps = new AutomataStrategyToGraphViz().draw(synthesisState.mps());
        String instance = new AutomataStrategyToGraphViz().draw(synthesisState.instance());

        String dgSvg = renderViz(dg);
        String mdgSvg = renderViz(mdg);
        String strategySvg = renderViz(mps);
        String instanceSvg = renderViz(instance);

        return new Result(dgSvg, mdgSvg, strategySvg, instanceSvg);
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
