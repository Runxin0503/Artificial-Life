import Constants.Constants.*;
import Constants.Constants.WindowConstants;
import Menu.MainMenu;
import World.World;
import World.worldPointer;
import globalGenomes.globalInnovations;
import globalGenomes.globalNodes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    //    public static void main(String[] args){
//        System.out.println(Double.NaN < 0);
//    }
    public static void main(String[] args) {
        World.globalNodes = new globalNodes();
        World.globalInnovations = new globalInnovations(World.globalNodes);
        setupImages();

        worldPointer worldPointer = new worldPointer();
        ExecutorService executorService = Constants.Constants.createThreadPool(WorldConstants.Settings.maxThread);
        new MainMenu(worldPointer,executorService);
        while (true) {
            if (worldPointer.world != null && worldPointer.world.exists) {

                Timer timer = new Timer(1000 / WorldConstants.Settings.framesPerSec, e -> {
                    // Schedule the function call on the EDT
                    try {
                        SwingUtilities.invokeLater(worldPointer.world::repaint);
                    } catch (NullPointerException npe) {
                        System.out.println("You did an oopsie! (Null Pointer Exception)");
                        npe.printStackTrace();
                    }
                });

                // Start the timer
                timer.start();

                while (worldPointer.world != null && worldPointer.world.exists) {
                    if (WorldConstants.Settings.ticksPerSec != 0) {
                        long timeStarted = System.currentTimeMillis();
                        try {
                            worldPointer.world.tick(executorService);
                        } catch (InterruptedException e) {
                            executorService.shutdownNow();
                            Thread.currentThread().interrupt();
                        }
                        try {
                            if (WorldConstants.Settings.ticksPerSec > 0)
                                Thread.sleep(1000 / WorldConstants.Settings.ticksPerSec);
                        } catch (InterruptedException ignored) {
                        }
                        if (worldPointer.world.tick == 0)
                            worldPointer.world.addTickRate(System.currentTimeMillis() - timeStarted);
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                timer.stop();
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public static void setupImages() {
        try {
            ImageConstants.berries = ImageIO.read(new File("resources/berries.png")).getScaledInstance(ImageConstants.berriesWidth, ImageConstants.berriesHeight, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.bush = ImageIO.read(new File("resources/bush.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.bird = ImageIO.read(new File("resources/bird.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.corpse = ImageIO.read(new File("resources/deadbird.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.egg = ImageIO.read(new File("resources/egg.png")).getScaledInstance(CreatureConstants.Reproduce.eggSize, CreatureConstants.Reproduce.eggSize, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.menuBackground = ImageIO.read(new File("resources/Menu Background.png")).getScaledInstance(WindowConstants.menuWidth, WindowConstants.menuHeight, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.titleCard = ImageIO.read(new File("resources/Title Card.png")).getScaledInstance(WindowConstants.titleWidth, WindowConstants.titleHeight, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.button = ImageIO.read(new File("resources/Button.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.buttonHover = ImageIO.read(new File("resources/Button Hover.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.buttonPressed = ImageIO.read(new File("resources/Button Pressed.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ImageConstants.scrollPanel = ImageIO.read(new File("resources/Scroll Panel.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 360; i++) {
            ImageConstants.birdRotations[i] = new BufferedImage(ImageConstants.bird.getWidth(), ImageConstants.bird.getHeight(), ImageConstants.bird.getType());
            Graphics2D g2d = (Graphics2D) ImageConstants.birdRotations[i].getGraphics();
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(i), ImageConstants.bird.getWidth() / 2.0, ImageConstants.bird.getHeight() / 2.0);
            if ((i > 90 && i < 270)) {
                at.scale(1, -1);
                at.translate(0, -ImageConstants.bird.getHeight());
            }
            g2d.drawImage(ImageConstants.bird, at, null);
            g2d.dispose();
        }
    }
}
