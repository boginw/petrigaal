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
    public static final String DEFAULT_IMAGE = "start.png";
    public static Consumer<SynthesisState> onNewState;
    private final ObservableList<SynthesisState> stateList = FXCollections.observableArrayList();
    private final ObservableList<File> closeFiles = FXCollections.observableArrayList();
    private StateView view;

    @Override
    public void start(Stage primaryStage) throws Exception {
        onNewState = this::newState;

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

    private TabPane getTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab strategyTab = new Tab("Strategy", view.strategy().getWebView());
        Tab dependencyGraphTab = new Tab("Dependency Graph", view.dg().getWebView());
        Tab closeTab = new Tab("Close", view.close().getWebView());
        tabPane.getTabs().addAll(strategyTab, dependencyGraphTab, closeTab);
        return tabPane;
    }

    private ListView<File> getFileListView() {
        ListView<File> closeList = new ListView<>(closeFiles);
        closeList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            renderClose(newValue);
        });
        return closeList;
    }

    private ListView<SynthesisState> getSynthesisStateListView() {
        ListView<SynthesisState> listView = new ListView<>(stateList);
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            renderState(newValue);
        });
        return listView;
    }

    private void newState(SynthesisState synthesisState) {
        stateList.add(synthesisState);
    }

    private void renderClose(File newValue) {
        if (newValue != null) {
            view.close().loadImage(newValue.getAbsolutePath());
        } else {
            view.close().loadImage(DEFAULT_IMAGE);
        }
    }

    private void renderState(SynthesisState synthesisState) {
        int index = stateList.indexOf(synthesisState);

        File dgFile = getFile(index, "dg");
        File strategyFile = getFile(index, "strategy");
        List<File> closeFiles = Arrays.stream(
                Objects.requireNonNull(getDir(index).listFiles((f, n) -> n.startsWith("close")))
        ).sorted().toList();

        if (!dgFile.exists() || !strategyFile.exists()) {
            String strategy = new AutomataStrategyToGraphViz().draw(synthesisState.strategy());
            EDGToGraphViz edgToGraphViz = new EDGToGraphViz();
            edgToGraphViz.setDisplayOnlyConfigurationsWhichPropagateOne(true);
            String dg = edgToGraphViz.draw(synthesisState.root(), synthesisState.propagationByConfiguration());
            dgFile = renderViz(dg, index, "dg");
            strategyFile = renderViz(strategy, index, "strategy");
            int closes = 0;

            closeFiles = new ArrayList<>();
            for (Set<Configuration> configurations : synthesisState.close()) {
                Map<Configuration, Boolean> collect = configurations.stream().collect(Collectors.toMap(k -> k, v -> true));
                String close = new EDGToGraphViz().draw(synthesisState.root(), collect);
                String name = "close" + (closes++);
                File closeFile = renderViz(close, index, name);
                closeFiles.add(closeFile);
            }
        }

        this.closeFiles.clear();
        this.closeFiles.addAll(closeFiles);

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
            SvgViewer strategy,
            SvgViewer close
    ) {
    }
}
