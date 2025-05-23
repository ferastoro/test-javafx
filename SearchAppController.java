import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchAppController {

    @FXML
    private TextField keywordField;

    @FXML
    private Button addFolderButton;

    @FXML
    private Button searchButton;

    @FXML
    private Label selectedFolderLabel;

    @FXML
    private TextArea resultsArea;

    @FXML
    private Label statusLabel;

    private List<File> selectedFolders = new ArrayList<>();

    @FXML
    public void initialize() {
        resultsArea.setText("Search results will appear here...\n");
        selectedFolderLabel.setText("No folder selected.");
        statusLabel.setText("");
    }

    @FXML
    private void handleAddFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Search");
        File folder = directoryChooser.showDialog(addFolderButton.getScene().getWindow());

        if (folder != null) {
            if (!selectedFolders.contains(folder)) {
                selectedFolders.add(folder);
                updateSelectedFolderLabel();
                showAlert(Alert.AlertType.INFORMATION, "Folder Added", "Added folder: " + folder.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.WARNING, "Folder Already Added", "Folder: " + folder.getAbsolutePath() + " has already been added.");
            }
        }
    }

    private void updateSelectedFolderLabel() {
        if (selectedFolders.isEmpty()) {
            selectedFolderLabel.setText("No folder selected.");
        } else {
            selectedFolderLabel.setText("Selected: " + selectedFolders.stream()
                    .map(File::getName)
                    .collect(Collectors.joining(", ")));
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = keywordField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a keyword to search.");
            return;
        }

        if (selectedFolders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please add at least one folder to search.");
            return;
        }

        resultsArea.setText(""); // Bersihkan hasil sebelumnya
        statusLabel.setText("Searching...");

        // Membuat Task untuk pencarian agar UI tidak freeze
        Task<Void> searchTask = new Task<>() {
            @Override
            protected Void call() {
                for (File folder : selectedFolders) {
                    searchInFolder(folder, keyword);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    if (resultsArea.getText().isEmpty()) {
                        resultsArea.appendText("No results found for keyword: '" + keyword + "'\n");
                    }
                    statusLabel.setText("Search completed.");
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    statusLabel.setText("Search failed.");
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during search.");
                });
            }
        };

        new Thread(searchTask).start();
    }

    private void searchInFolder(File folder, String keyword) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchInFolder(file, keyword); // Pencarian rekursif jika ada subfolder
                } else {
                    // Hanya cari di file teks, bisa ditambahkan filter ekstensi lain jika perlu
                    if (file.getName().endsWith(".txt") || file.getName().endsWith(".java") || file.getName().endsWith(".py") || file.getName().endsWith(".log")) {
                        searchInFile(file, keyword);
                    }
                }
            }
        }
    }

    private void searchInFile(File file, String keyword) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.toLowerCase().contains(keyword.toLowerCase())) { // Pencarian case-insensitive
                    String result = String.format("%s (Line %d): %s\n", file.getAbsolutePath(), lineNumber, line.trim());
                    Platform.runLater(() -> resultsArea.appendText(result));
                }
            }
        } catch (IOException e) {
            // Tampilkan pesan error di TextArea atau status bar jika diperlukan
            String errorMsg = String.format("Error reading file %s: %s\n", file.getAbsolutePath(), e.getMessage());
            Platform.runLater(() -> resultsArea.appendText(errorMsg));
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(addFolderButton.getScene().getWindow()); // Menetapkan owner untuk dialog
        alert.showAndWait();
    }
}