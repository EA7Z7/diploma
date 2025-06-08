package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.service.OdtToDocxConverter;
import org.example.service.WordReferenceUpdater;

import java.io.File;

public class MainController {

    @FXML private TextField inputField;
    @FXML private TextField outputField;
    @FXML private TextField titleField;
    @FXML private TextArea logArea;
    @FXML private ProgressBar progressBar;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void browseInput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ODT File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ODT Files", "*.odt")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            inputField.setText(file.getAbsolutePath());
            if (outputField.getText().isEmpty()) {
                outputField.setText(file.getParent());
            }
        }
    }

    @FXML
    private void browseOutput() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Folder");
        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            outputField.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    private void processDocument() {
        String inputPath = inputField.getText();
        String outputPath = outputField.getText();
        String title = titleField.getText();

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            log("Please select input file and output folder");
            return;
        }

        // Проверка расширения файла
        if (!inputPath.toLowerCase().endsWith(".odt")) {
            log("Error: Input file must have .odt extension");
            return;
        }

        progressBar.setProgress(0.1);
        log("Starting processing...");

        try {
            // Конвертация ODT -> DOCX
            log("Converting ODT to DOCX...");
            OdtToDocxConverter.convert(inputPath, outputPath);
            progressBar.setProgress(0.4);

            // Извлечение имени файла без расширения
            File inputFile = new File(inputPath);
            String fileName = inputFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

            // Обработка документа
            log("Processing references...");
            WordReferenceUpdater updater = new WordReferenceUpdater(baseName, title);
            updater.sortReferences();
            progressBar.setProgress(1.0);

            log("Processing completed successfully!");
            log("Output file: " + outputPath + "/new" + baseName + ".docx");

        } catch (Exception e) {
            log("Error: " + e.getMessage());
            e.printStackTrace();
            progressBar.setProgress(0);
        }
    }

    @FXML
    private void clearLog() {
        logArea.clear();
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }
}