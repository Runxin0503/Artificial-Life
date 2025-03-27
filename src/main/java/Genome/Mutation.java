package Genome;

/** Package-private Static class that mutates the genome of any given {@link NN} */
class Mutation {

    /** Chooses a random synapse (if there is one) to shift its weight by a random amount */
    public static void shiftWeights(NN nn) {
        if (nn.genome.isEmpty()) return;
        for (int count = 0; count < 100; count++) {
            edge e = nn.genome.get((int) (Math.random() * nn.genome.size()));

            if (e.shiftWeights(nn.Constants.mutationWeightShiftStrength)) {
                assert nn.classInv() : nn.toString();
                return;
            }
        }
    }

    /** Chooses a random synapse (if there is one) to randomly set its weight */
    public static void randomWeights(NN nn) {
        if (nn.genome.isEmpty()) return;
        for (int count = 0; count < 100; count++) {
            edge e = nn.genome.get((int) (Math.random() * nn.genome.size()));

            if (e.randomWeights(nn.Constants.mutationWeightRandomStrength)) {
                assert nn.classInv() : nn.toString();
                return;
            }
        }
    }

    /** Chooses a random node to shift its bias by a random amount */
    public static void shiftBias(NN nn) {
        //randomly picks an index for all nodes except an input node
        int nodeIndex = (int) (Math.random() * (nn.nodes.size() - nn.Constants.getInputNum()) + nn.Constants.getInputNum());

        node n = nn.nodes.get(nodeIndex);
        n.shiftBias(nn.Constants);
        assert nn.classInv() : nn.toString();
    }

    /** Chooses a random node and selects a random, new Activation Function */
    public static void changeAF(NN nn) {
        //randomly picks an index for hidden nodes
        int nodeIndex = (int) (Math.random() * (nn.nodes.size() - nn.Constants.getInputNum() - nn.Constants.getOutputNum()) + nn.Constants.getInputNum());

        node n = nn.nodes.get(nodeIndex);
        n.changeAF();
        assert nn.classInv() : nn.toString();
    }

    /** Chooses two random nodes that aren't directly connected and create a synapse between them */
    public static void mutateSynapse(NN nn) {
        for (int count = 0; count < 100; count++) {
            int i1 = (int) (Math.random() * (nn.nodes.size() - nn.Constants.getOutputNum())), i2 = (int) (Math.random() * (nn.nodes.size() - nn.Constants.getInputNum())) + nn.Constants.getInputNum();

            if (Modifier.addEdge(nn, nn.Constants.getInitializedValue(), i1, i2)) return;
        }
    }

    /**
     * Chooses a random synapse (if there is one) and insert a new Node directly in the middle<br>
     * The two previously connected nodes will now connect through this new node.<br>
     * The previous synapse will be removed. Two new synapses will be created connecting the 3 nodes
     */
    public static void mutateNode(NN nn) {
        if (nn.genome.isEmpty()) return;
        for (int count = 0; count < 100; count++) {
            //any edge in the genome is valid for node splitting
            int i = (int) (Math.random() * nn.genome.size());

            if (Modifier.splitEdge(nn, nn.Constants.getInitializedValue(), nn.Constants.getDefaultHiddenAF(), i))
                return;
        }
    }
}
