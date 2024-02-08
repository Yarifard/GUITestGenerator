package ir.ac.um.guitestgenerating.TestGeneration;

import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantProvider;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.FeatureModel;
import ir.ac.um.guitestgenerating.TestUtils.TestPartsGenerator;
import ir.ac.um.guitestgenerating.ValidTestData.TestDataProvider;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.io.*;
import java.util.List;

public class SimpleTestCaseGenerator {
    private static final int PostCondition = 1;
    private static final int PreCondition = 0;
    private ProjectInformation projectInformation;
    private FeatureModel featureModel;
    private TestDataProvider testDataProvider;
    private InvariantProvider invariantProvider;

    public SimpleTestCaseGenerator(ProjectInformation projectInformation, FeatureModel featureModel){
        this.projectInformation = projectInformation;
        this.featureModel = featureModel;
        this.testDataProvider = new TestDataProvider(projectInformation.getDataRepository());
        this.invariantProvider = new InvariantProvider(projectInformation);
    }

    private String getTestPath() {
        return projectInformation.getAndroidTestDirectory().getCanonicalPath();
    }

    private String getTestSubPath() {
        return projectInformation.getTestSubPath();
    }

    private List<ActivityInformation> getActivitiesList() {
        return featureModel.getProjectActivities();
    }

    private void createTestClassFor(ActivityInformation currentActivity) {
        List<EventHandlerInformation> mainFeaturesList = currentActivity.getMainFeaturesList();
        LabelGenerator labelGenerator = new LabelGenerator(invariantProvider,testDataProvider);
        String testMethodBody = "";
        if(!mainFeaturesList.isEmpty()){
            File testFileClass = new File(TestPartsGenerator.getTestFilePath(getTestPath(),getTestSubPath(),
                    currentActivity.getActivityName()));
            try{
                if(!testFileClass.exists())
                    if(testFileClass.createNewFile()){
                        FileWriter writer = new FileWriter(testFileClass);
                        writer.write(TestPartsGenerator.createBeginParts(currentActivity));
                        for(EventHandlerInformation event : mainFeaturesList){
                           if(event.isIndependent() && event.isExecutable()){
                                testMethodBody = TestPartsGenerator.createTestMethodFor(currentActivity,event);
                                writer.write(/*TestPartsGenerator.postprocess(*/testMethodBody/*)*/);
                           }
                        }
                        writer.write(TestPartsGenerator.createEndPart());
                        writer.close();
                    }
                Utils.showMessage("test cases are generated successfully!!!!");
            }
            catch (IOException ioe){
                Utils.showMessage("Issue an error when the test file is created!!!");
            }
        }
    }

    public void generateTestCases(){
        List<ActivityInformation> activitiesList = getActivitiesList();
        for(ActivityInformation currentActivity : activitiesList){
            createTestClassFor(currentActivity);
        }
    }
}
