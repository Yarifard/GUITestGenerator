package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.ClassFinder;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ActivityInformation {
    private String title;
    private PsiClass activityClass;
    private List<PsiClass> accessibleActivities;
    private ProjectInformation projectInformation;
    private ActivityInformationExtractor activityInformationExtractor;
    private boolean isListActivity;
    private List<EventHandlerInformation> mainFeaturesList;

    public ActivityInformation(ProjectInformation projectInformation, PsiClass activityClass)
            throws FileNotFoundException {
        this.title = "";
        isListActivity = false;
        this.activityClass = activityClass;
        this.projectInformation = projectInformation;
        this.accessibleActivities = new ArrayList<>();
        this.activityInformationExtractor = new ActivityInformationExtractor(this);
    }

    public PsiClass getActivityClass(){
        return this.activityClass;
    }


    public ProjectInformation getProjectInformation() {
        return projectInformation;
    }

    public String getActivityClassPath(){
        VirtualFile filePath = this.activityClass.getContainingFile().getOriginalFile().getVirtualFile();
        return filePath.getCanonicalPath().toString();
    }

    public String getActivityName(){
        return this.activityClass.getName().toString();
    }

    public List<PsiClass> getAccessibleActivity(){

        List<String> activityList = this.activityInformationExtractor.getAccessibleActivity();
        PsiClass tempAccessible;
        for(String activity:activityList){
            tempAccessible = ClassFinder.getClassByName(this.projectInformation.getProjectJavaClassList(),activity);
            if(!this.accessibleActivities.contains(tempAccessible))
                this.accessibleActivities.add(tempAccessible);
        }
        return this.accessibleActivities;
    }

    public String getTitle(){ return title;}

    public boolean isListActivity(){ return isListActivity;}

    public String getEventContext(EventHandlerInformation event){
        return event.getMainContext();
       // return activityInformationExtractor.getMainContext(event);
    }

    public String getEventParenContext(MethodDeclaration event) {

        return activityInformationExtractor.getParentContext(event);
    }

    public String getEventLabelFor(EventHandlerInformation eventHandler){
        return activityInformationExtractor.getEventLabelFor(eventHandler);

    }

    public void setTitle(String activityTitle){
        this.title = activityTitle;
    }

    public void setListActivity(){ isListActivity = true; }

    public  void extractInformation() {
        //List<EventHandlerInformation> mainFeaturesList;
        Utils.showMessage("I'm in " + this.activityClass.getName() + ": extractInformation-->start");
        this.activityInformationExtractor.collectInformation();
        mainFeaturesList = activityInformationExtractor.getMainFeaturesList();

        String message = "";

        if(mainFeaturesList.isEmpty())
            message += "The " + this.activityClass.getName() + " has not important features!!!";
        else
            for (EventHandlerInformation event : mainFeaturesList) {
                message += "\n" + event.getTitle();
                message += "[\nEventName:" + event.getName();
                message += "\nViewType:" + event.getAttachedViewType();
                message += "\nViewLabel:" + event.getAttachedViewLable();
                message += "\nViewID:" + event.getAttachedViewId();
                message += "\nComplexity :" + event.getComplexity();
                message += "\nLabel :" + projectInformation.getLabelContent(event.getKeyOfLabel());
                message += "\n]\n";
            }

        Utils.showMessage(message);
        Utils.showMessage("\n}\n");
    }

    public List<EventHandlerInformation> getMainFeaturesList(){
        if(mainFeaturesList == null){
            mainFeaturesList = activityInformationExtractor.getMainFeaturesList();
        }
        return mainFeaturesList;
    }

    public List<EventHandlerInformation> getFeaturesList(){
        return activityInformationExtractor.getFeaturesList();
    }

}
