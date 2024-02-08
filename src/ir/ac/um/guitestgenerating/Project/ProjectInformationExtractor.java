package ir.ac.um.guitestgenerating.Project;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.guitestgenerating.Database.DatabaseAdapter;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.XMLFiles.ManifestInformationExtractor;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.ClassFinder;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.JavaAnonymousClassCollector;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.JavaClassCollector;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;


public class ProjectInformationExtractor {

    private ProjectInformation projectInformation;
    private JavaClassCollector javaClassesCollector;
    private JavaAnonymousClassCollector javaAnonymousClassCollector;

    public ProjectInformationExtractor(ProjectInformation projectInformation) {
        Utils.showMessage("I'm in Position 2");
        this.projectInformation = projectInformation;
        projectJavaClassesCollector();
        Utils.showMessage("I'm in Position 22");
    }

    public void projectJavaClassesCollector() {
        Utils.showMessage("I'm in ProjectInformationExtractor:projectJavaClassesCollector-->Start");
        this.javaClassesCollector = new JavaClassCollector();
        this.javaAnonymousClassCollector = new JavaAnonymousClassCollector();
        this.projectInformation.getProjectElement().accept(this.javaClassesCollector);
        this.projectInformation.getProjectElement().accept(javaAnonymousClassCollector);
        Utils.showMessage("I'm in ProjectInformationExtractor:projectJavaClassesCollector-->End");
    }

    public VirtualFile getSrcDirectory(VirtualFile baseDirectory){
        VirtualFile srcDirectory;
        srcDirectory = findSourceDirectory(baseDirectory);
        if(srcDirectory == null)
            Utils.showMessage("Failed to detect source directory.");
        return srcDirectory;
    }

    public VirtualFile getMainDirectory(VirtualFile srcDirectory){
        VirtualFile mainDirectory;
        mainDirectory = findMainDirectory(srcDirectory);
        if(mainDirectory == null)
            Utils.showMessage("Failed to detect main directory.");
        return mainDirectory;

    }

    public VirtualFile getAndroidTestDirectory(VirtualFile srcDirectory){
        VirtualFile androidTestDirectory;
        androidTestDirectory = findAndroidTestDirectory(srcDirectory);
        if(androidTestDirectory == null)
            Utils.showMessage("Failed to detect androidTestDirectory.");
        return androidTestDirectory;
    }

    public VirtualFile getResourceDirectory(VirtualFile mainDirectory){
        VirtualFile resourceDirectory;
        resourceDirectory = findResourcesDirectory(mainDirectory);
        if(resourceDirectory == null)
            Utils.showMessage("Failed to detect resources directory.");
        return resourceDirectory;

    }

    public VirtualFile getLayoutsDirectory(VirtualFile resourceDirectory){
        VirtualFile layoutDirectory;
        layoutDirectory = findLayoutDirectory(resourceDirectory);
        if(layoutDirectory == null)
            Utils.showMessage("Failed to detect layouts directory.");

        return layoutDirectory;
    }

    public VirtualFile getMenusDirectory(VirtualFile resourceDirectory){
        VirtualFile menuDirectory;
        menuDirectory = findMenuDirectory(resourceDirectory);
        if(menuDirectory == null)
            Utils.showMessage("Failed to detect menus directory.");
        return menuDirectory;
    }

    public VirtualFile getValuesDirectory(VirtualFile resourceDirectory){
        VirtualFile valuesDirectory;
        valuesDirectory = findValuesDirectory(resourceDirectory);
        if(valuesDirectory == null)
            Utils.showMessage("Failed to detect values directory.");
        return valuesDirectory;

    }

    private VirtualFile findSourceDirectory(VirtualFile directory) {

        return getChildDirectory("src", directory, true);
    }

    private VirtualFile findAndroidTestDirectory(VirtualFile directory) {
        return getChildDirectory("androidTest", directory, false);
    }

    private VirtualFile findMainDirectory(VirtualFile directory) {

        return getChildDirectory("main", directory, false);
    }

    private VirtualFile findResourcesDirectory(VirtualFile directory) {
        return getChildDirectory("res", directory, true);
    }

    private VirtualFile findLayoutDirectory(VirtualFile directory) {
        return getChildDirectory("layout", directory, true);
    }

    private VirtualFile findMenuDirectory(VirtualFile directory) {

        return getChildDirectory("menu", directory, true);
    }

    private VirtualFile findValuesDirectory(VirtualFile directory) {
        return getChildDirectory("values", directory, true);
    }

    private boolean hasAndroidManifest(VirtualFile directory) {
        boolean result = false;
        VirtualFile[] children = directory.getChildren();
        search:
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                VirtualFile[] children2 = child.getChildren();
                for (VirtualFile child2 : children2) {
                    if (child2.getName().equals("AndroidManifest.xml")) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public VirtualFile getAndroidManifestFile(VirtualFile mainDirectory){
        Utils.showMessage("I'm in ProjectInformationExtractor:getAndroidManifestFile-->Starting");
        VirtualFile result = null;
        VirtualFile[] children = mainDirectory.getChildren();
        search:
        for (VirtualFile child : children) {
            if (!child.isDirectory()) {
                if (child.getName().equals("AndroidManifest.xml")) {
                        result = child;
                        break;
                }
            }
        }

        Utils.showMessage(result.getName());
        Utils.showMessage("I'm in ProjectInformationExtractor:getAndroidManifestFile-->End");
        return result;
    }

    private VirtualFile getChildDirectory(String childDirectoryName, VirtualFile parentDirectory, boolean depthFirst) {
        VirtualFile result = null;
        VirtualFile[] children = parentDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals(childDirectoryName)) {
                    if (childDirectoryName.equals("src")) {
                        if (containsSubDirectories(child, "main", "androidTest")
                                || containsSubDirectories(child, "main", "test")
                                || (containsSubDirectories(child, "main") && hasAndroidManifest(child))) {
                            result = child;
                            break;
                        }
                    } else {
                        result = child;
                        break;
                    }
                } else if (depthFirst) {
                    VirtualFile temp = getChildDirectory(childDirectoryName, child, depthFirst);
                    if (temp != null) {
                        result = temp;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean containsSubDirectories(VirtualFile directory, String... subDirectoryNames) {
        boolean result = true;
        VirtualFile[] children = directory.getChildren();
        for (String subDirectoryName : subDirectoryNames) {
            boolean exists = false;
            for (VirtualFile child : children) {
                if (child.isDirectory() && child.getName().equals(subDirectoryName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                result = false;
                break;
            }
        }
        return result;
    }

    //public VirtualFile getSourceDirectory(){ return  this.srcDirectory;}

    //public VirtualFile getLayoutsDirectory(){ return  this.layoutsDirectory;}

    //public VirtualFile getMenusDirectory(){ return this.menusDirectory; }

    //public VirtualFile getValuesDirectory(){ return  this.valuesDirectory;}

    public PsiClass getLuncherActivity(VirtualFile manifestFile){
        Utils.showMessage("I'm in ProjectInformationExtractor:getLaunchActivity-->Starting");
        PsiClass result = null;
        String manifestFilePath;
        String luncherActivityClassName;

        ManifestInformationExtractor manifestInformationExtractor = new ManifestInformationExtractor(this.projectInformation);
        manifestFilePath = manifestFile.getCanonicalPath();
        Utils.showMessage(manifestFilePath);
        luncherActivityClassName = manifestInformationExtractor.getLuncherActivityClassName();
        Utils.showMessage("the result is: " + luncherActivityClassName);
        result = ClassFinder.getClassByName(
                this.javaClassesCollector.getJavaClasses(),luncherActivityClassName );
        Utils.showMessage("the result is:" + result.getName());
        return result;
    }

    public List<PsiClass> getProjectJavaClassList(){
        return this.javaClassesCollector.getJavaClasses();
    }

    public PsiClass getProjectClassByName(String className) {
        for (PsiClass projectJavaClass : javaClassesCollector.getJavaClasses()) {
            if (projectJavaClass.getQualifiedName().equals(className)) {
                return projectJavaClass;
            }
        }
        return null;
    }

    public List<PsiClass> getProjectJavaAnonymousClassList(){
        return this.javaAnonymousClassCollector.getProjectAnonymousClasses();
    }

    public String getActivityClassPath(String activityName){
        return activityName;
    }

    public List<PsiClass> getClassListByActivityName(String activityName){
        List<PsiClass> activityClassList = new ArrayList<>();

        return activityClassList;
    }


    private String getDatabaseName(){
        String packageName =
                ((PsiJavaFile) projectInformation.getLuncherActivity().getContainingFile()).getPackageName();
        return packageName.substring(packageName.lastIndexOf('.') +1);
    }

    public String getAppPackageTitle() {
        //return getDatabaseName();
        Utils.showMessage("I'm in ProjectInformationExtractor:getLaunchActivity-->Starting");
        String result = "";
        ManifestInformationExtractor manifestInformationExtractor = new ManifestInformationExtractor(this.projectInformation);
        result = manifestInformationExtractor.getAppPackageTitle();
        return result;
    }

    public DatabaseAdapter prepareDatabase(String databaseName) {
        return new DatabaseAdapter(databaseName);
    }


    public List<String> getAppPermissionsList(VirtualFile manifestFile) {
        Utils.showMessage("I'm in ProjectInformationExtractor:getAppPermissions-->Starting");
        List<String> appPermissionList;
        ManifestInformationExtractor manifestInformationExtractor =
                new ManifestInformationExtractor(this.projectInformation);
        appPermissionList = manifestInformationExtractor.getAppPermissions();
        Utils.showMessage("I'm in ProjectInformationExtractor:getAppPermissions-->End");

        return appPermissionList;
    }
}