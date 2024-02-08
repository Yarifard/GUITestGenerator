package ir.ac.um.guitestgenerating.TestUtils;


import com.intellij.psi.PsiJavaFile;
import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantProvider;
import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantRepository;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TestPartsGenerator {

    public static String createImportSection(String packageName,String appPackage) {
        String result = "";
        result +=
                "import android.view.View;\n" +
                "import android.view.ViewGroup;\n" +
                "import android.view.ViewParent;\n\n" +
                "import org.hamcrest.Description;\n" +
                "import org.hamcrest.Matcher;\n" +
                "import org.hamcrest.TypeSafeMatcher;\n" +
                "import org.junit.Rule;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n\n" +
                "import androidx.test.espresso.Espresso;\n" +
                "import androidx.test.espresso.NoMatchingViewException;\n" +
                "import androidx.test.espresso.ViewInteraction;\n" +
                "import androidx.test.espresso.action.ViewActions;\n" +
                "import androidx.test.filters.LargeTest;\n" +
                "import androidx.test.rule.ActivityTestRule;\n" +
                "import androidx.test.rule.GrantPermissionRule;\n" +
                "import androidx.test.runner.AndroidJUnit4;\n\n" +
                "import static androidx.test.espresso.Espresso.onData;\n" +
                "import static androidx.test.espresso.Espresso.onView;\n" +
                "import androidx.test.espresso.matcher.RootMatchers;\n" +
                "import " + packageName + ".Helper.NumberHelper;\n" +
                "import " + packageName + ".Helper.VisibilityHelper;\n" +
                "import " + packageName + ".Helper.EnableHelper;\n"+
                "import " + packageName + ".Helper.CheckedHelper;\n" +
                 "import " + packageName + ".Helper.TextHelper;\n\n"+
                "import static androidx.test.espresso.action.ViewActions.click;\n" +
                "import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;\n" +
                "import static androidx.test.espresso.action.ViewActions.longClick;\n" +
                "import static androidx.test.espresso.action.ViewActions.replaceText;\n" +
                "import static androidx.test.espresso.action.ViewActions.scrollTo;\n" +
                "import static androidx.test.espresso.action.ViewActions.typeText;\n" +
                "import static androidx.test.espresso.assertion.ViewAssertions.matches;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.withHint;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.withId;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.isRoot;\n" +
                "import static androidx.test.espresso.matcher.ViewMatchers.withText;\n" +
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeGreaterThanMatcher.withSizeGreaterThan;\n"+
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeGreaterThanOrEqualToMatcher.withSizeGreaterThanOrEqualTo;\n"+
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeLessThanMatcher.withSizeLessThan;\n"+
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeLessThanOrEqualToMatcher.withSizeLessThanOrEqualTo;\n"+
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeEqualToMatcher.withSizeEqualTo;\n"+
                "import static " + packageName + ".CustomMatchers.AdapterViewSizeNotEqualToMatcher.withSizeNotEqualTo;\n"+
                "import static " + packageName + ".CustomMatchers.TextViewTextGreaterThanMatcher.withTextGreaterThan;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextGreaterThanOrEqualToMatcher.withTextGreaterThanOrEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLessThanMatcher.withTextLessThan;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLessThanOrEqualToMatcher.withTextLessThanOrEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextEqualToMatcher.withTextEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextNotEqualToMatcher.withTextNotEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthEqualToMatcher.withTextLengthEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthNotEqualToMatcher.withTextLengthNotEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthGreaterThanMatcher.withTextLengthGreaterThan;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthGreaterThanOrEqualToMatcher.withTextLengthGreaterThanOrEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthLessThanMatcher.withTextLengthLessThan;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewTextLengthLessThanOrEqualToMatcher.withTextLengthLessThanOrEqualTo;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewEditableMatcher.withEditable;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewEnableMatcher.withTextViewEnable;\n" +
                "import static " + packageName + ".CustomMatchers.ImageViewEnableMatcher.withImageViewEnable;\n" +
                "import static " + packageName + ".CustomMatchers.ProgressBarEnableMatcher.withProgressBarEnable;\n" +
                "import static " + packageName + ".CustomMatchers.ViewGroupEnableMatcher.withViewGroupEnable;\n" +
                "import static " + packageName + ".CustomMatchers.TextViewVisibilityMatcher.withTextViewVisibility;\n" +
                "import static " + packageName + ".CustomMatchers.ImageViewVisibilityMatcher.withImageViewVisibility;\n" +
                "import static " + packageName + ".CustomMatchers.ProgressBarVisibilityMatcher.withProgressBarVisibility;\n" +
                "import static " + packageName + ".CustomMatchers.ViewGroupVisibilityMatcher.withViewGroupVisibility;\n" +
                "import static " + packageName + ".CustomMatchers.CompoundButtonCheckedMatcher.withChecked;\n" +
                "import static org.hamcrest.CoreMatchers.is;\n" +
                "import static org.hamcrest.Matchers.allOf;\n" +
                "import static org.hamcrest.Matchers.instanceOf;\n" +
                "import static org.hamcrest.Matchers.anything;\n" +
                "import static org.hamcrest.Matchers.anyOf;\n\n" +
                "import " + appPackage + ".R;\n";
        return result;
    }

    private static String getTestPath(ActivityInformation currentActivity) {
        return currentActivity.getProjectInformation().getAndroidTestDirectory().getCanonicalPath();
    }

    public static String getTestFolderPath(String testFilePath, String packageName,
                                             String activityName) {
        return testFilePath +
               "\\java\\" + getPackagePath(packageName) +
               "\\" + activityName + "Test.java";
    }

    public static String getTestFilePath(String testFilePath, String packageName,
                                        String activityName) {
        return testFilePath +
                "\\java\\" + getPackagePath(packageName) +
                "\\" + activityName + "Test.java";
    }

    private static String getPackagePath(String packageName) {
            String packagePath = "";

            packagePath = packageName;
            String temp = packagePath.replace(".","\\\\");

            return temp;
    }

    private static String getPackageName(ActivityInformation activity){
        //return activity.getProjectInformation().getPackageName();
        return ((PsiJavaFile) activity.getActivityClass().getContainingFile()).getPackageName();

    }

    private static List<String> getAppPermissionsList(ActivityInformation currentActivity){
        return currentActivity.getProjectInformation().getAppPermissionsList();
    }

    private static String getEventLabelFor(ActivityInformation activity,EventHandlerInformation event){
        return activity.getProjectInformation().getLabelContent(event.getKeyOfLabel());
    }

    private static void replaceEventLabelFor(ActivityInformation currentActivity,
                                      EventHandlerInformation event,
                                      String newLabel){
        currentActivity.getProjectInformation().replaceLabel(event.getKeyOfLabel(),newLabel);

    }

    private static boolean isNotCreatedTestCaseFor(ActivityInformation currentActivity,EventHandlerInformation event) {
        String label = getEventLabelFor(currentActivity,event);
        if(!label.contains("//Begin of precondition assertion section"))
            return true;
        return false;
    }

    private static InvariantRepository getInvariantReposittory(ActivityInformation currentActivity){
        return currentActivity.getProjectInformation().getGuiInvariantRepository();
    }

    private static String enrichTestCaseWithPreconditionAssertionFor(ActivityInformation currentActivity,
                                                              EventHandlerInformation event,
                                                              String result) {
        String assertion = "";
        InvariantProvider invariantProvider = new InvariantProvider(currentActivity.getProjectInformation());
        if(result.contains("empty")){
            assertion = invariantProvider.getPreConditionAssertionFor(currentActivity,event);
            if(!assertion.isEmpty())
                result = result.replace("//empty",assertion);

            else
                result = result.replace("empty","There isn't precondition assertion");
        }
        return result;
    }

    private static String enrichTestCaseWithPostConditionAssertionFor(ActivityInformation currentActivity,
                                                               EventHandlerInformation event,
                                                               String result) {
        InvariantProvider invariantProvider = new InvariantProvider(/*getInvariantReposittory(currentActivity)*/currentActivity.getProjectInformation());
        if(result.contains("//Nothings")){
            result = invariantProvider.getPostConditionAssertionFor(currentActivity,event,result);
            if(result.contains("//Nothings"))
                result = result.replace("//Nothings","//There is no postCondition Assertion");
        }
        return  result;
    }

    private static String getEventInstruction(ActivityInformation currentActivity,
                                              EventHandlerInformation event, String result) {
        if(isNotCreatedTestCaseFor(currentActivity,event)/* !event.isGeneratedTest()*/){
            //String result = "";
            result += "\n\t\t//Begin of precondition assertion section";
            result += "\n\t\t//empty";
            result += "\n\t\t//End of precondition assertion section";
            result += "\n\t\t\t//tag\n";
            result += getEventLabelFor(currentActivity,event);
            result += "\n\n\t\t//Begin of postCondition assertion section";
            result += "\n\t\t//Nothings";
            result += "\n\t\t//End of postCondition assertion section\n";
            result = enrichTestCaseWithPreconditionAssertionFor(currentActivity,event,result);
            result = enrichTestCaseWithPostConditionAssertionFor(currentActivity,event,result);
            replaceEventLabelFor(currentActivity,event,result);
            return result;
        }
        else
            return getEventLabelFor(currentActivity,event);

    }

    private static String getParentEventInstruction(ActivityInformation activityInformation,
                                                    EventHandlerInformation event,String result) {

//        if(event.hasParent())
//            return getParentEventInstruction(activityInformation,event.getParentEventHandlerInformation(),result);
//        else
            return getEventInstruction(activityInformation,event,result);
    }

    public static String testCasePurgation(String result) {
        result = result.replaceAll("//tag","");
        if(result.contains("//There isn't precondition assertion"))
            result = result.replace("//Begin of precondition assertion section" +
                    "\t\t\t//There isn't precondition assertion\n" +
                    "\t\t//End of precondition assertion section","");
        if(result.contains("//There is no postCondition Assertion"))
            result.replace("\n\n\t\t//Begin of postCondition assertion section" +
                    "\n\t\t\t//There is no postCondition Assertion" +
                    "\n\t\t//End of postCondition assertion section\n","");
        return result;

    }

    public static String createEndPart(){
        return "\n}";
    }

    public static String postprocess(String testMethodBody){
        if(testMethodBody.contains("//Begin of precondition assertion section\n/*")){
            testMethodBody = testMethodBody.replace("//Begin of precondition assertion section\n/*",
                    "//Begin of precondition assertion section");
            testMethodBody = testMethodBody.replace("*///End of precondition assertion section",
                    "//End of precondition assertion section");
        }
        if(testMethodBody.contains("//Begin of postCondition assertion section\n/*")){
            testMethodBody = testMethodBody.replace("//Begin of postCondition assertion section\n/*",
                    "//Begin of postCondition assertion section");
            testMethodBody = testMethodBody.replace("*///End of postCondition assertion section",
                    "//End of postCondition assertion section");
        }
        if(testMethodBody.contains("//Begin of first part of postcondition assertion \n\t\t/*")){
            testMethodBody = testMethodBody.replace("//Begin of first part of postcondition assertion \n\t\t/*",
                    "//Begin of first part of postcondition assertion \n\t\t");
            testMethodBody = testMethodBody.replace("*///End of fist part of postcondition assertion",
                    "//End of fist part of postcondition assertion");
        }


        return testMethodBody;
    }

    public static String preprocess(String testMethodBody){
        if(testMethodBody.contains("//Begin of precondition assertion section")){
            testMethodBody = testMethodBody.replace("//Begin of precondition assertion section",
                    "//Begin of precondition assertion section\n/*");
            testMethodBody = testMethodBody.replace("//End of precondition assertion section",
                    "*///End of precondition assertion section");
        }
        if(testMethodBody.contains("//Begin of postCondition assertion section")){
            testMethodBody = testMethodBody.replace("//Begin of postCondition assertion section",
                    "//Begin of postCondition assertion section\n/*");
            testMethodBody = testMethodBody.replace("//End of postCondition assertion section",
                    "*///End of postCondition assertion section");
        }
        if(testMethodBody.contains("//Begin of first part of postcondition assertion \n\t\t")){
            testMethodBody = testMethodBody.replace("//Begin of first part of postcondition assertion \n\t\t",
                    "//Begin of first part of postcondition assertion \n\t\t/*");
            testMethodBody = testMethodBody.replace("//End of fist part of postcondition assertion",
                    "*///End of fist part of postcondition assertion");
        }

        return testMethodBody;
    }


    public static String createTestMethodFor(ActivityInformation currentActivity, EventHandlerInformation event){
        String result = "";
        result += "\n\t@Test";
        result += "\n\tpublic void " + event.getTitle() + "() {\n\n";
        String testContent = "";
        if(event.hasParent())
            testContent += getParentEventInstruction(currentActivity, event.getParentEventHandlerInformation(),
                                                    testContent);
        testContent = getEventInstruction(currentActivity,event,testContent);
        result += testContent;
        result += "\n\t}\n\n";
        result = testCasePurgation(result);
        return result;
    }

    public static String generateTestMethodFor(ActivityInformation currentActivity,
                                         EventHandlerInformation indepEvent,
                                         EventHandlerInformation depEvent){
        String result = "";
        result += "\n\t@Test";
        result += "\n\tpublic void " + depEvent.getTitle() + "() {\n\n";
        String testContent = "";
        if(indepEvent.hasParent())
            testContent = getParentEventInstruction(currentActivity,indepEvent.getParentEventHandlerInformation(),testContent);
        testContent = getEventInstruction(currentActivity,indepEvent,testContent);
        result += testContent;
        testContent = "";
        if(depEvent.hasParent())
            testContent = getParentEventInstruction(currentActivity,depEvent.getParentEventHandlerInformation(),testContent);
        testContent = getEventInstruction(currentActivity,depEvent,testContent);
        result += testContent;
        result += "\n\t}\n\n";
        result = testCasePurgation(result);
        return result;

    }

    public static  String createBeginParts(ActivityInformation currentActivity) {
        String result = "";
        List<String> appPermissionList = getAppPermissionsList(currentActivity);
        String permissionrule = "";
        result = "package  " + getPackageName(currentActivity) + ";\n\n\n";
        result += TestPartsGenerator.createImportSection(getPackageName(currentActivity),
                currentActivity.getProjectInformation().getAppPackage()) + "\n\n";
        result += "@RunWith(AndroidJUnit4.class)\n";
        result += "public  class  " + currentActivity.getActivityName() + "Test  {\n";
        result += "\n\t@Rule" +
                "\n\tpublic ActivityTestRule<" + currentActivity.getActivityName() +
                "> mActivityTestRule = new ActivityTestRule<"+
                currentActivity.getActivityName()+ ">(" +
                currentActivity.getActivityName() + ".class);\n";
        if(!appPermissionList.isEmpty()){
            permissionrule = "\t@Rule" +
                    "\n\tpublic GrantPermissionRule mGrantPermissionRule = " +
                    "GrantPermissionRule.grant(";
            for (String item:appPermissionList) {
                permissionrule += "\n\t\t\t\t\t\t\""+ item + "\",";
            }
            permissionrule = permissionrule.substring(0,permissionrule.length()-2);
            permissionrule += "\"\n\t);\n";
            result += permissionrule;
        }
        return result;
    }

    public static boolean createEvaluationTestClass(ActivityInformation currentActivity, EventHandlerInformation event,
                                                    int index){
        boolean result = false;
        Random rand = new Random();
        File testFileClass;
        String testClassName;
        try{
            do{
                testClassName = "EvaluateTestClass_" + rand.nextInt(100);
                testFileClass = new File(TestPartsGenerator.getTestFilePath(
                        getTestPath(currentActivity),
                        getPackageName(currentActivity),
                        testClassName));
            }while(testFileClass.exists());
            if(testFileClass.createNewFile()){
                FileWriter writer = new FileWriter(testFileClass);
                writer.write(createBeginParts(currentActivity));
                String testMethodBody = createTestMethodFor(currentActivity,event);
                writer.write(/*preprocess(*/testMethodBody/*)*/);
                writer.write(createEndPart());
                writer.close();
                result = true;
            }
        }
        catch (IOException ioe){
            Utils.showMessage("Issue an error when the EvaluateTestClass is created!!!");
            result = false;
        }
        return result;
    }

    public static boolean createEvaluationTestClass(ActivityInformation currentActivity, EventHandlerInformation event){
        boolean result = false;
        Random rand = new Random();
        File testFileClass;
        String testClassName;
        try{
            do{
                testClassName = "EvaluateTestClass_" + rand.nextInt(100);
                testFileClass =
                        new File(TestPartsGenerator.getTestFilePath(getTestPath(currentActivity),getPackageName(currentActivity),testClassName));
            }while(testFileClass.exists());
            if(testFileClass.createNewFile()){
                FileWriter writer = new FileWriter(testFileClass);
                writer.write(createBeginParts(currentActivity));
                String testMethodBody = createTestMethodFor(currentActivity,event);
                writer.write(preprocess(testMethodBody));
                writer.write(createEndPart());
                writer.close();
                result = true;
            }
        }
        catch (IOException ioe){
            Utils.showMessage("Issue an error when the EvaluateTestClass is created!!!");
            result = false;
        }
        return result;
    }
    public static boolean createEvaluationTestClass(ActivityInformation currentActivity, String testMethod){
        boolean result = false;
       // Random rand = new Random();
        String testClassName = "Evaluation";
        File testFileClass = new File(TestPartsGenerator.getTestFilePath(
                getTestPath(currentActivity),
                getPackageName(currentActivity),
                testClassName));
//        try{
//            do{
//                testClassName = "EvaluateTestClass_" + rand.nextInt(100);
//                testFileClass = new File(TestPartsGenerator.getTestFilePath(
//                        getTestPath(currentActivity),
//                        getPackageName(currentActivity),
//                        testClassName));
//            }while(testFileClass.exists());
           try{
                if(testFileClass.exists())
                    testFileClass.delete();
                if(testFileClass.createNewFile()){
                    FileWriter writer = new FileWriter(testFileClass);
                    writer.write(createBeginParts(currentActivity));
                    writer.write(/*preprocess(*/testMethod/*)*/);
                    writer.write(createEndPart());
                    writer.close();
                    result = true;
                }
        }
        catch (IOException ioe){
            Utils.showMessage("Issue an error when the EvaluateTestClass is created!!!");
            result = false;
        }
        return result;
    }
     public static boolean createTestClassFor(ActivityInformation currentActivity){
        boolean result = false;
        String[] activityPatterns = {"PhoneCall","SendEmail","CaptureImage"};
        String testClassName = currentActivity.getActivityName();
         File testFileClass = new File(TestPartsGenerator.getTestFilePath(
                 getTestPath(currentActivity),
                 getPackageName(currentActivity),
                 testClassName));
         try{
             if(testFileClass.exists())
                 testFileClass.delete();
             if(testFileClass.createNewFile()){
                 FileWriter writer = new FileWriter(testFileClass);
                 writer.write(createBeginParts(currentActivity));
                 for(EventHandlerInformation event:currentActivity.getFeaturesList()){
                    // if(!Pattern.isMatch(activityPatterns,event.getTargetActivity())){
                         String testMethodBody = createTestMethodFor(currentActivity,event);
                         writer.write(/*preprocess(*/testMethodBody/*)*/);
                 }
                 System.out.println(("==================================================="));
                 writer.write(createEndPart());
                 writer.close();
                 result = true;
             }
         }
         catch (IOException ioe){
             Utils.showMessage("Issue an error when the EvaluateTestClass is created!!!");
             result = false;
         }
        return true;
     }

    public static void updateTestClass(ActivityInformation currentActivity) {
        File testFileClass = new File(TestPartsGenerator.getTestFilePath(
                getTestPath(currentActivity),
                getPackageName(currentActivity),
                currentActivity.getActivityName()));
        try{
            if(testFileClass.exists())
                testFileClass.delete();
            if(testFileClass.createNewFile()){
                FileWriter writer = new FileWriter(testFileClass);
                writer.write(createBeginParts(currentActivity));
                for(EventHandlerInformation event:currentActivity.getFeaturesList()){
                    String testMethodBody = createTestMethodFor(currentActivity,event);
                    writer.write(/*preprocess(*/testMethodBody/*)*/);
                }
                writer.write(createEndPart());
                writer.close();
               // result = true;
            }
        }
        catch (IOException ioe){
            Utils.showMessage("Issue an error when the EvaluateTestClass is created!!!");

        }
    }
}
