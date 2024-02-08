package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

import java.util.ArrayList;
import java.util.List;

/*
  Author: Samad Paydar
  Reviewed by Ali Asghar yarifard
 */
public class MethodComplexityAnalyzer {
    private ProjectInformationExtractor projectInformationExtractor;

    public MethodComplexityAnalyzer(ProjectInformationExtractor projectInformationExtractor){
       this.projectInformationExtractor = projectInformationExtractor;
    }

    public MethodComplexity getComplexity(EventHandlerInformation eventHandler){
        return getComplexity(eventHandler,false);
    }

    public MethodComplexity getComplexity(EventHandlerInformation eventHandler, boolean includeCalledLocalMethods){
        MethodComplexity result = new MethodComplexity();
       // PsiClass tmp = eventHandler.getPsiMethod().getContainingClass();


        double cyclomaticComplexity = getCyclomaticComplexity(eventHandler.getAttachedMethod());
        List<PsiMethod> calledMethods = getMethodsDirectlyCalledBy(eventHandler.getPsiMethod());

        calledMethods = replaceAbstractsWithConcretes(calledMethods);
        double calledMethodComplexity = 0.0;
        for (PsiMethod calledMethod : calledMethods) {
            if (calledMethod.equals(eventHandler.getPsiMethod())) {
                //ignore recursive calls
            } else if (isLocalMethod(calledMethod)) {
                String method = calledMethod.getText();
                MethodDeclaration md = StaticJavaParser.parseMethodDeclaration(method);
                EventHandlerInformation event = new EventHandlerInformation();
                event.setPsiMethod(calledMethod);
                event.setMethodDeclaration(md);
                event.setName(md.getName().toString());
                MethodComplexity temp =
                        getComplexity(event, includeCalledLocalMethods);
                calledMethodComplexity += temp.getTotalComplexity();
            } else {
                calledMethodComplexity += getAPIComplexity(calledMethod);
            }
        }

        double intentComplexity = 0.0;
        double asyncComplexity = 0.0;
        if (includeCalledLocalMethods) {
            intentComplexity = getIntentComplexity(eventHandler.getPsiMethod());
            asyncComplexity = getAsyncTaskComplexity(eventHandler.getPsiMethod());
        }

        double halsteadComplexity = getHalsteadComplexity(eventHandler.getPsiMethod());
        result.setCyclomaticComplexity(cyclomaticComplexity);
        result.setCalledMethodComplexity(calledMethodComplexity);
        result.setIntentComplexity(intentComplexity);
        result.setAsyncComplexity(asyncComplexity);
        result.setHalstedComplexity(halsteadComplexity);

        return result;

    }

    private double getHalsteadComplexity(PsiMethod method) {
        HalsteadComplexityAnalyzer halsteadComplexity = new HalsteadComplexityAnalyzer();
        return halsteadComplexity.getComplexity(method);
    }

    private double getAsyncTaskComplexity(PsiMethod method) {
        AsyncTaskComplexityAnalyzer asyncTaskComplexityAnalyzer = new AsyncTaskComplexityAnalyzer(this.projectInformationExtractor);
        return asyncTaskComplexityAnalyzer.getComplexity(method);
    }

    private double getIntentComplexity(PsiMethod method) {
        IntentComplexityAnalyzer intentComplexityAnalyzer = new IntentComplexityAnalyzer(this.projectInformationExtractor);
        return intentComplexityAnalyzer.getComplexity(method);
    }

    //    TODO Since polymorphism is used in the database-related methods,
    //     the Android database API methods are not directly called
    //    hence, this method does not match anything
    private double getAPIComplexity(PsiMethod calledMethod) {
        double weight = 0.0;
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        String[] classNames = {
                "android.database.sqlite.SQLiteDatabase",
                "android.database.sqlite.SQLiteStatement",
                "android.database.Cursor",
                "java.net.URLConnection",
                "java.net.HttpURLConnection",
        };
        double[] weights = {3.0, 3.0, 3.0, 5.0, 5.0};
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            if (className.equals(calledMethodClassName)) {
                weight = weights[i];
                break;
            }
        }

        PsiClassType[] throwsTypes = calledMethod.getThrowsList().getReferencedTypes();
        weight += throwsTypes.length;
        return weight;
    }


    private double getCyclomaticComplexity(MethodDeclaration method) {
        CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer = new CyclomaticComplexityAnalyzer();
        return cyclomaticComplexityAnalyzer.getComplexity(method);
    }
    private List<PsiMethod> getMethodsDirectlyCalledBy(PsiMethod method) {
        MethodCallAnalyzer methodCallAnalyzer = new MethodCallAnalyzer(method);
        method.accept(methodCallAnalyzer);
        return methodCallAnalyzer.getCalledMethods();
    }


    private boolean isLocalMethod(PsiMethod calledMethod) {
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        List<PsiClass> projectJavaClasses = this.projectInformationExtractor.getProjectJavaClassList();
        for (PsiClass projectJavaClass : projectJavaClasses) {
            String className = projectJavaClass.getQualifiedName();
            if (className != null && className.equals(calledMethodClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * this method addresses the problem caused by interfaces and abstract classes.
     * When a method calls a method which belongs to an interface, it is required to look for
     * the concrete implementation of the abstract method.
     * If this is not considered, then using API Complexity concept is not effective
     *
     * @param methods
     * @return
     */
    private List<PsiMethod> replaceAbstractsWithConcretes(List<PsiMethod> methods) {
        for (int i = 0; i < methods.size(); i++) {
            PsiMethod method = methods.get(i);
            if (!isLocalMethod(method)) {
                continue;
            }
            PsiClass theClass = method.getContainingClass();
            if (theClass.isInterface()) {
                List<PsiClass> implementingClasses = getImplementingClasses(theClass);
                if (!implementingClasses.isEmpty()) {
                    for (PsiClass implementingClass : implementingClasses) {
                        PsiMethod concreteMethod = getConcreteMethod(implementingClass, method);
                        if (concreteMethod != null) {
                            methods.set(i, concreteMethod);
                        }
                    }
                }
            } else if (isAbstract(method)) {
//                TODO complete code
            }
        }
        return methods;
    }

    private boolean isAbstract(PsiMethod method) {
        return method.hasModifier(JvmModifier.ABSTRACT);
    }

    private List<PsiClass> getImplementingClasses(PsiClass theInterface) {
        List<PsiClass> implementingClasses = new ArrayList<>();
        List<PsiClass> projectClasses = projectInformationExtractor.getProjectJavaClassList();
        for (PsiClass projectClass : projectClasses) {
            PsiClassType[] types = projectClass.getImplementsListTypes();
            for (PsiClassType type : types) {
                if (type.getClassName().equals(theInterface.getName())) {
                    implementingClasses.add(projectClass);
                }
            }
        }
        return implementingClasses;
    }

    private PsiMethod getConcreteMethod(PsiClass theClass, PsiMethod method) {
        PsiMethod[] concreteClassMethods = theClass.getMethods();
        for (PsiMethod concreteMethod : concreteClassMethods) {
            if (matches(concreteMethod, method)) {
                return concreteMethod;
            }
        }
        return null;
    }

    private boolean matches(PsiMethod method1, PsiMethod method2) {
        if (method1.getName().equals(method2.getName())) {
            JvmParameter[] parameters1 = method1.getParameters();
            JvmParameter[] parameters2 = method2.getParameters();
            boolean match = true;
            if (parameters1 != null && parameters2 != null) {
                match = parameters1.length == parameters2.length;
                if (match) {
                    for (int i = 0; i < parameters1.length; i++) {
                        JvmParameter parameter1 = parameters1[i];
                        JvmParameter parameter2 = parameters2[i];
                        if (!parameter1.getType().equals(parameter2.getType())) {
                            match = false;
                            break;
                        }
                    }
                }
            }
            return match;
        }
        return false;
    }

}
