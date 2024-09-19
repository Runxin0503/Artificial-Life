package Evolution;

import Constants.Constants.NeuralNet;

import java.util.ArrayList;

public class Species {
    private final NN representative;
    public ArrayList<NN> NeuralNets = new ArrayList<NN>();

    public Species(NN representative) {
        this.representative = representative;
        this.NeuralNets.add(representative);
    }

    public boolean add(NN newNeuralNet) {
        if (representative.compare(newNeuralNet) < NeuralNet.compatibilityThreshold) {
            NeuralNets.add(newNeuralNet);
            return true;
        }
        return false;
    }

    public void reset() {

    }

    public void extinct() {

    }

    public void populateGenome(NN emptyNN) {

    }

}
