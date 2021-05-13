package petrigaal.app;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.DGToGraphViz;
import petrigaal.draw.EDGToGraphViz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static petrigaal.app.PetriGAALApplication.FORMAT;

public class SynthesisRender {
    private static int counter = 0;

    public Result render(Synthesizer.Result synthesisState, Synthesizer.Options options) throws IllegalAccessException {
        clearResults();
        int index = counter++;

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

        File dgFile = renderViz(dg, index, "dg");
        File mdgFile = renderViz(mdg, index, "mdg");
        File strategyFile = renderViz(mps, index, "strategy");

        return new Result(dgFile, mdgFile, strategyFile);
    }

    private File renderViz(String graph, int index, String name) {
        try {
            File outFile = getFile(index, name);

            MutableGraph g = new Parser().read(graph);
            Graphviz.fromGraph(g).totalMemory(480000000).render(FORMAT).toFile(outFile);
            return outFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearResults() throws IllegalAccessException {
        File outFolder = new File("./out/");
        if (outFolder.exists()) {
            deleteDirectory(outFolder);
            clearResults();
        } else if (outFolder.getParentFile().canWrite()) {
            boolean folderCreated = outFolder.mkdir() && outFolder.exists();
            assert folderCreated;
        } else {
            throw new IllegalAccessException("Cannot create ./out folder");
        }
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private File getFile(int index, String name) {
        return Path.of(getDir(index).getAbsolutePath(), name + "." + FORMAT.fileExtension).toFile();
    }

    private File getDir(int index) {
        File dir = Path.of(".", "out", String.valueOf(index)).toFile();
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            assert mkdir;
        }
        return dir;
    }

    public static record Result(
            File dgFile,
            File mdgFile,
            File strategyFile
    ) {
    }
}
