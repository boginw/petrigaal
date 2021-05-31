package petrigaal.app;

import guru.nidi.graphviz.engine.Format;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class PetriGAALApplication extends Application {
    public static final Format FORMAT = Format.SVG;
    public static final String DEFAULT_IMAGE = "start.svg";
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

        SplitPane splitView = new SplitPane();
        splitView.getItems().add(leftPanel);
        splitView.getItems().add(getTabPane());
        splitView.setDividerPositions(0.25f, 0.75f);

        BorderPane root = new BorderPane();
        root.setCenter(splitView);

        Scene scene = new Scene(root, 1400, 720);

        primaryStage.setTitle("PetriGAAL");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node getLoadView(Stage stage) {
        loadView = new LoadView(stage, this::synthesize);
        return loadView.getView();
    }

    private void synthesize(File modelFile, String formula, boolean displayOnlyOne, boolean legacyRender) {
        loadView.startLoading();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Synthesizer.Options options = new Synthesizer.Options(
                            modelFile,
                            formula,
                            displayOnlyOne,
                            legacyRender
                    );
                    Synthesizer synthesizer = new Synthesizer(options);
                    Synthesizer.Result synthesis = synthesizer.synthesize();
                    SynthesisRender.Result render = new SynthesisRender().render(synthesis, options);

                    Platform.runLater(() -> {
                        render(render, legacyRender, synthesis.time());
                        loadView.stopLoading();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadView.stopLoading();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("An error occurred while trying to render one of the models.");
                        alert.setContentText(e.getLocalizedMessage());
                        alert.showAndWait();
                    });
                    e.printStackTrace();
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

    private void render(SynthesisRender.Result result, boolean legacyRender, long time) {
        loadView.setLoadTime(time);

        if (result.dgSvg() == null) {
            view.dg().loadImage(DEFAULT_IMAGE);
        } else if (legacyRender) {
            view.dg().loadImage(result.dgSvg());
        } else {
            view.dg().loadGraph(result.dgSvg());
        }

        if (result.mdgSvg() == null) {
            view.meta().loadImage(DEFAULT_IMAGE);
        } else if (legacyRender) {
            view.meta().loadImage(result.mdgSvg());
        } else {
            view.meta().loadGraph(result.mdgSvg());
        }

        if (result.strategySvg() == null) {
            view.strategy().loadImage(DEFAULT_IMAGE);
        } else if (legacyRender) {
            view.strategy().loadImage(result.strategySvg());
        } else {
            view.strategy().loadGraph(result.strategySvg());
        }

        if (result.instanceSvg() == null) {
            view.instance().loadImage(DEFAULT_IMAGE);
        } else if (legacyRender) {
            view.instance().loadImage(result.instanceSvg());
        } else {
            view.instance().loadGraph(result.instanceSvg());
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
