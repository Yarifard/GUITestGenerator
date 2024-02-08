package ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel;

import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

import javax.swing.*;
import java.util.List;

public class UpdateEventListThread extends Thread {

    List<ActivityInformation> activitesList;
    String selectedActivityName;
    DefaultListModel<String> list;
    public UpdateEventListThread(List<ActivityInformation> activitesList,String selectedActivityName,
                                 DefaultListModel<String> eventList){
        this.activitesList = activitesList;
        this.selectedActivityName = selectedActivityName;
        this.list = eventList;
    }

    public void run(){
        ActivityInformation activity;
        activity = findActivityByName(activitesList,selectedActivityName);
        List<EventHandlerInformation> events = activity.getMainFeaturesList();
        list.clear();
        for(EventHandlerInformation event:events)
            list.addElement(event.getTitle());
    }
    private  ActivityInformation findActivityByName(List<ActivityInformation> activitiesList,String selectedItem) {
        for(ActivityInformation activity : activitiesList)
            if(activity.getActivityName().equals(selectedItem))
                return activity;
        return null;
    }

}
