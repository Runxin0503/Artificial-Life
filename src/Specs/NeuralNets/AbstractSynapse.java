package Specs.NeuralNets;

import java.io.Serializable;

/**
 * A connection (Synapse) between two Nodes in a Neural Network.<br>
 * Stores the weight and Innovation ID of this specific synapse,
 * the two nodes this synapse connects to,
 * if this synapse is enabled (active) or not,
 * and the latest calculation performed by this synapse
 */
public interface AbstractSynapse extends Serializable,Comparable<AbstractSynapse> {

    /**
     * Returns the calculated output of input while keeping track of the latest input
     */
    double calculate(double input);

    /**
     * Returns the latest input of this synapse
     */
    double getLatestInput();

    /**
     * Shifts this synapse's weight by a random amount depending on mutationStrength
     */
    void mutateWeightShift(double mutationStrength);

    /**
     * Sets this synapse's weight to a random double depending on mutationStrength
     */
    void mutateWeightsRandom(double mutationStrength);

    /**
     * Compares the two synapses on their Innovation ID
     * @param other the synapse to be compared.
     * @return a negative integer, zero, or a positive integer if the Innovation ID of this is less than, equal to, or greater than other's
     */
    int compareTo(AbstractSynapse other);

    /**
     * Returns the difference in weight between the two synapses
     */
    double difference(AbstractSynapse other);

    /**
     * Returns false if o is not a synapse or has a different Innovation ID, true otherwise
     */
    boolean equals(Object o);

    /**
     * Returns the Innovation ID of this specific synapse as a HashCode
     */
    int HashCode();

    /**
     * Returns the exact clone of this specific synapse with all memory instance variable set to 0
     */
    AbstractSynapse clone();
}
