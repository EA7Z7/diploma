package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загрузка FXML из ресурсов
        URL fxmlUrl = getClass().getResource("/main.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Передача Stage в контроллер
        MainController controller = loader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("Document Processor");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}