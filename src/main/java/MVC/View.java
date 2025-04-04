package MVC;

import Entities.Entity;
import Utils.Constants;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import Physics.*;
import Utils.Constants.WindowConstants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class View extends Application implements Initializable {

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

    /**  */
    @FXML
    private Text fpsCounter;

    /** A function in View that Controller calls to update Text directly*/
    @FXML
    private Text stepsPerSecCounter;

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
    private ImageView backgroundImage;

    /**
     * Adds a 'Select Critter' task with a reference to the critter itself to TaskQueue
     * <br>Be able to select any hex tiles, only send event when selected hex tile is a critter
     */
    @FXML
    private Canvas canvas;

    private Affine canvasTransform;

    @FXML private Button recenterButton;

    @FXML
    private StackPane worldScroller;

    @FXML
    private MenuBar menuBar;

    /** This class should have an iterator that communicates to Controller the information in TaskQueue */
    private Queue<Task> TaskQueue;

    /**
     * Stores the instance of the model this View renders.
     * <br>Synchronized on {@link #canvas},
     * Controller writes to model via {@link #updateViewModel},
     * View reads from model via {@link #drawCanvas()}
     */
    private GridWorld.ReadOnlyWorld model;

    private boolean redrawCanvas;

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
            stage.setTitle("WALLY'S WORLD");
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

        canvasTransform = new Affine();

        redrawCanvas = false;

//        backgroundImage.fitWidthProperty().bind(worldScroller.widthProperty());
//        backgroundImage.fitHeightProperty().bind(worldScroller.heightProperty());
        worldScroller.widthProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle2D currentViewport = backgroundImage.getViewport();
            double newWidth = newVal.doubleValue();
            double newHeight = worldScroller.getHeight();
            backgroundImage.setViewport(new Rectangle2D(
                    currentViewport.getMinX(),
                    currentViewport.getMinY(),
                    newWidth,
                    newHeight
            ));
        });

        backgroundImage.setPreserveRatio(true);
        backgroundImage.setViewport(new Rectangle2D(0,0,backgroundImage.getFitWidth(),backgroundImage.getFitHeight()));

        worldScroller.heightProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle2D currentViewport = backgroundImage.getViewport();
            double newWidth = worldScroller.getWidth();
            double newHeight = newVal.doubleValue();
            backgroundImage.setViewport(new Rectangle2D(
                    currentViewport.getMinX(),
                    currentViewport.getMinY(),
                    newWidth,
                    newHeight
            ));
        });

        canvas.widthProperty().bind(worldScroller.widthProperty().add(WindowConstants.CANVAS_PADDING * 2));
        canvas.heightProperty().bind(worldScroller.heightProperty().add(WindowConstants.CANVAS_PADDING * 2));

        canvas.widthProperty().addListener(event->redrawCanvas=true);
        canvas.heightProperty().addListener(event->redrawCanvas=true);

        newWorld.setOnAction(event-> {
            addTask(new Task(Task.TaskType.CREATE_NEW_WORLD));
        });

        backgroundImage.fitWidthProperty().bind(canvas.widthProperty());
        backgroundImage.fitHeightProperty().bind(canvas.heightProperty());

        addTask(new Task(Task.TaskType.CREATE_NEW_WORLD));


        loadWorld.setOnAction(event -> {
            File file = openFileExplorer();
            if(file == null) return;
            addTask(new Task(Task.TaskType.LOAD_WORLD,file.getAbsolutePath()));
        });

        worldScroller.setOnScroll(ae -> {
            if (ae.getDeltaY() != 0) {  // Only react to vertical scroll
                double zoomFactor = ae.getDeltaY() > 0 ? 1.1 : 0.9;  // Zoom in or out
                if ((canvasTransform.getMxx() >= WindowConstants.MAX_ZOOM / 1.1 || canvasTransform.getMyy() >= WindowConstants.MAX_ZOOM / 1.1) && zoomFactor > 1)
                    return;
                if ((canvasTransform.getMxx() <= WindowConstants.MIN_ZOOM / 0.9 || canvasTransform.getMyy() <= WindowConstants.MIN_ZOOM / 0.9) && zoomFactor < 1)
                    return;

                Point2D mouseCoords;
                try{
                    mouseCoords = canvasTransform.inverseTransform(canvas.sceneToLocal(ae.getSceneX(), ae.getSceneY()));
                } catch (NonInvertibleTransformException e) {
                    throw new RuntimeException(e);
                }

                double oldScaleX = canvasTransform.getMxx(), oldScaleY = canvasTransform.getMyy();

                canvasTransform.setMxx(Math.clamp(oldScaleX * zoomFactor, WindowConstants.MIN_ZOOM, WindowConstants.MAX_ZOOM));
                canvasTransform.setMyy(Math.clamp(oldScaleY * zoomFactor, WindowConstants.MIN_ZOOM, WindowConstants.MAX_ZOOM));

                // Translate to maintain zoom focus on the mouse position
                canvasTransform.setTx(canvasTransform.getTx() - mouseCoords.getX() * (canvasTransform.getMxx() - oldScaleX));
                canvasTransform.setTy(canvasTransform.getTy() - mouseCoords.getY() * (canvasTransform.getMyy() - oldScaleY));

                redrawCanvas = true;
            }
        });

        final double[] dragAnchor = new double[2]; // To store initial mouse click position
        worldScroller.setOnMousePressed(ae -> {
            // Store initial mouse position for panning
            dragAnchor[0] = ae.getSceneX() - canvasTransform.getTx();
            dragAnchor[1] = ae.getSceneY() - canvasTransform.getTy();
        });

        worldScroller.setOnMouseDragged(ae -> {
            // Calculate new position for panning
            double offsetX = ae.getSceneX() - dragAnchor[0];
            double offsetY = ae.getSceneY() - dragAnchor[1];
            canvasTransform.setTx(offsetX);
            canvasTransform.setTy(offsetY);

            redrawCanvas = true;
        });

        worldScroller.setOnMouseReleased(ae -> {
            if (model == null) return;

            Point2D mouseCoords;
            try{
                mouseCoords = canvasTransform.inverseTransform(canvas.sceneToLocal(ae.getSceneX(), ae.getSceneY()));
            } catch (NonInvertibleTransformException e) {
                throw new RuntimeException(e);
            }

            redrawCanvas = true;

            // TODO find the selected entity. If no entity is selected or continuousStep is true,
            //  set selectedEntity to null, otherwise set selectedEntity to that selected Entity

        });

        recenterButton.setOnAction(event -> {
            canvasTransform.setMxx(1);
            canvasTransform.setMyy(1);
            canvasTransform.setTx(0);
            canvasTransform.setTy(0);

            redrawCanvas = true;
        });

        critterGridPane.setVisible(false);
        worldInfoToggle.setSelected(true);
        entityInfoToggle.setDisable(true);

        speedSlider.setValue(100);
        speedSlider.setMax(1000);
        speedSliderDisplay.setText("100 steps/sec");
        speedSlider.valueProperty().addListener(num -> {
            synchronized (speedSlider){
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

        // start the periodic drawCanvas Timeline
        Timeline updateCanvasPeriodically = new Timeline(new KeyFrame(
                Duration.seconds(1.0 / WindowConstants.MAX_FPS),
                event -> {
                    if (model == null || !redrawCanvas) return;
                    drawCanvas();

                    updateSelectedEntity();

                    // TODO update every text information of the world on screen

//                    timeElapsed.setText(String.valueOf(model.getSteps()));
//                    aliveCritters.setText(String.valueOf(model.getNumberOfAliveCritters()));
//
//                    numFoodTiles.setText(String.valueOf(model.getNumberOfFood()));
//                    numRockTiles.setText(String.valueOf(model.getNumberOfRocks()));
                }
        ));

        new AnimationTimer() {
            private long lastUpdate = 0;
            private int frameCount = 0;
            private long lastFrameTime = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate > 0) {
                    frameCount++;

                    // Update FPS every 0.2 seconds
                    if (now - lastFrameTime >= 200_000_000) { // 0.5 seconds = 500,000,000 nanoseconds
                        double fps = frameCount / ((now - lastFrameTime) / 1_000_000_000.0);
                        fpsCounter.setText(String.valueOf(((int)fps*100)/100.0));
                        frameCount = 0;
                        lastFrameTime = now;
                    }
                }
                lastUpdate = now;
            }
        }.start();

        updateCanvasPeriodically.setCycleCount(Timeline.INDEFINITE);
        updateCanvasPeriodically.play();
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

    /** A method that draws the Canvas according to {@link #model} */
    private synchronized void drawCanvas() {
        GridWorld.ReadOnlyWorld model;
        synchronized (canvas) {
            model = this.model;
        }

        double minX = Math.clamp(
                (WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx(),
                WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.xBound
        );
        double minY = Math.clamp(
                (WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy(),
                WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.yBound
        );
        double maxX = Math.clamp(
                (canvas.getWidth() - WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx(),
                WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.xBound
        );
        double maxY = Math.clamp(
                (canvas.getHeight() - WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy(),
                WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.yBound
        );

//      Create the bounding box
        Rectangle2D canvasCameraBoundingBox = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());

        gc.save();
        gc.translate(canvasTransform.getTx(),canvasTransform.getTy());  // Translate first
        gc.scale(canvasTransform.getMxx(),canvasTransform.getMyy());  // Then apply scale

        gc.setStroke(Color.BLACK);

        // TODO draw the canvas here with model normally, assuming no translation is needed

        // TODO draw red line around selected Entity

        gc.restore();
        redrawCanvas = false;
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
