package ir.ac.um.guitestgenerating.Project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import ir.ac.um.guitestgenerating.Database.DatabaseAdapter;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Taging.POSTagger;
import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantRepository;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ValidTestData.TestDataRepository;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProjectInformation {
    private Project project;
    private String testSubPath;
    private String appPackageTitle;
    private int counter;
    private PsiElement projectElement;
    private VirtualFile manifestFile;
    private VirtualFile srcDirectory;
    private VirtualFile baseDirectory;
    private VirtualFile mainDirectory;
    private VirtualFile menusDirectory;
    private VirtualFile valuesDirectory;
    private VirtualFile layoutsDirectory;
    private VirtualFile resourcesDirectory;
    private VirtualFile androidTestDirectory;
    private POSTagger posTagger;
    private MapCollection labelMap;
    private DatabaseAdapter dbAdapter;
    private List<PsiClass> javaClasses;
    private PsiClass launcherActivityClass;
    private TestDataRepository testDataRepository;
    private InvariantRepository invariantRepository;
    private Map<String , String> validTestDataCollection;
    private ProjectInformationExtractor projectInformationExtractor;
    private List<EventHandlerInformation> dependentEventsList;
    private List<EventHandlerInformation> independentEventsList;
    private List<String> appPermissionsList;
    private List<String> complexTestScenariosPatterns;



    public ProjectInformation(Project project,PsiElement psiElement){
        this.project = project;
        this.counter = 0;
       // this.packageName = "";
        this.projectElement = psiElement;
        this.labelMap = new MapCollection();
        this.validTestDataCollection = new HashMap<>();
        this.posTagger = new POSTagger();
        this.dbAdapter = null;
        this.testDataRepository = new TestDataRepository();
        this.invariantRepository = new InvariantRepository(this);
        this.projectInformationExtractor = new ProjectInformationExtractor(this);
        this.dependentEventsList = new ArrayList<>();
        this.independentEventsList = new ArrayList<>();
        this.complexTestScenariosPatterns = new ArrayList<>();

    }

    public boolean collectInformation() {

        if (initializeDirectories()) {
            this.manifestFile = projectInformationExtractor.getAndroidManifestFile(this.mainDirectory);
            if (this.manifestFile == null) {
                Utils.showMessage("Failed to detect manifest file");
                return false;
            }

            this.launcherActivityClass = projectInformationExtractor.getLuncherActivity(this.manifestFile);
            if (this.launcherActivityClass == null) {
                Utils.showMessage("Failed to detect launcher activity in manifest file");
                return false;
            }

            this.appPackageTitle = projectInformationExtractor.getAppPackageTitle();
            if (this.appPackageTitle == null) {
                Utils.showMessage("Failed to detect app package name");
                return false;
            }


            this.appPermissionsList = projectInformationExtractor.getAppPermissionsList(this.manifestFile);

            this.javaClasses = projectInformationExtractor.getProjectJavaClassList();
            if(this.javaClasses == null)
                return false;

            this.dbAdapter = projectInformationExtractor.prepareDatabase(getPackageName());
            invariantRepository.setAdapter(this.dbAdapter);
            if(this.dbAdapter == null){
                Utils.showMessage("There is a problem when I tried for connecting the database!!!");
                return false;
            }

//           if(!this.invariantRepository.collectGUIInvariants(getPackageName()))
//               return false;
            return true;
        }
        else
          Utils.showMessage("Failed to initialize directories");
        return false;
    }

    private boolean initializeDirectories(){
        this.baseDirectory = LocalFileSystem.getInstance().findFileByPath(this.project.getBasePath());
        if(this.baseDirectory == null)
            return false;

        this.srcDirectory  = projectInformationExtractor.getSrcDirectory(this.baseDirectory);
        if(this.srcDirectory == null)
            return false;

        this.mainDirectory = projectInformationExtractor.getMainDirectory(this.srcDirectory);
        if(this.mainDirectory == null)
            return false;

        this.resourcesDirectory = projectInformationExtractor.getResourceDirectory(this.mainDirectory);
        if(resourcesDirectory == null)
            return false;

        this.androidTestDirectory = projectInformationExtractor.getAndroidTestDirectory(this.srcDirectory);
        if(androidTestDirectory == null)
            return  false;

        this.layoutsDirectory = projectInformationExtractor.getLayoutsDirectory(resourcesDirectory);
        if(this.layoutsDirectory == null)
            return false;
        this.menusDirectory = projectInformationExtractor.getMenusDirectory(resourcesDirectory);

        this.valuesDirectory = projectInformationExtractor.getValuesDirectory(resourcesDirectory);
        if(this.valuesDirectory == null)
            return false;
        return true;
    }

    public void setManifestFile(VirtualFile manifestFilePath){
        this.manifestFile = manifestFilePath;
    }

    public void setBaseDirectory(VirtualFile baseDirectory){
        this.baseDirectory = baseDirectory;
    }

    public void setMainDirectory(VirtualFile mainDirectory){
        this.mainDirectory = mainDirectory;
    }

    public void setResourcesDirectory(VirtualFile resDir){
        this.resourcesDirectory = resDir;
    }

    public void setLayoutsDirectory(VirtualFile layoutsDirectory){
        this.layoutsDirectory = layoutsDirectory;
    }

    public void setValuesDirectory(VirtualFile valuesDirectory){
        this.valuesDirectory = valuesDirectory;
    }

    public void setMenusDirectory(VirtualFile menusDirectory){ this.menusDirectory = menusDirectory;}
    public void setTestSubPath(String testSubPath){ this.testSubPath = testSubPath;}

    public void setSrcDirectory(VirtualFile srcDirectory){ this.srcDirectory = srcDirectory;}

    public String getPackageName(){ return
            this.appPackageTitle.substring(appPackageTitle.lastIndexOf('.') +1 );
    }
     public String getAppPackage(){ return  appPackageTitle; }

    public int setLabelInLabelsCollection(String label){
        int labelKey = -1;
        if(labelMap.putLabel(label))
            labelKey = labelMap.getKeyValue();
        return labelKey;

    }

    public boolean replaceLabel(int key, String label){
        return labelMap.replaceLabel(key,label);
    }

    public String getLabelContent(int keyValue) {
        return labelMap.getLabel(keyValue);
    }
    public String getTestSubPath(){ return testSubPath;}

    public InvariantRepository getGuiInvariantRepository(){
        return invariantRepository;
    }

    public TestDataRepository getDataRepository(){
        return testDataRepository;
    }

    public VirtualFile getManifestFile(){ return this.manifestFile;}
    public VirtualFile getAndroidTestDirectory(){ return this.androidTestDirectory; }
    public Project getProjectObject(){ return this.project; }
    public PsiElement getProjectElement(){return this.projectElement;}
    public PsiClass getLuncherActivity(){
        return  this.launcherActivityClass;
    }

    public POSTagger   getPosTagger(){ return this.posTagger;}
    public VirtualFile getBaseDirectory(){ return this.baseDirectory;}
    public VirtualFile getMainDirectory(){ return this.mainDirectory;}
    public VirtualFile getMenusDirectory(){ return this.menusDirectory;}
    public VirtualFile getLayoutsDirectory(){ return layoutsDirectory;}
    public VirtualFile getValuesDirectory(){ return valuesDirectory;}
    public VirtualFile getSrcDirectory(){ return srcDirectory;}
    public VirtualFile getResourcesDirectory(){ return resourcesDirectory;}
    public List<String> getAppPermissionsList(){return appPermissionsList;}
    public List<PsiClass> getProjectJavaClassList(){ return javaClasses;}
    public DatabaseAdapter getDbAdapter(){ return dbAdapter;}
    public List<EventHandlerInformation> getDependentEventsList(){return dependentEventsList;}
    public List<EventHandlerInformation> getIndependentEventsList(){return independentEventsList;}
    public List<String>  getComplexTestScenariosPatterns(){ return  complexTestScenariosPatterns;}
    public ProjectInformationExtractor getProjectInformationExtractor(){
        return  projectInformationExtractor;
    }
    public boolean addToIndependentEventsList(EventHandlerInformation event){
        return independentEventsList.add(event);
    }
    public boolean addToDependentEventsList(EventHandlerInformation event){
        return dependentEventsList.add(event);
    }
    public void appendIntoComplexTestScenario(String testScenario){
        this.complexTestScenariosPatterns.add(testScenario);
    }
    public void appendIntoComplexTestScenario(List<String> testScenarios){
        this.complexTestScenariosPatterns.addAll(testScenarios);
    }
    public  int getCounter(){
        return ++counter;
    }



}
