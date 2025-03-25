package Specs.NeuralNets;

import java.io.Serializable;
import java.util.*;
import Specs.Statics.*;

/**
 * Stores the data ({@link HashMap} and {@link ArrayList})
 * of static class {@link AbstractGlobalNodes} and {@link AbstractGlobalInnovations}
 */
public interface AbstractDatabase extends Serializable {
    /*
     *  Must have:
     *  - One HashMap for primitiveSynapses (mapping synapse id to synapse)
     *  - One HashMap for split nodes (mapping synapse id to node id)
     *  - One ArrayList for primitiveNodes (mapping node id to node)
     */

    /** Returns the HashMap of Primitive Synapses used in creating new synapses */
    HashMap<AbstractSynapse,AbstractSynapse> getPrimitiveSynapses();

    /** Returns the HashMap used in splitting synapses into two and adding primitive nodes */
    HashMap<AbstractSynapse,AbstractNode> getSplitNodes();

    /** Returns the ArrayList of Primitive Nodes used in creating new nodes */
    ArrayList<AbstractNode> getPrimitiveNodes();
}
