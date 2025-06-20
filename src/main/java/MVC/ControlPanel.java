package MVC;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

public final class ControlPanel implements Initializable {

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
        bindProperties();

        speedSlider.setValue(100);
        speedSlider.setMax(1000);

        continuousStep.setOnAction(e -> {
            if (continuousStep.isSelected()) {
                taskAdder.accept(new Task(Task.TaskType.RUN_CONTINUOUSLY));
                continuousStep.setText("Pause");
                advanceStep.setDisable(true);
            } else {
                taskAdder.accept(new Task(Task.TaskType.STOP_RUNNING));
                continuousStep.setText("Play");
                advanceStep.setDisable(false);
            }
        });

        advanceStep.setOnAction(e -> taskAdder.accept(new Task(Task.TaskType.STEP)));
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Consumer<Task> addTask) {
        taskAdder = addTask;
    }

    /** Binds the various width and height properties of the JavaFX FXML components correspondent
     * to this class. */
    private void bindProperties() {
        Runnable onSliderUpdate = () -> {
            synchronized (speedSlider) {
                int simSpeed = (int) speedSlider.getValue();
                speedSliderDisplay.setText(switch (simSpeed) {
                    case 0 -> "PAUSED";
                    case 1000 -> "MAX SPEED";
                    default -> simSpeed + " steps/sec";
                });
            }
        };

        speedSlider.valueProperty().addListener(num -> onSliderUpdate.run());
        speedSlider.setOnMouseReleased(event -> onSliderUpdate.run());
    }

    /** Unselects the continuous step toggle button when a new world is loaded. */
    public void unselectContinuousStep() {
        continuousStep.setSelected(false);
        continuousStep.setText("Play");
        advanceStep.setDisable(false);
    }
}
