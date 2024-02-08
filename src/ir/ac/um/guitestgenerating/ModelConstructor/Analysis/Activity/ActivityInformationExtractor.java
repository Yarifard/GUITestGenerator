package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity;

import com.github.javaparser.ast.body.MethodDeclaration;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code.ASTUtils;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method.MethodComplexityAnalyzer;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.SystemParameters;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code.CodeAnalyzer;
import ir.ac.um.guitestgenerating.Util.Utils;
import org.apache.commons.lang.StringUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ActivityInformationExtractor {

    private ActivityInformation context;
    private CodeAnalyzer codeAnalyzer;
    private List<EventHandlerInformation> eventsList;

    public ActivityInformationExtractor(ActivityInformation context) throws FileNotFoundException {
        this.context = context;
        codeAnalyzer = new CodeAnalyzer(context);
    }

    public void collectInformation() {

         codeAnalyzer.analyzeActivitySourceCode();
         eventsList = codeAnalyzer.getExtractedEventHandlerList();
         calculateComplexityOfExtractedEventList(eventsList);

    }

    public List<EventHandlerInformation> getMainFeaturesList() {
        List<EventHandlerInformation> extractedEventHandlerList = codeAnalyzer.getExtractedEventHandlerList();
        List<EventHandlerInformation> mainFeaturesList = new ArrayList<>();
        for (EventHandlerInformation eventHandler : extractedEventHandlerList){
            if (isImportant(eventHandler)){
                if(!mainFeaturesList.contains(eventHandler))
                     mainFeaturesList.add(eventHandler);
                if(eventHandler.getParentEventHandlerInformation() != null)
                    if(!mainFeaturesList.contains(eventHandler.getParentEventHandlerInformation()))
                         mainFeaturesList.add(eventHandler.getParentEventHandlerInformation());
            }
        }
        return mainFeaturesList;
    }

    public List<EventHandlerInformation> getFeaturesList(){
        return codeAnalyzer.getExtractedEventHandlerList();
    }

    private boolean isImportant(EventHandlerInformation eventHandler) {
        if (calculateImportanceMetricFor(eventHandler) > SystemParameters.getThreshold()){
            eventHandler.setImportantFlag();
            return true;
        }
        return false;
    }

    private void calculateComplexityOfExtractedEventList(List<EventHandlerInformation> extractedEventHandlerList) {
        double complexity = 0.0;
        double maxComplexity = 0.0;
        MethodComplexityAnalyzer methodComplexityAnalyzer =
                new MethodComplexityAnalyzer(this.context.getProjectInformation().getProjectInformationExtractor());
        for(EventHandlerInformation eventHandler:extractedEventHandlerList) {
            eventHandler.setComplexity(
                    methodComplexityAnalyzer.getComplexity(eventHandler));
            complexity = eventHandler.getComplexity();
            if(complexity > maxComplexity)
                maxComplexity = complexity;

        }
        NormalizeComplexity(extractedEventHandlerList,maxComplexity);
    }

    private void NormalizeComplexity(List<EventHandlerInformation> extractedEventHandlerList,
                                     double maxComplexity) {
        for(EventHandlerInformation evenHandler: extractedEventHandlerList)
            evenHandler.setMaxComplexity(maxComplexity);
    }

    private double calculateImportanceMetricFor(EventHandlerInformation eventHandler) {
        int startOrDestroyActivity = 0, updateGUI = 0, readOrWriteFormImportanceResource= 0;
        double methodComplexity = 0.0;

        if(codeAnalyzer.isStartOrDestroyActivityBy(eventHandler))
            startOrDestroyActivity = 1;
        if(codeAnalyzer.isUpdateGUIBy(eventHandler))
            updateGUI = 1;
        if(codeAnalyzer.isReadOrWriteFromImportanceResourceBy(eventHandler))
            readOrWriteFormImportanceResource = 1;

        methodComplexity = eventHandler.getComplexity();

        return  SystemParameters.getAlpha() * startOrDestroyActivity +
                SystemParameters.getBeta() * updateGUI +
                SystemParameters.getGama() * readOrWriteFormImportanceResource +
                SystemParameters.getLambda() * methodComplexity;
    }

    public List<String> getAccessibleActivity() {
        List<String> accessibleActivityList = new ArrayList<>();
        List<EventHandlerInformation> eventHandlerList = new ArrayList<>();
        eventHandlerList = codeAnalyzer.getExtractedEventHandlerList();
        for (EventHandlerInformation eventHandler : eventHandlerList) {
            if (codeAnalyzer.isOpenActivityBy(eventHandler.getAttachedMethod())) {
                String activity = codeAnalyzer.getActivityOpenedBy(eventHandler.getAttachedMethod());
                if((activity != "") &&(!accessibleActivityList.contains(activity)))
                    accessibleActivityList.add(activity);
            }
        }
        return accessibleActivityList;
    }

    public String getMainContext(EventHandlerInformation event){
        String context = "";
        if(codeAnalyzer.isDialogMethod(event.getAttachedMethod()) || codeAnalyzer.cotainDialog(event.getAttachedMethod()))
            context = codeAnalyzer.extractLabelForDialogMethod(event) + "Dialog";
        else
            context = this.context.getActivityName();
        return  context;
    }

    public String getParentContext(MethodDeclaration method) {
        String parentContext = "";
        if(codeAnalyzer.isDialogMethod(method))
            parentContext = this.context.getActivityName();
        return parentContext;
    }

    public String getEventLabelFor(EventHandlerInformation eventHandler) {
        String methodName = "";
        if(codeAnalyzer.isDialogMethod(eventHandler.getAttachedMethod())){
            methodName = eventHandler.getAttachedViewLable() + eventHandler.getAttachedViewType() +
                         "_" + StringUtils.capitalize(eventHandler.getAttachedMethod().getNameAsString());
        } else if(ASTUtils.isLocalMethod(eventHandler.getAttachedMethod())){
            if(Utils.isMatch(eventHandler.getAttachedMethod().getNameAsString(),"onListItemClick") &&
               context.isListActivity())
                methodName = "listItems_onItemClick";
            else
               methodName = eventHandler.getAttachedMethod().getNameAsString();
        } else if (codeAnalyzer.isMenuEventHandlerMethod(eventHandler))
            methodName = eventHandler.getAttachedViewId() + "_onMenuItemClick()";
        else
            methodName = codeAnalyzer.extractLabelForInnerMethod(eventHandler.getAttachedMethod());
        return methodName;
    }
}