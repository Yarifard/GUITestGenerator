package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNewExpression;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class AsyncTaskFinder extends JavaRecursiveElementVisitor {

    private ProjectInformationExtractor projectInformationExtractor;
    private List<PsiClass> asyncTaskClasses;

    public AsyncTaskFinder(ProjectInformationExtractor projectInformationExtractor) {
        this.projectInformationExtractor = projectInformationExtractor;
        asyncTaskClasses = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            PsiJavaCodeReferenceElement reference = expression.getClassReference();
            if (reference != null) {
                String className = reference.getQualifiedName();
                PsiClass theClass = projectInformationExtractor.getProjectClassByName(className);
                if (theClass != null && isAnAsyncTask(theClass)) {
                    asyncTaskClasses.add(theClass);
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    public List<PsiClass> getAsyncTaskClasses() {
        return asyncTaskClasses;
    }

    private boolean isAnAsyncTask(PsiClass theClass) {
        PsiClass[] superClasses = theClass.getSupers();
        for (PsiClass superClass : superClasses) {
            if (superClass.getQualifiedName().startsWith("android.os.AsyncTask")) {
                return true;
            }
        }
        return false;
    }
}

