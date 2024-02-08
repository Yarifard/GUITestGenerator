package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.ClassComplexityAnalyzer;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;

import java.util.List;

public class AsyncTaskComplexityAnalyzer {
    private ProjectInformationExtractor projectInformationExtractor;

    public AsyncTaskComplexityAnalyzer(ProjectInformationExtractor projectInformationExtractor) {
        this.projectInformationExtractor = projectInformationExtractor;
    }

    public double getComplexity(PsiMethod method) {
        AsyncTaskFinder asyncTaskFinder = new AsyncTaskFinder(this.projectInformationExtractor);
        method.accept(asyncTaskFinder);
        List<PsiClass> asyncTaskClasses = asyncTaskFinder.getAsyncTaskClasses();
        ClassComplexityAnalyzer classComplexityAnalyzer =
                new ClassComplexityAnalyzer(this.projectInformationExtractor);
        double complexity = 0.0;
        for (PsiClass asyncTaskClass : asyncTaskClasses) {
            double asyncTaskComplexity =classComplexityAnalyzer.getComplexity(asyncTaskClass);
            complexity += asyncTaskComplexity;
        }
        return complexity;
    }

}
