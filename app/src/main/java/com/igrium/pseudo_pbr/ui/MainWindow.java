package com.igrium.pseudo_pbr.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.igrium.pseudo_pbr.methods.BlueFlyTrap36;
import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.texture_sets.TextureSet;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class MainWindow {

    protected enum DirectoryClass { TEXTURE, MOD, CONTENT }

    protected String texturePathCache;
    protected String modPathCache;
    protected String contentPathCache;

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
    private TextField contentPathField;

    @FXML
    private VBox textureSetters;

    @FXML
    private Button generateButton;

    @FXML
    private CheckMenuItem autofillCheck;

    private Map<ConversionMethod<?>, List<TextureSetter>> setterCache = new HashMap<>();

    private ConversionMethod<?> conversionMethod;

    public boolean shouldAutofill() {
        return autofillCheck.isSelected();
    }

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
                setters.add(TextureSetter.open(name, this));
            }
        }
        
        for (TextureSetter setter : setters) {
            textureSetters.getChildren().add(setter.getPane());
        }

        setterCache.put(method, setters);
    }

    @FXML
    public void handleBrowseEngine() {
        File folder = showDirectoryChooser(enginePathField.getText(), "Select Engine Directory", DirectoryClass.MOD);
        if (folder == null) return;
        enginePathField.setText(folder.toString());
        if (modPathCache == null) modPathCache = folder.toString();
    }

    @FXML
    public void handleBrowseMod() {
        File folder = showDirectoryChooser(modPathField.getText(), "Select Mod Directory", DirectoryClass.MOD);
        if (folder == null) return;
        modPathField.setText(folder.toString());
        modPathCache = folder.toString();
    }

    @FXML
    public void handleBrowseContent() {
        File folder = showDirectoryChooser(contentPathField.getText(), "Select Content Directory", DirectoryClass.CONTENT);
        if (folder == null) return;
        contentPathField.setText(folder.toString());
        contentPathCache = folder.toString();
    }

    @FXML
    public void handleBrowseQC() {
        File file = showFileChooser(modPathField.getText(), "Select QC File", DirectoryClass.CONTENT, new FileChooser.ExtensionFilter("QC Files", "*.qc"));
        if (file == null) return;
        qcInputField.setText(file.toString());
    }

    protected File showDirectoryChooser(String initial, String title, DirectoryClass dClass) {
        DirectoryChooser chooser = new DirectoryChooser();
        initial = getInitialDirectory(initial, dClass);
        if (initial != null) chooser.setInitialDirectory(new File(initial));

        chooser.setTitle(title);
        return chooser.showDialog(modPathField.getScene().getWindow());
    }

    protected File showFileChooser(String initial, String title, DirectoryClass dClass, FileChooser.ExtensionFilter... extensions) {
        FileChooser chooser = new FileChooser();
        initial = getInitialDirectory(initial, dClass);
        if (initial != null) chooser.setInitialDirectory(new File(initial));

        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(extensions);
        return chooser.showOpenDialog(modPathField.getScene().getWindow());
    }

    private void showError(String header, String body) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.show();
    }

    protected String getInitialDirectory(String text, DirectoryClass dClass) {
        if (text != null && text.length() > 0) {
            return text;
        }

        if (dClass == DirectoryClass.TEXTURE) {
            if (texturePathCache != null) return texturePathCache;
            else return null;
        } else if (dClass == DirectoryClass.MOD) {
            if (modPathCache != null) return modPathCache;
            else return null;
        } else if (dClass == DirectoryClass.CONTENT) {
            if (contentPathCache != null) return contentPathCache;
            else return getInitialDirectory(text, DirectoryClass.MOD);
        } else {
            return null;
        }
    }

    @FXML
    public void generate() {
        File modRoot = new File(modPathField.getText());
        if (!modRoot.isDirectory()) {
            showError("Mod path must be a directory.", "This should be the directory in which gameinfo.txt sits.");
            return;
        }

        File engineRoot = new File(enginePathField.getText());
        // if (!engineRoot.isDirectory()) {
        //     showError("Engine path must be a directory.", "This should be the folder with studiomdl.exe.");
        //     return;
        // }

        File contentRoot = new File(contentPathField.getText());
        if (!contentRoot.isDirectory()) {
            showError("Content path must be a directory.", "This may be the same as your mod path.");
            return;
        }

        try {
            TextureSet textures = conversionMethod.getTextureSet();
            Map<String, BufferedImage> textureMaps = new HashMap<>();
            for (TextureSetter setter : setterCache.get(conversionMethod)) {
                if (setter.getImage() != null) {
                    textureMaps.put(setter.getTitle(), setter.getImage());
                }
            }
            textures.setTextureMaps(textureMaps);

        } catch (Throwable e) {
            showError("Error loading texture maps.", e.getMessage());
            e.printStackTrace();
            return;
        }

        ProgressViewer.runConversion(
            conversionMethod,
            new File(qcInputField.getText()),
            new FileConsumer.BasicFileConsumer(modRoot.toPath()),
            new FileConsumer.BasicFileConsumer(contentRoot.toPath()),
            engineRoot.toPath()
        );
    }
}
