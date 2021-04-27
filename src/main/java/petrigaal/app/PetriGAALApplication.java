package petrigaal.app;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.Configuration;
import petrigaal.strategy.TopDownStrategySynthesiser.SynthesisState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PetriGAALApplication extends Application {
    public static final Format FORMAT = Format.SVG;
    public static Consumer<SynthesisState> onNewState;
    private final ObservableList<SynthesisState> stateList = FXCollections.observableArrayList();
    private StateView view;
    private Tab strategyTab;
    private Tab dependencyGraphTab;
    private TabPane tabPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        onNewState = this::newState;

        view = new StateView(
                new SvgViewer("../../../../out/1.svg"),
                new SvgViewer("../../../../out/1.svg")
        );

        ListView<SynthesisState> listView = new ListView<>(stateList);
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            renderState(newValue);
        });
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        strategyTab = new Tab("Strategy", view.strategy().getWebView());
        dependencyGraphTab = new Tab("Dependency Graph", view.dg().getWebView());
        tabPane.getTabs().add(strategyTab);
        tabPane.getTabs().add(dependencyGraphTab);

        SplitPane splitView = new SplitPane();
        splitView.getItems().add(listView);
        splitView.getItems().add(tabPane);
        splitView.setDividerPositions(0.2f, 0.8f);

        BorderPane root = new BorderPane();
        root.setCenter(splitView);

        Scene scene = new Scene(root);

        primaryStage.setTitle("IntelliGaal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void newState(SynthesisState synthesisState) {
        stateList.add(synthesisState);
    }

    private void renderState(SynthesisState synthesisState) {
        int index = stateList.indexOf(synthesisState);

        File dgFile;
        File strategyFile;
        List<File> closeFiles = new ArrayList<>();
        if (true/*!getDir(index).exists()*/) {
            String strategy = new AutomataStrategyToGraphViz().draw(synthesisState.strategy());
            EDGToGraphViz edgToGraphViz = new EDGToGraphViz();
            edgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(true);
            String dg = edgToGraphViz.draw(synthesisState.root(), synthesisState.propagationByConfiguration());
            dgFile = renderViz(dg, index, "dg");
            strategyFile = renderViz(strategy, index, "strategy");
            int closes = 0;

            for (Set<Configuration> configurations : synthesisState.close()) {
                Map<Configuration, Boolean> collect = configurations.stream().collect(Collectors.toMap(k -> k, v -> true));
                String close = new EDGToGraphViz().draw(synthesisState.root(), collect);
                String name = "close" + (closes++);
                File closeFile = renderViz(close, index, name);
                closeFiles.add(closeFile);
            }
        } else {
            dgFile = getFile(index, "dg");
            strategyFile = getFile(index, "strategy");
            File[] closes = Objects.requireNonNull(getDir(index).listFiles((f, n) -> n.startsWith("close")));
            closeFiles = Arrays.stream(closes).sorted().toList();
        }

        tabPane.getTabs().retainAll(strategyTab, dependencyGraphTab);
        for (File closeFile : closeFiles) {
            SvgViewer viewer = new SvgViewer(closeFile.getAbsolutePath());
            tabPane.getTabs().add(new Tab(closeFile.getName(), viewer.getWebView()));
        }

        view.dg().loadImage(dgFile.getAbsolutePath());
        view.strategy().loadImage(strategyFile.getAbsolutePath());
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

    private static record StateView(
            SvgViewer dg,
            SvgViewer strategy
    ) {
    }
}
