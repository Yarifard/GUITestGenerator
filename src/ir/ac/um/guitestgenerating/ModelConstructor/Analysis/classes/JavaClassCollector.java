package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class JavaClassCollector extends JavaRecursiveElementVisitor{
    private List<PsiClass> javaClasses;


    public JavaClassCollector() {
        Utils.showMessage("I'm in JavaClassCollector:Constructor -->start");
        javaClasses = new ArrayList<>();
        Utils.showMessage("I'm in JavaClassCollector:Constructor -->end");
    }

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);
        Utils.showMessage("I'm in JavaClassCollector:visitJavaFile-->Start");
        if (psiJavaFile.getName().endsWith(".java") && !psiJavaFile.getName().equals("R.java")) {
            PsiClass[] psiClasses = psiJavaFile.getClasses();
            for (PsiClass psiClass : psiClasses) {
                javaClasses.add(psiClass);
            }
        }

    }

    public List<PsiClass> getJavaClasses(){
        return javaClasses;
    }

}
