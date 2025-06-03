package MVC;

import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants;
import Utils.Ref;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MainView extends Application implements Initializable {

    /** The Root node for the Info Display FXML file. */
    @FXML
    private Pane infoDisplayPane;

    /** The Root node for the Control Panel FXML file. */
    @FXML
    private Pane controlPanelPane;

    /** The Root node for the Canvas Control FXML file. */
    @FXML
    private Pane canvasControlPane;

    /** This class should have an iterator that communicates to Controller the information in TaskQueue */
    private Queue<Task> TaskQueue;

    /** The other Controller objects associated with the subsections of the fxml file. */
    private InfoDisplay infoDisplay;

    /** The other Controller objects associated with the subsections of the fxml file. */
    private ControlPanel controlPanel;

    /** The other Controller objects associated with the subsections of the fxml file. */
    private CanvasControl canvasControl;

    /**
     * Stores the reference to the instance of the model this View renders.
     * Controller writes to model via {@link #updateViewModel},
     * View reads from model via {@link CanvasControl#drawCanvas()}
     */
    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Stores a reference to the current selected Entity. */
    private Ref<Entity> selectedEntity;

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
            controller.init(model, selectedEntity);
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TaskQueue = new LinkedList<>();

        // starts the Controller Thread
        new Thread(new Controller(this)).start();
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Entity> selectedEntity) {
        this.model = model;
        this.selectedEntity = selectedEntity;
    }

    /** Adds a task to the {@linkplain #TaskQueue}. Doesn't do
     * anything if the Queue has over 100 tasks already. */
    void addTask(Task task) {
        synchronized (TaskQueue) {
            if (TaskQueue.size() > 100) return;
            TaskQueue.add(task);
        }
    }

    /** Allows the MVC Controller to remove 1 task from the TaskQueue. null if there is no Tasks */
    public Task pollTaskQueue() {
        synchronized (TaskQueue) {
            return TaskQueue.poll();
        }
    }

    /** Allows the MVC Controller to update text field */
    public void updateStepsPerSec(double stepsPerSec) {
        canvasControl.stepsPerSecCounter.setText(Double.toString(stepsPerSec));
    }

    /**
     * Allows the MVC Controller to poll simulation step speed.
     * @return any double from [0,1000], if returned value is 1000, run simulation as fast as possible */
    public int pollSimSpeed() {
        synchronized (controlPanel.speedSlider) {
            return (int) controlPanel.speedSlider.getValue();
        }
    }

    /**
     * Updates the View's rendering Model according to {@code newTiles}, used by Controller.
     * <br> Different from {@link #loadNewViewModel}, requires that {@code newModel} has the same
     * dimensions as the previous model
     */
    public void updateViewModel(GridWorld.ReadOnlyWorld newModel) {
        assert newModel != null;
        model.set(newModel);
        canvasControl.redrawCanvas();
    }

    /**
     * Loads a completely new Model into the GUI.
     * <br>generates all Hex rendering and is a lot more resource intensive than {@link #updateViewModel}
     */
    public synchronized void loadNewViewModel(GridWorld.ReadOnlyWorld newModel) {
        assert newModel != null;

        controlPanel.unselectContinuousStep();
        selectedEntity.clear();

        updateViewModel(newModel);
    }
}
