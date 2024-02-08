package ir.ac.um.guitestgenerating.Database;

import java.util.ArrayList;
import java.util.List;

public class Invariant {

    private int invardId;
    private String invariant;
    private int pointTypeId;
    private float priority;

    public Invariant(int pointTypeId,String invariant){
        if(invariant.contains("'"))
            this.invariant = invariant.replaceAll("'","''");
        else
            this.invariant = invariant;
        this.pointTypeId = pointTypeId;
    }

    public String getInvariant(){return invariant;}
    public int getId(){return invardId;}
    public int getPointTypeId(){return  pointTypeId;}
    public float getPriority() {return priority;}
    public void setInvardId(int invardId) {
        this.invardId = invardId;
    }

    public void setInvariant(String invariant) {
        this.invariant = invariant;
    }
    public void setPointTypeId(int pointTypeId){
        this.pointTypeId = pointTypeId;
    }
    public void setPriority(float priority) { this.priority = priority; }

}
