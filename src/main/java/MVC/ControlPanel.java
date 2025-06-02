package MVC;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

import java.util.function.Consumer;

class ControlPanel {

    /** disables {@link #advanceStep} when its toggled, adds a runContinuously/stopRunning Task to Controller TaskQueue  */
    @FXML
    private ToggleButton continuousStep;

    /** Adds a step Task to Controller TaskQueue */
    @FXML
    private Button advanceStep;

    /** Controls the step speed of the World. */
    @FXML
    public Slider speedSlider;

    /** Displays the current selected step speed of the World. */
    @FXML
    private Text speedSliderDisplay;

    /** Lambda function that adds a task to the Task-Queue in MainView according
     * to {@code MainView.addTask}. */
    private final Consumer<Task> taskAdder;

    public ControlPanel(Consumer<Task> addTask) {
        taskAdder = addTask;

        speedSlider.setValue(100);
        speedSlider.setMax(1000);
        speedSliderDisplay.setText("100 steps/sec");
        speedSlider.valueProperty().addListener(num -> {
            synchronized (speedSlider) {
                int simSpeed = (int) speedSlider.getValue();
                speedSliderDisplay.setText(simSpeed == 1000 ? "MAX SPEED" : simSpeed + " steps/sec");
            }
        });
        speedSlider.setOnMouseReleased(event -> {
            synchronized (speedSlider) {
                int simSpeed = (int) speedSlider.getValue();
                speedSliderDisplay.setText(simSpeed == 1000 ? "MAX SPEED" : simSpeed + " steps/sec");
            }
        });
    }

    /** Unselects the continuous step toggle button when a new world is loaded. */
    public void unselectContinuousStep() {
        continuousStep.setSelected(false);
        Platform.runLater(() -> handleContinuousStepPressed(null));
    }

    @FXML
    private void handleContinuousStepPressed(final ActionEvent e) {
        if (continuousStep.isSelected()) {
            taskAdder.accept(new Task(Task.TaskType.RUN_CONTINUOUSLY));
            continuousStep.setText("Pause");
            advanceStep.setDisable(true);
        } else {
            taskAdder.accept(new Task(Task.TaskType.STOP_RUNNING));
            continuousStep.setText("Play");
            advanceStep.setDisable(false);
        }
    }

    @FXML
    private void handleAdvanceStepPressed(final ActionEvent e) {
        taskAdder.accept(new Task(Task.TaskType.STEP));
    }
}
