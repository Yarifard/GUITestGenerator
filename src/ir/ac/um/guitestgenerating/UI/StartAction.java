package ir.ac.um.guitestgenerating.UI;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.ui.content.Content;
import ir.ac.um.guitestgenerating.ModelConstructor.ModelsExtractor;
import ir.ac.um.guitestgenerating.TestGeneration.GUITestGenerator;
import ir.ac.um.guitestgenerating.Util.Constants;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;
import org.jetbrains.annotations.NotNull;

public class StartAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        processProject(project, psiElement);
    }

    private void processProject(Project project, PsiElement psiElement) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.PLUGIN_NAME);
        int i = 0;
        ConsoleView consoleView = Utils.getConsoleView();
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Utils.setConsoleView(consoleView);
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), Constants.PLUGIN_NAME, true);
            toolWindow.getContentManager().addContent(content);
        }
        toolWindow.show(null);
        if(psiElement ==null)
            Utils.showMessage("null");
        else
            Utils.showMessage("not null");
        Utils.showMessage("Hello. This is start point");
        ProjectInformation projectInformation = new ProjectInformation(project,psiElement);
        if(projectInformation.collectInformation()){
            Utils.showMessage("OK");
            ModelsExtractor modelsExtractor = new ModelsExtractor(projectInformation);
            GUITestGenerator guiTestGenerator = new GUITestGenerator(projectInformation,modelsExtractor);
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                public void run() {
                    //TODO: we currently implemented model extractor thread.
                /*
                  In the future, we start other threads to GUI exploration and test generation.
                 */
                   ApplicationManager.getApplication().runReadAction(modelsExtractor);
                   ApplicationManager.getApplication().runReadAction(guiTestGenerator);
                }
            });
        }
        else
            Utils.showMessage("Failed to collect project information.");
        Utils.showMessage("Finished");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        boolean enabled = project != null && (psiElement instanceof PsiJavaDirectoryImpl)
                && ((PsiDirectory) psiElement).getVirtualFile().getCanonicalPath().equals(
                project.getBasePath());
        anActionEvent.getPresentation().setEnabledAndVisible(enabled);
    }
}
