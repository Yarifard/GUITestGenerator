package ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.FileNotFoundException;
import java.util.*;

public class FeatureModel {

    private ProjectInformation projectInformation;
    private List<ActivityInformation> projectActivities = new ArrayList<>();
    private ActivityInformation rootNode;


    public FeatureModel(ProjectInformation projectInformation){
        Utils.showMessage("I'm in FeatureModelExtractor:Constructor-->Start point");
        this.projectInformation = projectInformation;
        Utils.showMessage("I'm in FeatureModelExtractor:Constructor-->End point");
    }

    public void createFeatureModel() throws FileNotFoundException {
        boolean flag = true;
        Queue<PsiClass> activityQueue = new LinkedList<>();
        List<PsiClass> accessibleActivity;
        PsiClass currentActivity;

        Utils.showMessage("I'm in ModelExtractor:createFeatureModel-->Starting");
        SystemParameters.setParameter();
        //SystemParameters systemParameters = new SystemParameters(0.4,0.2,0.2,0.2,0.0);
        currentActivity = this.projectInformation.getLuncherActivity();
        String testSubPath = ((PsiJavaFile) currentActivity.getContainingFile()).getPackageName();
        this.projectInformation.setTestSubPath(testSubPath);

        if (currentActivity == null) {
            Utils.showMessage("Failed to detect the luncher activity.");
            return;
        }
        activityQueue.add(currentActivity);
        while (!activityQueue.isEmpty()) {
           currentActivity = activityQueue.remove();
            if (isNotProcessed(currentActivity)) {
                ActivityInformation currentActivityInformation = new
                        ActivityInformation(this.projectInformation,currentActivity);
                currentActivityInformation.extractInformation();
                if(flag){
                    this.rootNode = currentActivityInformation;
                    flag = false;
                }
                this.projectActivities.add(currentActivityInformation);
                accessibleActivity = currentActivityInformation.getAccessibleActivity();
                for(PsiClass activity:accessibleActivity)
                    if(activity != null)
                       activityQueue.add(activity);
            }
        }
        Utils.showMessage("I'm in ModelExtractor:createFeatureModel-->ending");
    }

    private boolean isNotProcessed(PsiClass activityClass) {
        boolean result = true;
        for(ActivityInformation activity:this.projectActivities)
            if(activity.getActivityClass().equals(activityClass))
                result = false;
        return result;
    }

    public ActivityInformation getRootNode(){ return rootNode;}

    public List<ActivityInformation> getProjectActivities(){
        return this.projectActivities;
    }


}
