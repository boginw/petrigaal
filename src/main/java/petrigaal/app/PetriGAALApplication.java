package petrigaal.app;

import guru.nidi.graphviz.engine.Format;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import petrigaal.edg.DGConfiguration;
import petrigaal.strategy.TopDownStrategySynthesiser.SynthesisState;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public class PetriGAALApplication extends Application {
    public static final Format FORMAT = Format.SVG;
    public static final String DEFAULT_IMAGE = "start.png";
    private final ObservableList<SynthesisState> stateList = FXCollections.observableArrayList();
    private final ObservableList<Map<DGConfiguration, Boolean>> closeFiles = FXCollections.observableArrayList();
    private StateView view;
    private LoadView loadView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        view = new StateView(
                new SvgViewer(DEFAULT_IMAGE),
                new SvgViewer(DEFAULT_IMAGE),
                new SvgViewer(DEFAULT_IMAGE),
                new SvgViewer(DEFAULT_IMAGE)
        );

        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(getLoadView(primaryStage));
        SplitPane horizontal = new SplitPane();
        horizontal.setOrientation(Orientation.VERTICAL);
        horizontal.getItems().addAll(getSynthesisStateListView(), getFileListView());
        leftPanel.getChildren().add(horizontal);

        SplitPane splitView = new SplitPane();
        splitView.getItems().add(leftPanel);
        splitView.getItems().add(getTabPane());
        splitView.setDividerPositions(0.2f, 0.8f);

        BorderPane root = new BorderPane();
        root.setCenter(splitView);

        Scene scene = new Scene(root, 1920, 1080);

        primaryStage.setTitle("IntelliGaal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node getLoadView(Stage stage) {
        loadView = new LoadView(stage, this::synthesize);
        return loadView.getView();
    }

    private void synthesize(File modelFile, String formula, boolean displayOnlyOne) {
        loadView.startLoading();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Synthesizer.Options options = new Synthesizer.Options(modelFile, formula, displayOnlyOne);
                    Synthesizer synthesizer = new Synthesizer(options);
                    Synthesizer.Result synthesis = synthesizer.synthesize();
                    SynthesisRender.Result render = new SynthesisRender().render(synthesis, options);

                    Platform.runLater(() -> {
                        render(render);
                        loadView.stopLoading();
                    });
                } catch (IllegalAccessException | FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        };
        task.run();
    }

    private TabPane getTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab strategyTab = new Tab("Strategy", view.strategy().getWebView());
        Tab dependencyGraphTab = new Tab("Dependency Graph", view.dg().getWebView());
        Tab metaTab = new Tab("Meta Dependency Graph", view.meta().getWebView());
        Tab instanceTab = new Tab("Strategy Instance", view.instance().getWebView());
        tabPane.getTabs().addAll(dependencyGraphTab, metaTab, strategyTab, instanceTab);
        return tabPane;
    }

    private ListView<Map<DGConfiguration, Boolean>> getFileListView() {
        return new CloseListView(closeFiles);
    }

    private ListView<SynthesisState> getSynthesisStateListView() {
        return new SynthesisStateListView(stateList);
    }

    private void render(SynthesisRender.Result result) {
        if (result.dgSvg() == null) {
            view.dg().loadImage(DEFAULT_IMAGE);
        } else {
            view.dg().loadImage(result.dgSvg());
        }

        if (result.mdgSvg() == null) {
            view.meta().loadImage(DEFAULT_IMAGE);
        } else {
            view.meta().loadImage(result.mdgSvg());
        }

        if (result.strategySvg() == null) {
            view.strategy().loadImage(DEFAULT_IMAGE);
        } else {
            view.strategy().loadImage(result.strategySvg());
        }

        if (result.instanceSvg() == null) {
            view.instance().loadImage(DEFAULT_IMAGE);
        } else {
            view.instance().loadImage(result.instanceSvg());
        }
    }

    private static record StateView(
            SvgViewer dg,
            SvgViewer strategy,
            SvgViewer meta,
            SvgViewer instance
    ) {
    }
}
