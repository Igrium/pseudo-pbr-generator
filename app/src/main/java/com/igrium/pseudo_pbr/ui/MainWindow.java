package com.igrium.pseudo_pbr.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.igrium.pseudo_pbr.methods.BlueFlyTrap36;
import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.texture_sets.SpecularGlossyTextureSet;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class MainWindow {

    @FXML
    private TextField modPathField;

    @FXML
    private Button modBrowseButton;

    @FXML
    private TextField enginePathField;

    @FXML
    private Button engineBrowseButton;

    @FXML
    private TextField qcInputField;

    @FXML
    private Button qcBrowseButton;

    @FXML
    private VBox textureSetters;

    private Map<ConversionMethod<?>, List<TextureSetter>> setterCache = new HashMap<>();

    private ConversionMethod<?> conversionMethod;

    @FXML
    private void initialize() {
        setConversionMethod(new BlueFlyTrap36());
    }

    public void setConversionMethod(ConversionMethod<?> method) {
        conversionMethod = method;
        textureSetters.getChildren().clear();
        List<TextureSetter> setters = setterCache.get(method);
        if (setters == null) {
            setters = new ArrayList<>();
            for (String name : method.getTextureSet().getTextureMapNames()) {
                setters.add(TextureSetter.open(name));
            }
        }
        
        for (TextureSetter setter : setters) {
            textureSetters.getChildren().add(setter.getPane());
        }

        setterCache.put(method, setters);
    }

    @FXML
    public void handleBrowseEngine() {
        File folder = showDirectoryChooser(enginePathField.getText(), "Select Engine Directory");
        if (folder == null) return;
        enginePathField.setText(folder.toString());
    }

    @FXML
    public void handleBrowseMod() {
        File folder = showDirectoryChooser(modPathField.getText(), "Select Mod Directory");
        if (folder == null) return;
        modPathField.setText(folder.toString());
    }

    @FXML
    public void handleBrowseQC() {
        File file = showFileChooser(modPathField.getText(), "Select QC File", new FileChooser.ExtensionFilter("QC Files", "*.qc"));
        if (file == null) return;
        qcInputField.setText(file.toString());
    }

    protected File showDirectoryChooser(String initial, String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (initial.length() > 0) {
            chooser.setInitialDirectory(new File(initial));
        }
        chooser.setTitle(title);
        return chooser.showDialog(modPathField.getScene().getWindow());
    }

    protected File showFileChooser(String initial, String title, FileChooser.ExtensionFilter... extensions) {
        FileChooser chooser = new FileChooser();
        if (initial.length() > 0) {
            chooser.setInitialDirectory(new File(initial));
        }
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(extensions);
        return chooser.showOpenDialog(modPathField.getScene().getWindow());
    }
}
