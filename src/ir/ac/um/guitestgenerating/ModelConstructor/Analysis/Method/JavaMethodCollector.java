package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;


import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

public class JavaMethodCollector  extends JavaRecursiveElementVisitor {
    List<PsiMethod> projectJavaMethods;
    public JavaMethodCollector(){
        projectJavaMethods = new ArrayList<>();
    }

}
