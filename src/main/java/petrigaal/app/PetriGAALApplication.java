package petrigaal.app;

import guru.nidi.graphviz.engine.Format;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import petrigaal.atl.CTLSyntaxErrorException;
import petrigaal.edg.DGConfiguration;
import petrigaal.strategy.TopDownStrategySynthesiser.SynthesisState;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class PetriGAALApplication extends Application {
    public static final Format FORMAT = Format.SVG;
    public static final String DEFAULT_IMAGE = "start.png";
    private final ObservableList<SynthesisState> stateList = FXCollections.observableArrayList();
    private final ObservableList<Map<DGConfiguration, Boolean>> closeFiles = FXCollections.observableArrayList();
    private StateView view;
    private final StackPane loadFileStack = new StackPane();
    private final VBox vb = new VBox();

    @Override
    public void start(Stage primaryStage) throws Exception {
        view = new StateView(
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
        final Label fileError = new Label();
        fileError.setTextFill(Color.RED);
        final Label formulaError = new Label();
        formulaError.setTextFill(Color.RED);
        final Button synthesizeButton = new Button("Synthesize");
        final TextField formulaField = new TextField();
        final TextField modelPathTextField = new TextField();
        final CheckBox checkBox = new CheckBox("Display only configurations which propagate 1");

        modelPathTextField.textProperty().addListener((obs, old, newValue) -> {
            fileError.setVisible(false);
            if (newValue.trim().isEmpty()) return;

            File file = new File(newValue);
            if (!file.exists() || !file.canRead()) {
                fileError.setText("Invalid file");
                fileError.setVisible(true);
            } else {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    File modelFile = Path.of(parentFile.getPath(), "formula").toFile();
                    if (modelFile.exists()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(modelFile))) {
                            formulaField.setText(reader.readLine());
                        } catch (IOException fileNotFoundException) {
                            fileNotFoundException.printStackTrace();
                        }
                    }
                }
            }
        });
        formulaField.textProperty().addListener((observable, oldValue, newValue) -> {
            formulaError.setVisible(false);
            if (newValue.trim().isEmpty()) return;
            try {
                new petrigaal.atl.Parser().parse(newValue);
            } catch (CTLSyntaxErrorException e) {
                formulaError.setText(e.getMessage());
                formulaError.setVisible(true);
            }
        });
        synthesizeButton.setOnMouseClicked(e -> synthesize(
                new File(modelPathTextField.getText()),
                formulaField.getText(),
                checkBox.isSelected()
        ));

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open TAPAAL Model");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TAPAAL models", "*.tapn"));

        vb.setSpacing(8);
        vb.setPadding(new Insets(10, 10, 10, 10));
        Button browseButton = new Button("Browse");

        browseButton.setOnMouseClicked(e -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) return;
            modelPathTextField.setText(file.getPath());
        });

        HBox hBox = new HBox(modelPathTextField, browseButton);
        hBox.setSpacing(10);
        HBox.setHgrow(modelPathTextField, Priority.ALWAYS);

        vb.getChildren().addAll(
                new Label("Model"),
                hBox,
                fileError,
                new Label("Formula"),
                formulaField,
                formulaError,
                checkBox,
                synthesizeButton
        );

        loadFileStack.getChildren().add(vb);

        return loadFileStack;
    }

    private void synthesize(File modelFile, String formula, boolean displayOnlyOne) {
        ProgressIndicator pi = new ProgressIndicator();
        VBox box = new VBox(pi);
        box.setAlignment(Pos.CENTER);
        vb.setDisable(true);
        loadFileStack.getChildren().add(box);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Synthesizer.Options options = new Synthesizer.Options(modelFile, formula, displayOnlyOne);
                    Synthesizer synthesizer = new Synthesizer(options);
                    Synthesizer.Result synthesis = synthesizer.synthesize();
                    SynthesisRender.Result render = new SynthesisRender().render(synthesis, options);

                    Platform.runLater(() -> {
                        vb.setDisable(false);
                        loadFileStack.getChildren().remove(1);
                        render(render);
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
        tabPane.getTabs().addAll(metaTab, dependencyGraphTab, strategyTab);
        return tabPane;
    }

    private ListView<Map<DGConfiguration, Boolean>> getFileListView() {
        return new CloseListView(closeFiles);
    }

    private ListView<SynthesisState> getSynthesisStateListView() {
        return new SynthesisStateListView(stateList);
    }

    private void render(SynthesisRender.Result result) {
        if (!result.dgFile().exists()) {
            view.dg().loadImage(DEFAULT_IMAGE);
        } else {
            view.dg().loadImage(result.dgFile().getAbsolutePath());
        }

        if (!result.mdgFile().exists()) {
            view.meta().loadImage(DEFAULT_IMAGE);
        } else {
            view.meta().loadImage(result.mdgFile().getAbsolutePath());
        }

        if (!result.strategyFile().exists()) {
            view.strategy().loadImage(DEFAULT_IMAGE);
        } else {
            view.strategy().loadImage(result.strategyFile().getAbsolutePath());
        }
    }

    private static record StateView(
            SvgViewer dg,
            SvgViewer strategy,
            SvgViewer meta
    ) {
    }
}
