package MVC;

import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants;
import Utils.Ref;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/MVC/MainView.fxml"));
            Parent root = mainLoader.load();

            final Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();

            stage.setMinWidth(Constants.WindowConstants.MIN_STAGE_WIDTH);
            stage.setMinHeight(Constants.WindowConstants.MIN_STAGE_HEIGHT);

            MainView controller = mainLoader.getController();

            // Load InfoDisplay.fxml
            FXMLLoader infoLoader = new FXMLLoader(getClass().getResource("/MVC/InfoDisplay.fxml"));
            Parent infoDisplayRoot = infoLoader.load();
            controller.infoDisplay = infoLoader.getController();
            controller.infoDisplayPane.getChildren().add(infoDisplayRoot);

            // Load ControlPanel.fxml
            FXMLLoader controlLoader = new FXMLLoader(getClass().getResource("/MVC/ControlPanel.fxml"));
            Parent controlPanelRoot = controlLoader.load();
            controller.controlPanel = controlLoader.getController();
            controller.controlPanelPane.getChildren().add(controlPanelRoot);

            // Load CanvasControl.fxml
            FXMLLoader canvasLoader = new FXMLLoader(getClass().getResource("/MVC/CanvasControl.fxml"));
            Parent canvasControlRoot = canvasLoader.load();
            controller.canvasControl = canvasLoader.getController();
            controller.canvasControlPane.getChildren().add(canvasControlRoot);

            // Init all controller classes
            Ref<GridWorld.ReadOnlyWorld> model = new Ref<>(null);
            Ref<Entity> selectedEntity = new Ref<>(null);
            controller.infoDisplay.init(model, selectedEntity);
            controller.controlPanel.init(controller::addTask);
            controller.canvasControl.init(model);

        } catch (final IOException ioe) {
            System.err.println("Can't load FXML file.");
            ioe.printStackTrace();
            try {
                stop();
            } catch (final Exception ignored) {
            }
        }
    }

}
