package Evolution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import Constants.Constants;
import Genome.node;
import Genome.synapse;
import World.World;
import globalGenomes.*;
import Constants.Constants.NeuralNet;

public class NN implements Serializable {

    public ArrayList<synapse> synapses;
    public ArrayList<node> nodes;
    public transient globalInnovations globalInnovations;
    public transient globalNodes globalNodes;
    public double[] output;
    private int[] countedInputs;

    public NN(){
        this.globalInnovations = World.globalInnovations;
        this.globalNodes = World.globalNodes;
        this.synapses = new ArrayList<synapse>();
        this.nodes = new ArrayList<node>();
        for(int i = 0;i < NeuralNet.inputNum + NeuralNet.outputNum;i++)nodes.add(globalNodes.get(i));

    }

    public static NN godGenome(){
        NN NN = new NN();
        ArrayList<node> nodes = NN.nodes;
        int inputNum = NeuralNet.inputNum;

        //turn to bush and corpses
        //2,8,14,20
        for(int i : new int[]{8,14})
            NN.addSynapse(new synapse(nodes.get(i),nodes.get(inputNum+2),0.5,true, World.globalInnovations.getInnovationNum(nodes.get(i),nodes.get(inputNum+2))));

        //3,9,15,21
        for(int i : new int[]{9,15}) {
            NN.addSynapse(new synapse(nodes.get(i), nodes.get(inputNum+3), 0.5, true, World.globalInnovations.getInnovationNum(nodes.get(i), nodes.get(inputNum+3))));
        }

        //connect bush angle to inhibit forward movement  2,3,8,9,14,15,20,21
        for(int i : new int[]{8,9,14,15}) {
            NN.addSynapse(new synapse(nodes.get(i),nodes.get(inputNum), -0.5, true, World.globalInnovations.getInnovationNum(nodes.get(i), nodes.get(inputNum))));
        }

        //connect bush and corpse distance to eat and mate
        for(int i : new int[]{inputNum+4,inputNum+6})
            for(int j : new int[]{7,13})
                NN.addSynapse(new synapse(nodes.get(j),nodes.get(i), -0.4, true, World.globalInnovations.getInnovationNum(nodes.get(j), nodes.get(i))));

        //connect stomach fullness input to mate output
        NN.addSynapse(new synapse(nodes.get(32),nodes.get(4+inputNum),0.4,true, World.globalInnovations.getInnovationNum(nodes.get(27),nodes.get(4+inputNum))));

        //connect speed to activate forward movement
        NN.addSynapse(new synapse(nodes.get(25),nodes.get(inputNum),-0.3,true, World.globalInnovations.getInnovationNum(nodes.get(25),nodes.get(inputNum))));

        //connect stomach fullness to digestion Rate
        NN.addSynapse(new synapse(nodes.get(32),nodes.get(inputNum+7),1,true, World.globalInnovations.getInnovationNum(nodes.get(32),nodes.get(inputNum+7))));

        //connect clock to eat
        NN.addSynapse(new synapse(nodes.get(34),nodes.get(inputNum+6), -1, true, World.globalInnovations.getInnovationNum(nodes.get(34), nodes.get(inputNum+6))));

        //connect stomach fullness to regen health
        NN.addSynapse(new synapse(nodes.get(32),nodes.get(inputNum+5),0.6,true, World.globalInnovations.getInnovationNum(nodes.get(32),nodes.get(inputNum+5))));

        NN.countInputs();
        return NN;
    }
    public static NN devGenome(){
        NN NN = new NN();
        ArrayList<node> nodes = NN.nodes;
        int inputNum = NeuralNet.inputNum;

        //change to separation testing now
        NN.addSynapse(new synapse(nodes.get(25),nodes.get(inputNum),-0.3,true, World.globalInnovations.getInnovationNum(nodes.get(25),nodes.get(inputNum))));
        NN.addSynapse(new synapse(nodes.get(31),nodes.get(inputNum+8),100,true, World.globalInnovations.getInnovationNum(nodes.get(31),nodes.get(inputNum+8))));

        NN.countInputs();
        return NN;
    }
    public static NN randomGenome(){
        NN NN = new NN();

        ArrayList<synapse> synapses = godGenome().synapses;
        for(int i=0;i< Constants.CreatureConstants.start.startingSynapses; i++){
            NN.addSynapse(synapses.remove((int)(Math.random()*synapses.size())));
        }

        for(int i : new int[]{4,5,6,7}){
            boolean found = false;
            for(synapse s : NN.synapses) if(s.to.equals(NN.nodes.get(i+NeuralNet.inputNum))) {
                found = true;
                break;
            }
            if(!found){
                int nodeID = (int)(Math.random()*NeuralNet.inputNum);
                NN.addSynapse(new synapse(NN.nodes.get(nodeID), NN.nodes.get(i+NeuralNet.inputNum),Math.random()*4-2,true, World.globalInnovations.getInnovationNum(NN.nodes.get(nodeID), NN.nodes.get(i+NeuralNet.inputNum))));
            }
        }

        for (int i = 0; i < 5; i++) {
            if (Math.random() < NeuralNet.mutationWeightShiftProbability * 3) NN.mutateSynapse();
            if (Math.random() < NeuralNet.mutationNodeProbability / 2) NN.mutateNode();
            if (Math.random() < NeuralNet.mutationWeightShiftProbability * 3) NN.shiftWeights(NeuralNet.mutationWeightShiftStrength);
            if (Math.random() < NeuralNet.mutationWeightRandomProbability * 3) NN.randomWeights(NeuralNet.mutationWeightRandomStrength);
            if (Math.random() < NeuralNet.mutationBiasShiftProbability * 3) NN.shiftBias(NeuralNet.mutationBiasShiftStrength);
            if (Math.random() < NeuralNet.mutationNodeAFProbability) NN.changeAF();
            if (Math.random() < 0.3) i--;
        }

        NN.countInputs();
        return NN;
    }

    public static void calculateWeightedOutput(NN NN, double... input){
        if (input.length!= NeuralNet.inputNum){
            return;
        }

        int[] countInputs = new int[NN.nodes.size()];

        if(NeuralNet.batchNormalizeInputs) batchNormalization(input);

        ArrayList<synapse> temp = new ArrayList<synapse>(NN.synapses);
        for(int i=0;i<temp.size();i++) if(!temp.get(i).enabled)temp.remove(i--);
        ArrayList<node> scanLayer = new ArrayList<node>();
        for(int i=0;i<NeuralNet.inputNum;i++){
            node n = NN.nodes.get(i);
            if(!n.isInput()){ //failsafe
                System.out.println("ERROR AT ID 1");
                return;
            }
            scanLayer.add(n);
            n.latestOutput = input[n.innovationID];
            n.latestInputSum = input[n.innovationID];
        }
        ArrayList<synapse> bank = new ArrayList<synapse>();
        double[] outputList = new double[NeuralNet.outputNum];
        Arrays.fill(outputList, Double.NaN);
        while (!scanLayer.isEmpty()){
            ArrayList<synapse> layerConnectedSynapse = new ArrayList<synapse>();
            ArrayList<node> nextScan = new ArrayList<node>();
            for(int i=0;i<temp.size();i++){
                synapse scan = temp.get(i);
                if(scanLayer.contains(scan.from)){
                    scan.latestInput=scan.from.latestOutput;
                    int toIndex = NN.getNodeIndex(scan.to);
                    if(++countInputs[toIndex]==NN.countedInputs[toIndex]&& !nextScan.contains(scan.to)){
                        nextScan.add(scan.to);
                        layerConnectedSynapse.add(temp.remove(i));
                        for(int j=0;j<bank.size();j++){
                            if(bank.get(j).to.equals(scan.to)){
                                layerConnectedSynapse.add(bank.remove(j--));
                            }
                        }
                    }else{
                        bank.add(temp.remove(i));
                    }
                    i--;
                }
            }

            if(NeuralNet.batchNormalizeHiddenLayers) batchNormalization(layerConnectedSynapse);//normalize output of previous layer

            for (node n : nextScan) {
                n.latestInputSum=0;
                for (int j = 0; j < layerConnectedSynapse.size(); j++) {
                    if (layerConnectedSynapse.get(j).to.equals(n)) {
                        n.latestInputSum += layerConnectedSynapse.get(j).latestInput * layerConnectedSynapse.remove(j--).weight;
                    }
                }
                n.latestInputSum += n.bias;
            }
            for(node n : nextScan){
                if(n.isOutput()){
                    outputList[n.innovationID-NeuralNet.inputNum]=n.latestInputSum;
                }else{
                    n.latestOutput = activationFunction(n.latestInputSum,n.activationFunction);
                }
            }
            for(int i=nextScan.size()-1;i>=0;i--)if(nextScan.get(i).isOutput())nextScan.remove(i);
            scanLayer = nextScan;
        }

        if(NeuralNet.outputAF.equalsIgnoreCase("softmax")){
            outputList = softmaxActivationFunction(outputList);
        }else{
            for(int i = 0; i< NeuralNet.outputNum; i++){
                if(Double.isNaN(outputList[i])){
                    outputList[i] = 0;
                }else {
                    outputList[i] = activationFunction(outputList[i], NeuralNet.outputAF);
                }
            }
        }
        NN.output = outputList;
    }

    private static double activationFunction(double num,String activationFunction ){
        if(activationFunction.equalsIgnoreCase("none")){
            return num;
        }else if(activationFunction.equalsIgnoreCase("relu")){
            return (num > 0) ? num : 0;
        }else if(activationFunction.equalsIgnoreCase("sigmoid")){
            return 1/(1+Math.pow(Math.E,-num));
        }else if(activationFunction.equalsIgnoreCase("tanh")){
            return (Math.pow(Math.E,num)-Math.pow(Math.E,-num))/(Math.pow(Math.E,num)+Math.pow(Math.E,-num));
        }else if(activationFunction.equalsIgnoreCase("leaky relu")){
            return Math.max(num,0.1*num);
        }
        System.out.println("ERROR");
        return num;
    }
    private static double[] softmaxActivationFunction(double[] nums){
        double sum=0;
        double[] result = new double[nums.length];
        for (double num : nums) {
            sum += num;
        }
        for(int i=0;i<nums.length;i++){
            result[i]= sum==0 ? 0 : nums[i]/sum;
        }
        return result;
    }

    private static void batchNormalization(double[] input){
        int len = input.length;
        double sum=0;
        for (double val : input) {
            sum += val;
        }
        double mean = sum/len;
        sum=0;
        for (double val : input) {
            double temp = val - mean;
            sum += temp * temp;
        }
        double ISD = (sum==0.0 ? 1 : 1/Math.sqrt(sum/len));
        for(int i=0;i<len;i++){
            input[i]=(input[i]-mean)*ISD;
        }
    }
    private static void batchNormalization(ArrayList<synapse> layerConnectedSynapse){
        double[] input = new double[layerConnectedSynapse.size()];
        for(int i=0;i<layerConnectedSynapse.size();i++)input[i]=layerConnectedSynapse.get(i).latestInput;
        batchNormalization(input);
        for(int i=0;i<layerConnectedSynapse.size();i++)layerConnectedSynapse.get(i).latestInput=input[i];
    }

    public double compare(NN other){
        if(this.synapses.isEmpty()&&other.synapses.isEmpty())return 0;
        NN maxInnoNet = this;
        NN minInnoNet = other;
        int maxInnoNum = maxInnoNet.synapses.isEmpty() ? 0 : maxInnoNet.synapses.get(maxInnoNet.synapses.size()-1).innovationID;
        int minInnoNum = minInnoNet.synapses.isEmpty() ? 0 : minInnoNet.synapses.get(minInnoNet.synapses.size()-1).innovationID;
        if(maxInnoNum<minInnoNum){
            maxInnoNet = other;
            minInnoNet = this;
        }

        int index1 = 0,index2 = 0;

        int disjoint = 0,excess,similar = 0;
        double weight_diff = 0;


        while(index1 < maxInnoNet.synapses.size() && index2 < minInnoNet.synapses.size()){

            synapse gene1 = maxInnoNet.synapses.get(index1);
            synapse gene2 = minInnoNet.synapses.get(index2);

            int firstInnovationID = gene1.innovationID;
            int secondInnovationID = gene2.innovationID;

            if(firstInnovationID == secondInnovationID){
                //similargene
                similar ++;
                weight_diff += Math.abs(gene1.weight - gene2.weight);
                index1++;
                index2++;
            }else if(firstInnovationID > secondInnovationID){
                //disjoint gene of b
                disjoint ++;
                index2++;
            }else{
                //disjoint gene of a
                disjoint ++;
                index1 ++;
            }
        }

        weight_diff /= Math.max(1,similar);
        excess = maxInnoNet.synapses.size() - index1;

        double N = Math.max(maxInnoNet.synapses.size(),minInnoNet.synapses.size());
        if(N < 20) N = 1;

        return NeuralNet.weightedDisjoints * disjoint / N + NeuralNet.weightedExcess * excess / N + NeuralNet.weightedWeights * weight_diff;
    }
    public static NN crossover(NN first, NN second, double firstScore, double secondScore){
        NN NN = new NN();

        int index1 = 0,index2 = 0;
        boolean equalScore = firstScore==secondScore;
        if(firstScore<secondScore){
            NN temp = first;
            first = second;
            second = temp;
        }

        while(index1 < first.synapses.size() && index2 < second.synapses.size()){

            synapse gene1 = first.synapses.get(index1);
            synapse gene2 = second.synapses.get(index2);

            int firstInnovationID = gene1.innovationID;
            int secondInnovationID = gene2.innovationID;

            if(firstInnovationID == secondInnovationID){
                if(Math.random() > 0.5){
                    NN.synapses.add(gene1.clone());
                }else{
                    NN.synapses.add(gene2.clone());
                }
                index1++;
                index2++;
            }else if(firstInnovationID > secondInnovationID){
                if(equalScore) NN.synapses.add(gene2.clone());
                //disjoint gene of b
                index2++;
            }else{
                //disjoint gene of a
                NN.synapses.add(gene1.clone());
                index1++;
            }
        }

        while(index1 < first.synapses.size()){
            synapse gene1 = first.synapses.get(index1);
            NN.addSynapse(gene1.clone());
            index1++;
        }
        if(equalScore){
            while(index2 < second.synapses.size()){
                synapse gene2 = second.synapses.get(index2);
                NN.addSynapse(gene2.clone());
                index2++;
            }
        }

        for(synapse s : NN.synapses){
            s.from = NN.addNode(s.from.clone());
            s.to = NN.addNode(s.to.clone());
        }

//        if(genome.nodes.size() < first.inputNum+first.outputNum){
//            System.out.println(first.nodes+"\n"+first.synapses+"\n\n"+second.nodes+"\n"+second.synapses);
//        }

        NN.countInputs();
        return NN;
    }

    public void addSynapse(synapse s){
        if(!synapses.contains(s)){
            if(synapses.isEmpty()||synapses.get(synapses.size()-1).innovationID<s.innovationID){
                synapses.add(s);
            }else{
                for(int i=0;i<synapses.size();i++){
                    if(synapses.get(i).innovationID>s.innovationID){
                        synapses.add(i,s);
                        return;
                    }
                }
            }
        }
    }
    public node addNode(node n){
        if(!nodes.contains(n)){
            if(nodes.isEmpty()||nodes.get(nodes.size()-1).innovationID<n.innovationID){
                nodes.add(n);
                return n;
            }else{
                for(int i=0;i<nodes.size();i++){
                    if(nodes.get(i).innovationID>n.innovationID){
                        nodes.add(i,n);
                        return n;
                    }
                }
                nodes.add(n);
                return n;
            }
        }else{
            return nodes.get(nodes.indexOf(n));
        }
    }

    public int getNodeIndex(node n){
        int left = 0;
        int right = nodes.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            // Check if target is present at mid
            if (nodes.get(mid).innovationID == n.innovationID) {
                return mid;
            }

            // If target greater, ignore left half
            if (nodes.get(mid).innovationID < n.innovationID) {
                left = mid + 1;
            } else {
                // If target is smaller, ignore right half
                right = mid - 1;
            }
        }

        // Target was not present
        return -1;
    }

    @Override
    public String toString(){
        return "================================================================\nSynapses("+synapses.size()+"): "+synapses+"\nNodes("+nodes.size()+"): "+nodes+"\n================================================================";
    }

    public void mutate(){
        boolean mutated = false;
        if(Math.random() < NeuralNet.mutationSynapseProbability) {
            mutateSynapse();
            mutated = true;
        }
        if(Math.random() < NeuralNet.mutationNodeProbability){
            mutateNode();
            mutated = true;
        }
        if(Math.random() < NeuralNet.mutationWeightShiftProbability){
            shiftWeights(NeuralNet.mutationWeightShiftStrength);
            mutated = true;
        }
        if(Math.random() < NeuralNet.mutationWeightRandomProbability){
            randomWeights(NeuralNet.mutationWeightRandomStrength);
            mutated = true;
        }
        if(Math.random() < NeuralNet.mutationBiasShiftProbability){
            shiftBias(NeuralNet.mutationBiasShiftStrength);
            mutated = true;
        }
        if(Math.random() < NeuralNet.mutationNodeAFProbability){
            changeAF();
            mutated = true;
        }
        if(mutated) countInputs();
    }

    public void shiftWeights(double mutationStrength){
        if(synapses.isEmpty())return;
        synapse s = synapses.get((int)(Math.random()*synapses.size()));
        s.weight = s.weight*(Math.random()*mutationStrength);
    }

    public void randomWeights(double mutationStrength){
        if(synapses.isEmpty())return;
        synapse s = synapses.get((int)(Math.random()*synapses.size()));
        s.weight = (Math.random()*2-1)*mutationStrength;
    }

    public void shiftBias(double mutationStrength){
        if(nodes.isEmpty())return;
        for(int i=0;i<100;i++){
            node n = nodes.get((int)(Math.random()*nodes.size()));
            if(n.isOutput())continue;
            n.bias += (Math.random()*2-1)*mutationStrength;
            return;
        }
    }

    public void changeAF(){
        for(int i=0;i<100;i++){
            node n = nodes.get((int)(Math.random() * nodes.size()));
            if (!n.isHidden()){
                //failed to add
                continue;
            }
            n.activationFunction = NeuralNet.AFs[(int)(Math.random()*NeuralNet.AFs.length)];
            return;
        }
    }

    public void mutateSynapse(){
        for(int i=0;i<100;i++){
            node from=nodes.get((int)(Math.random() * nodes.size()));
            node to=nodes.get((int)(Math.random() * nodes.size()));
            if (from.equals(to) || from.isOutput() || to.isInput() || isLooping(from, to)){
                //failed to add
                continue;
            }
            if(synapses.contains(new synapse(from,to))){
                synapses.remove(new synapse(from,to));
                if(from.isHidden()){
                    boolean found = false;
                    for (synapse s : synapses)
                        if (s.from == from || s.to == from) {
                            found = true;
                            break;
                        }
                    if (!found) nodes.remove(from);
                }
                if(to.isHidden()){
                    boolean found = false;
                    for (synapse s : synapses)
                        if (s.from == to || s.to == to) {
                            found = true;
                            break;
                        }
                    if (!found) nodes.remove(to);
                }
            }else{
                int innovationID = globalInnovations.getInnovationNum(from,to);
                synapse newSynapse = new synapse(from, to,(Math.random()*2-1)* NeuralNet.mutationWeightRandomStrength,true,innovationID);
                addSynapse(newSynapse);
            }
            return;
        }
    }

    public void mutateNode(){
        if(synapses.isEmpty())return;
        for(int i=0;i<100;i++){
            synapse s = synapses.get((int)(Math.random()*synapses.size()));
            if(!s.enabled)continue;
            s.enabled=false;
            node newNode = globalInnovations.getSplitNode(s);
            addNode(newNode);
            addSynapse(new synapse(newNode,s.to,s.weight,true,globalInnovations.getInnovationNum(newNode,s.to)));
            addSynapse(globalInnovations.get(s.from,newNode));
            return;
        }
    }

    private boolean isLooping(node startingPoint, node to){
        int i=100;
        ArrayList<node> stack = new ArrayList<node>();
        stack.add(to);
        while (!stack.isEmpty()){
            if(--i<=0)return true;
            node toScan = stack.remove(0);
//            System.out.print(toScan+" ");
            if(toScan.equals(startingPoint)){
                return true;
            }
            if(!toScan.isOutput()){
                for (synapse synapse : synapses){
                    if (synapse.from.equals(toScan)) {
//                        System.out.print("("+synapse.from+"-"+synapse.to+"),");
                        stack.add(0,synapse.to);
                    }
                }
            }
        }
        return false;
    }

    private void countInputs(){
        countedInputs = new int[nodes.size()];

        for (synapse s : synapses) {
            if (s.enabled) {
                countedInputs[getNodeIndex(s.to)]++;
            }
        }
    }
}
