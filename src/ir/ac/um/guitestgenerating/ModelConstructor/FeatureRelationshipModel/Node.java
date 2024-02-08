package ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel;

import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private EventHandlerInformation eventHandler;
    private List<Node> adjacencyList;
    private int keyOfLabel;
    private boolean flag;
    private int numBack;

    public Node(){
        eventHandler = null;
        adjacencyList = new ArrayList<>();
        keyOfLabel = -1;
        flag = false;
        numBack = 0;

    }

    public void setEventHandler(EventHandlerInformation eventHandler){
        this.eventHandler = eventHandler;
    }

    public void setKeyOfLabel(int key){
        this.keyOfLabel = key;
    }

    public void setFlag(){
        this.flag = true;
    }

    public int getKeyOfLabel(){ return keyOfLabel;}

    public boolean getFlag(){ return flag;}

    public void addIntoAdjacencyList(Node tmp){
        adjacencyList.add(tmp);
    }

    public boolean isAdjacency(Node tmpNode){
        boolean result = false;
        for(Node node : adjacencyList){
            if(node == tmpNode){
                result = true;
                break;
            }
        }
        return result;
    }

    public List<Node> getAdjacencyList(){ return adjacencyList; }

    public EventHandlerInformation getEventHandler(){ return eventHandler;}

    public void setTitle(String root) {
        eventHandler.setTitle(root);
    }

    public void setSourceActivity(String activityName) {
        eventHandler.setSourceActivity(activityName);
    }

    public void setTargetActivity(String activityName) {
        eventHandler.setTargetActivity(activityName);
    }

    public String getSourceActivity() {
        return  eventHandler.getSourceActivity();
    }

    public String getTargetActivity() {
        return eventHandler.getTargetActivity();
    }

    public void increamentNumBack(){
        numBack++;
    }

    public int getNumBack(){
        return  numBack;
    }
}
