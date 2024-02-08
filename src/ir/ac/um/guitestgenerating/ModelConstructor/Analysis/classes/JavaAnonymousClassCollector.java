package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes;

import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFactoryImpl;

import java.util.ArrayList;
import java.util.List;

public class JavaAnonymousClassCollector extends JavaRecursiveElementVisitor {
    private List<PsiClass> anonymousClasses;


    public JavaAnonymousClassCollector(){

        this.anonymousClasses = new ArrayList<>();
    }

    @Override
    public void visitAnonymousClass(PsiAnonymousClass aClass) {
        super.visitAnonymousClass(aClass);
        anonymousClasses.add(aClass);
    }

    public List<PsiClass> getAnonymousClasses(){return this.anonymousClasses;}

    public List<PsiClass> getProjectAnonymousClasses(){
        return this.anonymousClasses;
    }

}
