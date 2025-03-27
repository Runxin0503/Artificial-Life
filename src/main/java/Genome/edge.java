package Genome;

import java.util.List;

/** Genome Blueprint for constructing the {@code sortedNetwork} of genome class */
class edge extends Gene {

    /*
     * must contain:
     * - innovationID
     * - weight
     * - enabled: If this edge is active or not
     * - index of previous and next node in topological order
     */

    /** The weight of this edge */
    double weight;

    /**
     * If false, this synapse should not be considered in forward-feeding / back-propagation calculations.
     * <br>This synapse can also not be split to make new nodes if false.
     */
    boolean enabled;

    /** The local indices of the previous and next node */
    public int prevIndex, nextIndex;

    /** The absolute Innovation ID of the previous and next node */
    private final int previousIID, nextIID;

    edge(int innovationID, double weight, boolean enabled, int previousIID, int nextIID) {
        this.innovationID = innovationID;
        this.weight = weight;
        this.previousIID = previousIID;
        this.nextIID = nextIID;
        this.enabled = enabled;
    }

    //todo made public for testing
    public edge(int innovationID, double weight, boolean enabled, int prevIndex, int nextIndex, int previousIID, int nextIID) {
        this.innovationID = innovationID;
        this.weight = weight;
        this.prevIndex = prevIndex;
        this.nextIndex = nextIndex;
        this.previousIID = previousIID;
        this.nextIID = nextIID;
        this.enabled = enabled;
    }

    /** Returns the weight of this edge */
//todo made public for testing
    public double getWeight() {
        return weight;
    }

    /** Returns true if this edge is disabled, false otherwise */
//todo made public for testing
    public boolean isDisabled() {
        return !enabled;
    }

    /** Returns the Innovation ID of the previous node */
    public int getPreviousIID() {
        return previousIID;
    }

    /** Returns the Innovation ID of the next node */
    public int getNextIID() {
        return nextIID;
    }

    @Override
    protected void addValue(double deltaValue) {
        this.weight += deltaValue;
    }

    /**
     * Shifts the weight of this edge by a random amount
     * @return false if this edge can't apply this mutation, true otherwise
     */
    public boolean shiftWeights(double mutationWeightShiftStrength) {
        if (!enabled) return false;
        this.weight *= Math.pow(mutationWeightShiftStrength, Math.random() * 2 - 1);
        return true;
    }

    /**
     * Sets the weight of this edge to a random number
     * @return false if this edge can't apply this mutation, true otherwise
     */
    public boolean randomWeights(double mutationWeightRandomStrength) {
        if (!enabled) return false;
        this.weight = mutationWeightRandomStrength * (Math.random() * 2 - 1);
        return true;
    }

    public edge clone(List<node> nodes) {
        return new edge(innovationID, weight, enabled, nodes.get(prevIndex).innovationID, nodes.get(nextIndex).innovationID);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof edge && ((edge) obj).innovationID == innovationID;
    }

    boolean identical(edge other) {
        return other.innovationID == innovationID && other.weight == weight && other.enabled == enabled &&
                other.prevIndex == prevIndex && other.nextIndex == nextIndex &&
                other.previousIID == previousIID && other.nextIID == nextIID;
    }
}
