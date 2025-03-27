package Genome.enums;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/** Activation Function enum containing regular and derivative functions of commonly-used Activation Functions */
public enum Activation {
    none(
            input -> input,
            (input, gradient) -> gradient
    ), square(
            input -> input * input,
            (input, gradient) -> gradient * 2 * input
    ), sin(
            Math::sin,
            (input, gradient) -> gradient * Math.cos(input)
    ), cos(
            Math::cos,
            (input, gradient) -> gradient * -Math.sin(input)
    ), abs(
            Math::abs,
            (input, gradient) -> gradient * Math.signum(input)
    ),
    reLU(
            input -> Math.max(0, input),
            (input, gradient) -> input > 0 ? gradient : 0
    ),
    sigmoid(
            input -> 1 / (1 + Math.exp(-input)),
            (input, gradient) -> {
                double a = 1 / (1 + Math.exp(-input));
                return gradient * a * (1 - a);
            }),
    tanh(
            Math::tanh,
            (input, gradient) -> {
                double tanhValue = Math.tanh(input);
                return gradient * (1 - tanhValue * tanhValue);
            }),
    leakyReLU(
            input -> input > 0 ? input : 0.1 * input,
            (input, gradient) -> input > 0 ? gradient : 0.1 * gradient
    );

    private static final Random RANDOM = new Random();
    private static final BiFunction<Integer, Integer, Double> HE_Initialization = (inputSize, outputSize) -> RANDOM.nextGaussian(0, Math.sqrt(2.0 / (inputSize + outputSize)));
    private static final BiFunction<Integer, Integer, Double> XAVIER_Initialization = (inputSize, outputSize) -> RANDOM.nextGaussian(0, Math.sqrt(1 / Math.sqrt(inputSize + outputSize)));


    private final Function<Double, Double> function;
    private final BiFunction<Double, Double, Double> derivativeFunction;

    Activation(Function<Double, Double> function, BiFunction<Double, Double, Double> derivativeFunction) {
        this.function = function;
        this.derivativeFunction = derivativeFunction;
    }

    /** Returns the result of AF({@code z})*/
    public double calculate(double z) {
        assert Double.isFinite(z) : "Attempted to input invalid values into Activation Function " + z;
        double output = this.function.apply(z);
        assert Double.isFinite(output) : "Activation Function returning invalid values " + z;
        return output;
    }

    /**
     * Effect: multiplies {@code da_dC} with AF'({@code z})
     * @return {@code dz_dC}
     */
    public double derivative(double z, double da_dC) {
        assert Double.isFinite(da_dC) : "Attempted to input invalid values into Deriv of Activation Function " + z + "  " + da_dC;
        double newGradient = this.derivativeFunction.apply(z, da_dC);
        assert Double.isFinite(newGradient) : "Deriv of Activation Function returning invalid values " + z + "  " + da_dC;
        return newGradient;
    }

    /** Returns the weights and bias initializer supplier best associated with {@code AF} function */
    public static Supplier<Double> getInitializer(Activation AF, int inputNum, int outputNum) {
        if (AF.equals(reLU) || AF.equals(leakyReLU)) return () -> HE_Initialization.apply(inputNum, outputNum);
        else return () -> XAVIER_Initialization.apply(inputNum, outputNum);
    }

    public enum arrays {
        none,
        square,
        sin,
        cos,
        abs,
        ReLU,
        sigmoid,
        tanh,
        LeakyReLU,
        softmax(input -> {
            double[] output = new double[input.length];
            double latestInputSum = 0, max = Double.MIN_VALUE;
            for (double num : input) max = Math.max(max, num);
            for (double num : input) latestInputSum += Math.exp(num - max);
            for (int i = 0; i < input.length; i++) output[i] = Math.exp(input[i] - max) / latestInputSum;
            return output;
        }, (input, gradient) -> {
            double[] output = new double[input.length];
            double[] softmaxOutput = new double[input.length];
            double latestInputSum = 0, max = Double.MIN_VALUE;
            for (double num : input) max = Math.max(max, num);
            for (double num : input) latestInputSum += Math.exp(num - max);
            for (int i = 0; i < input.length; i++)
                softmaxOutput[i] = Math.exp(input[i] - max) / latestInputSum;

            // Compute the gradient using the vectorized form
            double dotProduct = 0.0;
            for (int i = 0; i < softmaxOutput.length; i++)
                dotProduct += softmaxOutput[i] * gradient[i];

            for (int i = 0; i < softmaxOutput.length; i++)
                output[i] = softmaxOutput[i] * (gradient[i] - dotProduct);

            return output;
        });

        private final Function<double[], double[]> arrayFunction;
        private final BiFunction<double[], double[], double[]> arrayDerivativeFunction;

        arrays() {
            arrayFunction = (input) -> {
                Function<Double, Double> func = Activation.valueOf(toString()).function;
                double[] output = new double[input.length];
                for (int i = 0; i < input.length; i++) output[i] = func.apply(input[i]);
                return output;
            };
            arrayDerivativeFunction = (input, gradient) -> {
                BiFunction<Double, Double, Double> func = Activation.valueOf(toString()).derivativeFunction;
                double[] output = new double[input.length];
                for (int i = 0; i < input.length; i++) output[i] = func.apply(input[i], gradient[i]);
                return output;
            };
        }

        arrays(Function<double[], double[]> arrayFunction, BiFunction<double[], double[], double[]> arrayDerivativeFunction) {
            this.arrayFunction = arrayFunction;
            this.arrayDerivativeFunction = arrayDerivativeFunction;
        }

        /** Returns the result of AF(x) for every x in {@code input} array*/
        public double[] calculate(double[] input) {
            for (double v : input)
                assert Double.isFinite(v) : "Attempted to input invalid values into Activation Function " + Arrays.toString(input);
            double[] output = this.arrayFunction.apply(input);
            for (double v : output)
                assert Double.isFinite(v) : "Activation Function returning invalid values " + Arrays.toString(input);
            return output;
        }

        /**
         * Effect: multiplies each element in {@code da_dC[i]} with their corresponding element {@code AF'(z[i])}
         * @return {@code dz_dC}
         */
        public double[] derivative(double[] z, double[] da_dC) {
            for (double v : da_dC)
                assert Double.isFinite(v) : "Attempted to input invalid values into Deriv of Activation Function " + Arrays.toString(z) + "  " + Arrays.toString(da_dC);
            double[] newGradient = this.arrayDerivativeFunction.apply(z, da_dC);
            for (double v : newGradient)
                assert Double.isFinite(v) : "Deriv of Activation Function returning invalid values " + Arrays.toString(z) + "  " + Arrays.toString(da_dC);
            return newGradient;
        }
    }
}
