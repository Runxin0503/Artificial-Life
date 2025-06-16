package MVC;

import java.awt.Point;
import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.ResourceBundle;

import Entities.Bush;
import Entities.Corpse;
import Entities.Creature.Creature;
import Entities.Creature.Egg;
import Entities.Entity;
import Physics.GridWorld;
import Utils.Constants;
import Utils.Ref;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public final class CanvasControl implements Initializable {

    /** When true, this object will redraw the world model at the next available tick. */
    private boolean redrawModel = false;

    /**
     * Adds a 'Select Critter' task with a reference to the critter itself to TaskQueue
     * <br>Be able to select any hex tiles, only send event when selected hex tile is a critter
     */
    @FXML
    private Canvas canvas;

    /**
     * Background Image that moves with the world canvas.
     */
    @FXML
    private ImageView backgroundImage;

    /** The transformation applied to the canvas, used in {@linkplain #drawCanvas}. */
    private final Affine canvasTransform = new Affine();

    /** The Panel or pane used to detect mouse actions on the canvas. */
    @FXML
    private AnchorPane canvasScroller;

    /** The text display that shows the frames-per-second rendering speed at a periodic interval. */
    @FXML
    private Text fpsCounter;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    public Text stepsPerSecCounter;

    private Ref<GridWorld.ReadOnlyWorld> model;

    private Ref<Integer> selectedEntityID;

    private final HashMap<Integer, ArrayList<Point>> bushToBerryLocations = new HashMap<>();

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindProperties();

        // Physically scale the imageView's dimensions (not via transform)
        backgroundImage.setFitWidth(Constants.ImageConstants.backgroundImg.getWidth() * Constants.ImageConstants.bgScaleFactor);
        backgroundImage.setFitHeight(Constants.ImageConstants.backgroundImg.getHeight() * Constants.ImageConstants.bgScaleFactor);

//        Center it at the world center
        backgroundImage.setX(Constants.WorldConstants.xBound / 2.0 - backgroundImage.getFitWidth() / 2.0);
        backgroundImage.setY(Constants.WorldConstants.yBound / 2.0 - backgroundImage.getFitHeight() / 2.0);

//        Apply canvasTransform only (for zooming/panning)
        backgroundImage.getTransforms().setAll(canvasTransform);

        Platform.runLater(this::setupCanvas);

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

    private void setupCanvas() {
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

                clampTransform();

                redrawModel();
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

            clampTransform();

            redrawModel();
        });

        canvasScroller.setOnMouseReleased(ae -> {
            if (model.isEmpty()) return;

            double sceneX = ae.getSceneX();
            double sceneY = ae.getSceneY();

            // Convert scene coordinates to local canvas coordinates
            Point2D local = canvas.sceneToLocal(sceneX, sceneY);
            double localX = local.getX();
            double localY = local.getY();

            // Invert the transform manually using the inverse of the Affine matrix
            int worldX, worldY;
            try {
                Affine inverse = canvasTransform.createInverse();
                worldX = (int) Math.round(inverse.getMxx() * localX + inverse.getMxy() * localY + inverse.getTx());
                worldY = (int) Math.round(inverse.getMyx() * localX + inverse.getMyy() * localY + inverse.getTy());

                // Now you have worldX and worldY as doubles (same as mouseCoords.getX() and getY())
            } catch (NonInvertibleTransformException e) {
                throw new RuntimeException(e);
            }

            // find the selected entity. If no entity is selected or continuousStep is true,
            //  set selectedEntity to null, otherwise set selectedEntity to that selected Entity
            //  ie. change the selectedEntityID to the appropriate ID value.
            Entity.ReadOnlyEntity selectedEntity = model.get().getEntity(worldX, worldY);
            selectedEntityID.set(Objects.isNull(selectedEntity) ? null : selectedEntity.ID());

            System.out.println(worldX + " " + worldY + ", " + selectedEntity);

            redrawModel();
        });

    }

    private void clampTransform() {
        Bounds viewBounds = canvas.getBoundsInParent();

        double scaleX = canvasTransform.getMxx();
        double scaleY = canvasTransform.getMyy();

        // Clamp the top-left corner to stay within bounds
        double tx = canvasTransform.getTx();
        double ty = canvasTransform.getTy();

        double minTx = -Constants.WindowConstants.canvasMaxX * scaleX + viewBounds.getWidth();
        double maxTx = -Constants.WindowConstants.canvasMinX * scaleX;

        double minTy = -Constants.WindowConstants.canvasMaxY * scaleY + viewBounds.getHeight();
        double maxTy = -Constants.WindowConstants.canvasMinY * scaleY;

        canvasTransform.setTx(Math.clamp(tx, minTx, maxTx));
        canvasTransform.setTy(Math.clamp(ty, minTy, maxTy));

    }


    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Integer> selectedEntityID) {
        this.model = model;
        this.selectedEntityID = selectedEntityID;
    }

    /** Binds the various width and height properties of the JavaFX FXML components correspondent
     * to this class. */
    private void bindProperties() {
        // TODO implement
        canvas.widthProperty().bind(canvasScroller.widthProperty());
        canvas.heightProperty().bind(canvasScroller.heightProperty());

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            clampTransform();
            redrawModel();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            clampTransform();
            redrawModel();
        });
    }

    /** Updates and repaints all fields of this GUI according to the most recent data. */
    public void repaint() {
        if (!model.isEmpty() && redrawModel) drawCanvas();
    }

    /** A method that draws the world model according to {@code model}. */
    private void drawCanvas() {
        synchronized (canvas) {
            GridWorld.ReadOnlyWorld model = this.model.get();

            int minX = Math.clamp((int) Math.round((Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx()), Constants.WindowConstants.CANVAS_PADDING, Constants.WorldConstants.xBound);
            int minY = Math.clamp((int) Math.round((Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy()), Constants.WindowConstants.CANVAS_PADDING, Constants.WorldConstants.yBound);
            int maxX = Math.clamp((int) Math.round((canvas.getWidth() - Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTx()) / canvasTransform.getMxx()), Constants.WindowConstants.CANVAS_PADDING, Constants.WorldConstants.xBound);
            int maxY = Math.clamp((int) Math.round((canvas.getHeight() - Constants.WindowConstants.CANVAS_PADDING - canvasTransform.getTy()) / canvasTransform.getMyy()), Constants.WindowConstants.CANVAS_PADDING, Constants.WorldConstants.yBound);

            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setStroke(Color.BLACK);
            gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

            gc.save();
            gc.translate(canvasTransform.getTx(), canvasTransform.getTy());  // Translate first
            gc.scale(canvasTransform.getMxx(), canvasTransform.getMyy());  // Then apply scale

            // draw the canvas here with model normally, assuming no translation is needed
            gc.setStroke(Color.RED);
            gc.strokeRect(minX, minY, maxX - minX, maxY - minY);

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
                    if (cameraBoundingBox.intersects(roe.x(), roe.y(), roe.width(), roe.height())) {
                        // draws black bounding box outline (red if selected) and black dot coordinate around entity
                        if (!selectedEntityID.isEmpty() && selectedEntityID.get().equals(roe.ID())) {
                            gc.setStroke(Color.RED);
                            gc.setFill(Color.BLACK);
                            gc.strokeRect(roe.x(), roe.y(), roe.width(), roe.height());
                            gc.fillRect(roe.getX() - 2, roe.getY() - 2, 4, 4);
                        } else if (Constants.WorldConstants.Settings.devMode) {
                            gc.setStroke(Color.BLACK);
                            gc.setFill(Color.BLACK);
                            gc.strokeRect(roe.x(), roe.y(), roe.width(), roe.height());
                            gc.fillRect(roe.getX() - 2, roe.getY() - 2, 4, 4);
                        }
                        drawReadOnlyEntity(roe, gc);
                    }
            }

            gc.restore();
            redrawModel = false;
        }
    }

    /** Draws the ReadOnlyEntity {@code entity} on the graphics object {@code gc}. */
    private void drawReadOnlyEntity(Entity.ReadOnlyEntity entity, GraphicsContext gc) {
        switch (entity) {
            case Bush.ReadOnlyBush bush -> {
                gc.drawImage(Constants.ImageConstants.bush, bush.x(), bush.y(), bush.width(), bush.height());

                // draw bush's berries from stashed data in bushToBerriesLocation
                bushToBerryLocations.putIfAbsent(bush.ID(), new ArrayList<>());
                ArrayList<Point> berries = bushToBerryLocations.get(bush.ID());
                if (berries.size() != bush.numBerries()) modifyBerriesLocations(bush, berries);
                for (Point berry : berries)
                    gc.drawImage(Constants.ImageConstants.berries, berry.x, berry.y, Constants.BushConstants.berriesWidth, Constants.BushConstants.berriesHeight);
            }
            case Egg.ReadOnlyEgg egg ->
                    gc.drawImage(Constants.ImageConstants.egg, egg.x(), egg.y(), egg.width(), egg.height());
            case Corpse.ReadOnlyCorpse corpse -> {
                gc.save();
                gc.translate(corpse.getX() - corpse.x(), corpse.getY() - corpse.y());
                gc.rotate(Math.toDegrees(corpse.rotation()));
                gc.drawImage(Constants.ImageConstants.corpse, corpse.x(), corpse.y(), corpse.width(), corpse.height());
                gc.restore();
            }
            case Creature.ReadOnlyCreature creature ->
                    gc.drawImage(Constants.ImageConstants.getBirdRotation(creature.rotation()), creature.x(), creature.y(), creature.width(), creature.height());
            case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
        }
    }

    private void modifyBerriesLocations(Bush.ReadOnlyBush bush, ArrayList<Point> berries) {
        for (int i = berries.size(); i < bush.numBerries(); i++)
            berries.add(new Point(bush.x() + Constants.BushConstants.berriesWidth + (int) (Math.random() * (bush.width() - 2 * Constants.BushConstants.berriesWidth)), bush.y() + Constants.BushConstants.berriesHeight + (int) (Math.random() * (bush.height() - 2 * Constants.BushConstants.berriesHeight))));

        for (int i = berries.size(); i > bush.numBerries(); i--)
            berries.remove(i - 1);
    }

    public void redrawModel() {
        synchronized (canvas) {
            redrawModel = true;
        }
    }
}
