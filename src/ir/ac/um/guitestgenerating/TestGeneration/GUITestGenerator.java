package ir.ac.um.guitestgenerating.TestGeneration;

import ir.ac.um.guitestgenerating.ModelConstructor.ModelsExtractor;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

public class GUITestGenerator implements Runnable{
    private ProjectInformation projectInformation;
    private ModelsExtractor modelsExtractor;
    public GUITestGenerator(ProjectInformation projectInformation,ModelsExtractor modelsExtractor){
        this.projectInformation = projectInformation;
        this.modelsExtractor = modelsExtractor;

    }

    @Override
    public void run() {
          // // //EventsEvaluator eventsEvaluator = new EventsEvaluator(projectInformation,modelsExtractor.getFeatureModel());
           SimpleTestCaseGenerator simpleTestCase = new SimpleTestCaseGenerator(projectInformation,modelsExtractor.getFeatureModel());
           ComplexTestCaseGenerator complexTestCase = new ComplexTestCaseGenerator(projectInformation,
                                                           modelsExtractor.getFeatureRelationshipModel());

          // ExpansionTestCase expansionTestCase = new ExpansionTestCase();
          simpleTestCase.generateTestCases();
          complexTestCase.generateTestCases();
         // generateTestCasesFromFeatureModel();



    }

}
