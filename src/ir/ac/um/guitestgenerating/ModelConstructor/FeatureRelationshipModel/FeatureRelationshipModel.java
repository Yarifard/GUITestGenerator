package ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel;

import com.intellij.psi.PsiClass;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.FeatureModel;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.util.*;
import java.util.List;

public class FeatureRelationshipModel {
    private FeatureModel featureModel;
    private Node rootNode;
    private List<PsiClass> processedActivity;
    private ProjectInformation projectInformation;

    public FeatureRelationshipModel(FeatureModel featureModel, ProjectInformation projectInformation){
        this.processedActivity = new ArrayList<>();
        this.featureModel = featureModel;
        this.projectInformation = projectInformation;
        rootNode = new Node();
    }

    private void initialize() {
        rootNode.setEventHandler(new EventHandlerInformation());
        rootNode.setTitle("root");
        rootNode.setSourceActivity(featureModel.getRootNode().getActivityName());
        rootNode.setTargetActivity(featureModel.getRootNode().getActivityName());
    }

    public void extractDependentEvent(EventHandlerInformation event, List<EventHandlerInformation> eventList){
        if(event.getDependentEventHandlersList().isEmpty()){
            if(!event.hasParent())
                return;
            else
                extractDependentEvent(event.getParentEventHandlerInformation(),eventList);
        } else{
             for(EventHandlerInformation eventItem:event.getDependentEventHandlersList()){
                 extractDependentEvent(eventItem,eventList);
                 if(!eventList.contains(eventItem))
                      eventList.add(eventItem);
             }
        }
//        if(!eventList.contains(event))
//            eventList.add(event);
    }

    private List<EventHandlerInformation> extractFeatureNames(String singleTestScenario) {
        List<EventHandlerInformation> eventsList = new ArrayList<>();
        EventHandlerInformation event;
        singleTestScenario = singleTestScenario.replace("<","");
        singleTestScenario = singleTestScenario.replace(">","");
        if(hasMoreFeatures(singleTestScenario)){
            String[] featureNames = singleTestScenario.split(",");
            for(int index = 0; index < featureNames.length; index++){
                featureNames[index] = featureNames[index].trim();
                event = findEventHandlerByName(featureNames[index]);
                extractDependentEvent(event,eventsList);
                eventsList.add(event);
            }
        }
        else{
             String featureName = singleTestScenario.trim();
             event = findEventHandlerByName(featureName);
             extractDependentEvent(event,eventsList);
             eventsList.add(event);
        }
        return eventsList;
    }

//    public List<EventHandlerInformation> extractAllMainFeaturesList(){
//
//        List<EventHandlerInformation> mainFeaturesList = new ArrayList<>();
//        List<ActivityInformation> activitiesList = new ArrayList<>();
//
//        activitiesList = featureModel.getProjectActivities();
//        for(ActivityInformation currentActivity: activitiesList){
//            mainFeaturesList.addAll(currentActivity.getMainFeaturesList());
//        }
//
//        return mainFeaturesList;
//    }

    private String extractActivityName(String featureName){
        String activityName = featureName.substring(0,featureName.lastIndexOf("."));
        return activityName.trim();
    }

    private String extractEventName(String featureName){
        String eventName = featureName.substring(featureName.lastIndexOf(".")+1);
        return eventName.trim();
    }

    private boolean isMatch(String source, String target){
        if(source.contentEquals(target))
            return true;
        return false;
    }

    private ActivityInformation findActivityClassByName(String activityName){
        List<ActivityInformation> actiivitiesNameist = featureModel.getProjectActivities();
        for(ActivityInformation activity : actiivitiesNameist)
            if(isMatch(activity.getActivityName(),activityName))
                return activity;
        return null;
    }

    private EventHandlerInformation findEventHandlerByName(String featureName) {
        String activityName = extractActivityName(featureName);
        String eventName = extractEventName(featureName);
        ActivityInformation activity = findActivityClassByName(activityName);
        List<EventHandlerInformation> eventList = activity.getMainFeaturesList();
        EventHandlerInformation findItem = null;
        for(EventHandlerInformation event : eventList){
            if(event.getTitle().contentEquals(eventName)){
               return event;
            }
        }
        return null;
    }

    private EventHandlerInformation findTargetEvent(String sourceActivity, String targetActivity) {
        ActivityInformation activityInformation = findActivityClassByName(sourceActivity);
        EventHandlerInformation targetEvent = null;
        for(EventHandlerInformation event : activityInformation.getMainFeaturesList()){
            if(event.isOpenActivity())
                if(event.getTargetActivity().contentEquals(targetActivity)){
                  // if(event.getDependentEventHandlersList().isEmpty()){
                        targetEvent = event;
                        break;
                   }
        }
        return targetEvent;
    }

    private String getKeyofShortestPathBetween(String sourceNode, String destinationNode,
                                               Stack<String> backStack) {

        ActivityInformation source,target;
        List<PsiClass> accessibleActivities;
        target = findActivityClassByName(destinationNode);
        String labelOfPath = "";

        source = findActivityClassByName(sourceNode);
        accessibleActivities = source.getAccessibleActivity();

        if(accessibleActivities.isEmpty())
            return labelOfPath;
        else{
            if(accessibleActivities.contains(target.getActivityClass())){
                EventHandlerInformation event = findTargetEvent(sourceNode,destinationNode);
//                if(event.hasParent()){
//                    EventHandlerInformation parentEvent = event.getParentEventHandlerInformation();
//                    labelOfPath += parentEvent.getKeyOfLabel() + ";";
//                }
                labelOfPath += event.getKeyOfLabel() + ";";
               // backStack.push(event.getTargetActivity());
            }
            else{
                int index = 0;
                while(index < accessibleActivities.size()){
                    labelOfPath += "/";
                    PsiClass newSource = accessibleActivities.get(index);
                    EventHandlerInformation event = findTargetEvent(sourceNode,newSource.getName());
//                    if(event.hasParent()){
//                        EventHandlerInformation parentEvent = event.getParentEventHandlerInformation();
//                        labelOfPath += parentEvent.getKeyOfLabel() + ";";
//                    }
                    labelOfPath += event.getKeyOfLabel() + ";";
                    backStack.push(newSource.getName());
                    String key = getKeyofShortestPathBetween(newSource.getName(),destinationNode,
                                                             backStack);
                    if(!key.isEmpty()){
                        labelOfPath += key + ";";
                        break;
                    }
                    labelOfPath = labelOfPath.substring(0,labelOfPath.lastIndexOf('/'));
                    index++;
                    backStack.pop();
                }

            }
        }
        return labelOfPath;
    }

    private int storeLabel(String eventsSequence){
        return this.projectInformation.setLabelInLabelsCollection(eventsSequence);
    }

    private int generateKeyLabelFrom(String keySequence) {
//        String[] keys = keySequence.split(",");
//        String eventSequence = "";
//        for(String key: keys){
//            key = key.replace("\n","");
//            if(key =="back")
//                eventSequence += "onView(isRoot()).perform(ViewActions.pressBack());\n";
//            else
//                eventSequence += getLabelByKey(Integer.parseInt(key));
//        }
        return storeLabel(keySequence);
    }

    private void processSingleTestScenario(String singleTestScenario,boolean flag) {
        List<EventHandlerInformation> featureNames = extractFeatureNames(singleTestScenario);
        String destinationActiviy, currentActivity;
        Node previousNode,nextNode;
        Stack<String> backStack = new Stack<>();
        previousNode = rootNode;
        backStack.push(previousNode.getSourceActivity());
        String labelOfPath;
        String mainActivity = projectInformation.getLuncherActivity().getName();

        for(EventHandlerInformation featureName:featureNames){
            labelOfPath = "";
            boolean isFound = false;
            nextNode = new Node();
            nextNode.setEventHandler(featureName);
            if(!flag && isLastItemOfList(featureNames,featureName))
                nextNode.setFlag();
            destinationActiviy = nextNode.getSourceActivity();

            while(!isMatch(backStack.peek(),mainActivity)){
              currentActivity = backStack.peek();
              if(!isMatch(currentActivity,destinationActiviy)){
                  labelOfPath +="Back();";
                  backStack.pop();
              }
              else{
                  isFound = true;
                  break;
              }
            }

            if(!isFound){
                currentActivity = backStack.peek();
                if(!isMatch(currentActivity,destinationActiviy)){
                    labelOfPath += getKeyofShortestPathBetween(currentActivity, destinationActiviy, backStack);
                    backStack.push(destinationActiviy);
                }

//                if(!isMatch(nextNode.getSourceActivity(),nextNode.getTargetActivity())){
//                    if(!isMatch(nextNode.getTargetActivity(),"back"))
//                        backStack.push(nextNode.getTargetActivity());
//                    else
//                        backStack.pop();
//                }
            }

            if(!isMatch(nextNode.getSourceActivity(),nextNode.getTargetActivity())){
                if(!isMatch(nextNode.getTargetActivity(),"back") &&
                   !nextNode.getTargetActivity().startsWith("back:"))
                     backStack.push(nextNode.getTargetActivity());
                else{
                    String returnActivity = backStack.pop();
                    //nextNode.setTargetActivity("back:" + returnActivity);
                }
            }

            if(!previousNode.isAdjacency(nextNode)){
                int key = generateKeyLabelFrom(labelOfPath);
                nextNode.setKeyOfLabel(key);
                previousNode.addIntoAdjacencyList(nextNode);
            }
            previousNode = nextNode;
        }

        if(!isMatch(previousNode.getTargetActivity(),projectInformation.getLuncherActivity().getName()))
            while(!isMatch(backStack.peek(),projectInformation.getLuncherActivity().getName())){
                backStack.pop();
                previousNode.increamentNumBack();
            }
    }

    private boolean isLastItemOfList(List<EventHandlerInformation> featureNames, EventHandlerInformation featureName) {
        if(featureNames.indexOf(featureName) == featureNames.size()-1)
            return true;
        return false;
    }

    private void extractComplexTestScenarios(String patterns) {
        String[] sequences = patterns.split("\n");
        for(int i = 0; i < sequences.length; i++){
//            if(sequences[i].contains("<"))
//               sequences[i] = sequences[i].substring(sequences[i].indexOf('<') + 1);
//            if(sequences[i].contains(">"))
//              sequences[i] = sequences[i].substring(0,sequences[i].indexOf('>'));
//            sequences[i] = sequences[i].trim();
            if(!sequences[i].isEmpty())
                projectInformation.appendIntoComplexTestScenario(sequences[i].trim());
        }
        //return sequences;
       // return projectInformation.getComplexTestScenariosPatterns();
    }

//    private void processGeneratedComplexTestScenarios(String complexTestSceenarios) {
//        List<String> complexTestScenariosSet = extractComplexTestScenarios(complexTestSceenarios);
//
////        for(int i = 0; i < complexTestScenariosSet.length; i++){
////            processSingleTestScenario(complexTestScenariosSet[i]);
////        }
//         for(String testScenario : complexTestScenariosSet)
//             processSingleTestScenario(testScenario);
//    }

    private boolean isFeasible(String patterns) {
        //TODO:: This method must be implemented concisely
        return true;
    }

    private  boolean hasMoreFeatures(String testScenario){
        if(testScenario.contains(","))
            return true;
        return false;
    }

    public void createFeatureRelationshipModel(){

        String complexTestScenarios = "";
        Window window = new Window();
        initialize();
        do{
            // List<EventHandlerInformation> mainFeaturesList = extractAllMainFeaturesList();
            complexTestScenarios = window.getTestSenarios(featureModel.getProjectActivities());
            //patterns = generatePredefinedPatterns();
            if(!complexTestScenarios.isEmpty())
                extractComplexTestScenarios(complexTestScenarios);
        } while(!isFeasible(complexTestScenarios));

        for(String testScenario : projectInformation.getComplexTestScenariosPatterns())
            if(hasMoreFeatures(testScenario))
               processSingleTestScenario(testScenario,true);
            else
                processSingleTestScenario(testScenario,false);

    }

    public Node getExtractedFeatureRelationshipModel(){ return rootNode;}

    private String getLabelByKey(int key){
        return projectInformation.getLabelContent(key);
    }

}
