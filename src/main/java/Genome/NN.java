package Genome;


import Evolution.Constants;
import Genome.enums.Activation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Neural Network that uses topological sort to minimize computational time and increase memory efficiency */
public class NN {

    /**
     * Stores a list of edges.
     * <br>Cannot ever decrease in size or have any genome removed
     * <br>Class Invariant: Sorted in InnovationID order
     */
    final ArrayList<edge> genome;

    /**
     * Stores a list of nodes.
     * <br>Cannot ever decrease in size or have any nodes removed.
     * <br>Class Invariant: Sorted in Topological order
     */
    final ArrayList<node> nodes;

    /** The number of input & output neurons in this Neural Network */
    final Constants Constants;

    /** The amount of times this Neural Network has been trained */
    private int t = 1;

    private NN(ArrayList<node> nodes, Constants Constants) {
        this.nodes = nodes;
        this.genome = new ArrayList<>();
        this.Constants = Constants;
    }

    private NN(ArrayList<node> nodes, ArrayList<edge> genome, Constants Constants) {
        this.genome = genome;
        this.nodes = nodes;
        this.Constants = Constants;
    }

    public static NN getDefaultNeuralNet(Constants Constants) {
        ArrayList<node> nodes = new ArrayList<>();

        //input nodes don't have biases or Activation Functions
        //output nodes don't have their own Activation Function because they have a global outputAF
        for (int i = -Constants.getInputNum() - Constants.getOutputNum(); i < -Constants.getOutputNum(); i++)
            nodes.add(new node(i, Activation.none, 0));
        for (int i = -Constants.getOutputNum(); i < 0; i++)
            //output nodes only use outputAF and not their own Activation Function
            nodes.add(new node(i, Activation.none, Constants.getInitializedValue()));

        NN nn = new NN(nodes, Constants);
        Innovation.resetNodeCoords(nn);

        assert nn.classInv();
        return nn;
    }

    /**
     * Returns a positive value denoting how similar the Genomes of both Neural Networks are<br>
     * Should be the same for if either Neural Network ran this method on each other<br>
     * EX: genome n1,n2; n1.compare(n2) == n2.compare(n1);
     */
    public double compare(NN other) {
        if (this.genome.isEmpty() && other.genome.isEmpty()) return 0;
        List<edge> maxInnoGenome = this.genome;
        List<edge> minInnoGenome = other.genome;
        if (maxInnoGenome.isEmpty() || (!minInnoGenome.isEmpty() && minInnoGenome.getLast().getInnovationID() > maxInnoGenome.getLast().getInnovationID())) {
            maxInnoGenome = other.genome;
            minInnoGenome = this.genome;
        }

        int index1 = 0, index2 = 0;

        int disjoint = 0, excess, similar = 0;
        double weight_diff = 0;


        while (index1 < maxInnoGenome.size() && index2 < minInnoGenome.size()) {

            edge gene1 = maxInnoGenome.get(index1);
            edge gene2 = minInnoGenome.get(index2);

            int firstInnovationID = gene1.getInnovationID();
            int secondInnovationID = gene2.getInnovationID();

            if (firstInnovationID == secondInnovationID) {
                //similargene
                similar++;
                weight_diff += Math.abs(gene1.getWeight() - gene2.getWeight());
                index1++;
                index2++;
            } else if (firstInnovationID > secondInnovationID) {
                //disjoint gene of b
                disjoint++;
                index2++;
            } else {
                //disjoint gene of a
                disjoint++;
                index1++;
            }
        }

        weight_diff /= Math.max(1, similar);
        excess = maxInnoGenome.size() - index1;

        double N = Math.max(maxInnoGenome.size(), minInnoGenome.size());
        if (N < 20) N = 1;

        return Constants.weightedDisjoints * disjoint / N + Constants.weightedExcess * excess / N + Constants.weightedWeights * weight_diff;

    }

    /**
     * Returns a new Neural Network as a result of crossover between the two given NNs<br>
     * Follows the NEAT Implementation of Innovation number matching
     */
    public static NN crossover(NN parent1, NN parent2, double firstScore, double secondScore) {
        assert parent1.Constants == parent2.Constants;//both parents should belong to the same Evolution object
        assert parent1.classInv() && parent2.classInv();

        int index1 = 0, index2 = 0;
        NN dominant = parent1, submissive = parent2;
        if (firstScore < secondScore) {
            dominant = parent2;
            submissive = parent1;
        }

        ArrayList<edge> newGenome = new ArrayList<>();
        while (index1 < dominant.genome.size() && index2 < submissive.genome.size()) {

            edge gene1 = dominant.genome.get(index1);
            edge gene2 = submissive.genome.get(index2);

            int firstInnovationID = gene1.getInnovationID();
            int secondInnovationID = gene2.getInnovationID();

            if (firstInnovationID == secondInnovationID) {
                newGenome.add(Math.random() > 0.5 ? gene1.clone(dominant.nodes) : gene2.clone(submissive.nodes));
                index1++;
                index2++;
            } else if (firstInnovationID > secondInnovationID) {
                //disjoint gene of b
                index2++;
            } else {
                //disjoint gene of a
                newGenome.add(gene1.clone(dominant.nodes));
                index1++;
            }
        }

        while (index1 < dominant.genome.size()) {
            edge gene1 = dominant.genome.get(index1);
            //if this loop is being run, dominant's last innovationID is greater than submissive's
            //so newGenome shouldn't have gene1 from dominant, and newGenome shouldn't be in submissive since
            //submissive's max innovationID should be less than gene1's innovationID
            assert !newGenome.contains(gene1);
            newGenome.add(gene1.clone(dominant.nodes));
            index1++;
        }
        //newGenome must be sorted by Innovation ID since they were inserted in order from smallest to biggest
        List<edge> test = new ArrayList<>(newGenome);
        test.sort(Comparator.comparingInt(edge::getInnovationID));
        assert test.equals(newGenome);

        ArrayList<node> newNodes = Innovation.constructNetworkFromGenome(newGenome, dominant.nodes, submissive.nodes, dominant.Constants);

        NN offspring = new NN(newNodes, newGenome, parent1.Constants);
        Innovation.resetNodeCoords(offspring);
        assert offspring.classInv();

        return offspring;
    }

    /** Calculates the weighted output of the values using the Neural Network currently in this Agent */
    public double[] calculateWeightedOutput(double[] input) {
        assert input.length == Constants.getInputNum();
        double[] calculator = new double[nodes.size()];
        Arrays.fill(calculator, Double.NaN);
        System.arraycopy(input, 0, calculator, 0, input.length);

        for (int i = 0; i < calculator.length; i++) {
            //if calculator array doesn't have any value at that node,
            //the node doesn't have an active incoming edge, so ignore that node
            if (Double.isNaN(calculator[i])) continue;
            node n = nodes.get(i);

            calculator[i] = n.activationFunction.calculate(n.bias + calculator[i]);
            for (edge e : n.getOutgoingEdges()) {
                if (e.isDisabled()) continue;
                int targetIndex = e.nextIndex;
                if (Double.isNaN(calculator[targetIndex])) calculator[targetIndex] = e.weight * calculator[i];
                else calculator[targetIndex] += e.weight * calculator[i];
            }
        }

        double[] output = new double[Constants.getOutputNum()];
        for (int i = calculator.length - output.length; i < calculator.length; i++) {
            if (Double.isNaN(calculator[i])) continue;
            output[i - calculator.length + output.length] = calculator[i];
        }
        return Constants.getOutputAF().calculate(output);
    }

    /**
     * Returns the loss of this Neural Network, or how far the expected output differs from the actual output.
     */
    public double calculateCost(double[] input, double[] expectedOutputs) {
        double[] output = calculateWeightedOutput(input);
        double sum = 0;

        for (double v : output) assert Double.isFinite(v);
        double[] costs = Constants.getCostFunction().calculate(output, expectedOutputs);

        for (double v : costs)
            sum += v;

        return sum;
    }

    /**
     * back-propagates the derivatives of the weights and biases through this Network, ignoring any
     * disconnected components during back-propagation
     */
    private void backPropagate(double[] input, double[] expectedOutput) {
        double[] a = new double[nodes.size()], z = new double[a.length], da_dCs = new double[nodes.size()];
        Arrays.fill(a, Double.NaN);
        System.arraycopy(input, 0, a, 0, input.length);

        for (int i = 0; i < a.length; i++) {
            //get first pass through to populate calculator with edge inputs (node AF outputs)
            //if calculator array doesn't have any value at that node,
            //the node doesn't have an active incoming edge, so ignore that node
            if (Double.isNaN(a[i])) continue;
            node n = nodes.get(i);

            z[i] = a[i] + n.bias;
            a[i] = n.activationFunction.calculate(z[i]);
            for (edge e : nodes.get(i).getOutgoingEdges()) {
                if (e.isDisabled()) continue;
                int targetIndex = e.nextIndex;
                if (Double.isNaN(a[targetIndex])) a[targetIndex] = e.weight * a[i];
                else a[targetIndex] += e.weight * a[i];
            }
        }

        double[] output = new double[Constants.getOutputNum()];
        for (int i = a.length - output.length; i < a.length; i++) {
            if (Double.isNaN(a[i])) continue;
            output[i - a.length + output.length] = a[i];
        }
        double[] outputActivation = Constants.getOutputAF().calculate(output);
        assert Arrays.equals(outputActivation, calculateWeightedOutput(input));

        double[] outputActivationGradients = Constants.getCostFunction().derivative(outputActivation, expectedOutput);
        double[] outputGradients = Constants.getOutputAF().derivative(output, outputActivationGradients);

        Arrays.fill(da_dCs, Double.NaN);
        System.arraycopy(outputGradients, 0, da_dCs, da_dCs.length - outputGradients.length, outputGradients.length);

        for (int i = da_dCs.length - 1; i >= Constants.getInputNum(); i--) {
            //reverse passing through to calculate final node derivatives
            //nodeGradients[i] contains da_dC, convert to dz_dC and update gradient of n and all incoming edges
            //then convert dz_dC to the da_dC of each node of the incoming edges
            //save da_dC to appropriate nodeGradients element
            node n = nodes.get(i);

            //if a[i] is NaN, the first pass didn't have any active incoming edges to
            //this node so skip this node, we check nodeGradients[i] because a[i] is NaN
            //for every node disconnected and beyond, but since we're traversing backwards we have to
            //skip all nodes that has only disconnected descendents
            if (Double.isNaN(a[i]) || Double.isNaN(da_dCs[i])) continue;
            //convert nodeGradient(da_dC) to dz_dC
            double dz_dC = n.activationFunction.derivative(z[i], da_dCs[i]);
            //db_dC = dz_dC since z = wx + b
            n.addGradient(dz_dC);

            for (edge e : n.getIncomingEdges()) {
                if (e.isDisabled()) continue;
                //dw_dC = dz_dC * x since z = w*x + b and x = a (prev node output)
                e.addGradient(dz_dC * a[e.prevIndex]);
                //da_dC of the previous node is just da_dC = dz_dC * w since z = w*x + b and x = a (prev node output)
                if (Double.isNaN(da_dCs[e.prevIndex]))
                    da_dCs[e.prevIndex] = dz_dC * e.getWeight();
                else da_dCs[e.prevIndex] += dz_dC * e.getWeight();
            }
        }
    }

    /** Re-initializes the weight and bias gradients, effectively setting all contained values to 0 */
    private void clearGradient() {
        for (node n : nodes) n.clearGradient();
        for (edge e : genome) e.clearGradient();
    }

    /**
     * Applies the {@link Gene}'s gradient to all nodes and edges in this Neural Network
     */
    private void applyGradient(double adjustedLearningRate, double momentum, double beta, double epsilon) {
        double correctionMomentum = 1 - Math.pow(momentum, t);
        double correctionBeta = 1 - Math.pow(beta, t);
        for (node n : nodes)
            n.applyGradient(adjustedLearningRate, momentum, correctionMomentum, beta, correctionBeta, epsilon);
        for (edge e : genome)
            e.applyGradient(adjustedLearningRate, momentum, correctionMomentum, beta, correctionBeta, epsilon);
        t++;
    }

    /**
     * "Trains" the given Neural Network class using the given inputs and expected outputs.
     * <br>Uses RMS-Prop as training algorithm, requires Learning Rate, beta, and epsilon hyper-parameter.
     * @param learningRate a hyper-parameter dictating how fast this Neural Network 'learn' from the given inputs
     * @param momentum a hyper-parameter dictating how much of the previous SGD velocity to keep. [0~1]
     * @param beta a hyper-parameter dictating how much of the previous RMS-Prop velocity to keep. [0~1]
     * @param epsilon a hyper-parameter that's typically very small to avoid divide by zero errors
     */
    public static void learn(NN NN, double learningRate, double momentum, double beta, double epsilon, double[][] testCaseInputs, double[][] testCaseOutputs) {
        assert testCaseInputs.length == testCaseOutputs.length;
        for (int i = 0; i < testCaseInputs.length; ++i)
            assert testCaseInputs[i].length == NN.Constants.getInputNum() && testCaseOutputs[i].length == NN.Constants.getOutputNum();

        //prevents other threads from calling learn on the same Neural Network
        synchronized (NN) {
            NN.clearGradient();

            Thread[] workerThreads = new Thread[testCaseInputs.length];
            for (int i = 0; i < testCaseInputs.length; i++) {
                double[] testCaseInput = testCaseInputs[i];
                double[] testCaseOutput = testCaseOutputs[i];
                workerThreads[i] = new Thread(null, () -> NN.backPropagate(testCaseInput, testCaseOutput), "WorkerThread");
                workerThreads[i].start();
            }

            for (Thread worker : workerThreads)
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            NN.applyGradient(learningRate / testCaseInputs.length, momentum, beta, epsilon);
        }
    }

    /** Randomly mutates this Neural Network */
    public void mutate() {
        if (!genome.isEmpty()) {
            if (Math.random() < Constants.mutationWeightShiftProbability) Mutation.shiftWeights(this);
            if (Math.random() < Constants.mutationWeightRandomProbability) Mutation.randomWeights(this);
            if (Math.random() < Constants.mutationNodeProbability) Mutation.mutateNode(this);
        }
        if (Math.random() < Constants.mutationBiasShiftProbability) Mutation.shiftBias(this);
        if (Math.random() < Constants.mutationSynapseProbability) Mutation.mutateSynapse(this);
        if (Math.random() < Constants.mutationChangeAFProbability) Mutation.changeAF(this);
    }

    @Override
    public Object clone() {
        ArrayList<node> newNodes = new ArrayList<>(nodes.size());
        ArrayList<edge> newGenome = new ArrayList<>(genome.size());
        for (edge e : genome) {
            edge newEdge = e.clone(nodes);
            newEdge.prevIndex = e.prevIndex;
            newEdge.nextIndex = e.nextIndex;
            newGenome.add(newEdge);
        }
        for (node n : nodes) {
            node newNode = n.clone();
            for (edge e : n.getIncomingEdges()) newNode.getIncomingEdges().add(newGenome.get(genome.indexOf(e)));
            for (edge e : n.getOutgoingEdges()) newNode.getOutgoingEdges().add(newGenome.get(genome.indexOf(e)));
            newNodes.add(newNode);
        }
        NN clone = new NN(newNodes, newGenome, Constants);
        Innovation.resetNodeCoords(clone);
        return clone;
    }

    /** Returns true if the class invariant of this instance is satisfied, false otherwise */
    public boolean classInv() {
        if (genome == null || nodes == null || nodes.isEmpty() ||
                nodes.size() < Constants.getInputNum() + Constants.getOutputNum() ||
                Constants.getInputNum() <= 0 || Constants.getOutputNum() <= 0)
            return false;
        //check node coordinates aren't both 0,0 (uninitialized)
        for (node n : nodes)
            if (n.x == n.y && n.y == 0)
                return false;
        //check input & output nodes are sorted in ascending IID order
        for (int i = -Constants.getOutputNum() - Constants.getInputNum(); i < -Constants.getOutputNum(); i++)
            if (nodes.get(i + Constants.getOutputNum() + Constants.getInputNum()).innovationID != i)
                return false;
        for (int i = -Constants.getOutputNum(); i < 0; i++)
            if (nodes.get(i + nodes.size()).innovationID != i)
                return false;
        //checks genome is sorted in increasing innovation ID
        for (int i = 1; i < genome.size(); i++)
            if (genome.get(i - 1).getInnovationID() >= genome.get(i).getInnovationID())
                return false;
        //checks edges and nodes have the correct local and absolute references to each other
        for (edge e : genome)
            if (nodes.get(e.prevIndex).innovationID != e.getPreviousIID() || nodes.get(e.nextIndex).innovationID != e.getNextIID())
                return false;
        for (node n : nodes) {
            for (edge e : n.getIncomingEdges()) {
                if (genome.get(genome.indexOf(e)) != e)
                    return false;
                node prevNode = nodes.get(e.prevIndex);
                if (e.getNextIID() != n.innovationID || !prevNode.getOutgoingEdges().contains(e) ||
                        prevNode.getOutgoingEdges().get(
                                prevNode.getOutgoingEdges().indexOf(e)) != e)
                    return false;
            }
            for (edge e : n.getOutgoingEdges()) {
                if (genome.get(genome.indexOf(e)) != e)
                    return false;
                node nextNode = nodes.get(e.nextIndex);
                if (e.getPreviousIID() != n.innovationID || !nextNode.getIncomingEdges().contains(e) ||
                        nextNode.getIncomingEdges().get(nextNode.getIncomingEdges().indexOf(e)) != e)
                    return false;
            }
        }

        //checks validity of nodes in sorted topological order
        for (edge e : genome)
            if (e.prevIndex >= e.nextIndex)
                return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        AtomicInteger count = new AtomicInteger();
        nodes.forEach(n -> {
            sb.append("[").append(count.getAndIncrement()).append("]");
            if (n.innovationID < -Constants.getOutputNum()) sb.append("Input Node (");
            else if (n.innovationID < 0) sb.append("Output Node (");
            else sb.append("Hidden Node (");
            sb.append(n.innovationID).append(",").append(n.activationFunction).append("):\t");
            sb.append(String.format("%.2f", n.x)).append(',').append(String.format("%.2f", n.y));
            sb.append("\n\t\t\tIncoming node indices - ");
            n.getIncomingEdges().forEach(e -> sb.append(e.prevIndex).append(','));
            sb.append("\n\t\t\tOutgoing node indices - ");
            n.getOutgoingEdges().forEach(e -> sb.append(e.nextIndex).append(','));
            sb.append('\n');
        });

        count.set(0);
        genome.forEach(e -> {
            sb.append("[").append(count.getAndIncrement()).append("]");
            sb.append("edge (").append(e.getInnovationID()).append(") from (").append(String.format("%.2f", nodes.get(e.prevIndex).x)).append(',').append(String.format("%.2f", nodes.get(e.prevIndex).y)).append(") to (");
            sb.append(String.format("%.2f", nodes.get(e.nextIndex).x)).append(',').append(String.format("%.2f", nodes.get(e.nextIndex).y)).append("), or ");
            sb.append(e.getPreviousIID()).append(" -> ").append(e.getNextIID()).append('\n');
        });

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NN nn))
            return false;
        if (nn.nodes.size() != nodes.size() || nn.genome.size() != genome.size() || nn.Constants != Constants)
            return false;

        for (int i = 0; i < nodes.size(); i++)
            if (!nn.nodes.get(i).identical(nodes.get(i)))
                return false;
        for (int i = 0; i < genome.size(); i++)
            if (!nn.genome.get(i).identical(genome.get(i)))
                return false;

        return nn.toString().equals(toString());
    }

    /** Returns the complexity score (sum of nodes and synapses) of this Neural Network */
    public int getComplexity() {
        return genome.size() + nodes.size();
    }
}
