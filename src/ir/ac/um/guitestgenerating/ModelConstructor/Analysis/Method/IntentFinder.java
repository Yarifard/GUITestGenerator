package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;
import com.intellij.psi.*;
import ir.ac.um.guitestgenerating.Project.ProjectInformationExtractor;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class IntentFinder extends JavaRecursiveElementVisitor {
    private ProjectInformationExtractor projectInformationExtractor;
    private List<PsiClass> intentClasses;

    public IntentFinder(ProjectInformationExtractor projectInformationExtractor) {
        this.projectInformationExtractor = projectInformationExtractor;
        intentClasses = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            PsiJavaCodeReferenceElement reference = expression.getClassReference();
            if (reference != null && reference.getQualifiedName() != null) {
                String name = reference.getQualifiedName();
                if (name.equals("android.content.Intent") || name.equals("Intent")) {
                    PsiExpressionList list = expression.getArgumentList();
                    if (list.getExpressionCount() > 1) {
                        PsiExpression secondArgument = list.getExpressions()[1];
                        String className = secondArgument.getType().getCanonicalText();
                        final String CLASS_PREFIX = "java.lang.Class";
                        PsiClass theClass = null;
                        if (className != null && className.startsWith(CLASS_PREFIX)) {
                            className = className.replace(CLASS_PREFIX, "");
                            className = className.replace("<", "");
                            className = className.replace(">", "");
                            theClass = projectInformationExtractor.getProjectClassByName(className);
                        }
                        if(theClass == null) {
                            className = secondArgument.getText();
                            final String CLASS_POSTFIX = ".class";
                            if (className != null && className.endsWith(CLASS_POSTFIX)) {
                                className = className.replace(CLASS_POSTFIX, "");
                                theClass = projectInformationExtractor.getProjectClassByName(className);
                            }
                        }
                        if (theClass != null) {
                            intentClasses.add(theClass);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    public List<PsiClass> getIntentClasses() {
        return intentClasses;
    }


}

