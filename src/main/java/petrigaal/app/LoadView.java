package petrigaal.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import petrigaal.ctl.CTLSyntaxErrorException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class LoadView {
    private final Label fileError = new Label();
    private final Label formulaError = new Label();
    private final Label loadTime = new Label();
    private final TextField formulaField = new TextField();
    private final TextField modelPathTextField = new TextField();
    private final CheckBox onlyOnes = new CheckBox("Display only configurations which propagate 1");
    private final CheckBox legacy = new CheckBox("Use the legacy GraphViz renderer");
    private final VBox vb = new VBox();
    private final StackPane loadFileStack = new StackPane();

    public LoadView(Stage stage, Callback callback) {
        fileError.setTextFill(Color.RED);
        formulaError.setTextFill(Color.RED);

        modelPathTextField.textProperty().addListener((obs, old, newValue) -> {
            checkModelPathTextField(newValue);
        });
        formulaField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkFormulaField(newValue);
        });

        Button synthesizeButton = new Button("Synthesize");
        synthesizeButton.setOnMouseClicked(e -> {
            if (checkModelPathTextField(modelPathTextField.getText()) && checkFormulaField(formulaField.getText())) {
                callback.apply(
                        new File(modelPathTextField.getText()),
                        formulaField.getText(),
                        onlyOnes.isSelected(),
                        legacy.isSelected()
                );
            }
        });

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

        VBox loadingTimeVbox = new VBox(loadTime);
        HBox synthesizeBox = new HBox(loadingTimeVbox, synthesizeButton);
        synthesizeBox.setSpacing(10);
        HBox.setHgrow(loadingTimeVbox, Priority.ALWAYS);

        vb.getChildren().addAll(
                new Label("Model"),
                hBox,
                fileError,
                new Label("Formula"),
                formulaField,
                formulaError,
                onlyOnes,
                legacy,
                synthesizeBox
        );

        loadFileStack.getChildren().add(vb);
    }

    public void setLoadTime(long ms) {
        loadTime.setText(String.format("Completed in %d ms", ms));
    }

    public Node getView() {
        return loadFileStack;
    }

    public void startLoading() {
        ProgressIndicator pi = new ProgressIndicator();
        VBox box = new VBox(pi);
        box.setAlignment(Pos.CENTER);
        vb.setDisable(true);
        loadFileStack.getChildren().add(box);
    }

    public void stopLoading() {
        vb.setDisable(false);
        if (loadFileStack.getChildren().size() > 1) {
            loadFileStack.getChildren().remove(1);
        }
    }

    private boolean checkFormulaField(String newValue) {
        formulaError.setVisible(false);
        if (newValue.trim().isEmpty()) return false;
        try {
            new petrigaal.ctl.Parser().parse(newValue);
        } catch (CTLSyntaxErrorException e) {
            formulaError.setText(e.getMessage());
            formulaError.setVisible(true);
            return false;
        }
        return true;
    }

    private boolean checkModelPathTextField(String newValue) {
        fileError.setVisible(false);
        if (newValue.trim().isEmpty()) return false;

        File file = new File(newValue);
        if (!file.exists() || !file.canRead()) {
            fileError.setText("Invalid file");
            fileError.setVisible(true);
            return false;
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
        return true;
    }

    @FunctionalInterface
    public interface Callback {
        void apply(File model, String formula, boolean onlyShowPropagationOfOne, boolean legacyRender);
    }
}
