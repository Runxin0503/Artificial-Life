package Specs.NeuralNets;

import java.io.Serializable;

/**
 * A Single Neuron (Node) in the Neural Network.<br>
 * Stores the bias, Innovation ID, Activation Function, and
 * the type of this specific node
 */
public interface AbstractNode extends Serializable,Comparable<AbstractNode> {

    /**
     * Mutates this node's bias by a random amount depending on mutationStrength
     */
    void mutateBias(double mutationStrength);

    /**
     * Mutates this node's Activation Function to an arbitrary activation function
     */
    void mutateAF();

    /**
     * Uses the latestInput instance and activation function instance variables to calculate the final output of this node while keeping
     * track of the latest output
     */
    double getOutput();

    /**
     * Compares the two nodes on their Innovation ID
     * @param other the node to be compared.
     * @return a negative integer, zero, or a positive integer if the Innovation ID of this is less than, equal to, or greater than other's
     */
    int compareTo(AbstractNode other);

    /**
     * Returns false if o is not a node or has a different ID, true otherwise
     */
    boolean equals(Object o);

    /**
     * Returns the unique ID of this specific Node as a HashCode
     */
    int HashCode();

    /**
     * Returns the exact clone of this specific Node with all memory instance variable set to 0
     */
    AbstractNode clone();
}
