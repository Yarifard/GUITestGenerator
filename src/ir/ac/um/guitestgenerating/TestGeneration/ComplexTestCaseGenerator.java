package ir.ac.um.guitestgenerating.TestGeneration;

import ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel.FeatureRelationshipModel;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel.Node;
import ir.ac.um.guitestgenerating.TestUtils.TestPartsGenerator;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComplexTestCaseGenerator {

    private ProjectInformation projectInformation;
    private FeatureRelationshipModel featureRelationshipModel;

    public ComplexTestCaseGenerator(ProjectInformation projectInformation, FeatureRelationshipModel featureRelationshipModel){
        this.projectInformation = projectInformation;
        this.featureRelationshipModel = featureRelationshipModel;
    }

    private String getPackageName(){
        return projectInformation.getTestSubPath();
    }

    private String getLauncherActivityName(){

        return projectInformation.getLuncherActivity().getName();
    }

    private String getTestPath() {
        return projectInformation.getAndroidTestDirectory().getCanonicalPath();
    }

    private  List<String> getAppPermissionsList(){
        return projectInformation.getAppPermissionsList();
    }

    public String prepareBeginParts() {
        String result = "";
        List<String> appPermissionList = getAppPermissionsList();
        String permissionrule = "";

        result = "package  " + getPackageName() + ";\n\n\n";
        result += TestPartsGenerator.createImportSection(getPackageName(),projectInformation.getAppPackage()) + "\n\n";
        result += "@RunWith(AndroidJUnit4.class)\n";
        result += "public  class SystemTest  {\n";
        result += "\n\t@Rule" +
                "\n\tpublic ActivityTestRule<" + getLauncherActivityName() +
                "> mActivityTestRule = new ActivityTestRule<"+
                getLauncherActivityName()+ ">(" +
                getLauncherActivityName() + ".class);";
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

    private List<List<Node>> getComplexSequenceScenarios(Node node) {
        List<List<Node>> completeNodeSequence = new ArrayList<>();
        int index = 0;
        if (node.getAdjacencyList().isEmpty()) {
            List<Node> nodeList = new ArrayList<>();
            nodeList.add(node);
            completeNodeSequence.add(nodeList);
        } else
            for (Node tmpNode : node.getAdjacencyList()) {
                List<List<Node>> subSequences = getComplexSequenceScenarios(tmpNode);
                for (List<Node> list : subSequences) {
                    list.add(0, node);
                    completeNodeSequence.add(list);
                }
            }
        return completeNodeSequence;
    }

    public static String prepareEndParts() {

        return "}\n";
    }

    private String createTestMethodFor(List<Node> testScenario, int counter){
        String result = "";
        result += "\n\t@Test";
        String activityName = testScenario.get(testScenario.size() -1).getEventHandler().getSourceActivity();
        String methodName = testScenario.get(testScenario.size() -1).getEventHandler().getTitle();

        if(testScenario.get(testScenario.size() -1).getFlag())
            result += "\n\tpublic void " + activityName + "_" + methodName +
                    "() {\n\n";
        else
            result += "\n\tpublic void testScenario_" + (counter + 1) +"() {\n\n";

        int index = 1;
        for(Node node : testScenario)
            if(node.getKeyOfLabel() != -1) {
               if(!node.getEventHandler().isExecutable())
                   result  += "\t\t// This test is need to review\n";
                result += "\t\t//Path to " + node.getEventHandler().getTitle() + "\n";
              //  result += projectInformation.getLabelContent(node.getKeyOfLabel());
                result += getLabeOfPath(node);
              //  result += "\t\t// appendGUIAssertion_" + index + "(node.getEventHandler)\n\n";
                result += projectInformation.getLabelContent(node.getEventHandler().getKeyOfLabel());
              //  result += "\t\t// appendGUIAssertion_" + (index++) + "(node.getEventHandler)\n\n";
            }
        for (int i = 1; i<=testScenario.get(testScenario.size()-1).getNumBack();i++)
             result += "\t\tonView(isRoot()).perform(ViewActions.pressBack());\n";

        result += "\n\t}\n\n";
        result = testCasePurgation(result);
        return result;
    }

    private String getLabeOfPath(Node node) {
        String labelOfPath = projectInformation.getLabelContent(node.getKeyOfLabel());
        labelOfPath = labelOfPath.replace("/","");
        String[] keys = labelOfPath.split(";");
        String eventSequence = "";
        for(String key: keys){
           // key = key.replace("\n","");
            if(!key.isEmpty())
                if(Utils.isMatch(key,"Back()"))
                    eventSequence += "\t\tonView(isRoot()).perform(ViewActions.pressBack());\n";
                else
                    eventSequence += projectInformation.getLabelContent(Integer.parseInt(key));

//            if(node.getEventHandler().hasParent()){
//                    if(Integer.parseInt(key) != node.getEventHandler().getParentEventHandlerInformation().getKeyOfLabel())
//                        eventSequence += projectInformation.getLabelContent(Integer.parseInt(key));
//                  }
        }
        return eventSequence;
    }

    public void generateTestCases() {
        Node rootNode = featureRelationshipModel.getExtractedFeatureRelationshipModel();
        List<List<Node>> testScenariosSequences = getComplexSequenceScenarios(rootNode);
        File testFileClass = new File(TestPartsGenerator.getTestFilePath(getTestPath(),getPackageName(),"System"));
        try{
            if(!testFileClass.exists())
                if(testFileClass.createNewFile()){
                    FileWriter writer = new FileWriter(testFileClass);
                    writer.write(prepareBeginParts());
                    int index = 0;
                    for(List<Node> list : testScenariosSequences)
                        writer.write(createTestMethodFor(list,index++));
                    writer.write(prepareEndParts());
                    writer.close();
                }
            Utils.showMessage("test cases are generated successfully!!!!");
        }
        catch (IOException e){
            Utils.showMessage("Error in creating test file");
        }
    }

    private String testCasePurgation(String result) {
        result = result.replaceAll("//tag","");
        if(result.contains("//There isn't precondition assertion"))
            result = result.replace("//Begin of precondition assertion section\n" +
                    "\t\t\t//There isn't precondition assertion\n" +
                    "\t\t//End of precondition assertion section","");
        if(result.contains("//There is no postCondition Assertion"))
            result.replace("\n\n\t\t//Begin of postCondition assertion section" +
                    "\n\t\t\t//There is no postCondition Assertion" +
                    "\n\t\t//End of postCondition assertion section\n","");
        return result;

    }
}




