package MVC;

import Entities.Entity;
import Physics.GridWorld;
import Utils.Ref;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MainView implements Initializable {

    @FXML
    Pane infoDisplayPane;

    @FXML
    Pane controlPanelPane;

    @FXML
    Pane canvasControlPane;

    /** This class should have an iterator that communicates to Controller the information in TaskQueue */
    private Queue<Task> TaskQueue;

    /** The other Controller objects associated with the subsections of the fxml file. */
    InfoDisplay infoDisplay;

    /** The other Controller objects associated with the subsections of the fxml file. */
    ControlPanel controlPanel;

    /** The other Controller objects associated with the subsections of the fxml file. */
    CanvasControl canvasControl;

    /**
     * Stores the reference to the instance of the model this View renders.
     * Controller writes to model via {@link #updateViewModel},
     * View reads from model via {@link CanvasControl#drawCanvas()}
     */
    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Stores a reference to the current selected Entity. */
    private Ref<Entity> selectedEntity;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TaskQueue = new LinkedList<>();

        // starts the Controller Thread
        new Thread(new Controller(this)).start();
    }

    /** Custom initializer called by {@linkplain GUI}. */
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
