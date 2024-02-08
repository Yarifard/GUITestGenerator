package ir.ac.um.guitestgenerating.TestUtils;

import com.intellij.psi.PsiJavaFile;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code.ASTUtils;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.FeatureModel;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.Window;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EventsEvaluator {
    private ProjectInformation projectInformation;
    private FeatureModel featureModel;
    private List<EventHandlerInformation> tmpIndepList;

    public EventsEvaluator(ProjectInformation projectInformation, FeatureModel featureModel){
        this.projectInformation = projectInformation;
        this.featureModel = featureModel;
        initialize();
    }

    private void initialize(){
        //start();
        preparedTestClasses();
        //createDependentList1();
      if(!projectInformation.getDependentEventsList().isEmpty())
         createDependentListManually();
      emptTestFolder();
    }

    private void preparedTestClasses(){
        int i = 0;
        for(ActivityInformation currentActivity:featureModel.getProjectActivities()){
            //projectInformation.getDependentEventsList().addAll(currentActivity.getFeaturesList());
            if(!TestPartsGenerator.createTestClassFor(currentActivity)){
                System.out.println("There is an error in creating test classses!!!");
                break;
            }
            i++;
        }
        if(TestRunner.isBuildTest(projectInformation.getBaseDirectory().getCanonicalPath()))
            splitEvents();
        else
            Utils.showMessage("There is an error in building Tests!!!");
    }

    public void createDependentList1(){
        int oldSize = 0;
        boolean terminate = false;
        tmpIndepList = new ArrayList<>();
        List<EventHandlerInformation> eventsList;
        while(!terminate){
            eventsList  = projectInformation.getDependentEventsList();
            oldSize = eventsList.size();
            int i = oldSize;
            for(EventHandlerInformation event:eventsList) {
                if(!(event.hasParent() || ASTUtils.isDialogMethod(event.getAttachedMethod())))
                   createDependentEventListFor1(event);
                i--;
                System.out.println("The remaining items are:" + i);
            }
            if(!tmpIndepList.isEmpty())
                projectInformation.getDependentEventsList().removeAll(tmpIndepList);
            //projectInformation.getIndependentEventsList().clear();
            projectInformation.getIndependentEventsList().addAll(tmpIndepList);
            tmpIndepList.clear();
            if(notChangedDependentEventList(oldSize))
                terminate = true;
        }
        if(!projectInformation.getDependentEventsList().isEmpty()){
            System.out.println("I don't find dependency event(s) for the following events:[\n");
            for(EventHandlerInformation event:projectInformation.getDependentEventsList())
                System.out.println(event.getSourceActivity() + ":" + event.getTitle());
            System.out.println("]\n");
            createDependentListManually();
        }
    }

    private void createDependentListManually(){
        List<EventHandlerInformation> dependentEventList;
        for(EventHandlerInformation event : projectInformation.getDependentEventsList()) {
           // do{
               // if(!(event.hasParent() || ASTElement.isDialogMethod(event.getAttachedMethod()))){
                    Window window = new Window();
                    String testScenario = "<" + event.getSourceActivity() +"." + event.getTitle() + ">";
                    dependentEventList =
                            createDependentEventListFrom(window.getTestSenarios(featureModel.getProjectActivities(),event));
//                    if(!dependentEventList.isEmpty())
//                        event.loadDependentEvents(dependentEventList);
                    projectInformation.appendIntoComplexTestScenario(testScenario);
          //  } while (!isFeasibile(dependentEventList,event) && !dependentEventList.isEmpty());
                  event.setDependentList(dependentEventList);
                //}
        }
    }

    private void emptTestFolder()  {
        String testSubPath = projectInformation.getTestSubPath();
        testSubPath = testSubPath.replace(".","/");
        String testFilesPath = projectInformation.getAndroidTestDirectory().getCanonicalPath() + "/java/" + testSubPath;

        File[] files = new File(Paths.get(testFilesPath).toString()).listFiles();
        for(File file:files)
            if (!file.isDirectory())
                file.delete();
    }


    private static String getPackagePath(String packageName) {
        String packagePath = "";
        packagePath = packageName;
        String temp = packagePath.replace(".","\\");
        return temp;
    }

    private String getTestFilesPath(){
        return projectInformation.getAndroidTestDirectory().getCanonicalPath() +  "\\java\\" + getPackagePath(projectInformation. getTestSubPath());
    }

    private EventHandlerInformation findEventByName(ActivityInformation activity,String eventName){
        for(EventHandlerInformation event:activity.getFeaturesList())
            if(isMatch(event.getTitle(),eventName))
                return event;
        return null;
    }

    private ActivityInformation findActivityByName(String targetActivity) {
        ActivityInformation result = null;
        for(ActivityInformation activity:featureModel.getProjectActivities())
            if(isMatch(activity.getActivityName(),targetActivity))
                result =  activity;
        return result;
    }

    private boolean isMatch(String activityName, String targetActivity) {
        if(activityName.contentEquals(targetActivity))
            return true;
        return false;
    }

    private boolean notChangedDependentEventList(int oldSize){
        if(projectInformation.getDependentEventsList().size() == oldSize)
            return true;
        return false;
    }

    private void copyDependentEventsList(EventHandlerInformation depEvent,EventHandlerInformation indepEvent){
        for(EventHandlerInformation event: indepEvent.getDependentEventHandlersList())
            depEvent.appendDependentEventHandler(event);
    }

    public Boolean createTestClassFor(EventHandlerInformation event){
        boolean result = false;
        List<EventHandlerInformation> depEventsList = event.getDependentEventHandlersList();
        ActivityInformation currentActivity;
        if(depEventsList.isEmpty()){
            currentActivity = findActivityByName(event.getSourceActivity());
            if(TestPartsGenerator.createEvaluationTestClass(currentActivity,event))
                    result = true;
        }
        else{
              result = true;
              for(EventHandlerInformation eventItem : depEventsList){
                    if(!createTestClassFor(eventItem)){
                        result = false;
                        break;
                    }
               }
               if(result){
                   currentActivity = findActivityByName(event.getSourceActivity());
                   if(TestPartsGenerator.createEvaluationTestClass(currentActivity,event))
                       result = true;
                   else
                       result = false;
               }
               else
                   System.out.println("There is a problem in creating test classes!!!!");

        }
        return result;
    }

    private boolean createTestByFirstStrategy(EventHandlerInformation indepEvent, EventHandlerInformation depEvent){
        boolean result = false;
        if(createTestClassFor(indepEvent))
            if(createTestClassFor(depEvent)){
                result = true;
        }
        return result;
    }

    private boolean createTestBySecondStartegy(EventHandlerInformation indepEvent, EventHandlerInformation depEvent){
        boolean result = false;
        List<EventHandlerInformation> eventsList = indepEvent.getDependentEventHandlersList();
        ActivityInformation currentActivity;
        if(indepEvent.getDependentEventHandlersList().isEmpty()){
            if(depEvent.hasParent())
               if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                   return false;
            currentActivity = findActivityByName(indepEvent.getSourceActivity());
            String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
            if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod))
               result = true;
        }
        else{
            for(EventHandlerInformation eventItem : indepEvent.getDependentEventHandlersList()){
                if(!createTestClassFor(eventItem)){
                    result = false;
                    break;
                }
            }
            if(result){
                if(depEvent.hasParent()){
                   if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                        if(!createTestClassFor(depEvent))
                         result = false;
                }
                else{
                    currentActivity = findActivityByName(indepEvent.getSourceActivity());
                    String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
                    if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod))
                          result = true;
                }
            }
            else{
                System.out.println("There is a problem in createing test classes for event's dependencies!!!");
            }
        }
        return result;
    }

    private boolean createTestBySecondStartegy2(EventHandlerInformation indepEvent, EventHandlerInformation depEvent){
        boolean result = false;
        List<EventHandlerInformation> eventsList = indepEvent.getDependentEventHandlersList();
        ActivityInformation currentActivity;
        if(indepEvent.getDependentEventHandlersList().isEmpty()){
            if(depEvent.hasParent())
                if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                    return false;
            currentActivity = findActivityByName(indepEvent.getSourceActivity());
            String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
            if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod))
                result = true;
        }
        else{
            for(EventHandlerInformation eventItem : indepEvent.getDependentEventHandlersList()){
                if(!TestRunner.isExecuable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                           projectInformation.getPackageName(),eventItem)){
                    result = false;
                    break;
                }
            }
            if(result){
                if(depEvent.hasParent()){
                    if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                        if(!createTestClassFor(depEvent))
                            result = false;
                }
                else{
                    currentActivity = findActivityByName(indepEvent.getSourceActivity());
                    String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
                    if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod))
                        result = true;
                }
            }
            else{
                System.out.println("There is a problem in createing test classes for event's dependencies!!!");
            }
        }
        return result;
    }

    private  boolean createDependentEventListFor(EventHandlerInformation depEvent){
        boolean result = false;
        List<EventHandlerInformation> eventsList = projectInformation.getIndependentEventsList();
        for(EventHandlerInformation indepEvent:eventsList){
            if(!isMatch(depEvent.getSourceActivity(),indepEvent.getSourceActivity())){
               if(createTestByFirstStrategy(indepEvent,depEvent))
                  if(TestRunner.isExecuable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                            projectInformation.getPackageName(),getTestFilesPath())){
                      depEvent.appendDependentEventHandler(indepEvent);
                      depEvent.setExecutableFlag();
                      tmpIndepList.add(depEvent);
                      result = true;
                      break;
                  }
            }
            else if(createTestBySecondStartegy(indepEvent,depEvent)){
                 if(TestRunner.isExecuable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                           projectInformation.getPackageName(),getTestFilesPath())){
                    if(indepEvent.getDependentEventHandlersList().isEmpty())
                         depEvent.setIndependentFlag();
                    if(!depEvent.hasParent())
                        projectInformation.replaceLabel(depEvent.getKeyOfLabel(),
                                               projectInformation.getLabelContent(indepEvent.getKeyOfLabel()) +
                                                    projectInformation.getLabelContent(depEvent.getKeyOfLabel()));
                    else if(!isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                        projectInformation.replaceLabel(depEvent.getKeyOfLabel(),
                                projectInformation.getLabelContent(indepEvent.getKeyOfLabel()) +
                                        projectInformation.getLabelContent(depEvent.getKeyOfLabel()));
                    depEvent.setExecutableFlag();
                    copyDependentEventsList(depEvent,indepEvent);
                    tmpIndepList.add(depEvent);
                    result = true;
                    break;
                 }
            }
        }
        System.gc();
        return result;
    }

    private boolean evaluateBySecondStrategy(EventHandlerInformation indepEvent, EventHandlerInformation depEvent){
        boolean result = false;
        ActivityInformation currentActivity;
        if(indepEvent.getDependentEventHandlersList().isEmpty()){
            if(depEvent.hasParent())
                if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                    return false;
            currentActivity = findActivityByName(indepEvent.getSourceActivity());
            String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
            if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod))
                if(TestRunner.isExecutableEvaluationTestClass(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                              projectInformation.getPackageName()))
                    result = true;
                else
                    result = false;
        }
        else if(TestRunner.isExecutableWithTails(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                 projectInformation.getPackageName(),indepEvent)){
                if(depEvent.hasParent()){
                    if(isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                        return false;
                }
                currentActivity = findActivityByName(indepEvent.getSourceActivity());
                String testMethod = TestPartsGenerator.generateTestMethodFor(currentActivity,indepEvent,depEvent);
                if(TestPartsGenerator.createEvaluationTestClass(currentActivity,testMethod)){
                    if(TestRunner.isExecutableEvaluationTestClass(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                                  projectInformation.getPackageName()))
                        result = true;
                    else
                        result = false;

                }
                else{
                     System.out.println("There is a problem in createing test classes for event's dependencies!!!");
                     result = false;
                }
        }
        return result;
    }

    private  boolean createDependentEventListFor1(EventHandlerInformation depEvent){
        boolean result = false;
        List<EventHandlerInformation> eventsList = projectInformation.getIndependentEventsList();
        for(EventHandlerInformation indepEvent:eventsList)
            if(TestRunner.isInstalledAppSuccess(projectInformation.getBaseDirectory().getCanonicalPath())){
                if (!isMatch(depEvent.getSourceActivity(), indepEvent.getSourceActivity())) {
                    if (TestRunner.isExecutableWithTails(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                         projectInformation.getPackageName(),indepEvent))
                        if (TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                    projectInformation.getPackageName(),projectInformation.getAppPackage(),indepEvent) &&
                            TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                    projectInformation.getPackageName(),projectInformation.getAppPackage(),depEvent)) {
                            depEvent.appendDependentEventHandler(indepEvent);
                            depEvent.setExecutableFlag();
                            tmpIndepList.add(depEvent);
                            projectInformation.appendIntoComplexTestScenario("<" + depEvent.getSourceActivity() +"." +depEvent.getTitle() + ">");
                            result = true;
                            break;
                        }
                }
                else if (evaluateBySecondStrategy(indepEvent, depEvent)) {
//                    if (indepEvent.getDependentEventHandlersList().isEmpty())
//                        depEvent.setIndependentFlag();
//                    if (!depEvent.hasParent()){
//                            projectInformation.replaceLabel(depEvent.getKeyOfLabel(),
//                                    projectInformation.getLabelContent(indepEvent.getKeyOfLabel()) +
//                                            projectInformation.getLabelContent(depEvent.getKeyOfLabel()));
//                            TestPartsGenerator.updateTestClass(findActivityByName(depEvent.getSourceActivity()));
//                    }
//                    else if (!isMatch(indepEvent.getTitle(), depEvent.getParentEventHandlerInformation().getTitle())){
//                            projectInformation.replaceLabel(depEvent.getKeyOfLabel(),
//                                    projectInformation.getLabelContent(indepEvent.getKeyOfLabel()) +
//                                            projectInformation.getLabelContent(depEvent.getKeyOfLabel()));
//                            TestPartsGenerator.updateTestClass(findActivityByName(depEvent.getSourceActivity()));
//                    }
                    if(!depEvent.hasParent())
                        depEvent.appendDependentEventHandler(indepEvent);
                    else if(!isMatch(indepEvent.getTitle(),depEvent.getParentEventHandlerInformation().getTitle()))
                        depEvent.appendDependentEventHandler(indepEvent);
                    depEvent.setExecutableFlag();
                  //  copyDependentEventsList(depEvent, indepEvent);
                    projectInformation.appendIntoComplexTestScenario("<" + depEvent.getSourceActivity() + "." + depEvent.getTitle() + ">");
                    tmpIndepList.add(depEvent);

                    result = true;
                    break;
                }
                if(!TestRunner.isUninstalledAppSuccess(projectInformation.getBaseDirectory().getCanonicalPath(),
                        projectInformation.getPackageName()))
                    Utils.showMessage("There is an error in uninstalling the App!!!");
            }
            else
                Utils.showMessage("There is an error in installing the App on the emulator!!!!");
        System.gc();
        if(result)
            if(depEvent.hasChildEvents()){
                depEvent.getChildEventHandlers().forEach(child->{
                    tmpIndepList.add(child);
                    child.setExecutableFlag();
                    projectInformation.appendIntoComplexTestScenario("<" + child.getSourceActivity() +"." + child.getTitle() + ">");
                });
            }
        return result;
    }

    private void createDependentList(){
        int oldSize = 0;
        boolean terminate = false;
        tmpIndepList = new ArrayList<>();
        List<EventHandlerInformation> eventsList;

        while(!terminate){
            eventsList  = projectInformation.getDependentEventsList();
            oldSize = eventsList.size();
            int i = oldSize;

            for(EventHandlerInformation event:eventsList) {
                createDependentEventListFor(event);
                i--;
                System.out.println("The remaining items are:" + i);

            }
            if(!tmpIndepList.isEmpty())
                projectInformation.getDependentEventsList().removeAll(tmpIndepList);
            projectInformation.getIndependentEventsList().clear();
            projectInformation.getIndependentEventsList().addAll(tmpIndepList);
            tmpIndepList.clear();
            if(notChangedDependentEventList(oldSize))
                terminate = true;
        }
        if(!projectInformation.getDependentEventsList().isEmpty()){
            System.out.println("I don't find dependency event(s) for the following events:[\n");
            for(EventHandlerInformation event:projectInformation.getDependentEventsList())
                System.out.println(event.getSourceActivity() + ":" + event.getTitle());
            System.out.println("]\n");
        }
    }

    private void start(){
        for (ActivityInformation currentActivity:featureModel.getProjectActivities()){
            for(EventHandlerInformation event:currentActivity.getFeaturesList()) {
               if (TestPartsGenerator.createEvaluationTestClass(currentActivity, event)) {
                   if (TestRunner.isExecuable(projectInformation.getBaseDirectory().getCanonicalPath(),
                                              projectInformation.getPackageName(),getTestFilesPath())) {
                       event.setIndependentFlag();
                       event.setExecutableFlag();
                       projectInformation.addToIndependentEventsList(event);
                   }
                   else
                       projectInformation.addToDependentEventsList(event);
               }
            }
        }
        System.gc();
    }

    private static String getPackageName(ActivityInformation activity){
        //return activity.getProjectInformation().getPackageName();
        return ((PsiJavaFile) activity.getActivityClass().getContainingFile()).getPackageName();
//
    }


    private void splitEvents(){
        String[] activityPatterns = {"PhoneCall","SendEmail","CaptureImage"};
        for(ActivityInformation currentActiviy:featureModel.getProjectActivities())
                for(EventHandlerInformation event:currentActiviy.getFeaturesList()){
                    if(!Pattern.isMatch(activityPatterns,event.getTargetActivity()))
                        if(TestRunner.isInstalledAppSuccess(projectInformation.getBaseDirectory().getCanonicalPath())){
                           if(TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),getPackageName(currentActiviy)
                                                     ,projectInformation.getAppPackage(),event)){
                                event.setIndependentFlag();
                                event.setExecutableFlag();
                                projectInformation.addToIndependentEventsList(event);
                           }
                          else
                            projectInformation.addToDependentEventsList(event);
                          if(!TestRunner.isUninstalledAppSuccess(projectInformation.getBaseDirectory().getCanonicalPath(),
                                                                 projectInformation.getAppPackage()))
                              Utils.showMessage("There is an error in uninstalling app!!!");
                        }
                }
    }

    private List<EventHandlerInformation> createDependentEventListFrom(String eventsStream){
        List<EventHandlerInformation> eventsList = new ArrayList<>();
        if(!eventsStream.isEmpty()){
            String eventName;
            String activityName;
            eventsStream = eventsStream.replace("<","");
            eventsStream = eventsStream.replace(">","");
            String[] featureNames = eventsStream.split(",");
            for(int index = 0; index < featureNames.length; index++){
                featureNames[index] = featureNames[index].trim();
                featureNames[index] = featureNames[index].replaceAll(" ","");
                activityName = featureNames[index].substring(0,featureNames[index].indexOf(".")).trim();
                eventName = featureNames[index].substring(featureNames[index].lastIndexOf(".")+1).trim();
                eventsList.add(findEventByName(findActivityByName(activityName),eventName));
            }
        }
        return  eventsList;
    }

    private boolean isFeasibile(List<EventHandlerInformation> eventList, EventHandlerInformation depEvent){
//        EventHandlerInformation lastEventItem = eventList.get(eventList.size()-1);
//        if(TestRunner.isInstalledAppSuccess(projectInformation.getBaseDirectory().getCanonicalPath())){
//            for(int index = 0; index < eventList.size() -1 ; index++){
//                if(eventList.get(index).getDependentEventHandlersList().isEmpty()){
//                    if(!TestRunner.isExecuable(projectInformation.getBaseDirectory().getCanonicalPath(),
//                                               projectInformation.getPackageName(),eventList.get(index)))
//                        return false;
//                }
//                else{
//                     if(!TestRunner.isExecutableWithTails(projectInformation.getBaseDirectory().getCanonicalPath(),
//                                                          projectInformation.getPackageName(),eventList.get(index)))
//                         return false;
//                     if(!TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),
//                                                projectInformation.getPackageName(),eventList.get(index)))
//                         return false;
//                }
//            }
//            if(!isMatch(lastEventItem.getSourceActivity(),depEvent.getSourceActivity())){
//                if(!lastEventItem.getDependentEventHandlersList().isEmpty())
//                    if(!TestRunner.isExecutableWithTails(projectInformation.getBaseDirectory().getCanonicalPath(),
//                            projectInformation.getPackageName(),lastEventItem))
//                        return  false;
//                if(!TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),
//                                            projectInformation.getPackageName(),lastEventItem))
//                    return false;
//                if(!TestRunner.isExecutable(projectInformation.getBaseDirectory().getCanonicalPath(),
//                        projectInformation.getPackageName(),depEvent))
//                    return false;
//            }
//            else{
//
//            }
//      }
        return true;
    }







}
