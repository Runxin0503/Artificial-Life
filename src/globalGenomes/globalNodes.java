package globalGenomes;

import Genome.node;

import java.util.ArrayList;

import static Constants.Constants.NeuralNet.*;

public class globalNodes {
    private final ArrayList<node> primitiveNodes;

    public globalNodes() {
        primitiveNodes = new ArrayList<node>();
        for (int i = 0; i < inputNum; i++) primitiveNodes.add(new node("input", i, 0.1, (i + 1) / (inputNum + 1.0)));
        for (int i = inputNum; i < inputNum + outputNum; i++)
            primitiveNodes.add(new node("output", i, 0.9, (i + 1 - inputNum) / (outputNum + 1.0), outputAF));
    }

    public node get(int innovationID) {
        if (innovationID < primitiveNodes.size()) return primitiveNodes.get(innovationID).clone();
        System.out.println("ERROR ON GLOBALNODES.JAVA --- " + innovationID + "," + primitiveNodes);
        return null;
    }

    public node add(node from, node to) {
        node n = new node("hidden", primitiveNodes.size(), (from.x + to.x) / 2, (from.y + to.y) / 2 + Math.random() * 0.1 - 0.05, hiddenAF);
        primitiveNodes.add(n);
        return n;
    }

    public int size() {
        return primitiveNodes.size();
    }
}
