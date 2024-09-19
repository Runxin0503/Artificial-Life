package globalGenomes;

import Genome.node;
import Genome.synapse;

import java.util.HashMap;

public class globalInnovations {
    private final HashMap<synapse, synapse> primitiveSynapses;
    public final HashMap<synapse, node> splitNode;
    private final globalNodes globalNodes;

    public globalInnovations(globalNodes globalNodes) {
        this.globalNodes = globalNodes;
        this.splitNode = new HashMap<synapse, node>();
        this.primitiveSynapses = new HashMap<synapse, synapse>();
    }

    public synapse get(node from, node to) {
        synapse s = new synapse(from, to);
        if (primitiveSynapses.containsKey(s)) return primitiveSynapses.get(s).clone();
        s.innovationID = primitiveSynapses.size();
        primitiveSynapses.put(s, s);
        return s.clone();
    }

    public int getInnovationNum(node from, node to) {
        synapse s = new synapse(from, to);
        if (primitiveSynapses.containsKey(s)) return primitiveSynapses.get(s).innovationID;
        s.innovationID = primitiveSynapses.size();
        primitiveSynapses.put(s, s);
        return primitiveSynapses.size() - 1;
    }

    public node getSplitNode(synapse s) {
//        System.out.println(splitNode);
        if (splitNode.get(s) != null) return splitNode.get(s);
        node n = globalNodes.add(s.from, s.to);
        splitNode.put(new synapse(s.from, s.to), n);
        return n;
    }

    public int size() {
        return primitiveSynapses.size();
    }
}
