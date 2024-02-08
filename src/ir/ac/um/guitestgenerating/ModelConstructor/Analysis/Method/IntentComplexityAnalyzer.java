package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.intellij.psi.*;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.ClassComplexityAnalyzer;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;

import java.util.List;

public class IntentComplexityAnalyzer {
    private ProjectInformationExtractor projectInformationExtractor;

    public IntentComplexityAnalyzer(ProjectInformationExtractor  projectInformationExtractor) {
        this.projectInformationExtractor = projectInformationExtractor;
    }

    public double getComplexity(PsiMethod method) {
        IntentFinder intentFinder = new IntentFinder(this.projectInformationExtractor);
        method.accept(intentFinder);
        List<PsiClass> intentClasses = intentFinder.getIntentClasses();
        ClassComplexityAnalyzer classComplexityAnalyzer = new ClassComplexityAnalyzer(this.projectInformationExtractor);
        double complexity = 0.0;
        for (PsiClass intentClass : intentClasses) {
            complexity += classComplexityAnalyzer.getComplexity(intentClass);
        }
        return complexity;
    }


}
