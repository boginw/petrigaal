package petrigaal;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import javafx.application.Application;
import petrigaal.app.PetriGAALApplication;
import petrigaal.atl.Optimizer;
import petrigaal.atl.Parser;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.ATLNode;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.DGToGraphViz;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.DGConfiguration;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.EDGSolver;
import petrigaal.solver.NonModifyingDGSolver;
import petrigaal.strategy.DGStrategySynthesiser;
import petrigaal.strategy.DGStrategySynthesiser.MetaConfiguration;
import petrigaal.strategy.TopDownStrategySynthesiser;
import petrigaal.strategy.automata.AutomataStrategy;
import petrigaal.strategy.automata.AutomataStrategyDeterminer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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
    @Option(names = {"-dn"}, description = "Disable negation and use normal solver instead")
    public Boolean dg = false;
    @Option(names = {"-o1"}, description = "Display only those configurations that propagate one")
    public Boolean d1 = false;
    @Parameters(description = "Model (Either TAPN or PNML)")
    public File file;
    @Option(names = {"-w"}, description = "Windowed")
    public Boolean windowed = false;
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

        DGConfiguration c = new DGConfiguration((ATLFormula) optimizedTree, game);
        size = new DependencyGraphGenerator().crawl(c);

        clearResults();

        System.out.printf("Configurations: %d\n", size);

        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        Map<DGConfiguration, Boolean> propagationByConfiguration;
        if (dg) propagationByConfiguration = new NonModifyingDGSolver().solve(c, this::nop);
        else propagationByConfiguration = new EDGSolver().solve(c, this::nop);
        long milliseconds = (endTime - startTime) / 1000000;
        System.out.printf("Total ms: %d", milliseconds);

        if (openDependencyGraph) {
            openGraph(c, propagationByConfiguration);
        }

        Thread thread = new Thread(() -> Application.launch(PetriGAALApplication.class));
        if (windowed) {
            thread.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!disableSynthesis) {
            AutomataStrategy synthesize = new TopDownStrategySynthesiser()
                    .synthesize(c, propagationByConfiguration, PetriGAALApplication.onNewState);

            DGStrategySynthesiser dgStrategySynthesiser = new DGStrategySynthesiser();
            MetaConfiguration c2 = dgStrategySynthesiser.synthesize(c, propagationByConfiguration);

            String draw = new DGToGraphViz<>(c2, Map.of()).draw(c2);
            openGraph(draw, "synthesis");

            AutomataStrategy deterministic = new AutomataStrategyDeterminer(synthesize).determine();
            openGraph(deterministic);
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    private void nop(int initial, int configurationsRemoved) {
        System.out.printf(
                "Queue Size: %d, Configurations Visited: %d (%.2f%%)\n",
                initial,
                configurationsRemoved,
                ((configurationsRemoved / (float) size) * 100)
        );
    }

    private void openGraph(DGConfiguration c) {
        openGraph(c, new HashMap<>());
    }

    private void openGraph(AutomataStrategy strategy) {
        openGraph(new AutomataStrategyToGraphViz().draw(strategy), String.valueOf(counter++));
    }

    private void openGraph(DGConfiguration c, Map<DGConfiguration, Boolean> propagationByConfiguration) {
        EDGToGraphViz edgToGraphViz = new EDGToGraphViz();
        edgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(d1);
        openGraph(edgToGraphViz.draw(c, propagationByConfiguration), String.valueOf(counter++));
    }

    private void openGraph(String graph, String name) {
        Format format = postScript ? Format.PS2 : Format.SVG;
        try {
            File outFile = Path.of(".", "out", name + "." + format.fileExtension).toFile();

            MutableGraph g = new guru.nidi.graphviz.parse.Parser().read(graph);
            Graphviz.fromGraph(g).totalMemory(480000000).render(format).toFile(outFile);
            if (!windowed) {
                Runtime.getRuntime().exec(getOpenCmd() + " " + outFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getOpenCmd() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "xdg-open";
        } else if (os.contains("mac")) {
            return "open";
        } else {
            throw new UnsupportedOperationException("Unsupported Operating system");
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
