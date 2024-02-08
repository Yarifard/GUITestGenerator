package ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.psi.PsiMethod;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method.MethodComplexity;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class EventHandlerInformation {
    private String mainContext;
    private String context_title;
    private String context_description;
    private String title;
    private String name;
    private int level;
    private boolean flag;
    private boolean isAlternativePathFlag;
    private int preConditionInvarId;
    private int postConditionInvarId;
    private double maxComplexity;
    private ViewInformation attachedView;
    private MethodDeclaration md;
    private PsiMethod psiMethod;
    private List<Widget> usedWidgets;
    private int keyOfLabel;
    private MethodComplexity complexity;
    private String targetActivity;
    private String sourceActivity;
    private boolean independentFlag;
    private boolean executable;
    private boolean importantFlag;
    private List<EventHandlerInformation> childEventHandlerInformation;
    private EventHandlerInformation parentEventHandlerInformation = null;
    private List<EventHandlerInformation> dependentEventHandlers;


    public EventHandlerInformation(EventHandlerInformation method){
        this.mainContext = "";
        this.context_title = "";
        this.context_description = "";
        this.title = "";
        this.name = method.getName();
        this.md = method.getAttachedMethod();
        this.psiMethod = method.getPsiMethod();
        this.level       = 0;
        this.flag = false;
        this.isAlternativePathFlag  = false;
        this.preConditionInvarId = -1;
        this.postConditionInvarId = -1;
        this.attachedView = new ViewInformation();
        this.usedWidgets = null;
        this.childEventHandlerInformation = new ArrayList<>();
        this.keyOfLabel =-1;
        this.complexity = null;
        this.maxComplexity = 1.0;
        this.targetActivity = "";
        this.sourceActivity = "";
        this.dependentEventHandlers = new ArrayList<>();
        this.independentFlag = false;
        this.executable = false;
    }

    public EventHandlerInformation(){
        this.mainContext = "";
        this.context_title = "";
        this.context_description = "";
        this.title = "";
        this.name = "";
        this.md = null;
        this.psiMethod = null;
        this.level       = 0;
        this.preConditionInvarId = -1;
        this.postConditionInvarId = -1;
        this.attachedView = new ViewInformation();
        this.usedWidgets = null;
        this.childEventHandlerInformation = new ArrayList<>();
        this.keyOfLabel =-1;
        this.complexity = null;
        this.maxComplexity = 1.0;
        this.targetActivity = "";
        this.sourceActivity = "";
        this.dependentEventHandlers = new ArrayList<>();
        this.independentFlag = false;
        this.executable = false;
        this.flag = false;
        this.isAlternativePathFlag = false;
    }

    public boolean hasParent(){
        if(this.parentEventHandlerInformation != null)
            return true;
        return false;
    }

    public String getMainContext(){ return  mainContext;}
    public String getContext_title(){return this.context_title;}

    public String getContext_description(){return this.context_description;}

    public String getTitle(){return this.title;}
    public boolean getFlag(){ return flag; }
    public boolean isSetAlternativePath(){return isAlternativePathFlag;}


    public PsiMethod getPsiMethod(){ return  this.psiMethod; }

    public EventHandlerInformation getParentEventHandlerInformation(){ return parentEventHandlerInformation;}
    public List<EventHandlerInformation> getChildEventHandlers(){ return childEventHandlerInformation;}

    public String getTargetActivity(){ return targetActivity;}

    public String getSourceActivity(){ return sourceActivity;}

    public int getTestInstructionsKey() {
        return keyOfLabel;
    }

    public int getPreConditionInvarId(){return  this.preConditionInvarId;}

    public int getPostConditionInvarId(){ return this.postConditionInvarId;}

    public List<Widget> getUsedWidgets() {
        return usedWidgets;
    }

    public double getComplexity() {
        return complexity.getTotalComplexity()/ maxComplexity;
    }

    public MethodDeclaration getAttachedMethod() {
        return md;
    }

    public String getName() {
        return name;
    }
    public boolean isImportant(){return importantFlag;}
    public boolean isIndependent(){return independentFlag;}
    public boolean isExecutable(){return executable;}
    public int getLevel() {
        return level;
    }

    public int getKeyOfLabel(){
        return  keyOfLabel;
    }

    public String getAttachedViewType(){ return attachedView.getViewType(); }

    public String getAttachedViewId(){ return attachedView.getViewId(); }
    public String getAttachedViewTag(){ return attachedView.getTag(); }
    public String getAttachedViewContentDescription(){ return  attachedView.getContentDesciption();}

    public String getAttachedViewLable() { return attachedView.getTitle(); }
    public boolean hasChildEvents(){
        if(childEventHandlerInformation.isEmpty())
            return false;
        return true;
    }

    public String getBindingName(){
        return attachedView.getBindingName();
    }
    public boolean isOptionMenuStyle(){ return attachedView.isOptionMenuStyle();}
    public List<EventHandlerInformation> getDependentEventHandlersList(){
        return dependentEventHandlers;
    }
    public void setIndependentFlag(){
        this.independentFlag = true;
    }
    public void setExecutableFlag(){ this.executable = true;}
    public void setAlternativePathFlag(){this.isAlternativePathFlag = true;}
    public  void appendDependentEventHandler(EventHandlerInformation event){
        this.dependentEventHandlers.add(event);
    }

    public void loadDependentEvents(List<EventHandlerInformation> depEventList){
        depEventList.addAll(depEventList);
    }

    public void setMainContext(String mainContext){ this.mainContext= mainContext;}
    public void setTitle(String title){ this.title = title;}

    public void setAttachedView(ViewInformation attachedView) {
        this.attachedView = attachedView;
    }
     public void setOptionMenuStyle(boolean boolValue){ attachedView.setOptionMenuStyle(boolValue);}

    public void setMaxComplexity(double maxComplexity){
        this.maxComplexity = maxComplexity;
    }

    public void setKeyOfLabel(int key) {
        this.keyOfLabel = key;
    }

    public void setFlag(boolean flag) { this.flag = flag;}

    public void setImportantFlag(){ this.importantFlag = true;}

    public void setPostConditionInvarId(int id){ this.postConditionInvarId = id;}

    public void setPreConditionInvarId(int id){ this.preConditionInvarId = id;}

    public void setMethodDeclaration(MethodDeclaration md) {
        this.md = md;
    }

      public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    public void setPsiMethod(PsiMethod psiMethod){ this.psiMethod = psiMethod;}

    public void setChildEventHandler(EventHandlerInformation eventHandlerInformation){
        childEventHandlerInformation.add(eventHandlerInformation);}
    public void setParentEventHandler(EventHandlerInformation eventHandlerInformation){
        parentEventHandlerInformation = eventHandlerInformation;}

    public void setComplexity(MethodComplexity complexity){
        this.complexity = complexity;
    }

    public void setUsedWidgets(List<Widget> usedWidgets) {
        this.usedWidgets = usedWidgets;
    }

    public void setContext_title(String context_title){ this.context_title = context_title;}

    public void setContext_description(String context_description){ this.context_description = context_description;}

    public void setSourceActivity(String sourceActivity){
        this.sourceActivity = sourceActivity;
    }

    public void setAttachedViewType(String viewType){
         attachedView.setViewType(viewType);
    }

    public void setAttachedViewId(String viewId){
        attachedView.setViewId(viewId);
    }

    public void setAttacheViewLabel(String viewLabel){
        attachedView.setTitle(viewLabel);
    }

    public void setAttachedViewTag(String viewTag){ attachedView.setViewTag(viewTag);}

    public void setAttachedViewBindingName(String bindingName){
        attachedView.setBindingName(bindingName);
    }

    public void setAttachedViewContentDescription(String contentDescription) {
        attachedView.setContentDesciption(contentDescription);
    }

    public void setTargetActivity(String activityName){
        this.targetActivity = activityName;
    }

    public boolean isOpenActivity(){
        boolean result = false;
        if(sourceActivity != targetActivity)
            if(targetActivity != "back")
                result = true;
        return result;
    }

    public void setDependentList(List<EventHandlerInformation> dependentEventList) {
        this.dependentEventHandlers = dependentEventList;
    }
}
