package petrigaal.strategy.TopDownStrategySynthesiserTest;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import petrigaal.Main;
import petrigaal.atl.Optimizer;
import petrigaal.atl.Parser;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.ATLNode;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.Configuration;
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.NonModifyingEDGSolver;
import petrigaal.strategy.AutomataStrategy;
import petrigaal.strategy.TopDownStrategySynthesiser;

public class TopDownStrategySynthesiserTest {

    private static int counter = 0;

    @Test
    void shouldProduceCorrectStrategy() throws IOException {
        String working_directory = System.getProperty("user.dir");
        String file_path = working_directory + "/src/test/java/petrigaal/strategy/TopDownStrategySynthesiserTest/simpleNet.tapn";
        File pnml = new File(file_path);
        PetriGame game = loadGame(pnml);
        ATLNode tree = new Parser().parse("E X (P1=1)");
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        Configuration c = new Configuration((ATLFormula) optimizedTree, game);
        Map<Configuration, Boolean> propagationByConfiguration = new NonModifyingEDGSolver().solve(c, TopDownStrategySynthesiserTest::nop);
        new TopDownStrategySynthesiser().synthesize(game, c, propagationByConfiguration, TopDownStrategySynthesiserTest::openGraph);
    }

    private static PetriGame loadGame(File file) throws FileNotFoundException {
        if (file.getName().endsWith(".pnml")) {
            return new PNMLLoader().load(new FileInputStream(file));
        } else if (file.getName().endsWith(".tapn")) {
            return new TAPNLoader().load(new FileInputStream(file));
        } else {
            throw new RuntimeException("Unsupported file format");
        }
    }

    private static void nop(int initial, int configurationsRemoved) {
        System.out.print("fisk");
    }

    private static void openGraph(Configuration c) {
        openGraph(c, new HashMap<>());
    }

    private static void openGraph(AutomataStrategy strategy) {
        openGraph(new AutomataStrategyToGraphViz().draw(strategy));
    }

    private static void openGraph(Configuration c, Map<Configuration, Boolean> propagationByConfiguration) {
        openGraph(new EDGToGraphViz().draw(c, propagationByConfiguration));
    }

    private static void openGraph(String graph) {
        try {
            //File svgFile = File.createTempFile("graph",".svg");
            File vizFile = new File("./out/" + (counter++) + ".gv");
            File svgFile = new File("./out/" + (counter++) + ".svg");

            BufferedWriter writer = new BufferedWriter(new FileWriter(vizFile));
            writer.write(graph);
            writer.close();

            MutableGraph g = new guru.nidi.graphviz.parse.Parser().read(graph);
            Graphviz.fromGraph(g).totalMemory(480000000).render(Format.SVG).toFile(svgFile);

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
