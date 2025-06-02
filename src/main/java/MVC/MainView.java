package MVC;

import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants.WindowConstants;
import Utils.Ref;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

class MainView extends Application implements Initializable {


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
    private final Ref<GridWorld.ReadOnlyWorld> model = new Ref<>(null);

    /** Stores a reference to the current selected Entity. */
    private final Ref<Entity> selectedEntity = new Ref<>(null);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        try {
            final URL r = getClass().getResource("/GUI.fxml");
            if (r == null) {
                System.err.println("No FXML resource found.");
                try {
                    stop();
                } catch (final Exception ignored) {
                }
            }
            assert r != null;
            final Parent node = FXMLLoader.load(r);
            final Scene scene = new Scene(node);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();
//            scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
//                System.out.println(event.getX() + "," + event.getY());
//            });
//            scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
//                System.out.println(scene.getWidth() + "," + scene.getHeight());
//            });
            stage.setMinWidth(WindowConstants.MIN_STAGE_WIDTH);
            stage.setMinHeight(WindowConstants.MIN_STAGE_HEIGHT);
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

    /** Adds a task to the {@linkplain #TaskQueue}. Doesn't do
     * anything if the Queue has over 100 tasks already. */
    private void addTask(Task task) {
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
