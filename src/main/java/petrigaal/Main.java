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
import petrigaal.solver.EDGSolver;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static int counter = 0;

    public static void main(String[] args) throws IOException {
        PetriGame game = new PetriGame();

        game.setMarking("p1", 1);
        Transition t1 = new Transition("t1").addInput("p1").addOutput("p2");
        Transition t2 = new Transition("t2").addInput("p2").addOutput("p1");
        //Transition t3 = new Transition("t3").addInput("p2").addOutput("p3");

        game.addTransition(Player.Controller, t1);
        game.addTransition(Player.Controller, t2);
        //game.addTransition(Player.Controller, t3);

        ATLNode tree = new Parser().parse("{1}#(p1 + p2 = 1)");
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        Configuration c = new Configuration((ATLFormula) optimizedTree, game);
        new DependencyGraphGenerator().crawl(c);

        if (new File("./out").exists()) {
            for (File f : Objects.requireNonNull(new File("./out").listFiles())) {
                f.delete();
            }
        }
        System.out.println(new EDGSolver().solve(c, Main::openGraph));
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
