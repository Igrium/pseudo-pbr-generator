package com.igrium.pseudo_pbr;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource("/ui/main_window.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("Pseudo PBR Generator");
        stage.setScene(scene);

        stage.show();
        
        // try {
        //     System.out.println("Loading "+Core.NATIVE_LIBRARY_NAME);
        //     OpenCV.loadShared();
        //     // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // } catch (Throwable e) {
        //     Alert alert = new Alert(AlertType.ERROR);
        //     alert.setTitle("Fatal");
        //     alert.setHeaderText("Unable to load OpenCV");
        //     alert.setContentText(e.getMessage());
        //     e.printStackTrace();
        //     alert.show();
        //     alert.setOnHidden(event -> {
        //         Platform.exit();
        //     });
        // }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
