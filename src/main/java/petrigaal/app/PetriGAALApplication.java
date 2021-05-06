package petrigaal.app;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import petrigaal.draw.AutomataStrategyToGraphViz;
import petrigaal.draw.EDGToGraphViz;
import petrigaal.edg.DGConfiguration;
import petrigaal.strategy.TopDownStrategySynthesiser.SynthesisState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PetriGAALApplication extends Application {
    public static final Format FORMAT = Format.SVG;
    public static final String DEFAULT_IMAGE = "start.png";
    public static Consumer<SynthesisState> onNewState;
    private static Consumer<String> onSynthesisDG;
    private final ObservableList<SynthesisState> stateList = FXCollections.observableArrayList();
    private final ObservableList<Map<DGConfiguration, Boolean>> closeFiles = FXCollections.observableArrayList();
    private SynthesisState current;
    private StateView view;
    private TabPane tabPane;
    private Tab closeTab;

    @Override
    public void start(Stage primaryStage) throws Exception {
        onNewState = this::newState;
        onSynthesisDG = this::newSynthesisDG;

        view = new StateView(
                new SvgViewer(DEFAULT_IMAGE),
                new SvgViewer(DEFAULT_IMAGE),
                new SvgViewer(DEFAULT_IMAGE)
        );

        SplitPane horizontal = new SplitPane();
        horizontal.setOrientation(Orientation.VERTICAL);
        horizontal.getItems().addAll(getSynthesisStateListView(), getFileListView());

        SplitPane splitView = new SplitPane();
        splitView.getItems().add(horizontal);
        splitView.getItems().add(getTabPane());
        splitView.setDividerPositions(0.2f, 0.8f);

        BorderPane root = new BorderPane();
        root.setCenter(splitView);

        Scene scene = new Scene(root, 1920, 1080);

        primaryStage.setTitle("IntelliGaal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void newSynthesisDG(String s) {

    }

    private TabPane getTabPane() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab strategyTab = new Tab("Strategy", view.strategy().getWebView());
        Tab dependencyGraphTab = new Tab("Dependency Graph", view.dg().getWebView());
        closeTab = new Tab("Close", view.close().getWebView());
        tabPane.getTabs().addAll(strategyTab, dependencyGraphTab, closeTab);
        return tabPane;
    }

    private ListView<Map<DGConfiguration, Boolean>> getFileListView() {
        ListView<Map<DGConfiguration, Boolean>> closeList = new CloseListView(closeFiles);
        closeList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            renderClose(newValue);
        });
        return closeList;
    }

    private ListView<SynthesisState> getSynthesisStateListView() {
        ListView<SynthesisState> listView = new SynthesisStateListView(stateList);
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            renderState(newValue);
        });
        return listView;
    }

    private void newState(SynthesisState synthesisState) {
        stateList.add(synthesisState);
    }

    private void renderClose(Map<DGConfiguration, Boolean> close) {
        if (close != null) {
            int index = closeFiles.indexOf(close);

            String name = "close" + index;
            File outFile = getFile(index, name);

            if (!outFile.exists()) {
                String closeGraph = new EDGToGraphViz().draw(current.root(), close);
                outFile = renderViz(closeGraph, index, name);
            }

            tabPane.getSelectionModel().select(closeTab);
            view.close().loadImage(outFile.getAbsolutePath());
        } else {
            view.close().loadImage(DEFAULT_IMAGE);
        }
    }

    private void renderState(SynthesisState synthesisState) {
        int index = stateList.indexOf(synthesisState);

        File dgFile = getFile(index, "dg");
        File strategyFile = getFile(index, "strategy");

        if (!dgFile.exists() || !strategyFile.exists()) {
            String strategy = new AutomataStrategyToGraphViz().draw(synthesisState.strategy());
            EDGToGraphViz edgToGraphViz = new EDGToGraphViz();
            edgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(true);
            String dg = edgToGraphViz.draw(synthesisState.root(), synthesisState.propagationByConfiguration());
            dgFile = renderViz(dg, index, "dg");
            strategyFile = renderViz(strategy, index, "strategy");
        }

        this.closeFiles.clear();
        for (Set<DGConfiguration> configurations : synthesisState.close()) {
            this.closeFiles.add(configurations.stream().collect(Collectors.toMap(k -> k, v -> true)));
        }

        view.dg().loadImage(dgFile.getAbsolutePath());
        view.strategy().loadImage(strategyFile.getAbsolutePath());
        current = synthesisState;
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
            SvgViewer strategy,
            SvgViewer close
    ) {
    }
}
