package Evolution;

import Genome.enums.Activation;
import Genome.enums.Cost;

import java.util.ArrayList;

public class Evolution {
    public Constants Constants;

    private Evolution(Constants Constants) {
        this.Constants = Constants;
    }

    /** The builder class for {@link Evolution}, a factory that produces, trains, and
     * applies the NEAT genetic algorithm on neural network agents. */
    public static class EvolutionBuilder {
        private final Constants Constants = new Constants();

        public EvolutionBuilder setNumSimulated(int numSimulated) {
            Constants.numSimulated = numSimulated;
            return this;
        }

        public EvolutionBuilder setInputNum(int inputNum) {
            Constants.inputNum = inputNum;
            return this;
        }

        public EvolutionBuilder setOutputNum(int outputNum) {
            Constants.outputNum = outputNum;
            return this;
        }

        public EvolutionBuilder setOutputAF(Activation.arrays outputAF) {
            Constants.outputAF = outputAF;
            return this;
        }

        public EvolutionBuilder setDefaultHiddenAF(Activation defaultHiddenAF) {
            Constants.defaultHiddenAF = defaultHiddenAF;
            return this;
        }

        public EvolutionBuilder setCostFunction(Cost CostFunction) {
            Constants.CostFunction = CostFunction;
            return this;
        }

        public Evolution build() throws MissingInformation {
            if (Constants.inputNum == -1 || Constants.outputNum == -1 || Constants.numSimulated == -1 || Constants.outputAF == null || Constants.CostFunction == null)
                throw new MissingInformation();
            Constants.defaultValueInitializer = Activation.getInitializer(Constants.defaultHiddenAF, Constants.inputNum, Constants.outputNum);
            return new Evolution(Constants);
        }

        public static class MissingInformation extends RuntimeException {
            @Override
            public String getMessage() {
                return "Missing Resources in EvolutionBuilder class";
            }
        }
    }
}
