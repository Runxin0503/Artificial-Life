package Genome;

import java.io.Serializable;

public class node implements Serializable {
    public double bias;
    public String type;
    public double latestInputSum;
    public double latestOutput;
    public int innovationID;
    public double x,y;
    public String activationFunction;

    public node(String type, int innovationID,double x,double y){
        this.bias=0;
        this.type = type;
        this.innovationID = innovationID;
        this.x=x;
        this.y=y;
    }
    public node(String type, int innovationID,double x,double y,String activationFunction){
        this.bias=0;
        this.type = type;
        this.innovationID = innovationID;
        this.x=x;
        this.y=y;
        this.activationFunction = activationFunction;
    }

    private node(double bias,String type,int innovationID,double x,double y,String activationFunction){
        this.bias = bias;
        this.type = type;
        this.innovationID = innovationID;
        this.x=x;
        this.y=y;
        this.activationFunction = activationFunction;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof node))return false;
        return this.innovationID == ((node)other).innovationID;
    }

    public boolean isOutput(){
        return this.type.equals("output");
    }

    public boolean isInput(){
        return this.type.equals("input");
    }

    public boolean isHidden(){
        return this.type.equals("hidden");
    }


    @Override
    public node clone(){
        return new node(bias,type,innovationID,x,y,activationFunction);
    }

    @Override
    public String toString(){
        return "(" + (isOutput() ? "^" : (isInput() ? "v" : bias)) + ")";
    }
}
