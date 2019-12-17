package petrigaal;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import petrigaal.atl.Optimizer;
import petrigaal.atl.Parser;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.ATLNode;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;
import petrigaal.pnml.PNMLLoader;
import petrigaal.solver.EDGSolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static int counter = 0;

    public static void main(String[] args) throws IOException {
        File pnml = new File("/home/hamburger/Downloads/experiments/order-workflow/order-workflow-10000.pnml");
        PetriGame game = new PNMLLoader().load(new FileInputStream(pnml));
        ATLNode tree = new Parser().parse("{1}(true U ((d1 = 1 & d2 = 1) & P17 > 0))");
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        Configuration c = new Configuration((ATLFormula) optimizedTree, game);
        new DependencyGraphGenerator().crawl(c);

        if (new File("./out").exists()) {
            for (File f : Objects.requireNonNull(new File("./out").listFiles())) {
                f.delete();
            }
        }

        //openGraph(c);

        System.out.println(new EDGSolver().solve(c, Main::nop));
    }

    private static void nop(Configuration c) {
    }

    private static void openGraph(Configuration c) {
        try {
            String graph = new EDGToGraphViz().draw(c);
            MutableGraph g = new guru.nidi.graphviz.parse.Parser().read(graph);
            //File file = File.createTempFile("graph",".svg");
            File file = new File("./out/" + (counter++) + ".svg");
            Graphviz.fromGraph(g).totalMemory(480000000).render(Format.SVG).toFile(file);
            //Runtime.getRuntime().exec("xdg-open " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
