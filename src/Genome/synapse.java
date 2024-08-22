package Genome;

import java.io.Serializable;

public class synapse implements Serializable {
    public double weight;
    public node from;
    public node to;
    public boolean enabled;
    public double latestInput;
    public int innovationID;

    public synapse(node from, node to){
        //primitive synapse declaration
        this.from = from;
        this.to = to;
        this.weight=1;
        this.enabled=true;
    }
    
    public synapse(node from, node to, int innovationID){
        this.weight=1;
        this.from=from;
        this.to=to;
        this.enabled=true;
        this.latestInput=0;
        this.innovationID = innovationID;
    }

    public synapse(node from, node to, double weight, boolean enabled, int innovationID){
        this.weight=weight;
        this.from = from;
        this.to = to;
        this.enabled=enabled;
        this.latestInput=0;
        this.innovationID = innovationID;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof synapse))return false;
        synapse s = (synapse)other;
        return this.from.equals(s.from)&&this.to.equals(s.to);
    }

    @Override
    public synapse clone(){
        return new synapse(from,to,weight,enabled,innovationID);
    }

    public String toString(){//(int)(weight*100)*1.0/100
        return (int)(weight*100)*1.0/100 + ":" + (enabled ? "("+from.innovationID+" -> "+to.innovationID+")" : "()");
    }
}
