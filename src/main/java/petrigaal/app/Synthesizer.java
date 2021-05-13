package petrigaal.app;

import petrigaal.atl.Optimizer;
import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.ATLNode;
import petrigaal.edg.DGConfiguration;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.NonModifyingDGSolver;
import petrigaal.strategy.MdgToMpsConverter;
import petrigaal.strategy.MetaDGGenerator;
import petrigaal.strategy.MetaDGGenerator.MetaConfiguration;
import petrigaal.strategy.automata.AutomataStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class Synthesizer {
    private final Options options;

    public Synthesizer(Options options) {
        this.options = options;
    }

    public Result synthesize() throws IllegalAccessException, FileNotFoundException {
        PetriGame game = loadGame(options.modelFile);
        ATLNode tree = new petrigaal.atl.Parser().parse(options.formula);
        ATLNode optimizedTree = new Optimizer().optimize(tree);

        DGConfiguration c = new DGConfiguration((ATLFormula) optimizedTree, game);
        int size = new DependencyGraphGenerator().crawl(c);

        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        Map<DGConfiguration, Boolean> propagationByConfiguration = new NonModifyingDGSolver<>(c).solve();
        long milliseconds = (endTime - startTime) / 1000000;
        System.out.printf("Total ms: %d", milliseconds);

        MetaDGGenerator metaDgGenerator = new MetaDGGenerator();
        MetaConfiguration c2 = metaDgGenerator.synthesize(c, propagationByConfiguration);
        Map<MetaConfiguration, Boolean> metaPropagationByConfiguration = new NonModifyingDGSolver<>(c2).solve();

        MdgToMpsConverter strategyConverter = new MdgToMpsConverter();
        AutomataStrategy strategy = strategyConverter.convert(c2, metaPropagationByConfiguration);

        return new Result(c, propagationByConfiguration, c2, metaPropagationByConfiguration, strategy);
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

    public static record Options(
            File modelFile,
            String formula,
            boolean displayOnlyOne
    ) {
    }

    public static record Result(
            DGConfiguration dg,
            Map<DGConfiguration, Boolean> propagationByDGConfiguration,
            MetaConfiguration mdg,
            Map<MetaConfiguration, Boolean> propagationByMetaConfiguration,
            AutomataStrategy mps
    ) {
    }
}
