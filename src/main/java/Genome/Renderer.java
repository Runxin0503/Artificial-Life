package Genome;

import Evolution.Agent;
import Evolution.Evolution;
import Evolution.Evolution.EvolutionBuilder;
import Genome.enums.Activation;
import Genome.enums.Cost;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Renderer extends Application implements Initializable {

    @FXML
    private Button rWeights;

    @FXML
    private Button sWeights;

    @FXML
    private Button sBias;

    @FXML
    private Button rAF;

    @FXML
    private Button rEdge;

    @FXML
    private Button rNode;

    @FXML
    private Button mutate;

    @FXML
    private Button calculate;

    @FXML
    private Canvas canvas;

    @FXML
    private AnchorPane canvasScroller;

    private Affine canvasTransform;

    private Evolution agentFactory;
    private Agent agent;
    private NN agentGenome;

    private final double radius = 10;
    private final double padding = 5 + radius;
    private final double MAX_FPS = 120;
    private final double MIN_ZOOM = 0;
    private final double MAX_ZOOM = 10;
    private boolean redrawCanvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        try {
            final URL r = getClass().getResource("/Visualizer.fxml");
            if (r == null) {
                System.err.println("No FXML resource found.");
                try {
                    stop();
                } catch (final Exception e) {
                }
            }
            final Parent node = FXMLLoader.load(r);
            final Scene scene = new Scene(node);
            stage.setScene(scene);
            stage.setTitle("Neural Network Visualizer");
            stage.setResizable(false);
            stage.show();
        } catch (final IOException ioe) {
            System.err.println("Can't load FXML file.");
            ioe.printStackTrace();
            System.out.println(ioe);
            try {
                stop();
            } catch (final Exception e) {
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        agentFactory = new EvolutionBuilder().setInputNum(2).setOutputNum(2)
                .setDefaultHiddenAF(Activation.sigmoid).setOutputAF(Activation.arrays.softmax)
                .setCostFunction(Cost.crossEntropy).setNumSimulated(1).build();
        agent = agentFactory.agents[0];
        agentGenome = NN.getDefaultNeuralNet(agentFactory.Constants);
        System.out.println(agentGenome);

        redrawCanvas = true;

        rWeights.setOnAction(e -> {
            Mutation.randomWeights(agentGenome);
            redrawCanvas = true;
        });

        sWeights.setOnAction(e -> {
            Mutation.shiftWeights(agentGenome);
            redrawCanvas = true;
        });

        sBias.setOnAction(e -> {
            Mutation.shiftBias(agentGenome);
            redrawCanvas = true;
        });

        rAF.setOnAction(e -> {
            Mutation.changeAF(agentGenome);
            redrawCanvas = true;
        });

        rEdge.setOnAction(e -> {
            Mutation.mutateSynapse(agentGenome);
            redrawCanvas = true;
        });

        rNode.setOnAction(e -> {
            Mutation.mutateNode(agentGenome);
            redrawCanvas = true;
        });

        mutate.setOnAction(e -> {
            agentGenome.mutate();
            redrawCanvas = true;
        });

        calculate.setOnAction(e -> {
            double[] input = new double[agentFactory.Constants.getInputNum()];
            for (int i = 0; i < input.length; i++) input[i] = Math.random() * 20 - 10;
            System.out.println("Input: " + Arrays.toString(input));
            System.out.println("Output: " + Arrays.toString(agentGenome.calculateWeightedOutput(input)));
        });

        canvasTransform = new Affine();

        canvasScroller.setOnScroll(ae -> {
            if (ae.getDeltaY() != 0) {  // Only react to vertical scroll
                double zoomFactor = ae.getDeltaY() > 0 ? 1.1 : 0.9;  // Zoom in or out
                if ((canvasTransform.getMxx() >= MAX_ZOOM / 1.1 || canvasTransform.getMyy() >= MAX_ZOOM / 1.1) && zoomFactor > 1)
                    return;
                if ((canvasTransform.getMxx() <= MIN_ZOOM / 0.9 || canvasTransform.getMyy() <= MIN_ZOOM / 0.9) && zoomFactor < 1)
                    return;

                Point2D mouseCoords;
                try {
                    mouseCoords = canvasTransform.inverseTransform(canvas.sceneToLocal(ae.getSceneX(), ae.getSceneY()));
                } catch (NonInvertibleTransformException e) {
                    throw new RuntimeException(e);
                }

                double oldScaleX = canvasTransform.getMxx(), oldScaleY = canvasTransform.getMyy();

                canvasTransform.setMxx(Math.clamp(oldScaleX * zoomFactor, MIN_ZOOM, MAX_ZOOM));
                canvasTransform.setMyy(Math.clamp(oldScaleY * zoomFactor, MIN_ZOOM, MAX_ZOOM));

                // Translate to maintain zoom focus on the mouse position
                canvasTransform.setTx(canvasTransform.getTx() - mouseCoords.getX() * (canvasTransform.getMxx() - oldScaleX));
                canvasTransform.setTy(canvasTransform.getTy() - mouseCoords.getY() * (canvasTransform.getMyy() - oldScaleY));

                redrawCanvas = true;
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

            redrawCanvas = true;
        });

        Timeline updateCanvasPeriodically = new Timeline(new KeyFrame(
                Duration.seconds(1.0 / MAX_FPS),
                event -> {
                    if (redrawCanvas) {
                        drawCanvas();
                        redrawCanvas = false;
                    }
                }
        ));

        updateCanvasPeriodically.setCycleCount(Timeline.INDEFINITE);
        updateCanvasPeriodically.play();
    }

    private void drawCanvas() {
        assert agentGenome.classInv();

        double minX = Math.clamp(
                -canvasTransform.getTx() / canvasTransform.getMxx(),
                0,
                canvas.getWidth()
        );
        double minY = Math.clamp(
                -canvasTransform.getTy() / canvasTransform.getMyy(),
                0,
                canvas.getHeight()
        );
        double maxX = Math.clamp(
                (canvas.getWidth() - canvasTransform.getTx()) / canvasTransform.getMxx(),
                0,
                canvas.getWidth()
        );
        double maxY = Math.clamp(
                (canvas.getHeight() - canvasTransform.getTy()) / canvasTransform.getMyy(),
                0,
                canvas.getHeight()
        );

        Rectangle2D canvasCameraBoundingBox = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.save();
        gc.translate(canvasTransform.getTx(), canvasTransform.getTy());  // Translate first
        gc.scale(canvasTransform.getMxx(), canvasTransform.getMyy());  // Then apply scale

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double cappedZoomRatio = Math.min(1, 1 / canvasTransform.getMxx());
        double adjustedRadius = radius * cappedZoomRatio;
        gc.setLineWidth(2 * cappedZoomRatio);
        gc.setFont(Font.font(null, FontWeight.LIGHT, 10 * cappedZoomRatio));

        double width = (canvas.getWidth() - padding * 2), height = (canvas.getHeight() - padding * 2);
        for (node n : agentGenome.nodes) {
            double x = n.x * width + padding, y = n.y * height + padding;

            for (edge e : n.getOutgoingEdges()) {
                if (e.isDisabled()) continue;
                node nextNode = agentGenome.nodes.get(e.nextIndex);
                double nextX = nextNode.x * width + padding, nextY = nextNode.y * height + padding;
                if (canvasCameraBoundingBox.intersects(x, Math.min(y, nextY), nextX - x, Math.abs(y - nextY))) {
                    gc.setStroke(Color.GRAY);
                    gc.strokeLine(x, y, nextX, nextY);
                    double clampedX, clampedY, clampedNextX, clampedNextY;
                    //y = mx+b
                    double m = (nextY - y) / (nextX - x);
                    //if x is clamped to left side, check if the point intersecting the line is within view of camera
                    //using clampedY. If it is, keep clampedY the way it is, otherwise, delegate to the "else if" statement
                    if ((clampedX = Math.max(x, canvasCameraBoundingBox.getMinX())) == canvasCameraBoundingBox.getMinX() &&
                            (clampedY = (clampedX - x) * m + y) < canvasCameraBoundingBox.getMaxY() && clampedY > canvasCameraBoundingBox.getMinY())
                        ;
                        //if clampedY is clamped to either top or bottom side, find clampedX using linear equation
                    else if ((clampedY = Math.clamp(y, canvasCameraBoundingBox.getMinY(), canvasCameraBoundingBox.getMaxY())) == canvasCameraBoundingBox.getMinY() || clampedY == canvasCameraBoundingBox.getMaxY())
                        clampedX = (clampedY - y) / m + x;
                    else {
                        clampedX = x;
                        clampedY = y;
                    }

                    if ((clampedNextX = Math.min(nextX, canvasCameraBoundingBox.getMaxX())) == canvasCameraBoundingBox.getMaxX() &&
                            (clampedNextY = (clampedNextX - x) * m + y) < canvasCameraBoundingBox.getMaxY() && clampedNextY > canvasCameraBoundingBox.getMinY())
                        ;
                    else if ((clampedNextY = Math.clamp(nextY, canvasCameraBoundingBox.getMinY(), canvasCameraBoundingBox.getMaxY())) == canvasCameraBoundingBox.getMinY() || clampedNextY == canvasCameraBoundingBox.getMaxY())
                        clampedNextX = (clampedNextY - y) / m + x;
                    else {
                        clampedNextX = nextX;
                        clampedNextY = nextY;
                    }

                    double midX = (clampedNextX + clampedX) / 2, midY = (clampedNextY + clampedY) / 2;
                    gc.setFill(Color.RED);
                    gc.fillText(String.format("%.2f", e.getWeight()), midX - adjustedRadius, midY - adjustedRadius * 0.65);
                }
            }
            if (canvasCameraBoundingBox.intersects(x - adjustedRadius, y - adjustedRadius, adjustedRadius * 2, adjustedRadius * 2)) {
                gc.setFill(Color.GRAY);
                gc.fillOval(x - adjustedRadius, y - adjustedRadius, adjustedRadius * 2, adjustedRadius * 2);
                gc.setFill(Color.BLACK);
                gc.fillText(n.activationFunction + "", x - adjustedRadius, y - adjustedRadius * 1.2);
                gc.fillText(String.format("%.2f", n.getBias()), x - adjustedRadius * 0.8, y + adjustedRadius * 1.9);
            }
        }

        gc.restore();
    }
}
