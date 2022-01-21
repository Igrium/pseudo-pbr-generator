package com.igrium.pseudo_pbr.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.igrium.pseudo_pbr.ui.MainWindow.DirectoryClass;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class TextureSetter {

    public static Image MISSING_IMAGE = new Image("/ui/images/missing_image.png");
    private MainWindow parent;

    private File currentFile;
    private BufferedImage image;
    
    @FXML
    private ImageView preview;

    @FXML
    private TextField fileNameField;

    @FXML
    private TitledPane rootPane;

    @Deprecated
    public TextureSetter() {}

    public File getFile() {
        return currentFile;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getTitle() {
        return rootPane.getText();
    }

    public void setTitle(String title) {
        rootPane.setText(title);
    }

    @FXML
    public void handleBrowse() {
        FileChooser chooser = new FileChooser();
        String parentPath = currentFile != null ? currentFile.getParent() : "";
        
        String initial = parent.getInitialDirectory(parentPath, DirectoryClass.TEXTURE);
        if (initial != null) chooser.setInitialDirectory(new File(initial));
        
        File file = chooser.showOpenDialog(preview.getScene().getWindow());
        if (file == null) return;
        try {
            loadFile(file);
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error opening image.");
            alert.setContentText(e.getMessage());
            e.printStackTrace();
            alert.show();
        }
        parent.texturePathCache = file.getParent();
    }

    @FXML
    public void handleClear() {
        try {
            loadFile(null);
        } catch (IOException e) {}
    }

    public void loadFile(File file) throws IOException {
        if (file == null) {
            preview.setImage(MISSING_IMAGE);
            currentFile = null;
            image = null;
            fileNameField.setText("");
            return;
        }

        image = ImageIO.read(file);
        currentFile = file;
        fileNameField.setText(currentFile.getName());
        preview.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public TitledPane getPane() {
        return rootPane;
    }
    
    /**
     * Open a new texture setter.
     * @param title Title to give the dropdown.
     * @return Texture setter instance.
     */
    public static TextureSetter open(String title, MainWindow parent) {
        FXMLLoader loader = new FXMLLoader(TextureSetter.class.getResource("/ui/texture_setter.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load texture setter.", e);
        }
        TextureSetter obj = loader.getController();
        obj.setTitle(title);
        obj.parent = parent;
        return obj;
    }
}
