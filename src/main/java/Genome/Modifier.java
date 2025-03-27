package Genome;

import Genome.enums.Activation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/** Package-private Static class that adds synapses and nodes to any given {@link NN} */
class Modifier {

    /**
     * Adds an edge connecting the nodes at index {@code i1} and {@code i2}
     * @return true if this operation is successful, false otherwise
     * @apiNote This operation fails if any of the following is true:
     * <br>- i1 is an output node
     * <br>- i2 is an input node
     * <br>- i1 and i2 are the same node
     * <br>- this edge introduces a cycle
     * <br>- there is already a non-disabled edge connecting the two nodes
     */
    public static boolean addEdge(NN nn, double weight, int i1, int i2) {
        if (i2 < nn.Constants.getInputNum() || i1 >= nn.nodes.size() - nn.Constants.getOutputNum() ||
                i1 == i2 || (i1 > i2 && isLooping(i1, i2, nn)))
            return false;

        node n1 = nn.nodes.get(i1), n2 = nn.nodes.get(i2);
        int edgeIID = Innovation.getEdgeInnovationID(n1.getInnovationID(), n2.getInnovationID());

        edge newEdge = new edge(edgeIID, weight, true, n1.getInnovationID(), n2.getInnovationID());
        newEdge.prevIndex = i1;
        newEdge.nextIndex = i2;

        if (!addEdge(nn, newEdge, n1, n2)) return false;

        // if i1 > i2, topologically sort the entire NN
        // if i1 < i2 no sorting required
        if (i1 > i2) {
            ArrayList<node> newNodes = Innovation.topologicalSort(nn.nodes, nn.genome, nn.Constants);
            nn.nodes.clear();
            nn.nodes.addAll(newNodes);
        }
        Innovation.resetNodeCoords(nn);

        assert nn.classInv() : nn.toString();
        return true;
    }

    /**
     * Splits the edge at index {@code edgeIndex} into two edges and a hidden node
     * @return true if this operation is successful, false otherwise
     * @apiNote This operation fails if any of the following is true:
     * <br>- the selected edge is disabled
     * <br>- the edge split somehow returned a repeated edge that already exists in {@code nn}
     */
    public static boolean splitEdge(NN nn, double bias, Activation AF, int edgeIndex) {
        edge edge = nn.genome.get(edgeIndex);
        if (edge.isDisabled()) return false;

        node newNode = new node(Innovation.getSplitNodeInnovationID(edge.innovationID, nn.nodes), AF, bias);

        node prevNode = nn.nodes.get(edge.prevIndex), nextNode = nn.nodes.get(edge.nextIndex);
        int prevIID = prevNode.getInnovationID(), midIID = newNode.getInnovationID(), nextIID = nextNode.getInnovationID();
        edge edge1 = new edge(Innovation.getEdgeInnovationID(prevIID, midIID), edge.getWeight(), true, prevIID, midIID);
        edge edge2 = new edge(Innovation.getEdgeInnovationID(midIID, nextIID), 1, true, midIID, nextIID);

        //add edges to genome
        if (!addEdge(nn, edge1, prevNode, newNode) || !addEdge(nn, edge2, newNode, nextNode))
            return false;

        edge.enabled = false;

        //new node index can't be in between input node indexes
        int newNodeIndex = Math.max(edge.prevIndex + 1, nn.Constants.getInputNum());

        //increase every edge's nodeIndex by one if their index is after the new node index
        for (edge e : nn.genome) {
            if (e.prevIndex >= newNodeIndex) e.prevIndex++;
            if (e.nextIndex >= newNodeIndex) e.nextIndex++;
        }
        nn.nodes.add(newNodeIndex, newNode);

        edge1.prevIndex = edge.prevIndex;
        edge2.nextIndex = edge.nextIndex;
        edge1.nextIndex = newNodeIndex;
        edge2.prevIndex = newNodeIndex;

        Innovation.resetNodeCoords(nn);

        assert nn.classInv() : nn.toString();
        return true;
    }

    /**
     * Attempts to add an edge to {@code nn}
     * <br>Effect: Inserts the edge in a sorted manner into nn's genome,
     * re-indexes every node's local reference to their edges after that operation.
     * @param n1 the source node of newEdge
     * @param n2 the destination node of newEdge
     * @return false if nn's genome already contains an identical edge to {@code newEdge}, true otherwise
     */
    private static boolean addEdge(NN nn, edge newEdge, node n1, node n2) {
        int edgeIndex = 0;
        //attempts to insert newEdge at appropriate index and shift node's local references
        //sets edgeIndex to the first index where the edge has
        // either the same or a larger InnovationID than newEdge, or nn.genome.size() if
        // it has the largest InnovationID
        for (; edgeIndex < nn.genome.size(); edgeIndex++) {
            if (nn.genome.get(edgeIndex).innovationID == newEdge.innovationID) {
                //replace disabled edges with new edge
                if (nn.genome.get(edgeIndex).isDisabled()) {
                    nn.genome.set(edgeIndex, newEdge);
                    n1.getOutgoingEdges().set(n1.getOutgoingEdges().indexOf(newEdge), newEdge);
                    n2.getIncomingEdges().set(n2.getIncomingEdges().indexOf(newEdge), newEdge);
                    return true;
                } else return false;
            } else if (nn.genome.get(edgeIndex).innovationID > newEdge.innovationID) break;
        }
        nn.genome.add(edgeIndex, newEdge);

        n1.addOutgoingEdge(newEdge);
        n2.addIncomingEdge(newEdge);
        return true;
    }

    /** Returns whether introducing an edge from {@code rootNodeIndex} to {@code newEdgeNodeIndex} will introduce a cycle */
    private static boolean isLooping(int rootNodeIndex, int newEdgeNodeIndex, NN nn) {
        HashSet<Integer> visitedNodes = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(newEdgeNodeIndex);
        visitedNodes.add(newEdgeNodeIndex);
        while (!queue.isEmpty()) {
            int nodeIndex = queue.remove();
            for (edge e : nn.nodes.get(nodeIndex).getOutgoingEdges()) {
                int nextNode = e.nextIndex;
                if (nextNode == rootNodeIndex) return true;
                if (!visitedNodes.contains(nextNode)) queue.add(nextNode);
            }
            visitedNodes.add(nodeIndex);
        }

        return false;
    }
}
