package Genome;

import Evolution.Constants;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Static class containing mappings between node and synapses using their InnovationIDs
 */
class Innovation {
    /*
     * Must contain:
     * - A HashMap mapping synapses to pairs of nodes (creating a new synapse from two existing nodes)
     */

    /** Used to obtain the innovationID of the two nodes of any given synapse innovationID */
    private static final HashMap<intPairs, Integer> nodePairsToEdge = new HashMap<>();

    /** Used to obtain the node Innovation ID when split from a synapse */
    private static final HashMap<Integer, ArrayList<Integer>> edgeToSplitNode = new HashMap<>();

    private static int splitNodeInnovation = 0;

    /**
     * Returns the Innovation ID of the edge connecting {@code node1IID} and {@code node2IID}
     */
    public static int getEdgeInnovationID(int node1IID, int node2IID) {
        intPairs pair = new intPairs(node1IID, node2IID);
        Integer edgeIID = nodePairsToEdge.get(pair);
        if (edgeIID != null) return edgeIID;

        nodePairsToEdge.put(pair, nodePairsToEdge.size());
        return nodePairsToEdge.size() - 1;
    }

    public static int getSplitNodeInnovationID(int edgeIID, List<node> nodes) {
        edgeToSplitNode.putIfAbsent(edgeIID, new ArrayList<>());
        for (int i : edgeToSplitNode.get(edgeIID)) {
            if (!nodes.contains(new node(i, null, -1))) {
                return i;
            }
        }
        edgeToSplitNode.get(edgeIID).add(splitNodeInnovation++);
        return splitNodeInnovation;
    }

    /** Re-initializes all hidden nodes to its appropriate values */
    public static void resetNodeCoords(NN nn) {
        int inputNum = nn.Constants.getInputNum(), outputNum = nn.Constants.getOutputNum(), nodesNum = nn.nodes.size();
        for (int i = 0; i < inputNum; i++) {
            nn.nodes.get(i).x = 0;
            nn.nodes.get(i).y = (i + 1) / (inputNum + 1.0);
        }
        for (int i = nodesNum - outputNum; i < nodesNum; i++) {
            nn.nodes.get(i).x = 1;
            nn.nodes.get(i).y = (i - (nodesNum - outputNum) + 1) / (outputNum + 1.0);
        }

        for (int i = inputNum, j = nn.nodes.size() - outputNum - 1; i <= j; i++, j--) {
            node front = nn.nodes.get(i), back = nn.nodes.get(j);
            front.x = (i - inputNum + 1) / (nodesNum - inputNum - outputNum + 1.0);
            back.x = (j - inputNum + 1) / (nodesNum - inputNum - outputNum + 1.0);
            if (i == j) {
                assert front == back;
                //middle node, y = avg of all prev and next connected nodes
                double y = 0;
                for (edge e : front.getIncomingEdges())
                    y += nn.nodes.get(e.prevIndex).y;
                for (edge e : front.getOutgoingEdges())
                    y += nn.nodes.get(e.nextIndex).y;
                y /= front.getIncomingEdges().size() + front.getOutgoingEdges().size();
                front.y = y;
            } else {
                //1 sided node, y = avg of either prev or next connected nodes
                double frontY = 0, backY = 0;
                for (edge e : front.getIncomingEdges())
                    frontY += nn.nodes.get(e.prevIndex).y;
                for (edge e : back.getOutgoingEdges())
                    backY += nn.nodes.get(e.nextIndex).y;
                frontY /= front.getIncomingEdges().size();
                backY /= back.getOutgoingEdges().size();
                front.y = frontY;
                back.y = backY;
            }
        }
        assert nn.classInv();
    }

    /**
     * Creates a topologically sorted array of nodes from a list of edges (genomes) used for calculating
     * <br>The nodes should contain identical input/output nodes from {@code dominantNodes}
     * <br>It should also bind all node IDs in edges to the local node Index.
     */
    public static ArrayList<node> constructNetworkFromGenome(ArrayList<edge> genome, ArrayList<node> dominantNodes, ArrayList<node> submissiveNodes, Constants Constants) {
        ArrayList<node> nodes = new ArrayList<>();
        for (int i = 0; i < Constants.getInputNum(); i++)
            nodes.add(dominantNodes.get(i).clone());
        for (int i = -Constants.getOutputNum(); i < 0; i++)
            nodes.add(dominantNodes.get(dominantNodes.size() + i).clone());

        // Step 1:
        for (edge e : genome) {
            node u = getNodeByInnovationID(e.getPreviousIID(), dominantNodes, submissiveNodes),
                    v = getNodeByInnovationID(e.getNextIID(), dominantNodes, submissiveNodes);

            //puts u and v inside map. sets u and v to appropriate object
            int uIndex = nodes.indexOf(u);
            if (uIndex == -1) nodes.add(Constants.getInputNum(), u = u.clone());
            else u = nodes.get(uIndex);

            int vIndex = nodes.indexOf(v);
            if (vIndex == -1) nodes.add(Constants.getInputNum(), v = v.clone());
            else v = nodes.get(vIndex);

            u.addOutgoingEdge(e);
            v.addIncomingEdge(e);
        }

        for (int i = -Constants.getOutputNum(); i < 0; i++)
            assert nodes.get(nodes.size() + i).innovationID < 0 && nodes.get(nodes.size() + i).innovationID >= -Constants.getOutputNum();

        return topologicalSort(nodes, genome, Constants);
    }

    /**
     * Topologically sorts the {@code nodes} arrayList and returns the result.
     * <br>Requires: each node in {@code nodes} have correct references to their connected edges and vice versa,
     * also requires input nodes to be first, then hidden nodes, and lastly output nodes in {@code nodes}
     * @param genome has to have correct innovationID references to their connected nodes
     * @param nodes has to have correct indices references to their outgoing edges in {@code genome}
     * @return
     */
    public static ArrayList<node> topologicalSort(List<node> nodes, List<edge> genome, Constants Constants) {
        // Maps Nodes to the number of incoming edges (for topological sorted orders)
        Map<Integer, AtomicInteger> indegree = new HashMap<>();
        Map<Integer, node> innovationIDtoNode = new HashMap<>();

        nodes.forEach(n -> {
            indegree.put(n.innovationID, new AtomicInteger(n.getIncomingEdges().size()));
            innovationIDtoNode.put(n.innovationID, n);
        });

        // Step 2: Initialize the queue with all input nodes in order of innovation ID
        Queue<node> queue = new LinkedList<>();
        for (int i = 0; i < Constants.getInputNum(); i++) {
            queue.add(nodes.get(i));
        }

        // Step 3: Process the nodes
        ArrayList<node> topologicalOrder = new ArrayList<>();
        int outputNodeCount = 0;
        while (!queue.isEmpty()) {
            node n = queue.poll();
            topologicalOrder.add(n);

            // For each neighbor, reduce the indegree and add to queue if it becomes 0
            for (edge e : n.getOutgoingEdges()) {
                node nextNode = innovationIDtoNode.get(e.getNextIID());
                if (indegree.get(nextNode.innovationID).decrementAndGet() <= 0) {
                    //add only hidden nodes to queue
                    if (nextNode.innovationID >= 0)
                        queue.add(nextNode);
                    else outputNodeCount++;
                }
            }
        }

        assert outputNodeCount <= Constants.getOutputNum();

        for (int i = -Constants.getOutputNum(); i < 0; i++) topologicalOrder.add(nodes.get(nodes.size() + i));

        HashMap<Integer, Integer> innovationIDtoLocalIndex = new HashMap<>();
        for (int i = 0; i < topologicalOrder.size(); i++)
            innovationIDtoLocalIndex.put(topologicalOrder.get(i).innovationID, i);

        for (edge e : genome) {
            e.prevIndex = innovationIDtoLocalIndex.get(e.getPreviousIID());
            e.nextIndex = innovationIDtoLocalIndex.get(e.getNextIID());
        }

        return topologicalOrder;
    }

    /**
     * Attempts to scan through all nodes in {@code nodeLists} and return a node of the right Innovation ID
     * <br>If none are found, throws an unchecked Exception
     * <br>(NOTE: If two nodes have identical InnovationID, whichever is first in an iterator is returned)
     */
    @SafeVarargs
    private static node getNodeByInnovationID(int innovationID, List<node>... nodeLists) {
        for (List<node> nodes : nodeLists) {
            for (node n : nodes) {
                if (n.innovationID == innovationID) {
                    return n;
                }
            }
        }
        throw new RuntimeException("no nodes found");
    }

    /** A Memory efficient object wrapper for two ints */
    private static class intPairs {
        private final long combined;

        private intPairs(int num1, int num2) {
            this.combined = ((long) num1 << 32) | (num2 & 0xFFFFFFFFL);
        }

        private int first() {
            return (int) (combined >> 32);
        }

        private int second() {
            return (int) combined;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(combined);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof intPairs && combined == ((intPairs) obj).combined;
        }
    }
}
