package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method.MethodComplexityAnalyzer;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

public class ClassComplexityAnalyzer {

    private ProjectInformationExtractor projectInformationExtractor;

    public ClassComplexityAnalyzer(ProjectInformationExtractor projectInformationExtractor) {
        this.projectInformationExtractor = projectInformationExtractor;
    }

    public double getComplexity(PsiClass theClass) {
        double complexity = 0.0;
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(projectInformationExtractor);
        PsiMethod[] methods = theClass.getMethods();
        for (PsiMethod method : methods) {
            String methodTemp = method.getText();
            MethodDeclaration md = StaticJavaParser.parseMethodDeclaration(methodTemp);
            EventHandlerInformation eventHandler = new EventHandlerInformation();
            eventHandler.setName(method.getName());
            eventHandler.setMethodDeclaration(md);
            eventHandler.setPsiMethod(method);
            complexity += methodComplexityAnalyzer.getComplexity(eventHandler)
                                                  .getTotalComplexity();
        }
        if(methods.length > 0) {
            complexity = complexity / methods.length;
        }

        return complexity;
    }

}
