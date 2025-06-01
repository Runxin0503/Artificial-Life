package MVC;

import Entities.Entity;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import Physics.*;
import Utils.Constants.WindowConstants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MainView extends Application implements Initializable {

    /** Clears TaskQueue, adds a stopRunning Task and createNewWorld Task to Controller TaskQueue */
    @FXML
    private MenuItem newWorld;

    /** adds a loadWorld Task associated with a file path to Controller TaskQueue */
    @FXML
    private MenuItem loadWorld;

    /** disables {@link #advanceStep} when its toggled, adds a runContinuously/stopRunning Task to Controller TaskQueue  */
    @FXML
    private ToggleButton continuousStep;

    /** Adds a step Task to Controller TaskQueue */
    @FXML
    private Button advanceStep;

    /** Updates the view to show world info */
    @FXML
    private ToggleButton worldInfoToggle;

    /** Updates the view to show world info */
    @FXML
    private ToggleButton entityInfoToggle;

    @FXML
    private Slider speedSlider;

    @FXML
    private Text speedSliderDisplay;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text aliveCritters;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text timeElapsed;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text numFoodTiles;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text numRockTiles;

    /** Controller will update this text through a function */
    @FXML
    private Text memsizeText;
    /** Controller will update this text through a function */
    @FXML
    private Text defenseText;
    /** Controller will update this text through a function */
    @FXML
    private Text attackText;
    /** Controller will update this text through a function */
    @FXML
    private Text sizeText;
    /** Controller will update this text through a function */
    @FXML
    private Text energyText;
    /** Controller will update this text through a function */
    @FXML
    private Text passnumText;
    /** Controller will update this text through a function */
    @FXML
    private Text postureText;

    /** Controller will update this text through a function */
    @FXML
    private TextArea lastRuleDone;

    /** Initializes when user clicks on a critter hex tile.
     * <br>Controller will erase this text through a function once critter dies */
    @FXML
    private TextArea critterProgram;

    @FXML
    private GridPane worldGridPane;

    @FXML
    private GridPane critterGridPane;

    @FXML
    private MenuBar menuBar;

    /** This class should have an iterator that communicates to Controller the information in TaskQueue */
    private Queue<Task> TaskQueue;

    /**
     * Stores the instance of the model this View renders.
     * Controller writes to model via {@link #updateViewModel},
     * View reads from model via {@link Canvas#drawCanvas()}
     */
    private GridWorld.ReadOnlyWorld model;

    /** Stores the current selected Entity */
    private Entity selectedEntity;

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
        model = null;

        newWorld.setOnAction(event -> {
            addTask(new Task(Task.TaskType.CREATE_NEW_WORLD));
        });

        addTask(new Task(Task.TaskType.CREATE_NEW_WORLD));


        loadWorld.setOnAction(event -> {
            File file = openFileExplorer();
            if (file == null) return;
            addTask(new Task(Task.TaskType.LOAD_WORLD, file.getAbsolutePath()));
        });

        critterGridPane.setVisible(false);
        worldInfoToggle.setSelected(true);
        entityInfoToggle.setDisable(true);

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

        // starts the Controller Thread
        new Thread(new Controller(this)).start();

    }

    private void addTask(Task task) {
        synchronized (TaskQueue) {
            if(TaskQueue.size() > 100) return;
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
        stepsPerSecCounter.setText(Double.toString(stepsPerSec));
    }

    /**
     * Allows the MVC Controller to poll simulation step speed.
     * @return any double from [0,1000], if returned value is 1000, run simulation as fast as possible */
    public int pollSimSpeed() {
        synchronized (speedSlider){
            return (int) speedSlider.getValue();
        }
    }

    /**
     * Updates the View's rendering Model according to {@code newTiles}, used by Controller.
     * <br> Different from {@link #loadNewViewModel}, requires that {@code newModel} has the same
     * dimensions as the previous model
     */
    public void updateViewModel(GridWorld.ReadOnlyWorld newModel) {
        assert newModel != null;
        synchronized (canvas){
            model = newModel;
            redrawCanvas = true;
        }
    }

    /**
     * Loads a completely new Model into the GUI.
     * <br>generates all Hex rendering and is a lot more resource intensive than {@link #updateViewModel}
     */
    public synchronized void loadNewViewModel(GridWorld.ReadOnlyWorld newModel) {
        assert newModel != null;

        continuousStep.setSelected(false);
        Platform.runLater(() -> handleContinuousStepPressed(new ActionEvent()));

        selectedEntity = null;

        updateViewModel(newModel);
    }

    private void updateSelectedEntity() {
        if (selectedEntity!=null) {
            entityInfoToggle.setDisable(false);
            worldGridPane.setVisible(false);
            critterGridPane.setVisible(true);
            worldInfoToggle.setSelected(false);
            entityInfoToggle.setSelected(true);
            // TODO list information about the selected entity here
//            ReadOnlyCritter readOnlyCritter;
//            try {
//                readOnlyCritter = model.getReadOnlyCritter(selectedTileCoords[0], selectedTileCoords[1]).get();
//            } catch (NoMaybeValue e) {
//                throw new RuntimeException("Unexpected outcome whilst trying to obtain Maybe value");
//            }
//
//            int[] critterMem = readOnlyCritter.getMemory();
//            memsizeText.setText(String.valueOf(critterMem[0]));
//            defenseText.setText(String.valueOf(critterMem[1]));
//            attackText.setText(String.valueOf(critterMem[2]));
//            sizeText.setText(String.valueOf(critterMem[3]));
//            energyText.setText(String.valueOf(critterMem[4]));
//            passnumText.setText(String.valueOf(critterMem[5]));
//            postureText.setText(String.valueOf(critterMem[6]));
//            lastRuleDone.setText(readOnlyCritter.getLastRuleString()
//                    .orElse("This Critter hasn't executed any rules yet..."));
//            critterProgram.setText(readOnlyCritter.getProgramString());
        } else {
            entityInfoToggle.setDisable(true);
            worldInfoToggle.setSelected(true);
            critterGridPane.setVisible(false);
            worldGridPane.setVisible(true);
            entityInfoToggle.setSelected(false);
        }
    }


    @FXML
    private void handleWorldCritterTogglePressed(final ActionEvent e) {
        if (e.getSource() == worldInfoToggle) {
            worldInfoToggle.setSelected(true);
            critterGridPane.setVisible(false);
            worldGridPane.setVisible(true);
            entityInfoToggle.setSelected(false);
        } else if (e.getSource() == entityInfoToggle) {
            worldGridPane.setVisible(false);
            critterGridPane.setVisible(true);
            worldInfoToggle.setSelected(false);
        }
    }

    @FXML
    private void handleContinuousStepPressed(final ActionEvent e) {
        if (continuousStep.isSelected()) {
            addTask(new Task(Task.TaskType.RUN_CONTINUOUSLY));
            continuousStep.setText("Pause");
            advanceStep.setDisable(true);
        } else {
            addTask(new Task(Task.TaskType.STOP_RUNNING));
            continuousStep.setText("Play");
            advanceStep.setDisable(false);
        }
    }

    @FXML
    private void handleAdvanceStepPressed(final ActionEvent e) {
        addTask(new Task(Task.TaskType.STEP));
    }

    /** Uses a popup to prompt the user for a file, returns null if user closes the popup */
    private File openFileExplorer() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window stage = menuBar.getScene().getWindow();

        //critter/world to be loaded
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection canceled.");
        }

        return selectedFile;
    }
}
