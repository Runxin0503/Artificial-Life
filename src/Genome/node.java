package Genome;

import Constants.Constants.NeuralNet.*;

import java.io.Serializable;

public class node implements Serializable {
    public double bias;
    public Type type;
    public double latestInputSum;
    public double latestOutput;
    public int innovationID;
    public double x, y;
    public AF activationFunction;

    public node(Type type, int innovationID, double x, double y) {
        this.bias = 0;
        this.type = type;
        this.innovationID = innovationID;
        this.x = x;
        this.y = y;
    }

    public node(Type type, int innovationID, double x, double y, AF activationFunction) {
        this.bias = 0;
        this.type = type;
        this.innovationID = innovationID;
        this.x = x;
        this.y = y;
        this.activationFunction = activationFunction;
    }

    private node(double bias, Type type, int innovationID, double x, double y, AF activationFunction) {
        this.bias = bias;
        this.type = type;
        this.innovationID = innovationID;
        this.x = x;
        this.y = y;
        this.activationFunction = activationFunction;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof node)) return false;
        return this.innovationID == ((node) other).innovationID;
    }

    public boolean isOutput() {
        return this.type.equals(Type.output);
    }

    public boolean isInput() {
        return this.type.equals(Type.input);
    }

    public boolean isHidden() {
        return this.type.equals(Type.hidden);
    }


    @Override
    public node clone() {
        return new node(bias, type, innovationID, x, y, activationFunction);
    }

    @Override
    public String toString() {
        return "(" + (isOutput() ? "^" : (isInput() ? "v" : bias)) + ")";
    }
}
