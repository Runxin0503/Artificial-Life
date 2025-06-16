package Utils;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.Serializable;

import Genome.Activation;
import Genome.Cost;
import Genome.Optimizer;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Contains constants used across the simulation, including configuration for creatures,
 * world generation, physics, and visuals.
 */
public class Constants implements Serializable {

    /**
     * Constants for the simulated world, including grid layout, bounds, and settings.
     */
    public static class WorldConstants {
        /** A semi-transparent red color used for highlighting. */
        public static final Color semiTransparentRed = new Color(1, 0, 0, 0.5);
        /** A semi-transparent green color used for highlighting. */
        public static final Color semiTransparentGreen = new Color(0, 1, 0, 0.5);
        /** X dimension of the world boundary. */
        public static final int xBound = 10000;
        /** Y dimension of the world boundary. */
        public static final int yBound = 10000;

        /** Width of each grid cell. */
        public static final int GridWidth = 200;
        /** Height of each grid cell. */
        public static final int GridHeight = 200;
        /** Number of horizontal grid cells. */
        public static final int GRID_NUM_X = Math.ceilDiv(xBound, GridWidth);
        /** Number of vertical grid cells. */
        public static final int GRID_NUM_Y = Math.ceilDiv(yBound, GridHeight);

        /** Rectangle representing the entire world boundary. */
        public static final Rectangle worldBorder = new Rectangle(0, 0, xBound, yBound);

        public static Polygon topVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, (int) worldBorder.width, (int) (worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50)},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, 0, -CreatureConstants.Vision.maxVisionDistance - 50},
                4);
        public static Polygon bottomVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, (int) worldBorder.width, (int) (worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50)},
                new int[]{(int) (worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50), (int) worldBorder.height, (int) worldBorder.height, (int) (worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50)},
                4);
        public static Polygon leftVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, 0, -CreatureConstants.Vision.maxVisionDistance - 50},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, (int) worldBorder.height, (int) (worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50)},
                4);
        public static Polygon rightVisionBox = new Polygon(
                new int[]{(int) (worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50), (int) worldBorder.width, (int) worldBorder.width, (int) (worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50)},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, (int) worldBorder.height, (int) (worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50)},
                4);

        /**
         * Configuration settings for the world simulation, including speed, frame rate, and debug mode.
         */
        public static class Settings {
            /** Maximum movement speed for any creature. */
            public static final int maxSpeed = 100;
            /** Maximum number of threads allowed for processing. */
            public static final int maxThread = 100;
            /** Development mode flag, enables debugging features. */
            public static boolean devMode = true;
            /** Target frames per second. */
            public static final int framesPerSec = 60;
            /** Game logic ticks per second. */
            public static int ticksPerSec = -1;

            /** Names of creature statistics shown in reports. */
            public static final String[] reportInfo = new String[]{"Size", "MinSize", "MaxSize", "Force", "Speed", "Energy", "MaxEnergy", "Reproduction Cost", "Offspring Investment", "Incubation Time", "Health", "MaxHealth", "Maturity", "Strength", "Armour", "Diet Value", "Herbivore Affinity", "Carnivore Affinity"};
            /** Total number of report metrics. */
            public static final int reportSize = reportInfo.length;
            /** Summary count titles shown in Graph UI (e.g., number of creatures). */
            public static final String[] countInfo = new String[]{"# Carnivore", "# Herbivore", "# Creatures", "# Eggs", "# Corpses", "# Berries"};
        }

        /**
         * Parameters that govern how the world is initially generated and populated.
         */
        public static class WorldGen {
            /** The minimum distance between every bush */
            public static int bushRadius = 0;
            /** Number of bushes in the world. */
            public static final int numBushes = 50;
            /** Starting number of creatures. */
            public static int startingPopulation = 0;//150
            /** Minimum number of creatures below which natural spawning may occur. */
            public static int naturalSpawningThreshold = 100;//60
            /** Probability of natural spawning occurring in each tick when below the threshold. */
            public static final double naturalSpawningProbability = 0.1;//0.002
            /** Whether to spawn corpses when Creatures die. */
            public static boolean spawnCorpse = true;
        }
    }

    /**
     * Constants related to the decomposition and behavior of corpses.
     */
    public static class CorpseConstants {
        /** The rate at which corpses decay each tick according to the Half-Life principle. */
        public static final double corpseDecayRate = 0.7;
        /** The percentage at which a corpse is considered rotten and is removed from the world. */
        public static final double corpseRottenPercentage = 0.2;
        /** Minimum size a corpse can shrink to before being removed from the world. */
        public static final double minCorpseSize = Math.nextDown(CreatureConstants.Reproduce.babySize);
        /** Modifier for how corpse size affects its movement; derived from creature movement constants. */
        public static double sizeMovementConstant = CreatureConstants.Movement.sizeMovementConstant / 2;
    }

    /**
     * Constants used for bushes and berry production.
     */
    public static class BushConstants {
        /** Default berries width. */
        public static final int berriesWidth = 20;
        /** Default berries height. */
        public static final int berriesHeight = 20;

        /** Default bush width. */
        public static final int bushWidth = 320;
        /** Default bush height. */
        public static final int bushHeight = 180;

        /** Initial maximum bush width. */
        public static final int initialMaxSize = 400;
        /** Initial minimum bush width. */
        public static final int initialMinSize = 240;

        /** Ratio used to compute bush height from width. */
        public static final double widthToHeight = (double) bushHeight / bushWidth;
        /** Maximum number of berries a bush can hold. */
        public static int maxBerries = 40;//10
        /** Energy value per berry. */
        public static double energy = 5;
        /** Probability of berry growth per tick. */
        public static final double berriesGrowProbability = 0.005;
    }

    /**
     * UI and window-related constants for controlling layout and rendering.
     */
    public static class WindowConstants {
        public static final double MIN_STAGE_WIDTH = 613;
        public static final double MIN_STAGE_HEIGHT = 435.5;

        public static final int canvasMinX = -5000, canvasMaxX = 15000, canvasMinY = -5000, canvasMaxY = 15000;
        public static final int CANVAS_PADDING = 5;
        public static final double MIN_ZOOM = 0.066;
        public static final double MAX_ZOOM = 1.2;

        public static final double MAX_FPS = 60;
        public static final int maxThread = 10;
        public static final double followZoom = 2;
        public static final int graphWidth = 800;
        public static final int graphHeight = 800;
        public static final int graphMaxDataSize = 1000;

        /** The number of Grids in scope of camera before switching from Spatial-Partition based
         * rendering to standard entity based rendering.
         * <br>
         * <br>Tradeoff here being that Spatial-Partitioned rendering checks for duplicates in each grid
         * while standard entity rendering checks if the entity is in scope of the camera. */
        public static final int standardOrGridThreshold = 0; // 50
    }

    /**
     * Constants for handling image resources and transformations.
     */
    public static class ImageConstants {

        /** A Tile-able Background Image of a galaxy. */
        public static final Image backgroundImg = new Image(ImageConstants.class.getResource("/galaxy.png").toString());
        /** Scale Factor of the galaxy background image. */
        public static final double bgScaleFactor = 4;

        /** Image for berries. */
        public static final Image berries = new Image(ImageConstants.class.getResource("/berries.png").toString());
        /** Image for bushes. */
        public static final Image bush = new Image(ImageConstants.class.getResource("/bush.png").toString());
        /** Image for corpses. */
        public static final Image corpse = new Image(ImageConstants.class.getResource("/dead_bird.png").toString());
        /** Image for eggs. */
        public static final Image egg = new Image(ImageConstants.class.getResource("/egg.png").toString());
        /** Cached array of bird images indexed by rotation degree. */
        public static final Image[] birdRotations = new Image[360];

        /**
         * Returns the bird image corresponding to a given radian rotation.
         *
         * @param radianRotation the rotation in radians
         * @return the corresponding rotated image
         */
        public static Image getBirdRotation(double radianRotation) {
            int degreeRotation = (int) Math.round(Math.toDegrees(radianRotation));
            while (degreeRotation < 0) degreeRotation += 360;
            while (degreeRotation >= 360) degreeRotation -= 360;
            return birdRotations[degreeRotation];
        }

        static {
            Image bird = new Image(ImageConstants.class.getResource("/bird.png").toString());
            for (int i = 0; i < 360; i++) {
                double size = Math.max(bird.getWidth(), bird.getHeight());

                Canvas canvas = new Canvas(size, size);
                GraphicsContext gc = canvas.getGraphicsContext2D();

                gc.save();
                gc.translate(size / 2, size / 2);
                gc.rotate(i);
                gc.translate(-bird.getWidth() / 2, -bird.getHeight() / 2);
                gc.drawImage(bird, 0, 0);
                gc.restore();

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                WritableImage rotated = new WritableImage((int) size, (int) size);
                birdRotations[i] = canvas.snapshot(params, rotated);
            }
        }
    }

    /**
     * Constants related to creature reproduction, including mutation and offspring characteristics.
     */
    public static class CreatureConstants {

        /**
         * Constants and formulas related to creature reproduction, such as gene mutation and egg characteristics.
         */
        public static class Reproduce {
            /** Probability of a shift in gene mutation,
             * scaling a random gene attribute up or down some proportion. */
            public static final double geneMutationShiftProbability = 0.1;
            /** Probability of a random gene mutation,
             * completely replacing a random gene attribute with a new random gene attribute of the same kind. */
            public static final double geneMutationRandomProbability = 0.01;
            /** Strength of the gene mutation shift, how far the scaling
             * goes when shifting a gene attribute. */
            public static final double geneMutationShiftStrength = 0.2;

            /** Maximum maturity age at which death will occur for Creatures. */
            public static final int maturityDeath = 200000;
            /** Size of a baby creature in the game world. */
            public static final double babySize = 20;
            public static final double minSize = babySize;
            /** Maximum size a creature can reach. */
            public static final int maxSize = 160;

            /** Size of an egg. */
            public static final int eggSize = 25;
            /** Percentage chance that an egg becomes rotten. */
            public static final double rottenEggPerct = 0.02;
            /** Percentage chance of multiple embryos in an egg,
             * each time this chance succeeds, roll again. */
            public static final double multipleEmbryoPerct = 0.01;
            /** Minimum and maximum time an egg can incubate before hatching. */
            public static final int minIncubationTime = 500, maxIncubationTime = 5000;

            /** The Perct Size difference between a creature's max and min size.
             * That is, {@code max * (1 - sizeDiff) >= min} */
            public static final double sizeDiff = 0.1;

            /** Minimum and maximum investment in an offspring. */
            public static final double minOffspringInvestment = 0.125, maxOffspringInvestment = 0.5;

            /**
             * A sigmoid function that calculates the {@code maturity rating}, which certain
             * attributes in a creature depend on whenever it scales with maturity.
             *
             * <br>Ex: Armour perct, Muscle strength, Speed, Size, etc.
             *
             * @param minSize The minimum size.
             * @param maxSize The maximum size.
             * @param size The current size.
             * @return The maturity rating
             */
            public static double scalesWithMaturity(double minSize, double maxSize, double size) {
                if (minSize == size && minSize == maxSize) return 1;
                return Equations.sigmoid(0.35, 1, 4.2, 0.5, (size - minSize) / (maxSize - minSize));
            }
        }

        /**
         * Constants and methods related to the stomach and digestion process of creatures.
         */
        public static class Digestion {
            /** Ratio of stomach size to the maximum stomach fluid capacity. */
            public static final double stomachSizeToMaxStomachFluid = 0.4;
            /** Maximum stomach size a creature can have. */
            public static final double maxStomachSize = 250;

            /** Digestion rate for plant-based food. */
            public static final double plantDigestionRate = 0.125;
            /** Energy generated per unit mass of plant material consumed. */
            public static final double plantMassToEnergy = 1.2;

            /** Digestion rate for meat-based food. */
            public static final double meatDigestionRate = 0.8;
            /** Energy generated per unit mass of meat consumed. */
            public static final double meatMassToEnergy = 3;

            /**
             * Calculates stomach size based on the size of any given creature.
             *
             * @param size The size of the creature.
             * @return The corresponding stomach size.
             */
            public static double sizeToStomachSize(double size) {
                return Equations.sigmoid(0, maxStomachSize, 0.06, 70, size);
//                return (size-10)/(Reproduce.maxSize-10) * maxStomachSize;
            }
        }

        /**
         * Constants and methods related to creature combat, including health, strength, and armour.
         */
        public static class Combat {
            /** The distance at which creatures can sense each other. */
            public static final int sensingDistance = 200;
            /** Minimum and maximum strength value for combat. */
            public static final double minStrength = 0, maxStrength = 20;
            /** Minimum and maximum health for a creature. */
            public static final int minHealth = 20, maxHealth = 200;
            /** Minimum and Maximum armour value for a creature. */
            public static final double minArmour = 0, maxArmour = 20;

            /** The strength increase factor after every attack action a Creature takes. */
            public static final double biteStrengthIncrease = 0.1;
            /** The Armour increase factor after every received attack from another Creature. */
            public static final double damageArmourIncrease = 0.2;

            /** Health regen speed per tick when spending energy to regenerate health. */
            public static final double healthRegen = 2;
            /** Health regen cost in energy when spending energy to regenerate health. */
            public static final double healthRegenCost = 2;

            /**
             * Calculates the maximum strength a creature can have based on its size.
             *
             * @param size The size of the creature.
             * @return The maximum strength based on size.
             */
            public static double sizeToMaxStrength(double size) {
                return Equations.sigmoid(0, maxStrength, 0.04, Reproduce.maxSize / 2, size);
            }

            /**
             * Calculates the maximum armour a creature can have based on its size.
             *
             * @param size The size of the creature.
             * @return The maximum armour based on size.
             */
            public static double sizeToMaxArmour(double size) {
                return Equations.sigmoid(0, maxArmour, 0.05, Reproduce.maxSize / 2, size);
            }

            /**
             * Calculates the maximum health a creature can have based on its size.
             *
             * @param size The size of the creature.
             * @return The maximum health based on size.
             */
            public static double sizeToMaxHealth(double size) {
                return Equations.sigmoid(0, maxHealth, 0.05, Reproduce.maxSize / 2, size);
            }
        }

        /**
         * Constants and formulas related to movement, force, and momentum of creatures.
         */
        public static class Movement {
            /** The loss of velocity due to impulse in movement. */
            public static final double momentumToDamage = 0.03;
            /** Maximum force a creature can exert. */
            public static final int maxForce = 40;
            /** Constant used to determine movement based on size. */
            public static double sizeMovementConstant = 0.0007;

            /**
             * Calculates the maximum force a creature can exert based on its size.
             *
             * @param size The size of the creature.
             * @return The maximum force based on size.
             */
            public static double sizeToMaxForce(double size) {
                return Equations.sigmoid(0, maxForce, 0.04, Reproduce.maxSize / 2, size);
            }
        }

        /**
         * Constants for boid behavior in a flocking system.
         */
        public static class Boids {
            /** Threshold angle for adjusting direction. */
            public static final double angleAdjustmentThreshold = Math.toRadians(2);
            /** Minimum speed for herding in a flock. */
            public static double minHerdingSpeed = 10;
            /** Maximum weight for separation behavior. */
            public static final double maxSeparationWeight = 10;
            /** Maximum weight for alignment behavior. */
            public static final double maxAlignmentWeight = 5;
            /** Maximum weight for cohesion behavior. */
            public static final double maxCohesionWeight = 5;
            /** Minimum weight for separation behavior. */
            public static final double minSeparationWeight = 0;
            /** Minimum weight for alignment behavior. */
            public static final double minAlignmentWeight = 0;
            /** Minimum weight for cohesion behavior. */
            public static final double minCohesionWeight = 0;
        }

        /**
         * Constants for vision, including range, cone angle, and the number of vision rays.
         */
        public static class Vision {
            /** Minimum and maximum vision value. */
            public static final int minVisionValue = 0, maxVisionValue = 1;
            /** Minimum and maximum vision distance. */
            public static final int minVisionDistance = 100, maxVisionDistance = 2000;
            /** Maximum number of vision rays a creature can have. */
            public static final int maxVisionRayCount = 20;
            /** Maximum angle for the vision cone. */
            public static final double maxVisionConeAngle = Math.toRadians(240);

            /**
             * Calculates the vision cone angle based on the vision value.
             *
             * @param visionValue The vision value of the creature.
             * @return The corresponding vision cone angle.
             */
            public static double visionConeAngle(double visionValue) {
                return Equations.exponential(0.5, 3, 0, visionValue) * maxVisionConeAngle;
            }

            /**
             * Calculates the number of vision rays based on the vision value.
             *
             * @param visionValue The vision value of the creature.
             * @return The corresponding number of vision rays.
             */
            public static int visionRayCount(double visionValue) {
                return (int) Math.round(Equations.exponential(0.5, 3, 0, visionValue) * maxVisionRayCount);
            }

            /**
             * Calculates the vision distance based on the vision value.
             *
             * @param visionValue The vision value of the creature.
             * @return The corresponding vision distance.
             */
            public static int visionDistance(double visionValue) {
                return (int) Math.round(Equations.exponential(2, 3, 3, visionValue) * (maxVisionDistance - minVisionDistance) + minVisionDistance);
            }
        }

        /**
         * Constants and formulas related to energy consumption, growth, and diet.
         */
        public static class Energy {
            /** Minimum and maximum value of diet (herbivory to carnivory scale). */
            public static final double minDietValue = 0, maxDietValue = 1;

            /** Minimum and maximum weight gain during growth. */
            public static final double minGrowthWeight = 0, maxGrowthWeight = 0.1;
            /** Minimum and maximum bias for growth. */
            public static final double minGrowthBias = 0, maxGrowthBias = 4000;

            /** Maximum energy a creature can store. */
            private static final double maxEnergy = 300;

            /**
             * Formula for calculating herbivory affinity based on diet.
             *
             * @param dietValue The value of the creature's diet.
             * @return The herbivory affinity.
             */
            public static double herbivoryAffinityFormula(double dietValue) {
                return Equations.sigmoid(-0.2, 1, 11, 0.5, dietValue);
            }

            /**
             * Formula for calculating carnivory affinity based on diet.
             *
             * @param dietValue The value of the creature's diet.
             * @return The carnivory affinity.
             */
            public static double carnivoryAffinityFormula(double dietValue) {
                return Equations.sigmoid(1.2, 0.2, 11, 0.4, dietValue);
            }

            /**
             * Formula for calculating the energy cost for a creature based on various factors.
             *
             * @param stomachFluid The amount of stomach fluid.
             * @param size The size of the creature.
             * @param strength The strength of the creature.
             * @param brainComplexity The complexity of the brain.
             * @return The energy cost.
             */
            public static double energyCostFormula(double stomachFluid, double size, double strength, int brainComplexity) {
                return 0.05 * stomachFluid / (Digestion.sizeToStomachSize(size) * Digestion.stomachSizeToMaxStomachFluid) + 0.3 * size / Reproduce.maxSize + 0.3 * strength / Combat.maxStrength + 0.01 * brainComplexity;
            }

            /**
             * Formula for calculating maximum energy capacity based on size.
             *
             * @param size The size of the creature.
             * @return The maximum energy capacity.
             */
            public static double sizeToMaxEnergyFormula(double size) {
                return Equations.sigmoid(20, maxEnergy, 0.06, Reproduce.maxSize / 2, size);
            }

            /**
             * Formula for calculating growth size based on maturity, growth weight, and growth bias.
             *
             * @param maturity The maturity level.
             * @param creatureMinSize The minimum size of the creature.
             * @param creatureMaxSize The maximum size of the creature.
             * @param growthWeight The growth weight.
             * @param growthBias The growth bias.
             * @return The resulting growth size.
             */
            public static double maturingSizeFormula(int maturity, double creatureMinSize, double creatureMaxSize, double growthWeight, double growthBias) {
                return Equations.sigmoid(creatureMinSize, creatureMaxSize, growthWeight, growthBias, maturity);
            }

            /**
             * Multiplier for armour growth based on the armour value.
             *
             * @param armour The armour value.
             * @return The armour growth multiplier.
             */
            public static double armourGrowthMultiplier(double armour) {
                return 1 + 0.5 * armour / Combat.maxArmour;
            }
        }

        /**
         * Constants and formulas related to the initial starting values for creatures.
         */
        public static class start {
            /** Minimum and maximum size a creature can start with. */
            public static final double minSize = 20, maxSize = 40;
            /** Initial size difference between minimum and maximum size. */
            public static final double sizeDiff = 0.4;

            /** Minimum and maximum force a creature can exert based on its size. */
            public static final double minForce = Movement.sizeToMaxForce(minSize), maxForce = Movement.sizeToMaxForce(maxSize);
            /** Initial strength value for a creature. */
            public static final double strength = 1;
            /** Armour value for the creature. */
            public static final double armour = 1;

            /** Minimum offspring investment. */
            public static final double minOffspringInvestment = 0.2;
            /** Maximum offspring investment. */
            public static final double maxOffspringInvestment = 0.3;
            /** Time in ticks for incubation. */
            public static final int incubationTime = 1000;

            /** Growth weight for the creature. */
            public static final double growthWeight = 0.003;
            /** Growth bias for the creature. */
            public static final double growthBias = 2000;

            /** Minimum and maximum vision value for a creature. */
            public static final double minVisionValue = 0.5, maxVisionValue = 0.5;
            /** Minimum and maximum diet value for a creature. */
            public static final double minDietValue = 0.1, maxDietValue = 0.9;
            /** Minimum and maximum weight for separation behavior in flocking. */
            public static final double minSeparationWeight = 1.9, maxSeparationWeight = 1.9;
            /** Minimum and maximum weight for alignment behavior in flocking.*/
            public static final double minAlignmentWeight = 1.3, maxAlignmentWeight = 1.3;
            /** Minimum and maximum weight for cohesion behavior in flocking. */
            public static final double minCohesionWeight = 1.8, maxCohesionWeight = 1.8;

            /** Minimum number of synapses in within the Neural network brain */
            public static final int minSynapses = 4;
        }
    }

    /** Constants for the Neural Network (brain) of each Creature. */
    public static class NeuralNet {
        /** How frequently per tick to prompt the Neural Network for its outputs. */
        public static final int PromptInterval = 1;
        /** The period of the internal clock, which is used to determine when to reset the clock. */
        public static final int internalClockPeriod = 60 * 20;

        /** The length of the input & output vector for the Neural Network. */
        private static final int inputNum = 35, outputNum = 11;
        /** The default Activation Function for new Neurons. Can be changed during mutation. */
        private static final Activation hiddenAF = Activation.sigmoid;
        /** The fixed Activation Function for the output neurons. Cannot be changed ever. */
        private static final Activation.arrays outputAF = Activation.arrays.sigmoid;
        /** The Cost Function of which output values are optimized to minimize. */
        private static final Cost costFunction = Cost.diffSquared;
        /** The Optimizer whose algorithm is used to minimize the Cost function above. */
        private static final Optimizer optimizer = Optimizer.RMS_PROP;

        /** The complete Neural Network Constants object, used in order to create
         * default Neural Networks with no synapses or any hidden neurons. */
        public static final Evolution.Constants EvolutionConstants =
                new Evolution.Constants(inputNum, outputNum, 0, hiddenAF, outputAF, costFunction, optimizer);

        /** Stores which Input neuron corresponds to which related input. */
        public static final String[] String = new String[]{
                "Creature Dist.", "Creature Vel.", "Creature Dir. L", "Creature Dir. R", "Creature Energy", "Creature Armour", "# Creature",
                "Bush Dist.", "Bush Dir. L", "Bush Dir. R", "Bush Energy", "Bush Size", "# Bush",
                "Corpse Dist.", "Corpse Dir. L", "Corpse Dir. R", "Corpse Energy", "Corpse Size", "# Corpse",
                "Egg Dist.", "Egg Dir. L", "Egg Dir. R", "Egg Energy", "Egg Hatch Time", "# Egg",
                "Velocity", "Angular Vel.", "Energy", "Health", "Size", "Metabolism", "Strength", "Stomach %", "Starving", "ON",
                "Forward", "Backward", "Left", "Right", "Mate", "Regen", "Eat", "Digestion Rate", "Herd", "Separate", "Reset Clock"};
    }


    /**
     * Utility class for basic physics calculations, including friction,
     * collision, and torque.
     */
    public static class Physics {
        /** Fraction of velocity lost during an impulse or collision. */
        public static final double impulseVelocityLoss = 0.4;

        /** Angular friction factor applied to rotational movement. */
        public static final double frictionAngular = 0.9;
        /** Friction in the direction of movement (slows motion forward/backward). */
        public static final double frictionParallel = 0.1;
        /** Friction perpendicular to direction of movement (slows lateral motion). */
        public static final double frictionPerpendicular = 0.15;
        /** Friction encountered in bushes or other resistive environments. */
        public static final double bushFriction = 0.01;

        /**
         * Converts torque into angular acceleration.
         *
         * @param radius The radius at which the force is applied.
         * @param mass The mass of the object.
         * @param force The applied force.
         * @return The resulting angular acceleration.
         */
        public static double torqueToAlpha(double radius, double mass, double force) {
            //sum of torque = Ia (Moment of Inertia * Angular Acceleration)
            return force * radius / (0.5 * mass * radius * radius);
        }

        /**
         * Computes the resulting velocities after a 1D elastic collision between two bodies.
         *
         * @param mass1 The mass of the first object.
         * @param mass2 The mass of the second object.
         * @param speed1 Velocity vector of the first object.
         * @param speed2 Velocity vector of the second object.
         * @return An array of two velocity vectors: [new velocity of object 1, new velocity of object 2].
         */
        public static Vector2D[] elasticCollision(double mass1, double mass2, Vector2D speed1, Vector2D speed2) {
            double mass = mass1 + mass2;
            double velxF1 = (mass2 * (2 * speed2.x - speed1.x) + mass1 * speed1.x) / mass;
            double velyF1 = (mass2 * (2 * speed2.y - speed1.y) + mass1 * speed1.y) / mass;
            return new Vector2D[]{new Vector2D(velxF1, velyF1), new Vector2D(speed1.x + velxF1 - speed2.x, speed1.y + velyF1 - speed2.y)};
        }
    }
}