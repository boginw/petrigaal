package petrigaal;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import petrigaal.atl.Optimizer;
import petrigaal.atl.Parser;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.ATLNode;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.Configuration;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.NonModifyingEDGSolver;
import petrigaal.strategy.AutomataStrategy;
import petrigaal.strategy.TopDownStrategySynthesiser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(
        name = "PetriGAAL",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Discrete Strategy Synthesis for CTL on Petri Net Games",
        helpCommand = true
)
public class Main implements Callable<Integer> {
    @Option(names = {"-q", "--query"}, required = true, description = "CTL Query (Note space between f.x. A and F is required)")
    public String query = "";
    @Option(names = {"-od", "--open-dg"}, description = "Open dependency graph after construction")
    public Boolean openDependencyGraph = false;
    @Option(names = {"-ds", "--disable-synthesis"}, description = "Disable strategy automata synthesis")
    public Boolean disableSynthesis = false;
    @Option(names = {"-ps"}, description = "Generate PostScript file instead of SVG")
    public Boolean postScript = false;
    @Parameters(description = "Model (Either TAPN or PNML)")
    public File file;
    private int counter = 0;
    private int size = 0;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws FileNotFoundException, IllegalAccessException {
        PetriGame game = loadGame(file);
        ATLNode tree = new Parser().parse(query);
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        Configuration c = new Configuration((ATLFormula) optimizedTree, game);
        size = new DependencyGraphGenerator().crawl(c);

        clearResults();

        System.out.printf("Configurations: %d\n", size);

        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        Map<Configuration, Boolean> propagationByConfiguration = new NonModifyingEDGSolver().solve(c, this::nop);
        long milliseconds = (endTime - startTime) / 1000000;
        System.out.printf("Total ms: %d", milliseconds);

        if (openDependencyGraph) {
            openGraph(c, propagationByConfiguration);
        }

        if (!disableSynthesis) {
            new TopDownStrategySynthesiser().synthesize(game, c, propagationByConfiguration, this::openGraph);
        }

        return 0;
    }

    private PetriGame loadGame(File file) throws FileNotFoundException {
        if (file.getName().endsWith(".pnml")) {
            return new PNMLLoader().load(new FileInputStream(file));
        } else if (file.getName().endsWith(".tapn")) {
            return new TAPNLoader().load(new FileInputStream(file));
        } else {
            throw new RuntimeException("Unsupported file format");
        }
    }

    private void clearResults() throws IllegalAccessException {
        File outFolder = new File("./out");
        if (outFolder.exists()) {
            for (File f : Objects.requireNonNull(outFolder.listFiles())) {
                assert f.delete();
            }
        } else if (file.canWrite()) {
            assert outFolder.mkdir() && outFolder.exists();
        } else {
            throw new IllegalAccessException("Cannot create ./out folder");
        }
    }

    private void nop(int initial, int configurationsRemoved) {
        System.out.printf(
                "Queue Size: %d, Configurations Visited: %d (%.2f%%)\n",
                initial,
                configurationsRemoved,
                ((configurationsRemoved / (float) size) * 100)
        );
    }

    private void openGraph(Configuration c) {
        openGraph(c, new HashMap<>());
    }

    private void openGraph(AutomataStrategy strategy) {
        openGraph(new AutomataStrategyToGraphViz().draw(strategy));
    }

    private void openGraph(Configuration c, Map<Configuration, Boolean> propagationByConfiguration) {
        openGraph(new EDGToGraphViz().draw(c, propagationByConfiguration));
    }

    private void openGraph(String graph) {
        Format format = postScript ? Format.PS2 : Format.SVG;
        try {
            File vizFile = new File("./out/" + (counter++) + ".gv");
            File svgFile = new File("./out/" + (counter++) + "." + format.fileExtension);

            BufferedWriter writer = new BufferedWriter(new FileWriter(vizFile));
            writer.write(graph);
            writer.close();

            MutableGraph g = new guru.nidi.graphviz.parse.Parser().read(graph);
            Graphviz.fromGraph(g).totalMemory(480000000).render(format).toFile(svgFile);

            Runtime.getRuntime().exec(getOpenCmd() + " " + svgFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getOpenCmd() {
        String openCmd = "xdg-open";
        if (System.getProperty("os.name").equals("Mac OS X")) {
            openCmd = "open";
        }
        return openCmd;
    }
}
