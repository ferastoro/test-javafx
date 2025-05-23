import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SearchApp.fxml")));
        primaryStage.setTitle("Real-Time Multi-Folder Search by AKJ"); // Judul seperti di gambar
        primaryStage.setScene(new Scene(root, 700, 500)); // Sesuaikan ukuran jika perlu
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}