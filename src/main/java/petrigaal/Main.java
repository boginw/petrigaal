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
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.EDGSolver;

import java.io.*;
import java.util.Objects;

public class Main {
    public static int counter = 0;
    private static int size = 0;

    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3) {
            System.err.println("Usage: java -jar petrigaal.jar QUERY PATH_TO_PNML [-s for solve]");
            return;
        }

        File pnml = new File(args[1]);
        PetriGame game = loadGame(pnml);
        ATLNode tree = new Parser().parse(args[0]);
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        Configuration c = new Configuration((ATLFormula) optimizedTree, game);
        size = new DependencyGraphGenerator().crawl(c);

        clearResults();

        openGraph(c);
        System.out.printf("Configurations: %d\n", size);

        if (args.length == 3) {
            long milliseconds = benchmark(() -> new EDGSolver().solve(c, Main::nop));
            openGraph(c);
            System.out.printf("Total ms: %d", milliseconds);
        }
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

    private static long benchmark(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1000000;
    }

    private static void clearResults() {
        if (new File("./out").exists()) {
            for (File f : Objects.requireNonNull(new File("./out").listFiles())) {
                f.delete();
            }
        }
    }

    private static void nop(int initial, int configurationsRemoved) {
        System.out.printf(
                "Workers: %d, Configurations Visited: %d (%.2f%%)\n",
                initial,
                configurationsRemoved,
                ((configurationsRemoved / (float) size) * 100)
        );
    }

    private static void openGraph(Configuration c) {
        try {
            String graph = new EDGToGraphViz().draw(c);
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
