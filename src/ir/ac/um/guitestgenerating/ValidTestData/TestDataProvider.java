package ir.ac.um.guitestgenerating.ValidTestData;

import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

public class TestDataProvider {

    private TestDataRepository testDataRepository;

    public TestDataProvider(TestDataRepository testDataRepository){
        this.testDataRepository = testDataRepository;
    }

    public String enrichTestCaseWithValidData(EventHandlerInformation event, String result) {
        //TODO::This method must be implemented based on the test data repository.
        return result;
    }
}
