package Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.concurrent.*;

public class Constants implements Serializable {
    public static class WorldConstants {
        public static final Color semiTransparentRed = new Color(255, 0, 0, 128);
        public static final Color semiTransparentGreen = new Color(0, 255, 0, 128);
        public static final int xBound = 10000;
        public static final int yBound = 10000;
        public static final int GridWidth = 200;
        public static final int GridHeight = 200;
        public static final Rectangle worldBorder = new Rectangle(0, 0, xBound, yBound);
        public static Polygon topVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, worldBorder.width, worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, 0, -CreatureConstants.Vision.maxVisionDistance - 50},
                4);
        public static Polygon bottomVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, worldBorder.width, worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50},
                new int[]{worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50, worldBorder.height, worldBorder.height, worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50},
                4);
        public static Polygon leftVisionBox = new Polygon(
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, 0, -CreatureConstants.Vision.maxVisionDistance - 50},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, worldBorder.height, worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50},
                4);
        public static Polygon rightVisionBox = new Polygon(
                new int[]{worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50, worldBorder.width, worldBorder.width, worldBorder.width + CreatureConstants.Vision.maxVisionDistance + 50},
                new int[]{-CreatureConstants.Vision.maxVisionDistance - 50, 0, worldBorder.height, worldBorder.height + CreatureConstants.Vision.maxVisionDistance + 50},
                4);

        public static class Settings {
            public static final int maxSpeed = 100;
            public static final int maxThread = 100;
            public static boolean devMode = true;
            public static final int framesPerSec = 60;
            public static int ticksPerSec = -1;
            public static final int ticksToSecond = 20; //ticks to second conversion rate
            public static final String[] reportInfo = new String[]{"Size", "MinSize", "MaxSize", "Force", "Speed", "Energy", "MaxEnergy", "Reproduction Cost", "Offspring Investment", "Incubation Time", "Health", "MaxHealth", "Maturity", "Strength", "Armour", "Diet Value", "Herbivore Affinity", "Carnivore Affinity"};
            public static final int reportSize = reportInfo.length;
            public static final String[] countInfo = new String[]{"# Carnivore", "# Herbivore", "# Creatures", "# Eggs", "# Corpses", "# Berries"};
        }

        public static class WorldGen {
            public static int bushRadius = 0;//150
            public static final int numBushes = 200;//50
            public static int startingPopulation = 0;//150
            public static int naturalSpawningThreshold = 100;//60
            public static final double naturalSpawningProbability = 0.1;//0.002
            public static boolean spawnCorpse = true;
        }
    }

    public static class CorpseConstants {
        public static final double corpseDecayRate = 0.0001;
        public static final double corpseRottenPercentage = 0.2;
        public static final double minCorpseSize = Math.nextDown(CreatureConstants.Reproduce.babySize);
        public static double sizeMovementConstant = CreatureConstants.Movement.sizeMovementConstant / 2;
    }

    public static class BushConstants {
        public static final int initialMaxSize = 400;//width
        public static final int initialMinSize = 240;//width
        public static final double sizeToHeight = (double) ImageConstants.bushHeight / ImageConstants.bushWidth;
        public static final double sizeToWidth = 1;
        public static int maxBerries = 40;//10
        public static double energy = 5;//10
        public static final double berriesGrowProbability = 0.005;
    }

    public static class WindowConstants {
        public static final int maxThread = 10;
        public static final int worldWidth = 1200;
        public static final int worldHeight = 800;
        public static final int menuWidth = 800;
        public static final int menuHeight = 800;
        public static final int titleWidth = 500;
        public static final int titleHeight = 100;
        public static final int controlPanelWidth = 400;
        public static final double minZoom = 0.1;
        public static final double maxZoom = 10;
        public static final double followZoom = 2;
        public static final int settingsWidth = 800;
        public static final int settingsHeight = 600;
        public static final int graphWidth = 800;
        public static final int graphHeight = 800;
        public static final int graphMaxDataSize = 1000;
        public static final int loadBarWidth = 200;
        public static final int loadBarHeight = 400;
    }

    public static class ImageConstants {
        public static final int bushWidth = 320;
        public static final int bushHeight = 180;
        public static Image berries;
        public static Image bush;
        public static BufferedImage bird;
        public static Image corpse;
        public static Image egg;
        public static Image menuBackground;
        public static Image titleCard;
        public static Image button;
        public static Image buttonHover;
        public static Image buttonPressed;
        public static Image scrollPanel;
        public static final Image[] birdRotations = new Image[360];

        public static Image getRotation(double radianRotation) {
            int degreeRotation = (int) Math.round(Math.toDegrees(radianRotation));
            while (degreeRotation < 0) degreeRotation += 360;
            while (degreeRotation >= 360) degreeRotation -= 360;
            return birdRotations[degreeRotation];
        }

        public static final int berriesWidth = 20;
        public static final int berriesHeight = 20;
        public static final int ResizeConstant = Image.SCALE_DEFAULT;
    }

    public static class CreatureConstants {
        public static class Reproduce {
            public static final double geneMutationShiftProbability = 0.1;
            public static final double geneMutationRandomProbability = 0.01;
            public static final double geneMutationShiftStrength = 0.2;
            public static final int maturityDeath = 200000;
            public static final double babySize = 20;
            public static final int eggSize = 25;
            public static final double rottenEggPerct = 0.02;
            public static final double multipleEmbryoPerct = 0.01;
            public static final int maxIncubationTime = 5000;
            public static final int minIncubationTime = 500;
            public static final int maxSize = 160;
            public static final double sizeDiff = 0.1;
            public static final double minSize = babySize;
            public static final double maxOffspringInvestment = 0.5;
            public static final double minOffspringInvestment = 0.125;

            public static double scalesWithMaturity(double minSize, double maxSize, double size) {
                if (minSize == size && minSize == maxSize) return 1;
                return Equations.sigmoid(0.35, 1, 4.2, 0.5, (size - minSize) / (maxSize - minSize));
            }
        }

        public static class Stomach {
            public static final double plantDigestionRate = 0.125;
            public static final double meatDigestionRate = 0.8;
            public static final double stomachSizeToMaxStomachFluid = 0.4;
            public static final double maxStomachSize = 250;
            public static final double plantMassToEnergy = 1.2; //how much energy per 1 unit of mass
            public static final double meatMassToEnergy = 3;

            public static double sizeToStomachSize(double size) {
                return Equations.sigmoid(0, maxStomachSize, 0.06, 70, size);
//                return (size-10)/(Reproduce.maxSize-10) * maxStomachSize;
            }
        }

        public static class Combat {
            public static final int sensingDistance = 200;
            public static final double minStrength = 0;
            public static final double maxStrength = 20;
            public static final int minHealth = 20;
            public static final int maxHealth = 200;
            public static final double biteStrengthIncrease = 0.1;
            public static final double damageArmourIncrease = 0.2;
            public static final double healthRegen = 2;
            public static final double minArmour = 0;
            public static final double maxArmour = 20;

            public static double sizeToMaxStrength(double size) {
                return Equations.sigmoid(0, maxStrength, 0.04, Reproduce.maxSize / 2, size);
            }

            public static double sizeToMaxArmour(double size) {
                return Equations.sigmoid(0, maxArmour, 0.05, Reproduce.maxSize / 2, size);
            }

            public static double sizeToMaxHealth(double size) {
                return Equations.sigmoid(0, maxHealth, 0.05, Reproduce.maxSize / 2, size);
            }
        }

        public static class Movement {
            public static final double momentumToDamage = 0.03;
            public static final int maxForce = 40;
            public static double sizeMovementConstant = 0.0007;

            public static double sizeToMaxForce(double size) {
                return Equations.sigmoid(0, maxForce, 0.04, Reproduce.maxSize / 2, size);
            }
        }

        public static class Boids {
            public static final double angleAdjustmentThreshold = Math.toRadians(2);
            public static double minHerdingSpeed = 10;
            public static final double maxSeparationWeight = 10;
            public static final double maxAlignmentWeight = 5;
            public static final double maxCohesionWeight = 5;
            public static final double minSeparationWeight = 0;
            public static final double minAlignmentWeight = 0;
            public static final double minCohesionWeight = 0;
        }

        public static class Vision {
            public static final int minVisionValue = 0;
            public static final int maxVisionValue = 1;
            public static final int minVisionDistance = 100;
            public static final int maxVisionDistance = 2000;
            public static final int maxVisionRayCount = 20;
            public static final double maxVisionConeAngle = Math.toRadians(240);

            public static double visionConeAngle(double visionValue) {
                return Equations.exponential(0.5, 3, 0, visionValue) * maxVisionConeAngle;
            }

            public static int visionRayCount(double visionValue) {
                return (int) Math.round(Equations.exponential(0.5, 3, 0, visionValue) * maxVisionRayCount);
            }

            public static int visionDistance(double visionValue) {
                return (int) Math.round(Equations.exponential(2, 3, 3, visionValue) * (maxVisionDistance - minVisionDistance) + minVisionDistance);
            }
        }

        public static class Energy {
            public static final double minDietValue = 0;
            public static final double maxDietValue = 1;
            public static final double minGrowthWeight = 0;
            public static final double maxGrowthWeight = 0.1;
            public static final double minGrowthBias = 0;
            public static final double maxGrowthBias = 4000;
            private static final double maxEnergy = 300;

            public static double herbivoryAffinityFormula(double dietValue) {
                return Equations.sigmoid(-0.2, 1, 11, 0.5, dietValue);
            }

            public static double carnivoryAffinityFormula(double dietValue) {
                return Equations.sigmoid(1.2, 0.2, 11, 0.4, dietValue);
            }

            public static double energyCostFormula(double stomachFluid, double size, double strength, double brainNodes, double brainSynapses) {
                return 0.05 * stomachFluid / (Stomach.sizeToStomachSize(size) * Stomach.stomachSizeToMaxStomachFluid) + 0.3 * size / Reproduce.maxSize + 0.3 * strength / Combat.maxStrength + 0.01 * (brainNodes - NeuralNet.inputNum - NeuralNet.outputNum) + 0.01 * brainSynapses;
            }

            public static double sizeToMaxEnergyFormula(double size) {
                return Equations.sigmoid(20, maxEnergy, 0.06, Reproduce.maxSize / 2, size);
            }

            public static double maturingSizeFormula(int maturity, double creatureMinSize, double creatureMaxSize, double growthWeight, double growthBias) {
                return Equations.sigmoid(creatureMinSize, creatureMaxSize, growthWeight, growthBias, maturity);
            }

            public static double armourGrowthMultiplier(double armour) {
                return 1 + 0.5 * armour / Combat.maxArmour;
            }
        }

        public static class start {
            public static final double minSize = 20;
            public static final double sizeDiff = 0.4;
            public static final double maxSize = 40;
            public static final double minForce = Movement.sizeToMaxForce(minSize);
            public static final double maxForce = Movement.sizeToMaxForce(maxSize);
            public static final double strength = 1;
            public static final int incubationTime = 1000;
            public static final double growthWeight = 0.003;
            public static final double growthBias = 2000;
            public static final double minVisionValue = 0.5;
            public static final double maxVisionValue = 0.5;
            public static final double minDietValue = 0.1;
            public static final double maxDietValue = 0.9;
            public static final double armour = 1;
            public static final double maxOffspringInvestment = 0.3;
            public static final double minOffspringInvestment = 0.2;
            public static final double maxSeparationWeight = 1.9;
            public static final double maxAlignmentWeight = 1.3;
            public static final double maxCohesionWeight = 1.8;
            public static final double minSeparationWeight = 1.9;
            public static final double minAlignmentWeight = 1.3;
            public static final double minCohesionWeight = 1.8;
            public static final int startingSynapses = 4;
        }
    }

    public static class NeuralNet {
        public static final int PromptInterval = 1;
        public static final boolean batchNormalizeInputs = false;
        public static final boolean batchNormalizeHiddenLayers = false;
        public static final int inputNum = 35;
        public static final int outputNum = 11;
        public enum Type {input,hidden,output}
        public enum AF {none,relu,sigmoid,tanh,leakyRelu}
        public static final AF hiddenAF = AF.sigmoid;
        public static final AF outputAF = AF.sigmoid;
        public static final int weightedExcess = 1;
        public static final int weightedDisjoints = 1;
        public static final int weightedWeights = 1;
        public static final int compatibilityThreshold = 4;
        public static final double mutationSynapseProbability = 0.08;
        public static final double mutationNodeProbability = 0.1;
        public static final double mutationNodeAFProbability = 0.06;
        public static final double mutationWeightShiftProbability = 0.06;
        public static final double mutationWeightRandomProbability = 0.05;
        public static final double mutationBiasShiftProbability = 0.06;
        public static final double mutationWeightShiftStrength = 2;
        public static final double mutationWeightRandomStrength = 2;
        public static final double mutationBiasShiftStrength = 0.3;
        public static final String[] String = new String[]{
                "Creature Dist.", "Creature Vel.", "Creature Dir. L", "Creature Dir. R", "Creature Energy", "Creature Armour", "# Creature",
                "Bush Dist.", "Bush Dir. L", "Bush Dir. R", "Bush Energy", "Bush Size", "# Bush",
                "Corpse Dist.", "Corpse Dir. L", "Corpse Dir. R", "Corpse Energy", "Corpse Size", "# Corpse",
                "Egg Dist.", "Egg Dir. L", "Egg Dir. R", "Egg Energy", "Egg Hatch Time", "# Egg",
                "Velocity", "Angular Vel.", "Energy", "Health", "Size", "Metabolism", "Strength", "Stomach %", "Starving", "ON",
                "Forward", "Backward", "Left", "Right", "Mate", "Regen", "Eat", "Digestion Rate", "Herd", "Separate", "Reset Clock"};
    }

    public static class Equations {
        public static double sigmoid(double lowerBound, double upperBound, double weight, double bias, double x) {
            return (upperBound - lowerBound) / (1 + Math.exp(-weight * (x - bias))) + lowerBound;
        }

        public static double exponential(double base, double weight, double bias, double x) {
            return Math.pow(base, weight * x - bias);
        }

        public static double angleDistance(double angle1, double angle2) {
            double angleDiff = angle2 - angle1;
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
            return angleDiff;
//            double temp = Math.abs(angle1 - angle2) % (Math.PI*2);
//            return temp>(Math.PI)?Math.PI*2-temp:temp;
        }

        public static boolean angleSignPositive(double a, double b) {
            // Normalize the angles to be within [0, 360) degrees
            double fullRad = Math.PI * 2;
            a = (a % fullRad + fullRad) % fullRad;
            b = (b % fullRad + fullRad) % fullRad;

            // Calculate the difference
            double difference = b - a;

            // Normalize the difference to the range (-180, 180]
            if (difference > Math.PI) {
                difference -= fullRad;
            } else if (difference <= -Math.PI) {
                difference += fullRad;
            }

            // Return true for positive direction, false for negative direction
            return difference > 0;
        }

        public static double distFromRect(Rectangle a, Rectangle b) {
            int x1 = a.x, y1 = a.y, x1b = x1 + a.width, y1b = y1 + a.height, x2 = b.x, y2 = b.y, x2b = x2 + b.width, y2b = y2 + b.height;
            boolean left = x2b < x1;
            boolean right = x1b < x2;
            boolean bottom = y2b < y1;
            boolean top = y1b < y2;

            if (top && left) {
                return Math.sqrt((x1 - x2b) * (x1 - x2b) + (y1b - y2) * (y1b - y2));
            } else if (left && bottom) {
                return Math.sqrt((x1 - x2b) * (x1 - x2b) + (y1 - y2b) * (y1 - y2b));
            } else if (bottom && right) {
                return Math.sqrt((x1b - x2) * (x1b - x2) + (y1 - y2b) * (y1 - y2b));
            } else if (right && top) {
                return Math.sqrt((x1b - x2) * (x1b - x2) + (y1b - y2) * (y1b - y2));
            } else if (left) {
                return x1 - x2b;
            } else if (right) {
                return x2 - x1b;
            } else if (bottom) {
                return y1 - y2b;
            } else if (top) {
                return y2 - y1b;
            } else {
                // Rectangles intersect
                return 0;
            }
        }

        public static double dist(double x1, double y1, double x2, double y2) {
            double dx = x1 - x2, dy = y1 - y2;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    public static class Vector2D implements Serializable {
        public double x, y;

        public Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vector2D() {
            this.x = 0;
            this.y = 0;
        }

        public Vector2D add(double dx, double dy) {
            this.x += dx;
            this.y += dy;
            return this;
        }

        public Vector2D add(Vector2D v) {
            this.x += v.x;
            this.y += v.y;
            return this;
        }

        public double length() {
            return Math.sqrt(x * x + y * y);
        }

        public double angle() {
            return Math.atan2(y, x);
        }

        public Vector2D normalize() {
            double strength = length();
            if (strength == 0) return this;
            this.x /= strength;
            this.y /= strength;
            return this;
        }

        public Vector2D multiply(double multiplier) {
            this.x *= multiplier;
            this.y *= multiplier;
            return this;
        }

        public Vector2D multiply(double mx, double my) {
            this.x *= mx;
            this.y *= my;
            return this;
        }

        public Vector2D divide(double divider) {
            this.x /= divider;
            this.y /= divider;
            return this;
        }

        public Vector2D subtract(double dx, double dy) {
            this.x -= dx;
            this.y -= dy;
            return this;
        }

        public Vector2D subtract(Vector2D v) {
            this.x -= v.x;
            this.y -= v.y;
            return this;
        }

        public Vector2D minVector(double minX, double minY) {
            if (minX != 0) if ((minX < 0 == x < 0) || Math.abs(x) < Math.abs(minX)) this.x = minX;
            if (minY != 0) if ((minY < 0 == y < 0) || Math.abs(y) < Math.abs(minY)) this.y = minY;
            return this;
        }

        public Vector2D min(double min) {
            if (min == 0) return this;
            if (Math.abs(x) < Math.abs(min)) this.x = x < 0 ? -min : min;
            if (Math.abs(y) < Math.abs(min)) this.y = y < 0 ? -min : min;
            return this;
        }

        public Vector2D max(double max) {
            if (max == 0) return this;
            if (Math.abs(x) > max) this.x = x < 0 ? -max : max;
            if (Math.abs(y) > max) this.y = y < 0 ? -max : max;
            return this;
        }

        @Override
        public String toString() {
            return this.x + "," + this.y;
        }
    }

    public static class Physics {
        public static final double impulseVelocityLoss = 0.4;
        public static final double frictionAngular = 0.9;
        public static final double frictionParallel = 0.8;
        public static final double frictionPerpendicular = 0.9;
        public static final double bushFriction = 0.01;

        public static double torqueToAlpha(double radius, double mass, double force) {
            //sum of torque = Ia (Moment of Inertia * Angular Acceleration)
            return force * radius / (0.5 * mass * radius * radius);
        }

        public static Vector2D[] elasticCollision(double mass1, double mass2, Vector2D speed1, Vector2D speed2) {
            double mass = mass1 + mass2;
            double velxF1 = (mass2 * (2 * speed2.x - speed1.x) + mass1 * speed1.x) / mass;
            double velyF1 = (mass2 * (2 * speed2.y - speed1.y) + mass1 * speed1.y) / mass;
            return new Vector2D[]{new Vector2D(velxF1, speed1.x + velxF1 - speed2.x), new Vector2D(velyF1, speed1.y + velyF1 - speed2.y)};
        }
    }

    public static ExecutorService createThreadPool(int maxThreads) {
        return new ThreadPoolExecutor(
                maxThreads, maxThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()) {

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t == null && r instanceof Future<?>) {
                    try {
                        ((Future<?>) r).get();
                    } catch (CancellationException ce) {
                        t = ce;
                    } catch (ExecutionException ee) {
                        t = ee.getCause();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Ignore/reset
                    }
                }
                if (t != null) {
                    t.printStackTrace();
                }
            }
        };
    }
}