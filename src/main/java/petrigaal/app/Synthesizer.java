package petrigaal.app;

import petrigaal.ctl.Optimizer;
import petrigaal.ctl.language.CTLFormula;
import petrigaal.ctl.language.CTLNode;
import petrigaal.edg.DGConfiguration;
import petrigaal.edg.DependencyGraphGenerator;
import petrigaal.loader.PNMLLoader;
import petrigaal.loader.TAPNLoader;
import petrigaal.petri.PetriGame;
import petrigaal.solver.NonModifyingDGSolver;
import petrigaal.strategy.MdgToMpsConverter;
import petrigaal.strategy.MetaDGGenerator;
import petrigaal.strategy.MetaDGGenerator.MetaConfiguration;
import petrigaal.strategy.MpsToInstanceConverter;
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
        CTLNode tree = new petrigaal.ctl.Parser().parse(options.formula);
        CTLNode optimizedTree = new Optimizer().optimize(tree);

        long startTime = System.nanoTime();
        DGConfiguration c = new DGConfiguration((CTLFormula) optimizedTree, game);
        int size = new DependencyGraphGenerator().crawl(c);

        Map<DGConfiguration, Boolean> propagationByConfiguration = new NonModifyingDGSolver<>(c).solve();

        MetaDGGenerator metaDgGenerator = new MetaDGGenerator();
        MetaConfiguration c2 = metaDgGenerator.synthesize(c, propagationByConfiguration);
        Map<MetaConfiguration, Boolean> metaPropagationByConfiguration = new NonModifyingDGSolver<>(c2).solve();

        MdgToMpsConverter strategyConverter = new MdgToMpsConverter();
        AutomataStrategy strategy = strategyConverter.convert(c2, metaPropagationByConfiguration);
        AutomataStrategy instance = new MpsToInstanceConverter().convert(strategy);

        long endTime = System.nanoTime();
        long milliseconds = (endTime - startTime) / 1000000;
        System.out.printf("Total ms: %d\n", milliseconds);

        return new Result(c, propagationByConfiguration, c2, metaPropagationByConfiguration, strategy, instance);
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
            boolean displayOnlyOne,
            boolean legacyRender
    ) {
    }

    public static record Result(
            DGConfiguration dg,
            Map<DGConfiguration, Boolean> propagationByDGConfiguration,
            MetaConfiguration mdg,
            Map<MetaConfiguration, Boolean> propagationByMetaConfiguration,
            AutomataStrategy mps,
            AutomataStrategy instance
    ) {
    }
}
