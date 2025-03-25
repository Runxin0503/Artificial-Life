package Specs.NeuralNets;

import java.io.Serializable;
import Specs.Entity.AbstractCreature;

/**
 * Stores a collection of {@link AbstractSynapse} and {@link AbstractNode},
 * used as the decision-making center for the {@link AbstractCreature} class.<br>
 * Must have a sorted list of nodes and synapses
 */
public interface AbstractNN extends Serializable {

    /**
     * calculates the output of this neural network based on an array of input and the pre-existing weights, biases, and activation
     * function of the components
     */
    double[] calculateWeightedOutput(double[] input);

    /**
     * Normalizes the input array by calculating the mean & standard deviation and then performing the formula (x-mean) / SD on all
     * elements x in the input array
     */
    void batchNormalization(double[] input);

    /**
     * Returns the difference in genome between the two Neural Networks in the form of a double
     * Calculated through finding disjointed genes, excess genes, and difference in weight within identical genes of both Neural Networks
     */
    double compare(AbstractNN other);

    /** Mutates a few arbitrary components of this Neural Network */
    void mutate();

    /** Shifts an arbitrary synapse's weight by a random amount */
    void shiftWeights();

    /** Sets an arbitrary synapse's weight to a random double */
    void randomWeights();

    /** Shifts an arbitrary node's bias by a random amount */
    void shiftBias();

    /** Changes an arbitrary synapse's Activation Function to a random Activation Function */
    void changeAF();

    /** Add or remove an arbitrary synapse to this neural network */
    void mutateSynapse();

    /** Splits an arbitrary synapse in this neural network into two new synapses with a completely new node connected in the middle */
    void mutateNode();
}
