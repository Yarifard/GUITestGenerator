package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.JavaAnonymousClassCollector;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodFinder {
    public static PsiMethod getMethodByName(PsiClass javaClass, MethodDeclaration name){
        PsiMethod result = null;
        List<PsiClass> classList;
        List<PsiMethod> methodsList = new ArrayList<>();

        PsiMethod[] methodList = javaClass.getMethods();
        methodsList.addAll(Arrays.asList(methodList));

        JavaAnonymousClassCollector javaAnonymousClassCollector = new JavaAnonymousClassCollector();
        javaClass.accept(javaAnonymousClassCollector);
        classList = javaAnonymousClassCollector.getAnonymousClasses();
        methodsList.addAll(getMethodsFromAnonymousClasses(classList));

        for(PsiMethod method:methodsList)
            if(method.getName().toString().equals(name.getName().toString())){
                String methodBody1 = method.getBody().getText();
                String methodBody2 = name.getBody().get().toString();
                methodBody2 = methodBody2.replaceAll("\n","");
                methodBody2 = methodBody2.replaceAll("\r","");
                methodBody2 = methodBody2.replaceAll("\\s","");
                methodBody1 = methodBody1.replaceAll("\n","");
                methodBody1 = methodBody1.replaceAll("\r","");
                methodBody1 = methodBody1.replaceAll("\\s","");
                methodBody1 = methodBody1.trim();
                methodBody2 = methodBody2.trim();
                //String tmp = StringUtils.difference(methodBody1,methodBody2);
                if(methodBody1.contentEquals(methodBody2)){
                    result = method;
                    break;
                }
            }


        return result;
    }
    private static List<PsiMethod> getMethodsFromAnonymousClasses(List<PsiClass> classList){
        List<PsiMethod> methodList = new ArrayList<>();
        for(PsiClass classItem: classList){
            PsiMethod[] methods = classItem.getMethods();
            methodList.addAll(Arrays.asList(methods));
        }
        return methodList;
    }

    public static PsiMethod getMethodByName(PsiMethod[] methodsList,String methodName){
        PsiMethod result = null;
        for(int i = 0; i < methodsList.length; i++){
            if(methodsList[i].getName().equals(methodName)){
                result = methodsList[i];
                break;
            }

        }
        return result;
    }
}
