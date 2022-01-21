package com.igrium.pseudo_pbr.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.ProgressListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressViewer {

    private static DecimalFormat df = new DecimalFormat();

    public class ProgressViewerListener implements ProgressListener {

        @Override
        public void progress(float percentage, String message) {
            Platform.runLater(() -> {
                setProgress(percentage);
                setStage(message);
            });
        }

    }

    protected Stage stage;

    @FXML
    ProgressBar progressBar;

    @FXML
    Label percentLabel;

    @FXML
    Label stageLabel;

    @FXML
    Label headerLabel;

    public void setProgress(float progress) {
        progressBar.setProgress(progress);
        percentLabel.setText(df.format(progress * 100) + "%");
    }

    public float getProgress() {
        return (float) progressBar.getProgress();
    }

    public void setStage(String stage) {
        this.stageLabel.setText(stage);
    }

    public void setHeader(String header) {
        this.headerLabel.setText(header);
    }

    public void close() {
        this.stage.close();
    }
    
    public static ProgressViewer open(String title) {
        FXMLLoader loader = new FXMLLoader(ProgressViewer.class.getResource("/ui/progress_viewer.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Error loading progress viewer UI", e);
        }
        
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle(title); 
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        ProgressViewer controller = loader.getController();
        controller.setHeader(title);
        controller.stage = stage;

        stage.show();

        return controller;
    }

    /**
     * Asynchronously execute a conversion, showing the progress in the UI.
     * @param method Conversion method to execute.
     * @param input See <code>ConversionMethod</code> for details.
     * @param gameFiles See <code>ConversionMethod</code> for details.
     * @param contentFiles See <code>ConversionMethod</code> for details.
     * @param enginePath See <code>ConversionMethod</code> for details.
     * @return A future that completes when the conversion is complete.
     */
    public static CompletableFuture<Void> runConversion(ConversionMethod<?> method, File input, FileConsumer gameFiles,
            FileConsumer contentFiles, Path enginePath) {
        ProgressViewer progressViewer = open("Generating Textures...");
        CompletableFuture<Void> future = new CompletableFuture<>();

        ForkJoinPool.commonPool().execute(() -> {
            try {
                method.execute(input, gameFiles, contentFiles, enginePath, progressViewer.new ProgressViewerListener());
                future.complete(null);
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });

        future.whenCompleteAsync((result, e) -> {    
            if (e != null) {
                Alert error = new Alert(AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Error performing texture conversion.");
                error.setContentText(e.getMessage());
                e.printStackTrace();
            } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Conversion successful.");
                alert.show();
            }
            progressViewer.close();
        }, Platform::runLater);
        
        return future;
    }

    static {
        df.setMaximumFractionDigits(2);
    }
}
