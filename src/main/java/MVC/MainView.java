package MVC;

import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants;
import Utils.Ref;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MainView extends Application implements Initializable {

    @FXML
    private Line divider;
    @FXML
    private SplitPane splitPane;
    @FXML
    private AnchorPane infoPane;

    /** The Root node for the Info Display FXML file. */
    @FXML
    private AnchorPane infoDisplayPane;

    /** The Root node for the Control Panel FXML file. */
    @FXML
    private AnchorPane controlPanelPane;

    /** The Root node for the Canvas Control FXML file. */
    @FXML
    private AnchorPane canvasControlPane;

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
     * View reads from model via {@code CanvasControl.drawCanvas()}
     */
    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Stores a reference to the current selected Entity as a ReadOnlyEntity object.
     * <br>Read by {@link InfoDisplay} and written to by {@link MainView} when {@linkplain #selectedEntityID} updates. */
    private Ref<Entity.ReadOnlyEntity> selectedEntity;

    /** Stores a reference to the current selected Entity by its EntityFactory ID property.
     * <br>Read by {@link MainView} to update {@linkplain #selectedEntity} and updated by {@link CanvasControl}. */
    private Ref<Integer> selectedEntityID;

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

            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            MainView controller = mainLoader.getController();

            // Load InfoDisplay.fxml
            FXMLLoader infoLoader = new FXMLLoader(getClass().getResource("/MVC/InfoDisplay.fxml"));
            Parent infoDisplayRoot = infoLoader.load();
            controller.infoDisplay = infoLoader.getController();
            controller.infoDisplayPane.getChildren().add(infoDisplayRoot);
            AnchorPane.setTopAnchor(infoDisplayRoot, 0.0);
            AnchorPane.setRightAnchor(infoDisplayRoot, 0.0);
            AnchorPane.setBottomAnchor(infoDisplayRoot, 0.0);
            AnchorPane.setLeftAnchor(infoDisplayRoot, 0.0);

            // Load ControlPanel.fxml
            FXMLLoader controlLoader = new FXMLLoader(getClass().getResource("/MVC/ControlPanel.fxml"));
            Parent controlPanelRoot = controlLoader.load();
            controller.controlPanel = controlLoader.getController();
            controller.controlPanelPane.getChildren().add(controlPanelRoot);
            AnchorPane.setTopAnchor(controlPanelRoot, 0.0);
            AnchorPane.setRightAnchor(controlPanelRoot, 0.0);
            AnchorPane.setBottomAnchor(controlPanelRoot, 0.0);
            AnchorPane.setLeftAnchor(controlPanelRoot, 0.0);

            // Load CanvasControl.fxml
            FXMLLoader canvasLoader = new FXMLLoader(getClass().getResource("/MVC/CanvasControl.fxml"));
            Parent canvasControlRoot = canvasLoader.load();
            controller.canvasControl = canvasLoader.getController();
            controller.canvasControlPane.getChildren().add(canvasControlRoot);
            AnchorPane.setTopAnchor(canvasControlRoot, 0.0);
            AnchorPane.setRightAnchor(canvasControlRoot, 0.0);
            AnchorPane.setBottomAnchor(canvasControlRoot, 0.0);
            AnchorPane.setLeftAnchor(canvasControlRoot, 0.0);

            // Init all controller classes
            Ref<GridWorld.ReadOnlyWorld> model = new Ref<>(null);
            Ref<Entity.ReadOnlyEntity> selectedEntity = new Ref<>(null);
            Ref<Integer> selectedEntityID = new Ref<>(null);
            controller.infoDisplay.init(model, selectedEntity);
            controller.controlPanel.init(controller::addTask);
            controller.canvasControl.init(model, selectedEntityID);
            controller.init(model, selectedEntity, selectedEntityID);
        } catch (final IOException ioe) {
            System.err.println("Can't load FXML file.");
            ioe.printStackTrace();
            try {
                stop();
            } catch (final Exception ignored) {
            }
        }
    }

    /** This function is called without any of the other View-Controller fields set yet. All
     * View-Controller initializer code should be in {@link #init}. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindProperties();

        TaskQueue = new LinkedList<>();
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Entity.ReadOnlyEntity> selectedEntity, Ref<Integer> selectedEntityID) {
        this.model = model;
        this.selectedEntity = selectedEntity;
        this.selectedEntityID = selectedEntityID;

        Timeline updateCanvasPeriodically = new Timeline(new KeyFrame(
                Duration.seconds(1.0 / Constants.WindowConstants.MAX_FPS),
                event -> {
                    // TODO add all repaint stuff in here

                    canvasControl.repaint();
                }
        ));

        updateCanvasPeriodically.setCycleCount(Timeline.INDEFINITE);
        updateCanvasPeriodically.play();

        // starts the Controller Thread
        new Thread(new Controller(this)).start();
    }

    /** Binds the various width and height properties of the JavaFX FXML components correspondent
     * to this class. */
    private void bindProperties() {
        divider.startYProperty().bind(splitPane.heightProperty().add(24));

        selectedEntityID.onUpdate(newID -> {
            if (model.isEmpty()) throw new IllegalStateException("Model is empty");

            for (Entity.ReadOnlyEntity roe : model.get().entities)
                if (roe.ID() == newID) {
                    selectedEntity.set(roe);
                    break;
                }
        });

        model.onUpdate(newModel -> {
            for (Entity.ReadOnlyEntity roe : newModel.entities)
                if (roe.ID() == selectedEntityID.get()) {
                    selectedEntity.set(roe);
                    break;
                }
        });

        // TODO implement resizing to fit full screen
//        infoPane.maxWidthProperty().bind(Bindings.min(
//                splitPane.heightProperty().multiply(200.0 / 376),
//                splitPane.widthProperty().multiply(0.3411)
//        ));
//        infoDisplayPane.minHeightProperty().bind(
//                infoPane.maxWidthProperty().multiply(294.0 / 200)
//        );
//
//        controlPanelPane.minHeightProperty().bind(
//                infoPane.maxWidthProperty().multiply(80.0 / 200)
//        );
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
        canvasControl.redrawModel();
    }

    /**
     * Loads a completely new Model into the GUI.
     * <br>generates all Hex rendering and is a lot more resource intensive than {@link #updateViewModel}
     */
    public synchronized void loadNewViewModel(GridWorld.ReadOnlyWorld newModel) {
        assert newModel != null;

        controlPanel.unselectContinuousStep();
        selectedEntity.clear();
        selectedEntityID.clear();

        updateViewModel(newModel);
    }
}
