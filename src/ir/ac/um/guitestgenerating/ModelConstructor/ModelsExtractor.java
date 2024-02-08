package ir.ac.um.guitestgenerating.ModelConstructor;

import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.FeatureModel;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel.FeatureRelationshipModel;
import ir.ac.um.guitestgenerating.TestUtils.EventsEvaluator;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.FileNotFoundException;

public class ModelsExtractor implements Runnable{
    private ProjectInformation projectInformation;
    FeatureModel featureModel;
    FeatureRelationshipModel featureRelationshipModel;

    public ModelsExtractor(ProjectInformation projectInformation){
         this.projectInformation = projectInformation;
         featureModel = new FeatureModel(projectInformation);
         featureRelationshipModel = new FeatureRelationshipModel(featureModel,projectInformation);
    }

    public FeatureModel getFeatureModel(){

        return featureModel;
    }

    public FeatureRelationshipModel getFeatureRelationshipModel(){

        return featureRelationshipModel;
    }

    @Override
    public void run() {
        Utils.showMessage("I'm in ModelExtractor class:run-->starting");

        try {
            featureModel.createFeatureModel();
            EventsEvaluator eventsEvaluator = new EventsEvaluator(projectInformation, featureModel);
            featureRelationshipModel.createFeatureRelationshipModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Utils.showMessage("I'm in ModelExtractor class:run-->end");

    }

}
