package MVC;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ControlPanel implements Initializable {

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
    private Consumer<Task> taskAdder;

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        speedSlider.setValue(100);
        speedSlider.setMax(1000);
        speedSliderDisplay.setText("100 steps/sec");
        speedSlider.valueProperty().addListener(num -> {
            synchronized (speedSlider) {
                int simSpeed = (int) speedSlider.getValue();
                speedSliderDisplay.setText(switch (simSpeed) {
                    case 0 -> "PAUSED";
                    case 1000 -> "MAX SPEED";
                    default -> simSpeed + " steps/sec";
                });
            }
        });
        speedSlider.setOnMouseReleased(event -> {
            synchronized (speedSlider) {
                int simSpeed = (int) speedSlider.getValue();
                speedSliderDisplay.setText(simSpeed == 1000 ? "MAX SPEED" : simSpeed + " steps/sec");
            }
        });
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Consumer<Task> addTask) {
        taskAdder = addTask;
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
