package MVC;

import Entities.Bush;
import Entities.Corpse;
import Entities.Creature.Creature;
import Entities.Creature.Egg;
import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants;
import Utils.Ref;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;

public class CanvasControl implements Initializable {

    /** When true, this object will redraw the canvas at the next available tick. */
    private boolean redrawCanvas = false;

    /**
     * Adds a 'Select Critter' task with a reference to the critter itself to TaskQueue
     * <br>Be able to select any hex tiles, only send event when selected hex tile is a critter
     */
    @FXML
    private Canvas canvas;

    /** The transformation applied to the canvas, used in {@linkplain #drawCanvas}. */
    private final Affine canvasTransform = new Affine();

    /** The Panel or pane used to detect mouse actions on the canvas. */
    @FXML
    private AnchorPane canvasScroller;

    /** The text display that shows the frames-per-second rendering speed at a periodic interval. */
    @FXML
    private Text fpsCounter;

    /** A function in View that Controller calls to update Text directly*/
    @FXML
    public Text stepsPerSecCounter;

    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindProperties();
//        canvas.widthProperty().bind(canvasScroller.widthProperty().add(Constants.WindowConstants.CANVAS_PADDING * 2));
//        canvas.heightProperty().bind(canvasScroller.heightProperty().add(Constants.WindowConstants.CANVAS_PADDING * 2));

        canvas.widthProperty().addListener(event -> redrawCanvas());
        canvas.heightProperty().addListener(event -> redrawCanvas());

        canvasScroller.setOnScroll(ae -> {
            if (ae.getDeltaY() != 0) {  // Only react to vertical scroll
                double zoomFactor = ae.getDeltaY() > 0 ? 1.1 : 0.9;  // Zoom in or out
                if ((canvasTransform.getMxx() >= Constants.WindowConstants.MAX_ZOOM / 1.1 || canvasTransform.getMyy() >= Constants.WindowConstants.MAX_ZOOM / 1.1) && zoomFactor > 1)
                    return;
                if ((canvasTransform.getMxx() <= Constants.WindowConstants.MIN_ZOOM / 0.9 || canvasTransform.getMyy() <= Constants.WindowConstants.MIN_ZOOM / 0.9) && zoomFactor < 1)
                    return;

                Point2D mouseCoords;
                try {
                    mouseCoords = canvasTransform.inverseTransform(canvas.sceneToLocal(ae.getSceneX(), ae.getSceneY()));
                } catch (NonInvertibleTransformException e) {
                    throw new RuntimeException(e);
                }

                double oldScaleX = canvasTransform.getMxx(), oldScaleY = canvasTransform.getMyy();

                canvasTransform.setMxx(Math.clamp(oldScaleX * zoomFactor, Constants.WindowConstants.MIN_ZOOM, Constants.WindowConstants.MAX_ZOOM));
                canvasTransform.setMyy(Math.clamp(oldScaleY * zoomFactor, Constants.WindowConstants.MIN_ZOOM, Constants.WindowConstants.MAX_ZOOM));

                // Translate to maintain zoom focus on the mouse position
                canvasTransform.setTx(canvasTransform.getTx() - mouseCoords.getX() * (canvasTransform.getMxx() - oldScaleX));
                canvasTransform.setTy(canvasTransform.getTy() - mouseCoords.getY() * (canvasTransform.getMyy() - oldScaleY));

                redrawCanvas();
            }
        });

        final double[] dragAnchor = new double[2]; // To store initial mouse click position
        canvasScroller.setOnMousePressed(ae -> {
            // Store initial mouse position for panning
            dragAnchor[0] = ae.getSceneX() - canvasTransform.getTx();
            dragAnchor[1] = ae.getSceneY() - canvasTransform.getTy();
        });

        canvasScroller.setOnMouseDragged(ae -> {
            // Calculate new position for panning
            double offsetX = ae.getSceneX() - dragAnchor[0];
            double offsetY = ae.getSceneY() - dragAnchor[1];
            canvasTransform.setTx(offsetX);
            canvasTransform.setTy(offsetY);

            redrawCanvas();
        });

        canvasScroller.setOnMouseReleased(ae -> {
            if (model == null) return;

            Point2D mouseCoords;
            try {
                mouseCoords = canvasTransform.inverseTransform(canvas.sceneToLocal(ae.getSceneX(), ae.getSceneY()));
            } catch (NonInvertibleTransformException e) {
                throw new RuntimeException(e);
            }

            redrawCanvas();

            // TODO find the selected entity. If no entity is selected or continuousStep is true,
            //  set selectedEntity to null, otherwise set selectedEntity to that selected Entity

        });


        //TODO move this to MainView.java?
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
                        fpsCounter.setText(String.valueOf(((int) fps * 100) / 100.0));
                        frameCount = 0;
                        lastFrameTime = now;
                    }
                }
                lastUpdate = now;
            }
        }.start();
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model) {
        this.model = model;
    }

    /** Binds the various width and height properties of the JavaFX FXML components correspondent
     * to this class. */
    private void bindProperties() {
        // TODO implement
    }

    /** A method that draws the Canvas according to {@code model}. */
    public synchronized void drawCanvas() {
        GridWorld.ReadOnlyWorld model;
        model = this.model.get();

        int minX = Math.clamp(
                (int) Math.round((Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx()),
                Constants.WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.xBound
        );
        int minY = Math.clamp(
                (int) Math.round((Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy()),
                Constants.WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.yBound
        );
        int maxX = Math.clamp(
                (int) Math.round((canvas.getWidth() - Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx()),
                Constants.WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.xBound
        );
        int maxY = Math.clamp(
                (int) Math.round((canvas.getHeight() - Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy()),
                Constants.WindowConstants.CANVAS_PADDING,
                Constants.WorldConstants.yBound
        );

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.save();
        gc.translate(canvasTransform.getTx(), canvasTransform.getTy());  // Translate first
        gc.scale(canvasTransform.getMxx(), canvasTransform.getMyy());  // Then apply scale

        gc.setStroke(Color.BLACK);

        // TODO draw the canvas here with model normally, assuming no translation is needed
        ArrayList<Entity.ReadOnlyEntity>[] entities = model.getEntities(minX, minY, maxX, maxY);
        if (entities.length < Constants.WindowConstants.standardOrGridThreshold) {
            HashSet<Entity.ReadOnlyEntity> entitiesSet = new HashSet<>();
            for (ArrayList<Entity.ReadOnlyEntity> elist : entities)
                for (Entity.ReadOnlyEntity roe : elist) {
                    if (entitiesSet.contains(roe)) continue;

                    drawReadOnlyEntity(roe, gc);

                    entitiesSet.add(roe);
                }
        } else {
            // check for entity-in-scope of camera bounding box in entity-based rendering
            Rectangle cameraBoundingBox = new Rectangle(minX, minY, maxX - minX, maxY - minY);
            for (Entity.ReadOnlyEntity roe : model.entities)
                if (cameraBoundingBox.contains(roe.x(), roe.y(), roe.width(), roe.height()))
                    drawReadOnlyEntity(roe, gc);
        }

        // TODO draw red line around selected Entity

        gc.restore();
        redrawCanvas = false;
    }

    /** Draws the ReadOnlyEntity {@code entity} on the graphics object {@code gc}. */
    private void drawReadOnlyEntity(Entity.ReadOnlyEntity entity, GraphicsContext gc) {
        //TODO implement all
        switch (entity) {
            case Bush.ReadOnlyBush bush -> {
//                gc.drawImage(Constants.ImageConstants.bush, bush.x(), bush.y());
//                if (Constants.WorldConstants.Settings.devMode) {
//                    gc.strokeRect(bush.x(), bush.y(), bush.width(), bush.height());
//                    gc.fillRect(bush.getX() - 2, bush.getY() - 2, 4, 4);
//                }
//
//                ArrayList<Point> berries = new ArrayList<>(bush.getBerries());
//                for (Point berry : berries)
//                    gc.drawImage(Constants.ImageConstants.berries, x + berry.x, y + berry.y, this);
            }
            case Egg.ReadOnlyEgg egg -> {

            }
            case Corpse.ReadOnlyCorpse corpse -> {

            }
            case Creature.ReadOnlyCreature creature -> {

            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
        }
    }

    public synchronized void redrawCanvas() {
        redrawCanvas = true;
    }
}
