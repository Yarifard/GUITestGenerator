package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.List;

public class ClassFinder {
    public static PsiClass getClassByName(List<PsiClass> javaClass, String name){
        PsiClass result = null;
        for(PsiClass psiElement:javaClass){
            if(psiElement.getName().equals(name)){
                result = psiElement;
                break;
            }
        }
        return result;
    }


}
