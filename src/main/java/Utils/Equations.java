package Utils;

/**
 * Utility class for common mathematical equations used in simulations,
 * particularly in biological and physical modeling.
 */
public final class Equations {

    /**
     * Computes a sigmoid function bounded by lower and upper values.
     * Commonly used to create smooth transitions between two values.
     *
     * @param lowerBound The minimum value of the function.
     * @param upperBound The maximum value of the function.
     * @param weight The steepness of the curve.
     * @param bias The midpoint or inflection point of the curve.
     * @param x The input value.
     * @return A value smoothly transitioning between lowerBound and upperBound.
     */
    public static double sigmoid(double lowerBound, double upperBound, double weight, double bias, double x) {
        return (upperBound - lowerBound) / (1 + Math.exp(-weight * (x - bias))) + lowerBound;
    }

    /**
     * Calculates an exponential function with adjustable base, weight, and bias.
     * Useful for nonlinear growth models.
     *
     * @param base The base of the exponential.
     * @param weight Multiplier of the input value.
     * @param bias A constant to shift the exponent.
     * @param x The input value.
     * @return The result of base raised to (weight * x - bias).
     */
    public static double exponential(double base, double weight, double bias, double x) {
        return Math.pow(base, weight * x - bias);
    }

    /**
     * Calculates the smallest difference (in radians) between two angles,
     * taking into account the circular nature of angles.
     *
     * @param angle1 First angle in radians.
     * @param angle2 Second angle in radians.
     * @return The shortest angular distance between angle1 and angle2.
     */
    public static double angleDistance(double angle1, double angle2) {
        double angleDiff = angle2 - angle1;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
        return angleDiff;
    }


    /**
     * Determines whether the shortest direction to rotate from angle a to angle b
     * is in the positive (counter-clockwise) direction.
     *
     * @param a The starting angle in radians.
     * @param b The target angle in radians.
     * @return True if the positive direction is shorter; false otherwise.
     */
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

    /**
     * Calculates the Euclidean distance between two 2D points.
     *
     * @param x1 X-coordinate of the first point.
     * @param y1 Y-coordinate of the first point.
     * @param x2 X-coordinate of the second point.
     * @param y2 Y-coordinate of the second point.
     * @return The straight-line distance between the two points.
     */
    public static double dist(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2, dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
