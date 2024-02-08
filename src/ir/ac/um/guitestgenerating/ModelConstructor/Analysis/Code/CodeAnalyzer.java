package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.intellij.psi.*;
import com.intellij.util.containers.ArrayListSet;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method.MethodFinder;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Taging.POSTagger;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.XMLFiles.LayoutInformationExtractor;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.XMLFiles.*;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.XMLFiles.MenuInformationExtractor;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.classes.ClassFinder;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.Utils;
import opennlp.tools.util.StringUtil;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CodeAnalyzer {
    private final int VERB = 1;
    private final int NONE = 0;
    private  CompilationUnit AST;
    private TypeDeclaration activityNode;
    private String menuFileName;
    private String layoutFileName;
    private List<String> includedLayoutFileNames = new ArrayList<>();
    private List<EventHandlerInformation> methodDeclarationList = new ArrayList<>();
    private List<EventHandlerInformation> emptyMethodDeclarationList = new ArrayList<>();
    private List<EventHandlerInformation> calledMethodDeclarationList = new ArrayList<>();
    private List<EventHandlerInformation> eventHandlerInformationList = new ArrayList<>();
    private List<MethodDeclaration> localMethods = new ArrayList<>();
    private ActivityInformation context;
    private MethodDeclaration onCreate, onStart;
    private PsiClass activityClass;

    public CodeAnalyzer(@NotNull ActivityInformation context) throws FileNotFoundException {
        this.context = context;
        this.AST =  StaticJavaParser.parse(new File(context.getActivityClassPath()));
        initialActivityNode();
        initialLayouts();
        initial();
    }

    public void initial() throws FileNotFoundException{
        StandardASTGenerator tmp =
                new StandardASTGenerator(StaticJavaParser.parse(new File(context.getActivityClassPath()))
                        ,layoutFileName,includedLayoutFileNames);
        this.AST = tmp.get();
        String content = AST.toString();
        PsiFile dumyFile = PsiFileFactory.getInstance(context.getProjectInformation().getProjectObject())
                .createFileFromText("activityClass.java",content);
        activityClass = ((PsiJavaFile) dumyFile).getClasses()[0];
        initialActivityNode();
    }

    public void initialActivityNode() {
        AST.getTypes().forEach(node -> {
            if (node.getClass().toString().contains("ClassOrInterfaceDeclaration")){
                activityNode = node;
            }
        });
    }

    public boolean isListActivity(ClassOrInterfaceDeclaration classNode) {
        NodeList<ClassOrInterfaceType> extendList =
                ((ClassOrInterfaceDeclaration) classNode).getExtendedTypes();
        for(ClassOrInterfaceType node: extendList)
            if(isMatchWithPattern(node.getNameAsString(),"ListActivity")){
                context.setListActivity();
                return true;
            }
        return false;
    }

    public void initialLayouts() {
        initialLayout();
        if(layoutFileName != null){
            LayoutInformationExtractor layoutInformationExtractor =
                    new LayoutInformationExtractor(context.getProjectInformation(), layoutFileName);
            if (layoutInformationExtractor.hasIncludedLayouts())
                includedLayoutFileNames = layoutInformationExtractor.getIncludedLayout();
        }
        else
            includedLayoutFileNames = null;
    }

    public void initialLayout() {
        MethodDeclaration onCreateMethod = getMethodByName(activityNode.getMethods(), "onCreate");
        MethodCallExpr setContentMethod = getMethodCallExprByName(onCreateMethod, "setContentView");
        if(setContentMethod != null){
            String arg = getArgument(setContentMethod, 0);
            layoutFileName = arg.substring(arg.lastIndexOf('.') + 1);
        }
        else
            layoutFileName = null;
    }

    public void analyzeActivitySourceCode() {
        extractAllMethodDeclarationList();
        processAutomaticCalledMethods();
        extractCalledMethodDeclarationList();
        extractEventHandlerList();
        removeEmptyEventHandler();
        decomposeCompactEventHandler();
        calculateEventHandlerLevel();
        updateTargetActivityofEventHandlers();
        collectInformationAboutAttachedView();
        generateTagForExtractedEventHandler();
        generateLabelForExtractedEventHandler();
    }

    private void updateTargetActivityofEventHandlers() {
        for(EventHandlerInformation event :eventHandlerInformationList)
            if(isOpenActivityBy(event.getAttachedMethod()))
                 event.setTargetActivity(extractTargetActivity(event));
            else if(isClosedActivityBy(event.getAttachedMethod()))
                 event.setTargetActivity("back");
        for(EventHandlerInformation event :eventHandlerInformationList)
            if(event.hasChildEvents())
                if(!isMatchWithPattern(event.getSourceActivity(),event.getTargetActivity()))
                    event.setTargetActivity(event.getSourceActivity());

    }

    private void extractAllMethodDeclarationList() {

        AST.findAll(MethodDeclaration.class).forEach(node -> {
            EventHandlerInformation event = new EventHandlerInformation();
            event.setName(node.getName().asString());
            event.setMethodDeclaration(node);
            PsiMethod result = MethodFinder.getMethodByName(activityClass, event.getAttachedMethod());
            event.setPsiMethod(result);
            event.setMainContext(activityClass.getName());
            event.setSourceActivity(activityClass.getName());
            event.setTargetActivity(activityClass.getName());
            methodDeclarationList.add(event);
            if (node.getName().toString().contentEquals("onCreate"))
                onCreate = node;
            else if(node.getName().toString().contentEquals("onStart"))
                onStart = node;

        });
    }

    public boolean isListActivity() {
        NodeList<ClassOrInterfaceType> extendList =
                ((ClassOrInterfaceDeclaration) activityNode).getExtendedTypes();
        for(ClassOrInterfaceType node: extendList)
            if(Utils.isMatchWithPattern(node.getNameAsString(),"ListActivity")){
                return true;
            }
        return false;
    }

    private String extractTargetActivity(EventHandlerInformation event) {
        String[] patterns = {"PhoneCall","SendEmail","CaptureImage","ViewWebPage","ShareContent"};
        String activityName = getActivityOpenedBy(event.getAttachedMethod());
        if(Pattern.isMatch(patterns,activityName))
            return event.getSourceActivity();
        return activityName;
    }

    private void extractCalledMethodDeclarationList() {
        List<MethodCallExpr> calledMethodNames = getCalledMethodNames();
        for (EventHandlerInformation md : methodDeclarationList)
            for (MethodCallExpr calledMethodName : calledMethodNames)
                if (md.getName().toString().equals(calledMethodName.getName().toString()))
                    if (isLocalMethod(calledMethodName)) {
                        if (!calledMethodDeclarationList.contains(md))
                            calledMethodDeclarationList.add(md);
                    }
    }

    private void extractEventHandlerList() {
        for (EventHandlerInformation event : methodDeclarationList)
            if (!calledMethodDeclarationList.contains(event))
                if (isValidEventHandler(event.getAttachedMethod())){
                    if(!ASTUtils.isObjectCreationMethod(event.getAttachedMethod()))
                         eventHandlerInformationList.add(event);
                }
                else{
                    if(isListActivity())
                        if(isMatchWithPattern(event.getAttachedMethod().getNameAsString(),"onListItemClick"))
                            eventHandlerInformationList.add(event);
                    }
    }

    public void removeEmptyEventHandler() {
        List<EventHandlerInformation> emptyEventHandler = new ArrayList<>();
        for (EventHandlerInformation eventHandler : eventHandlerInformationList)
            if (eventHandler.getAttachedMethod().getBody().get().isEmpty())
                emptyEventHandler.add(eventHandler);
        eventHandlerInformationList.removeAll(emptyEventHandler);
    }

    /******************************************************************
     *  This method splits up the method declaration that implemented *
     *  more than one event handler in them.                          *
     ******************************************************************/
    private void decomposeCompactEventHandler() {
        List<EventHandlerInformation> extractedCompactEventHandlerInformation = new ArrayList<>();
        for (EventHandlerInformation eventHandlerInformation : eventHandlerInformationList) {
            if (!eventHandlerInformation.getAttachedMethod().getParentNode().get().getClass().getName().contains("ObjectCreationExpr")) {
                Boolean flag = false;
                List<EventHandlerInformation> tmpList = new ArrayList<>();
                ArrayList<BlockStmt> eventHandlersBody = new ArrayList<>();
                BlockStmt body = new BlockStmt();
                BlockStmt prefix = new BlockStmt();
                BlockStmt postfix = new BlockStmt();
                Statement statement;
                String condition;
                //body = item.getMd().getBody().get().asBlockStmt();
                int numberOfStatements = eventHandlerInformation.getAttachedMethod().getBody().get().getStatements().size();

                /*
                   This codes provide the prefix sections of the likely separated event handlers.
                 */
                int i = 0;
                while (i < numberOfStatements) {
                    statement = StaticJavaParser.parseStatement(eventHandlerInformation.getAttachedMethod().getBody().get().getStatements().get(i).toString());
                    if (!statement.isIfStmt())
                        prefix.addStatement(statement);
                    else {
                        condition = (statement.asIfStmt().getCondition()).toString();
                        condition.trim();
                        condition = condition.replaceAll("\\s", "");
                        if (!(condition.contains("v.getId()==") || condition.contains("==v.getId()")
                                || condition.contains("item.getItemId()==") || condition.contains("==item.getItemId()")))
                            prefix.addStatement(statement);
                        else
                            break;
                    }
                    i++;
                }
                /*
                  The following codes separate event handlers that are compacted in one event handler, if it's exist.
                 */
                if (i < numberOfStatements && i >= 0) {
                    flag = true;
                    statement = StaticJavaParser.parseStatement(eventHandlerInformation.getAttachedMethod().getBody().get().getStatements().get(i).toString());
                    while (statement.isIfStmt()) {
                        EventHandlerInformation tmpNode = new EventHandlerInformation();
                        tmpNode.setMethodDeclaration(new MethodDeclaration());
                        tmpNode.setName(eventHandlerInformation.getName());
                        tmpNode.getAttachedMethod().setName(eventHandlerInformation.getName());
                        tmpNode.getAttachedMethod().setType(eventHandlerInformation.getAttachedMethod().getType());
                        tmpNode.getAttachedMethod().setModifiers(eventHandlerInformation.getAttachedMethod().getModifiers());
                        tmpNode.getAttachedMethod().setParentNode(eventHandlerInformation.getAttachedMethod().getParentNode().get());
                        tmpNode.getAttachedMethod().setRange(eventHandlerInformation.getAttachedMethod().getRange().get());
                        condition = (statement.asIfStmt().getCondition()).toString();
                        condition.trim();
                        condition = condition.replaceAll("\\s", "");
                        String[] parts = condition.split("==");
                        if (condition.contains("v.getId()=="))
                            tmpNode.setAttachedViewId(parts[1].trim());
                        else if (condition.contains("==v.getId()")) {
                            tmpNode.setAttachedViewId(parts[0].trim());
                        } else if (condition.contains("item.getItemId()==")) {
                            tmpNode.setAttachedViewId(parts[1].trim());
                            tmpNode.setAttachedViewType("MenuItem");
                        } else if (condition.contains("==item.getItemId()")) {
                            tmpNode.setAttachedViewId(parts[0].trim());
                            tmpNode.setAttachedViewType("MenuItem");
                        }

                        if (statement.asIfStmt().getThenStmt().isBlockStmt())
                            tmpNode.getAttachedMethod().setBody(statement.asIfStmt().getThenStmt().asBlockStmt());
                        else {
                            BlockStmt tmpBlock = new BlockStmt();
                            tmpBlock.addStatement(statement.asIfStmt().getThenStmt());
                            tmpNode.getAttachedMethod().setBody(tmpBlock);
                        }
                        tmpList.add(tmpNode);
                        if (statement.asIfStmt().getElseStmt().isEmpty()) {
                            i++;
                            break;
                        } else
                            statement = statement.asIfStmt().getElseStmt().get();
                    }//End of while
                }//end of if(i<numberOfStatement)
                /*
                   This section process postfix section of mehtod if it's exist.
                 */
                while (i < numberOfStatements) {
                    // String statementString = ;
                    statement = StaticJavaParser.parseStatement(eventHandlerInformation.getAttachedMethod().getBody().get().getStatements().get(i).toString());
                    postfix.addStatement(statement);
                    i++;
                }
                if (flag) {
                    int id = 1;
                    for (EventHandlerInformation index : tmpList) {
                        NodeList<Statement> st = new NodeList<>();

                        prefix.getStatements().forEach(in -> st.add(in));
                        index.getAttachedMethod().getBody().get().getStatements().forEach(in -> st.add(in));
                        postfix.getStatements().forEach(in -> st.add(in));
                        BlockStmt tmp = new BlockStmt(st);

                        PsiMethod element = (PsiMethod) eventHandlerInformation.getPsiMethod().copy();
                        PsiElementFactory factory = JavaPsiFacade.getInstance(element.getProject()).getElementFactory();
                        PsiCodeBlock methodBody = factory.createCodeBlockFromText(
                                tmp.toString(), null);
                        element.getBody().replace(methodBody);
                        // String test = element.getContainingClass().toString();
                        if (id == 1) {
                            eventHandlerInformation.setPsiMethod(element);
                            eventHandlerInformation.getAttachedMethod().setBody(tmp);
                            eventHandlerInformation.setAttachedViewType(index.getAttachedViewType());
                            eventHandlerInformation.setAttachedViewId(index.getAttachedViewId());
                            id++;
                        } else {
                            index.getAttachedMethod().setBody(tmp);
                            index.setPsiMethod(element);
                            extractedCompactEventHandlerInformation.add(index);
                        }
                    }
                    flag = false;
                }
            }//end of if

        }//end of for(ImportantViewInfo item:...
        if (!extractedCompactEventHandlerInformation.isEmpty())
            extractedCompactEventHandlerInformation.forEach(node -> eventHandlerInformationList.add(node));
    }//End of method

    /*****************************************************
     *    This method process automated call methods     *
     *    and removes them                               *
     * ***************************************************/

    private void processAutomaticCalledMethods() {
        //ToDo: Currently, we remove auromated methods from list.
        //Idead, we must process them before remove if it requies.
        //So, we later maybe process them in this method, if it requires.
        ArrayList<EventHandlerInformation> tmplist = new ArrayList<>();
        for (EventHandlerInformation eventHandlerInformation : methodDeclarationList) {
            switch (eventHandlerInformation.getName().toString()) {
                case "onCreate":
                    extractActivityTitle(eventHandlerInformation);
                    //extractActivityLayoutFileName(eventHandlerInformation.getMd());
                    tmplist.add(eventHandlerInformation);
                    break;
                case "onResume":
                case "onPause":
                case "onCreateContextMenu": //TODO::This case must be more process.
                    tmplist.add(eventHandlerInformation);
                    break;
                case "onCreateOptionsMenu":
                    extractMenuLayoutFileName(eventHandlerInformation.getAttachedMethod());
                    tmplist.add(eventHandlerInformation);
                    break;

            }
        }
        eventHandlerInformationList.removeAll(tmplist);
    }

    private void calculateEventHandlerLevel() {
        BlockStmt blockStmt = new BlockStmt();
        MethodDeclaration parentMethod;
        //ToDO: we must inspects more precise this method's functionality.
        // Because we start from the begining of the event handler list,
        // if it is possible, the child node placed before its parent. this state must be precisely inspect.

        for (EventHandlerInformation eventHandlerInformation : eventHandlerInformationList) {
            if(isListActivity((ClassOrInterfaceDeclaration) activityNode) &&
               ASTUtils.isLocalMethod(eventHandlerInformation.getAttachedMethod()))
                if(isMatchWithPattern(eventHandlerInformation.getAttachedMethod().getNameAsString(),
                        "onListItemClick")){
                    eventHandlerInformation.setLevel(0);
                    continue;
                }
            String parentClassName = getClassName(getParentNode(eventHandlerInformation.getAttachedMethod()));
            if (isPartialMatchWithPattern(parentClassName, "ObjectCreationExpr")) {
                parentMethod = getParentMethodDeclaration(eventHandlerInformation);
                EventHandlerInformation parentEventHandlerInformation;
                if (isEventHandler(parentMethod))
                    parentEventHandlerInformation = findEventHandlerByMethodDeclaration(parentMethod);
                else
                    parentEventHandlerInformation = findCallerEventHandler(parentMethod.getName().toString());

                if (parentEventHandlerInformation == null)
                    eventHandlerInformation.setLevel(0);
                else {
                    parentEventHandlerInformation.setChildEventHandler(eventHandlerInformation);
                    eventHandlerInformation.setParentEventHandler(parentEventHandlerInformation);
                    eventHandlerInformation.setLevel(parentEventHandlerInformation.getLevel() + 1);
                }
            } else
                eventHandlerInformation.setLevel(0);
        }
    }

    /************************************************************************
     *  This method collects the related information (e.g. Id,Label and Type)*
     *  about the extracted event handlers.                                  *
     ******************************************************************/
    private void collectInformationAboutAttachedView() {
        NodeList<Statement> methodBodyStatement;
        String viewType, viewId, viewLabel, bindingVariableName, context;
        for (EventHandlerInformation eventHandlerInformation : eventHandlerInformationList) {
            context = "";
            viewId = "";
            viewLabel = "";
            bindingVariableName = "";
            viewType = "";

            //------------------------------------------------------------------
            if(isListActivity()){
                if(isMatchWithPattern(eventHandlerInformation.getAttachedMethod().getNameAsString(),
                               "onListItemClick")){
                    eventHandlerInformation.setAttachedViewId("android.R.id.list");
                    eventHandlerInformation.setAttachedViewType("ListView");
                    continue;
                }

            }

            if(isDirectAttachedEventHandler(eventHandlerInformation))
                  if(isDialogMethod(eventHandlerInformation.getAttachedMethod())){
                      if (isDefaultDialogMethodPattern(eventHandlerInformation.getAttachedMethod())) {
                          int argIndex = 0;
                          String argument ="";
                          String className = "";
                          MethodCallExpr bindingMethod = extractBindindMethodCallExprFrom(
                                  eventHandlerInformation.getAttachedMethod());
                          className = resolveClassName(eventHandlerInformation.getAttachedMethod(),
                                  ASTUtils.getScope(bindingMethod));
                          viewId = extractViewIdForDefaultDialogMethod(bindingMethod);
                          eventHandlerInformation.setAttachedViewId(viewId);
                          if(isMatchWithPattern(bindingMethod.getNameAsString(),"setButton")){
                              argIndex = 1;
                          }
                          argument = ASTUtils.getArgument((MethodCallExpr) bindingMethod, argIndex);
                          viewLabel = extractLabelFromContent(argument);
                          eventHandlerInformation.setAttacheViewLabel(viewLabel);
                          eventHandlerInformation.setAttachedViewType("Button");
                      }else{
                          bindingVariableName = getViewBindingVariableName(eventHandlerInformation.getAttachedMethod());
                          if (!bindingVariableName.isEmpty())
                              eventHandlerInformation.setAttachedViewBindingName(bindingVariableName);
                          viewType = resolveViewType(eventHandlerInformation);
                          eventHandlerInformation.setAttachedViewType(viewType);
                          if (eventHandlerInformation.getAttachedViewId().isEmpty()) {
                              viewId = extractAttachedViewId(eventHandlerInformation);
                              eventHandlerInformation.setAttachedViewId(viewId);
                          }
                          viewLabel = extractViewLabelFromDialogLayout(eventHandlerInformation);
                          if (!viewLabel.isEmpty())
                              eventHandlerInformation.setAttacheViewLabel(viewLabel);
                      }
                      if (containDialog(eventHandlerInformation.getAttachedMethod())) {

                          EventHandlerInformation dialogEvent = getDialogMethodFrom(eventHandlerInformation);
                          context = extractContext(dialogEvent);
                          if (!context.isEmpty())
                              eventHandlerInformation.setContext_title(context);
                      }
                      else{
                          context = extractContext(eventHandlerInformation);
                          if (!context.isEmpty())
                              eventHandlerInformation.setContext_title(context);
                      }
                  }else if(isMenuEventHandlerMethod(eventHandlerInformation)){
                      if(isMainMenuItemEventHandler(eventHandlerInformation))
                           eventHandlerInformation.setAttachedViewType("MainMenuItem");
                      else
                          eventHandlerInformation.setAttachedViewType("ContextMenuItem");

                      if(isAssignedMenuViewId(eventHandlerInformation)){
                          viewId = extractMenuIdFrom(eventHandlerInformation);
                          if(isDeclaredMenuLayout(eventHandlerInformation)){
                              String menuLayout = extractMenuLayout(eventHandlerInformation);
                              if(isExistMenuItem(menuLayout,viewId)){
                                  eventHandlerInformation.setAttachedViewId(viewId);
                                  viewLabel = extractMenuItemLabelFromLayout(menuLayout,viewId);
                                  if(isOptionMenuStyle(menuLayout,viewId)){
                                      eventHandlerInformation.setOptionMenuStyle(true);
                                      if(!viewLabel.isEmpty())
                                          eventHandlerInformation.setAttacheViewLabel(viewLabel);
                                  }
                                  else
                                       if(!viewLabel.isEmpty())
                                          eventHandlerInformation.setAttachedViewContentDescription(viewLabel);
                              }
                              else{
                                  if(isOptionMenuStyle(eventHandlerInformation,viewId))
                                      eventHandlerInformation.setOptionMenuStyle(true);
                                  viewLabel = extractMenuItemLabelFromSouceCodeById(eventHandlerInformation, viewId);
                                  if(!viewLabel.isEmpty())
                                      eventHandlerInformation.setAttachedViewContentDescription(viewLabel);
                              }
                          }
                          else{
                              if(isOptionMenuStyle(eventHandlerInformation,viewId))
                                  eventHandlerInformation.setOptionMenuStyle(true);
                              viewLabel = extractMenuItemLabelFromSouceCodeById(eventHandlerInformation, viewId);
                              if(!viewLabel.isEmpty())
                                  eventHandlerInformation.setAttachedViewContentDescription(viewLabel);
                          }
                      }
                      else{
                          viewLabel = extractMenuItemLabelFromSouceCode(eventHandlerInformation);
                          if(!viewLabel.isEmpty())
                              eventHandlerInformation.setAttachedViewContentDescription(viewLabel);
                      }
                      if (containDialog(eventHandlerInformation.getAttachedMethod())) {

                          EventHandlerInformation dialogEvent = getDialogMethodFrom(eventHandlerInformation);
                          context = extractContext(dialogEvent);
                          if (!context.isEmpty())
                              eventHandlerInformation.setContext_title(context);
                      }
                  } else{
                      if(isListActivity((ClassOrInterfaceDeclaration) activityNode) &&
                         isMatchWithPattern(eventHandlerInformation.getAttachedMethod().getNameAsString(),"onListItemClick"))
                      {
                         eventHandlerInformation.setAttachedViewId("android.R.id.list");
                         eventHandlerInformation.setAttachedViewType("ListView");
                         eventHandlerInformation.setAttachedViewBindingName("listItems");
                      }
                      else{
                          bindingVariableName = getViewBindingVariableName(eventHandlerInformation.getAttachedMethod());
                          if (!bindingVariableName.isEmpty())
                              eventHandlerInformation.setAttachedViewBindingName(bindingVariableName);
                          viewType = resolveViewType(eventHandlerInformation);
                          eventHandlerInformation.setAttachedViewType(viewType);
                          if (eventHandlerInformation.getAttachedViewId().isEmpty()) {
                              viewId = extractAttachedViewId(eventHandlerInformation);
                              eventHandlerInformation.setAttachedViewId(viewId);
                          }
                          if(!viewId.isEmpty()){
                              viewLabel = extractAttachedViewLableFromLayout(eventHandlerInformation.getAttachedViewId());
                              if (!viewLabel.isEmpty())
                                  eventHandlerInformation.setAttacheViewLabel(viewLabel);
                              String contentDescription = extractAttachedViewContentDesciptionFromLayout(viewId);
                              if(!contentDescription.isEmpty())
                                  eventHandlerInformation.setAttachedViewContentDescription(contentDescription);
                          }
                      }
                      if (containDialog(eventHandlerInformation.getAttachedMethod())) {
                          EventHandlerInformation dialogEvent = getDialogMethodFrom(eventHandlerInformation);
                          context = extractContext(dialogEvent);
                          if (!context.isEmpty())
                              eventHandlerInformation.setContext_title(context);
                      }
                  }
        }
    }

    private String extractViewIdForDefaultDialogMethod(MethodCallExpr bindingMethod) {
        String viewId = "";
        switch (bindingMethod.getNameAsString()){
            case "setPositiveButton": viewId = "android.R.id.button1";break;
            case "setNegativeButton": viewId = "android.R.id.button2";break;
            case "setNeutralButton" : viewId = "android.R.id.button3";break;
            case "setButton"        : String argument = getArgument(bindingMethod,0);
                                      if(argument.endsWith("_POSITIVE"))
                                          viewId = "android.R.id.button1";
                                      else if(argument.endsWith("_NEGATIVE"))
                                          viewId = "android.R.id.button2";
                                      else if(argument.endsWith("_NEUTRAL"))
                                          viewId = "android.R.id.button3";
                                      break;
        }
        return viewId;
    }

    private boolean isExistMenuItem(String menuLayout, String menuId) {
        MenuInformationExtractor menuInformationExtractor =
                new MenuInformationExtractor(context.getProjectInformation(), menuLayout);
        if(menuInformationExtractor.isExistMenuItem(menuId))
            return true;
        return false;
    }

    private String extractMenuItemLabelFromSouceCodeById(EventHandlerInformation eventHandlerInformation,
                                                         String viewId) {
        String viewLabel = "";
        String menuBindingId = "";
        List<MethodCallExpr> menuDeclCalExprs;
        Node containBlocks = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        menuDeclCalExprs = getMethodCallExprsByNameCalledDirectlyBy(containBlocks,"add");
        for(MethodCallExpr item:menuDeclCalExprs)
            if(item.hasScope())
                if(isMatchWithPattern(ASTUtils.getScope(item),"menu")){
                    menuBindingId = ASTUtils.getArgument(item,1);
                    if(isMatchWithPattern(menuBindingId,viewId))
                        return getContent(ASTUtils.getArgument(item,3));
                }
        return viewLabel;
    }

    private boolean isOptionMenuStyle(String menuLayout, String viewId) {
        MenuInformationExtractor menuInformationExtractor = new
                MenuInformationExtractor(context.getProjectInformation(), menuLayout);
        if(menuInformationExtractor.isOptionMenu(viewId))
            return true;
        return false;
    }

    private boolean isOptionMenuStyle(EventHandlerInformation eventHandler, String viewId) {
        Node includedBlock = getIncludedBlockNode(eventHandler.getAttachedMethod());
        MethodDeclaration method = (MethodDeclaration) getParentNode(includedBlock);
        List<MethodCallExpr> methodCallExprs =
                getMethodCallExprsByNameCalledDirectlyBy(method,"setShowAsAction");
        if(methodCallExprs.isEmpty())
            return true;

        return false;
    }

    private String extractAttachedViewContentDesciptionFromLayout(String viewId) {
        String viewContentDescription = "";
        LayoutInformationExtractor layoutInformationExtractor =
                new LayoutInformationExtractor(context.getProjectInformation(), layoutFileName);
        viewContentDescription = layoutInformationExtractor.findViewContentDesciptionById(viewId);
        if(viewContentDescription.isEmpty()){
            if (layoutInformationExtractor.hasIncludedLayouts()) {
                includedLayoutFileNames = layoutInformationExtractor.getIncludedLayout();
                for (String includedLayoutFileName : includedLayoutFileNames) {
                    layoutInformationExtractor.setXmlFile(includedLayoutFileName);
                    viewContentDescription = layoutInformationExtractor.findViewContentDesciptionById(viewId);
                    if (!viewContentDescription.isEmpty()) {
                        break;
                    }
                }
            }
        }
        return viewContentDescription;
    }

    private String extractMenuItemIdFromSouceCode(EventHandlerInformation eventHandlerInformation) {
        String viewId = "";
        MethodCallExpr methodCallExpr = (MethodCallExpr) getParentMethodCallExpr(eventHandlerInformation.getAttachedMethod()).getChildNodes().get(0);
        if(methodCallExpr.getArguments().size() > 1)
            viewId= getArgument(methodCallExpr,1);
        return  viewId;
    }

    private String extractMenuItemLabelFromSouceCode(EventHandlerInformation eventHandlerInformation) {
        String viewLabel = "";
        MethodCallExpr methodCallExpr = (MethodCallExpr) getParentMethodCallExpr(eventHandlerInformation.getAttachedMethod()).getChildNodes().get(0);
        if(methodCallExpr.getArguments().size() == 1)
            viewLabel = getArgument(methodCallExpr,0);
        else
            viewLabel = getArgument(methodCallExpr,3);

        if(viewLabel.startsWith("getString"))
            viewLabel = viewLabel.substring(viewLabel.indexOf('(') + 1, viewLabel.lastIndexOf(')'));
        if(viewLabel.startsWith("R.string"))
            viewLabel = getStringValue(viewLabel);
        return  viewLabel;
    }

    private String extractMenuItemLabelFromSourceCodeById(EventHandlerInformation eventHandlerInformation,
                                                          String viewId) {
        AtomicReference<String> viewLabel = new AtomicReference<>("");
        Node includedBlock = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        List<MethodCallExpr>  callExprs = getMethodCallExprsByNameCalledDirectlyBy(includedBlock,"add");
        callExprs.forEach(callExpr -> {
            if(callExpr.hasScope())
                if(isMatchWithPattern(ASTUtils.getScope(callExpr),"menu")){
                    String menuId = "";
                    if(callExpr.getArguments().size() > 1){
                         menuId = getArgument(callExpr,1);
                         if(isMatchWithPattern(menuId,viewId)){
                             String content = callExpr.getArguments().get(3).toString();
                             if(content.startsWith("getString("))
                                 content = content.substring(content.indexOf('(') + 1, content.lastIndexOf(')'));
                             if(content.startsWith("R.string."))
                                 content = getStringValue(content);
                             viewLabel.set(content);
                         }
                    }
                }
        });

        return viewLabel.get();
    }

    private String getStringValue(String value) {
        String result = "";
        if(value.startsWith("R.string.") || value.startsWith("@string/")){
        StringValueExtractor stringValueExtractor =
                new StringValueExtractor(context.getProjectInformation(), "strings");
        result = stringValueExtractor.findViewLabelById(value);
        }
        return result;
    }

    private boolean isAssignedMenuViewId(EventHandlerInformation eventHandlerInformation) {
        boolean result = false;
        MethodCallExpr methodCallExpr = getParentMethodCallExpr(eventHandlerInformation.getAttachedMethod());
        String content = methodCallExpr.getScope().toString();
        if (isPartialMatchWithPattern(content,"menu.findItem("))
            result = true;
        return result;
    }

    private String extractMenuItemLabelFromLayout(String menuLayout, String viewId) {
        String viewLabel = "";
        MenuInformationExtractor menuInformationExtractor =
                new MenuInformationExtractor(context.getProjectInformation(), menuLayout);
        viewLabel = menuInformationExtractor.findViewLabelById(viewId);
        return viewLabel;
    }

    private String extractMenuLayout(EventHandlerInformation eventHandlerInformation) {
        String menuLayoutFile = "";
        Node includedBlock = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        MethodDeclaration method = (MethodDeclaration) getParentNode(includedBlock);
        List<MethodCallExpr> methodCallExprs = getMethodCallExprsByNameCalledDirectlyBy(method,"inflate");
        menuLayoutFile = methodCallExprs.get(0).getArgument(0).toString();
        return menuLayoutFile.substring(menuLayoutFile.lastIndexOf('.') + 1);
    }


    private boolean isDeclaredMenuLayout(EventHandlerInformation eventHandlerInformation) {
        boolean result = false;
        Node includedBlock = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        MethodDeclaration method = (MethodDeclaration) getParentNode(includedBlock);
        List<MethodCallExpr> methodCallExprs = getMethodCallExprsByNameCalledDirectlyBy(method,"inflate");
        if(!methodCallExprs.isEmpty())
            result = true;
        return result;
    }

    private String extractMenuIdFrom(EventHandlerInformation eventHandlerInformation) {
        String viewId = "";
        MethodCallExpr methodCallExpr = getParentMethodCallExpr(eventHandlerInformation.getAttachedMethod());
        String content = methodCallExpr.getScope().toString();
        viewId = content.substring(content.indexOf('(') + 1, content.indexOf(')'));
        return viewId;
    }

    private MethodCallExpr getParentMethodCallExpr(Node node) {
        while(node.getClass() != MethodCallExpr.class)
            node = getParentNode(node);
        return (MethodCallExpr) node;
    }

    private boolean isMainMenuItemEventHandler(EventHandlerInformation eventHandlerInformation) {
        boolean flag = false;
        Node includedBlock = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        MethodDeclaration method = (MethodDeclaration) getParentNode(includedBlock);
        if(isMatchWithPattern(method.getNameAsString(),"onCreateOptionsMenu"))
            flag = true;
        return flag;
    }

    public boolean isMenuEventHandlerMethod(EventHandlerInformation eventHandlerInformation) {
        return isMatchWithPattern(eventHandlerInformation.getAttachedMethod().getNameAsString(),
                         "onMenuItemClick");
    }

    private String preparedContent(String content){
        if(content.startsWith("getString("))
            content = content.substring(content.lastIndexOf('(') + 1, content.lastIndexOf(')'));
        if(content.startsWith("R.string.")){
            StringValueExtractor stringValueExtractor = new StringValueExtractor(context.getProjectInformation(),"strings");
            content = stringValueExtractor.findViewLabelById(content);
        }
        return content;
    }

    private String extractViewContextFromDefaultDialog(EventHandlerInformation eventHandlerInformation) {
        int argIndex = 0;
        String content = "";
        Node node = getIncludedBlockNode(eventHandlerInformation.getAttachedMethod());
        MethodCallExpr methodCallExpr =
                getMethodCallExprByName(node, "setTitle");
        if (methodCallExpr != null)
            content =  getContent(methodCallExpr.getArgument(argIndex).toString());
        List<VariableDeclarator> localVariable = getLocalVariables(node);
        if(!content.isEmpty())
            if(!isLocalVariable(localVariable,content))
                return content;
            else
                content = extractValue(node,content);
        return content;
    }

    public String extractValue(Node node, String content) {
        AtomicReference<String> result = new AtomicReference<>(content);
        node.findAll(VariableDeclarator.class).forEach(item -> {
            if(isMatchWithPattern(item.getNameAsString(),content)){
                String value = item.getInitializer().get().toString();
                String valueId = "";
                if (value.contains("+")) {
                    String[] parts = value.split("\\+");
                    String resultString = "";
                    for(String part : parts){
                        part = part.trim();
                        if(part.startsWith("getString(")){
                            valueId = part.substring(part.lastIndexOf('(') + 1, part.lastIndexOf(')'));
                            part = getString(valueId);
                            //resultString += part;
                        }
                        else if(part.startsWith("\"") && part.endsWith("\""))
                            part = part.substring(1,part.length()-1);
                        resultString += " " + part;
                    }
                    result.set(resultString);
                } else {
                    if (value.startsWith("getString(")){
                        valueId = value.substring(value.lastIndexOf('(') + 1, value.lastIndexOf(')'));
                        value = getString(valueId);
                    }
                    result.set(value);
                }
            }
        });
        return result.get().trim();
    }

    private String extractAttachedViewTypeFromLayoutById(String viewId) {
        String viewType = "";
        LayoutInformationExtractor layoutInformationExtractor =
                new LayoutInformationExtractor(context.getProjectInformation(), layoutFileName);
        viewType = layoutInformationExtractor.findViewTypeById(viewId);
        if (!viewType.isEmpty())
            return viewType;
        else {
            if (layoutInformationExtractor.hasIncludedLayouts()) {
                includedLayoutFileNames = layoutInformationExtractor.getIncludedLayout();
                for (String includedLayoutFileName : includedLayoutFileNames) {
                    layoutInformationExtractor.setXmlFile(includedLayoutFileName);
                    viewType = layoutInformationExtractor.findViewTypeById(viewId);
                    if (!viewType.isEmpty()) {
                        return viewType;
                    }
                }
            }
        }
        return viewType;

    }

    private String extractAttachedViewLabelFromMenuLayout(EventHandlerInformation eventHandlerInformation) {
        MenuInformationExtractor menuInformationExtractor =
                new MenuInformationExtractor(context.getProjectInformation(), menuFileName);
        return menuInformationExtractor.findViewLabelById(
                eventHandlerInformation.getAttachedViewId());
    }

    private String extractAttachedViewLableFromLayout(String viewId) {
        String viewLabel = "";
        LayoutInformationExtractor layoutInformationExtractor =
                new LayoutInformationExtractor(context.getProjectInformation(), layoutFileName);
        viewLabel = layoutInformationExtractor.findViewLabelById(viewId);
        if(viewLabel.isEmpty()){
            if (layoutInformationExtractor.hasIncludedLayouts()) {
                includedLayoutFileNames = layoutInformationExtractor.getIncludedLayout();
                for (String includedLayoutFileName : includedLayoutFileNames) {
                    layoutInformationExtractor.setXmlFile(includedLayoutFileName);
                    viewLabel = layoutInformationExtractor.findViewLabelById(viewId);
                    if (!viewLabel.isEmpty()) {
                        break;
                    }
                }
            }
        }
        if(!viewLabel.isEmpty() && viewLabel.startsWith("R.strings.")){
            StringValueExtractor stringValueExtractor = new StringValueExtractor(context.getProjectInformation(),"strings");
            viewLabel = stringValueExtractor.findViewLabelById(viewLabel.substring(viewLabel.lastIndexOf('.')+1));
        }
        return viewLabel;
    }

    private String extractViewLabelFromDialogLayout(EventHandlerInformation eventHandler) {
        LayoutInformationExtractor layoutInformationExtractor;
        String dynamicLayoutFileName = getDynamicLayoutFileName(eventHandler);
        layoutInformationExtractor = new LayoutInformationExtractor(context.getProjectInformation(), dynamicLayoutFileName);
        layoutInformationExtractor.setXmlFile(dynamicLayoutFileName);
        return layoutInformationExtractor.findViewLabelById(eventHandler.getAttachedViewId());
    }

    private String extractViewContextFromDialogLayout(EventHandlerInformation eventHandler) {
        LayoutInformationExtractor layoutInformationExtractor;
        String dynamicLayoutFileName = getDynamicLayoutFileName(eventHandler);
        layoutInformationExtractor = new LayoutInformationExtractor(context.getProjectInformation(), dynamicLayoutFileName);
        // layoutInformationExtractor.setXmlFile(dynamicLayoutFileName);
        return layoutInformationExtractor.findViewContext();
    }

    private String extractAttachedViewId(EventHandlerInformation eventHandler) {
        String objectName = getViewBindingVariableName(eventHandler.getAttachedMethod());
        return extractWidgetId(eventHandler.getAttachedMethod(), objectName);
    }

    private boolean isDefaultDialogViewType(String viewType) {
        if (isPartialMatchWithPattern(viewType, "Dialog"))
            return true;
        return false;
    }

    private String extractContext(EventHandlerInformation dialogEventHandler) {
        String context = "";
        String className = "";
        if(ASTUtils.isDefaultDialogMethodPattern(dialogEventHandler.getAttachedMethod())){
            className = resolveClassName(dialogEventHandler.getAttachedMethod(),
                    ASTUtils.getScope(ASTUtils.getParentMethodCallExpr(dialogEventHandler.getAttachedMethod())));
            if(isMatchWithPattern(className,"DatePickerDialog"))
                context = "pick a date";
            else
                context = extractViewContextFromDefaultDialog(dialogEventHandler);
        }
        else{
            context = extractViewContextFromDefaultDialog(dialogEventHandler);
            if(context.isEmpty())
                context = extractViewContextFromDialogLayout(dialogEventHandler);
        }
        return getContent(context);

    }

    private EventHandlerInformation getDialogMethodFrom(EventHandlerInformation eventHandler) {
        for(EventHandlerInformation event: eventHandler.getChildEventHandlers())
            if(ASTUtils.isDialogMethod(event.getAttachedMethod()))
                return event;
        return null;
    }

    private boolean isDirectAttachedEventHandler(EventHandlerInformation eventHandlerInformation) {
        Node node = (Node) eventHandlerInformation.getAttachedMethod();
        String parentClassName = getClassName(getParentNode(node));
        if (isPartialMatchWithPattern(parentClassName, "ObjectCreationExpr"))
            return true;
        return false;
    }

    private void generateTagForExtractedEventHandler() {
        String generatedLabel = "";
        for (EventHandlerInformation eventHandler : eventHandlerInformationList) {
            generatedLabel = extractMeaningfullTagFor(eventHandler);
            eventHandler.setTitle(generatedLabel);
            System.out.println(eventHandler.getTitle());


        }
        refinement(eventHandlerInformationList);
        prints(eventHandlerInformationList);
    }

    private void prints(List<EventHandlerInformation> eventHandlerInformationList) {
        System.out.println("===============" + " " + activityNode.getName() + " ================");
        String label = "";
        for(EventHandlerInformation eventHandler: eventHandlerInformationList){
            if(containDialog(eventHandler.getAttachedMethod())){
                if(isSetFlag(eventHandler)){
                    label = extractNameFromBindingVaribale(eventHandler.getBindingName())
                            + eventHandler.getTitle() + "Dialog";
                    eventHandler.setFlag(false);
                }
                else
                    label = eventHandler.getTitle() + "Dialog";

               if(eventHandler.hasParent() && !ASTUtils.isDialogMethod(eventHandler.getAttachedMethod()))
                    eventHandler.setMainContext(eventHandler.getSourceActivity());
                if(eventHandler.hasChildEvents())
                    for(EventHandlerInformation eventItem: eventHandler.getChildEventHandlers())
                        eventItem.setMainContext(label);
                if(!(label.startsWith("show") || label.startsWith("Show")))
                    label = "Show" + label;

                eventHandler.setTitle(inspectGeneratedLabel(label));
                System.out.println(eventHandler.getTitle());
                continue;
            }
            else if(isDialogMethod(eventHandler.getAttachedMethod())){
                if(eventHandler.hasParent()) {
                    if(containDialog(eventHandler.getParentEventHandlerInformation().getAttachedMethod())){
                        if (isSetFlag(eventHandler.getParentEventHandlerInformation()))
                            label = extractNameFromBindingVaribale(eventHandler.getParentEventHandlerInformation().getBindingName())
                                    + eventHandler.getParentEventHandlerInformation().getTitle()
                                    + "$" + eventHandler.getTitle();
                        else
                            label = eventHandler.getParentEventHandlerInformation().getTitle()
                                    + "$" + eventHandler.getTitle();
                        if(!(label.startsWith("show") || label.startsWith("Show")))
                            label = "Show" + label;
                        eventHandler.setTitle(inspectGeneratedLabel(label));

                    }
                    else{
                        label = generateLabelForDialog(eventHandler.getContext_title(),eventHandler.getAttachedViewType()) + "Dialog";
                        eventHandler.setMainContext(label);
                        label += "$" + eventHandler.getTitle();
                        if(!(label.startsWith("show") || label.startsWith("Show")))
                          label = "Show" + label;

                        eventHandler.setTitle(inspectGeneratedLabel(label));
                        // method.setMainContext(method.getMainContext());
                        //System.out.println(method.getTitle());
                    }
                }
                else{
                    label = generateLabelForDialog(eventHandler.getContext_title(),eventHandler.getAttachedViewType()) + "Dialog";
                    eventHandler.setMainContext(label);
                    label += "$" + eventHandler.getTitle();
                    if(!(label.startsWith("show") || label.startsWith("Show"))){
                        label = "Show" + label;
                    eventHandler.setTitle(inspectGeneratedLabel(label));

                    }
                }
                System.out.println(eventHandler.getTitle());
                continue;
            }
            else if(isMenuEventHandlerMethod(eventHandler)){
               String genLabel = eventHandler.getTitle();
               if(hasIntentPattern(eventHandler.getAttachedMethod())){
                    genLabel = "Open" + genLabel;
                    if(!genLabel.toLowerCase().contains("activity"))
                        genLabel += "Activity";
              }
              genLabel +="ByMenuItem";
              eventHandler.setTitle(inspectGeneratedLabel(genLabel));
              System.out.println(genLabel);
              continue;
            }
            else if(eventHandler.hasChildEvents() && hasOtherTypeEvent(eventHandler)){
                eventHandler.setTitle(inspectGeneratedLabel("Show" + eventHandler.getTitle() + "Panel"));
                System.out.println(eventHandler.getTitle());
                continue;
            }
            else if(hasIntentPattern(eventHandler.getAttachedMethod())){
                 String generatedLabel = "Open" + eventHandler.getTitle();
                 if(!generatedLabel.toLowerCase().contains("activity"))
                    generatedLabel = generatedLabel + "Activity";
                 System.out.println(generatedLabel);
                 eventHandler.setTitle(inspectGeneratedLabel(generatedLabel));
            }
            else{
                eventHandler.setTitle(inspectGeneratedLabel(eventHandler.getTitle()));
                System.out.println(eventHandler.getTitle());
            }
        }
        System.out.println("--------------------------------------------------------");
    }

    private String inspectGeneratedLabel(String label) {
        return label.replaceAll("-","_");
    }

    private boolean hasOtherTypeEvent(EventHandlerInformation method) {
        boolean flag = false;
        for(EventHandlerInformation childEvent: method.getChildEventHandlers())
            if(!ASTUtils.isDialogMethod(childEvent.getAttachedMethod())){
                flag = true;
                break;
            }
        return flag;
    }

    private boolean isSetFlag(EventHandlerInformation eventHandler) {
        if(eventHandler.getFlag())
            return true;
        return false;
    }

    private void refinement(List<EventHandlerInformation> eventHandlerInformationList) {
        for (int i = 0; i < eventHandlerInformationList.size(); i++)
            for (int j = i + 1; j < eventHandlerInformationList.size(); j++) {
                EventHandlerInformation eventItemI = eventHandlerInformationList.get(i);
                EventHandlerInformation eventItemJ = eventHandlerInformationList.get(j);
                if (hasSimilarTitle(eventItemI.getTitle(), eventItemJ.getTitle())) {
                    if (isChild(eventItemI) && isChild(eventItemJ)) {
                        if(hasParentsSimilarTitle(eventItemI,eventItemJ)){
                            if(eventItemI.hasParent())
                                eventItemI.getParentEventHandlerInformation().setFlag(true);
                            else if(eventItemJ.hasParent())
                                eventItemJ.getParentEventHandlerInformation().setFlag(true);
                            else
                                eventItemI.setFlag(true);
                        }

                    }
                }

            }
    }

    private boolean hasParentsSimilarTitle(EventHandlerInformation sourceEventHandler,
                                           EventHandlerInformation destinationEventHandler) {
        String sourceTitle = "", destinationTitle = "";
        if(sourceEventHandler.hasParent() && destinationEventHandler.hasParent()){
            sourceTitle = sourceEventHandler.getParentEventHandlerInformation().getTitle();
            destinationTitle = destinationEventHandler.getParentEventHandlerInformation().getTitle();
            if(isMatchWithPattern(sourceTitle,destinationTitle))
                    return true;

        }
        else if(sourceEventHandler.hasParent()){
             sourceTitle = sourceEventHandler.getParentEventHandlerInformation().getTitle();
             destinationTitle = generateLabelForDialog(destinationEventHandler.getContext_title(),"");
             if(isMatchWithPattern(sourceTitle,destinationTitle))
                 return true;
        }
        else if(destinationEventHandler.hasParent()){
            sourceTitle = generateLabelForDialog(sourceEventHandler.getContext_title(),"");
            destinationTitle = destinationEventHandler.getParentEventHandlerInformation().getTitle() ;
            if(isMatchWithPattern(sourceTitle,destinationTitle))
                return true;
        }
        else{
            sourceTitle = generateLabelForDialog(sourceEventHandler.getContext_title(),"");
            destinationTitle = generateLabelForDialog(destinationEventHandler.getContext_title(),"");
            if(isMatchWithPattern(sourceTitle,destinationTitle))
                return true;


        }
            return false;
    }

    private boolean isChild(EventHandlerInformation eventHandler) {
        if(isDialogMethod(eventHandler.getAttachedMethod()) || eventHandler.hasParent())
            return true;
        return false;
    }

    private boolean hasSimilarTitle(String source, String target) {
        if(isMatchWithPattern(source,target))
            return true;
        return false;
    }

    /*
     In this method we are trying to extract a meaningful name for its input eventhandle based on around information.
     The name is constructed as verb + none format. the verb is extracted from the following information:
        1- attached widget label.
        2- widget id
        3- binding variable
        4- called method
        5- its method name
   */
    @NotNull
    private String extractMeaningfullTagFor(EventHandlerInformation eventHandler) {
        String[] confirmPatterns = {"Yes","OK"};
        String[] cancelPatterns = {"No", "Cancel","Close"};
        String verbPart = "";
        String nonePart = "";
        String tag = "";

        if(containDialog(eventHandler.getAttachedMethod()))
        {
            verbPart = extractVerbPartOfTagForDialog(eventHandler);
            nonePart = extractNonePartOfTagForDialog(eventHandler);
            tag      = generateTag(verbPart,nonePart);
            return StringUtils.capitalize(tag);

        }

        if(isDialogMethod(eventHandler.getAttachedMethod())){
            if(Pattern.isMatch(confirmPatterns,eventHandler.getAttachedViewLable()) ||
               Pattern.isMatch(cancelPatterns,eventHandler.getAttachedViewLable()) ||
               ASTUtils.isDefaultDialogMethodPattern(eventHandler.getAttachedMethod())){
                if(Pattern.isMatch(cancelPatterns,eventHandler.getAttachedViewLable()))
                    tag = "CloseDialog";
                else{
                    if(eventHandler.hasParent()){
                        if(Pattern.isMatch(confirmPatterns,eventHandler.getAttachedViewLable()))
                            tag = eventHandler.getParentEventHandlerInformation().getTitle();
                        else
                            tag = eventHandler.getAttachedViewLable() + eventHandler.getParentEventHandlerInformation().getTitle();
                    }
                    else
                        tag = generateLabel(eventHandler);
                }
            }
            else
                tag = generateLabel(eventHandler);
            return StringUtils.capitalize(tag);
        }

        if(isPartialMatchWithPattern(eventHandler.getAttachedViewType(),"MenuItem")){
            if(hasIntentPattern(eventHandler.getAttachedMethod()))
                tag = generateLabelForIntentPattern(eventHandler.getAttachedMethod());
               //getPredfinedPattern(eventHandler.getAttachedMethod());
            else if(!eventHandler.getAttachedViewLable().isEmpty()){
                tag =  eventHandler.getAttachedViewLable();
                tag = WordUtils.capitalize(tag);
                tag = tag.replaceAll(" ","");
            }
            else if(!eventHandler.getAttachedViewContentDescription().isEmpty()){
                verbPart = extractVerbPart(eventHandler.getAttachedViewType(),eventHandler.getAttachedViewContentDescription());
                nonePart = extractNounPart(eventHandler.getAttachedViewType(),eventHandler.getAttachedViewContentDescription());
                if(!(verbPart.isEmpty() || nonePart.isEmpty())){
                    tag =  StringUtils.capitalize(verbPart) + StringUtils.capitalize(nonePart);
                    return StringUtils.capitalize(tag);
                }
            }
            else{
                List<MethodCallExpr> callExprs = getMethodCallExprsCalledDirectlyBy(eventHandler.getAttachedMethod());
                for(MethodCallExpr callExpr : callExprs)
                    if(!callExpr.hasScope() && ASTUtils.isLocalMethod(findMethodByName(callExpr))){
                        tag = callExpr.getNameAsString();
                        break;
                    }
                // This section must be reviewed
            }
            return StringUtils.capitalize(tag);
        }
        //------------------------------------------------------------------
        if(hasIntentPattern(eventHandler.getAttachedMethod())){
            tag = generateLabelForIntentPattern(eventHandler.getAttachedMethod());
            return StringUtils.capitalize(tag);
        }
        if(!eventHandler.getAttachedViewLable().isEmpty()){
            verbPart = extractVerbPart(eventHandler.getAttachedViewType(),eventHandler.getAttachedViewLable());
            nonePart = extractNounPart(eventHandler.getAttachedViewType(),eventHandler.getAttachedViewLable());
            if(!(verbPart.isEmpty() || nonePart.isEmpty())){
                tag =  StringUtils.capitalize(verbPart) + StringUtils.capitalize(nonePart);
                return StringUtils.capitalize(tag);
            }
        }

        if(!eventHandler.getAttachedViewContentDescription().isEmpty()) {
            verbPart = extractVerbPart(eventHandler.getAttachedViewType(), eventHandler.getAttachedViewContentDescription());
            nonePart = extractNounPart(eventHandler.getAttachedViewType(), eventHandler.getAttachedViewContentDescription());
            if (!(verbPart.isEmpty() || nonePart.isEmpty())){
                tag = StringUtils.capitalize(verbPart) + StringUtils.capitalize(nonePart);
                return StringUtils.capitalize(tag);
            }
        }
        tag = prepareContent1(eventHandler.getBindingName()) +
                StringUtils.capitalize(eventHandler.getAttachedMethod().getNameAsString());;
        return StringUtils.capitalize(tag);
    }

    private String generateLabelForIntentPattern(MethodDeclaration method) {
        return generatePrefixNameFor(getActivityOpenedBy(method));
    }

    private String extractNounPart(String viewType, String content) {
        String nounTag = "";
        nounTag = getPOSTagger().getNoneTagFor(viewType,content);
        return nounTag;
    }

    private String extractVerbPart(String viewType, String content) {
        String result = "";
        String verbTag = getPOSTagger().getVerbTagFor(viewType,content,VERB);
        if(!verbTag.isEmpty()){
            result = verbTag;
        }
        return result;
    }

    private String generatePrefixNameFor(String content){
        String bindingVaribaleName = content;
        bindingVaribaleName = bindingVaribaleName.replace("_", " ");
        bindingVaribaleName = WordUtils.capitalize(bindingVaribaleName);
        return bindingVaribaleName.replace(" ", "");
    }

    private boolean hasPredefinedPattern(MethodDeclaration attachedMethod) {
        if(hasIntentPattern(attachedMethod))
            return true;
        return false;
    }

    private String getPredfinedPattern(MethodDeclaration attachedMethod) {
        if(hasIntentPattern(attachedMethod))
            return generatePrefixNameFor(getIntentActivity(attachedMethod));
        return "";
    }

    private String extractNameFromBindingVaribale(String content) {
        content = prepareContent(content);
        return StringUtils.capitalize(getPOSTagger().getNoneTagFor("",content));
    }

    private String prepareContent(String content) {
        return content.replace("_"," ");
    }

    private String generateLabel(EventHandlerInformation eventHandler) {
        String verbPart = "";
        String nonePart = "";
        verbPart = extractVerbPartOfTagForDialog(eventHandler);
        nonePart = extractNonePartOfTagForDialog(eventHandler);
        return StringUtils.capitalize(verbPart) + StringUtils.capitalize(nonePart);
    }

    private String generateLabelForDialog(String content, String viewType){
        String tag = "";
        String verbTag = getPOSTagger().getVerbTagFor(viewType,contentRefinemnet(content),VERB);
        String noneTag = getPOSTagger().getNoneTagFor(viewType,contentRefinemnet(content));
        tag      = generateTag(verbTag,noneTag);
        return StringUtils.capitalize(tag);
    }

    public  String generateTag(String verbPart, String nonePart) {
        String verb = StringUtil.toLowerCase(verbPart);
        String none = StringUtil.toLowerCase(nonePart);
        if(verb.length() >= none.length()){
            if(verb.contains(none))
                return StringUtils.capitalize(verbPart);
        }
        else{
            if(none.contains(verb))
                return StringUtils.capitalize(nonePart);
        }

        return  StringUtils.capitalize(verbPart) +
                StringUtils.capitalize(nonePart);
    }

    private String extractVerbPartOfTagForDialog(EventHandlerInformation eventHandler) {
        String result = "";
        String content = eventHandler.getContext_title();
        String viewType = eventHandler.getAttachedViewType();
        String verbTag = getPOSTagger().getVerbTagFor(viewType,content,VERB);
        if(!verbTag.isEmpty()){
            result = verbTag;
        }
        return result;
    }

    private String extractVerbPartOfTagForMenu(EventHandlerInformation eventHandler) {
        String result = "";
        String content = eventHandler.getAttachedViewLable();
        String viewType = "MenuItem";
        String verbTag = getPOSTagger().getVerbTagFor(viewType,content,VERB);
        if(!verbTag.isEmpty()){
            result = verbTag;
        }
        return result;
    }

    private String extractNonePartOfTagForMenu(EventHandlerInformation eventHandler) {
        String content = "";
        String noneTag = "";
        String viewType = "MenuItem";
        content = eventHandler.getAttachedViewLable();
        if(!content.isEmpty())
            noneTag = getPOSTagger().getNoneTagFor(viewType,content);
        return noneTag;
    }


    private String extractNonePartOfTagForDialog(EventHandlerInformation eventHandler) {
        String content = "";
        String noneTag = "";
        String viewType = eventHandler.getAttachedViewType();
        content = eventHandler.getContext_title();//provideContentForNoneTagging(eventHandler);
        if(!content.isEmpty())
            noneTag = getPOSTagger().getNoneTagFor(viewType,content);
        return noneTag;
    }

    private String provideContentForVerbTagging(EventHandlerInformation eventHandler) {
        String[] confirmPatterns = {"Yes","OK"};
        String[] cancelPatterns = {"No", "Cancel"};
        String content = "";
        content = eventHandler.getAttachedViewLable();
        if(!content.isEmpty()){
            if(isDialogMethod(eventHandler.getAttachedMethod()))
                if (Pattern.isMatch(confirmPatterns, content) || Pattern.isMatch(cancelPatterns,content))
                    content = eventHandler.getContext_title();
        }
        else
            content = eventHandler.getContext_title();
        return contentRefinemnet(content);
    }

    private String contentRefinemnet(String content) {
        String[] wordPatterns = {"Please","Would youe like to", "please","would you like to"};
        if(Pattern.isPartialMatch(wordPatterns,content))
            content = removeWordPatterns(wordPatterns,content);
        return content;
    }

    private String removeWordPatterns(String[] wordPatterns, String content) {
        for(int index = 0; index < wordPatterns.length; index++)
            if(isPartialMatchWithPattern(wordPatterns[index],content))
               content = content.replaceAll(wordPatterns[index],"");
        return content;
    }

    private String prepareContent1(String bindingName) {
        if(bindingName.isEmpty())
            return "";
        else{
            bindingName = bindingName.substring(bindingName.lastIndexOf('.') + 1);
            bindingName = bindingName.replaceAll("_","");
            return bindingName;
        }
    }

    private boolean containDialog(MethodDeclaration method) {
        boolean result = false;
        List<MethodCallExpr> methodCallExprs =
                ASTUtils.getMethodCallExprsListByName(method,"show");
        for (MethodCallExpr methodCallExpr : methodCallExprs)
            if(methodCallExpr.hasScope()){
                String className = resolveClassName(methodCallExpr, ASTUtils.getScope(methodCallExpr));
                if (isPartialMatchWithPattern(className, "Dialog"))
                    if (ASTUtils.isPlacedInSameBlock(method.getBody().get(),
                            ASTUtils.getParentBlock(methodCallExpr)))
                        return true;
                    else if (!ASTUtils.isPlacedInConditionBlock(ASTUtils.getParentMethodCallExpr(methodCallExpr)))
                        return true;
            }
        List<MethodCallExpr> callExprList = getMethodCallExprsCalledDirectlyBy(method);
        if(callExprList.isEmpty())
            return false;
        for(MethodCallExpr callExpr:callExprList)
            if(!callExpr.hasScope())
                if (ASTUtils.isPlacedInSameBlock(method.getBody().get(),
                        ASTUtils.getParentBlock(callExpr)) ||
                        !ASTUtils.isPlacedInConditionBlock(callExpr)){
                    MethodDeclaration targetMethod = findMethodByName(callExpr);
                    if(targetMethod != null)
                        if(ASTUtils.isLocalMethod(targetMethod) &&
                           !ASTUtils.areSameMethod(method,targetMethod))
                            if(containDialog(targetMethod)){
                                result = true;
                                break;
                            }
                }
        return result;
    }

    private String getIntentActivity(MethodDeclaration attachedMethod) {
        return  getActivityOpenedBy(attachedMethod);
    }

    private boolean hasIntentPattern(MethodDeclaration attachedMethod) {
        String[] patterns = {"Intent"};
        if(hasPredefinedObjectCreationPatternIn(attachedMethod,patterns))
            return true;
        return false;
    }

    public  boolean hasPredefinedObjectCreationPatternIn(MethodDeclaration method, String[] patterns) {
        List<ObjectCreationExpr> objectCreationExprs = new ArrayList<>();
        method.findAll(ObjectCreationExpr.class).forEach(item->{
            if(Utils.isMatchWithPatterns(patterns,item.getType().toString()))
                if(ASTUtils.isPlacedInSameBlock(method.getBody().get(), ASTUtils.getParentBlock(item)) ||
                        !ASTUtils.isPlacedInConditionBlock(item))
                    objectCreationExprs.add(item);
        });
        if(objectCreationExprs.size() > 0)
            return true;
        List<MethodCallExpr> callExprsList = ASTUtils.getMethodCallExprsCalledDirectlyBy(method);
        for(MethodCallExpr callExpr:callExprsList)
            if (ASTUtils.isPlacedInSameBlock(method.getBody().get(),
                    ASTUtils.getParentBlock(callExpr)) ||
                    !ASTUtils.isPlacedInConditionBlock(callExpr)){
                MethodDeclaration targetMethod = findMethodByName(callExpr);
                if(targetMethod != null)
                    if(ASTUtils.isLocalMethod(targetMethod) && !ASTUtils.areSameMethod(method,targetMethod))
                       if(hasPredefinedObjectCreationPatternIn(targetMethod,patterns)){
                                return true;
                            }
            }
        return false;
    }

    private POSTagger getPOSTagger() {
        return this.context.getProjectInformation().getPosTagger();
    }

    private void generateLabelForExtractedEventHandler() {
        for(EventHandlerInformation eventHandler:eventHandlerInformationList){
            String label = generateLabelFor(eventHandler);
            if(!label.isEmpty()){
                int keyOfLabel = this.context.getProjectInformation().setLabelInLabelsCollection(label);
                eventHandler.setKeyOfLabel(keyOfLabel);
            }
        }
    }

    private String extractLabelForInnerCLass(MethodDeclaration method) {
        String dialogTitle = "" , dialogMessage = "";
        StringValueExtractor stringValueExtractor = new StringValueExtractor(context.getProjectInformation(),
                "strings");
        List<MethodCallExpr> callExprs = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(node -> {
            if (node.getNameAsString().contentEquals("setTitle") || node.getNameAsString().contentEquals("setMessage"))
                callExprs.add(node);
        });

        for (MethodCallExpr expr : callExprs) {
            if (expr.getNameAsString().contentEquals("setTitle")) {
                String objectName = ASTUtils.getObjectName(expr);
                String containingClassName = resolveClassName(method, objectName);
                if (containingClassName.contains("Dialog")) {
                    if (expr.getArgument(0).toString().startsWith("R.string."))
                        dialogTitle = stringValueExtractor.findViewLabelById(expr.getArgument(0).toString());
                    else {
                        dialogTitle = expr.getArgument(0).toString();
                        dialogTitle = dialogTitle.substring(1,dialogTitle.length()-1);
                    }

                }
            } else if (expr.getNameAsString().contentEquals("setMessage")) {
                String objectName = ASTUtils.getObjectName(expr);
                String containingClassName = resolveClassName(method, objectName);
                if (containingClassName.contains("Dialog")) {
                    if (expr.getArgument(0).toString().startsWith("R.string."))
                        dialogMessage = stringValueExtractor.findViewLabelById(expr.getArgument(0).toString());
                    else{
                        dialogMessage = expr.getArgument(0).toString();
                        dialogMessage = dialogMessage.substring(1,dialogMessage.length()-1);

                    }
                }
            }
        }
        POSTagger posTagger = new POSTagger();
        String label = "";
        if (dialogTitle != "")
            label = posTagger.generateLabelforDialog("I want to " + dialogTitle.toLowerCase());
        else if (dialogMessage != "")
            label = posTagger.generateLabelforDialog(dialogMessage);
        return label;
    }

    private String generateContextForChildEvents(EventHandlerInformation eventHandler) {
        if(cotainDialog(eventHandler.getAttachedMethod()))
            return extractLabelForInnerCLass(eventHandler.getAttachedMethod());
        else{
            List<MethodCallExpr> methodCallExprs = getDirectlyCalledMethodsBy(eventHandler.getAttachedMethod());
            for(MethodCallExpr methodCallExpr : methodCallExprs){
                MethodDeclaration method = findMethodByName(methodCallExpr);
                if(method != null){
                    if(cotainDialog(method))
                        return extractLabelForInnerCLass(method);
                }
            }
        }
        return "";
    }

    private String resolveViewType(EventHandlerInformation eventHandler) {
        List<VariableDeclarator> localVariablesList;
        String objectName = eventHandler.getBindingName();//getViewBindingVariableName(eventHandler.getAttachedMethod());
        Node currentNode = getParentBlock(eventHandler.getAttachedMethod());

        if(objectName.contains("."))
            objectName = objectName.substring(0,objectName.indexOf('.'));

        do{
            localVariablesList = getLocalVariables(currentNode);
            if(isLocalVariable(localVariablesList,objectName))
                return getContainingClassName(localVariablesList,objectName);
            currentNode = getParentBlock(currentNode);
        }while(currentNode != null);

        List<VariableDeclarator> globalVariablesList = getGlobalVariableList();
        if(isGlobalVariable(globalVariablesList,objectName))
            return getContainingClassName(globalVariablesList,objectName);
        return "";

    }

    //*********************************************************************
   private MethodDeclaration getMethodByName(List<MethodDeclaration> methods,String targetMethodName) {
       MethodDeclaration targetMethod = null;
       for(MethodDeclaration method : methods)
           if(isMatchWithPattern(method.getNameAsString(),targetMethodName)){
               targetMethod = method;
               break;
           }
       return targetMethod;
   }

    private MethodCallExpr getMethodCallExprByName(Node node,String targetMethodCallExpr) {
        List<MethodCallExpr> callExprsList = getMethodCallExprsListByName(node,targetMethodCallExpr);
        if(callExprsList.size() > 0)
            return callExprsList.get(0);
        else
            return null;
    }

    private List<MethodCallExpr> getMethodCallExprsListByName(Node method,String pattern){
        List<MethodCallExpr> methodCallExprsWithSimilarName = new ArrayList<>();
        List<MethodCallExpr> methodCallExprList = getMethodCallExprsListFrom(method);
        for(MethodCallExpr callExprItem : methodCallExprList)
            if(isMatchWithPattern(pattern,callExprItem.getNameAsString()))
                methodCallExprsWithSimilarName.add(callExprItem);
        return methodCallExprsWithSimilarName;
    }

    private List<MethodCallExpr> getMethodCallExprsListFrom(Node node){
        List<MethodCallExpr> wholeCallExprsList = new ArrayList<>();
        node.findAll(MethodCallExpr.class).forEach(item->{
            wholeCallExprsList.add(item);
        });
        return wholeCallExprsList;
    }

    private BlockStmt getParentBlock(Node node){
        BlockStmt parentBlock = null;
        Node tmpNode = node;
        do{
            tmpNode = getParentNode(tmpNode);
        }while(!(isBlockStmt(tmpNode) || isClassOrInterfaceExpr(tmpNode)));


        if(isBlockStmt(tmpNode))
            parentBlock = (BlockStmt) tmpNode;
        return parentBlock;

    }

    private String getViewBindingVariableName(Node node) {
        String scope = "";
        while (!isMethodCallExpr(node))
            node = getParentNode(node);
        scope = ASTUtils.getScope(node);

        if(isPartialMatchWithPattern(scope,"."))
            scope = scope.substring(0,scope.indexOf('.'));
        return scope;
    }

    private String getDynamicLayoutFileName(EventHandlerInformation eventHandler) {
        Node tmpNode;
        if(isDialogMethod(eventHandler.getAttachedMethod()))
            tmpNode = ASTUtils.getParentBlock(eventHandler.getAttachedMethod());
        else
            tmpNode = ASTUtils.getParentBlock(eventHandler.getChildEventHandlers().get(0).getAttachedMethod());
        List<MethodCallExpr> setContentMethodList = getMethodCallExprsListByName(tmpNode,"setContentView");
        int argId = 0;
        for(MethodCallExpr methodCallExpr : setContentMethodList)
            if(methodCallExpr.hasScope()){
                String layoutName = getArgument(methodCallExpr,argId);
                return layoutName.substring(layoutName.lastIndexOf('.') + 1);
            }
        return "";
    }

    private boolean isValidEventHandler(MethodDeclaration node){
        Node parentNode = getParentNode(node);
        Node ancientNode = getAncientNode(node);
        if(isPartialMatchWithPattern(getClassName(parentNode),"ObjectCreationExpr")
                && !isPartialMatchWithPattern(getClassName(ancientNode),"VariableDeclarator"))
            return true;
        return false;
    }

    private MethodCallExpr extractBindindMethodCallExprFrom(Node node) {
        return (MethodCallExpr) getAncientNode(node);
    }

 //####################################################################################################
    private List<MethodDeclaration> subtractSets(List<EventHandlerInformation> wholeSet, List<EventHandlerInformation> subSet){
        List<MethodDeclaration> resultSet = new ArrayList<>();
        for(EventHandlerInformation item : wholeSet)
            if(!subSet.contains(item))
                resultSet.add(item.getAttachedMethod());
        return resultSet;
    }

    private List<MethodCallExpr> subtract(List<MethodCallExpr> wholeSet, List<MethodCallExpr> subSet){
        List<MethodCallExpr> resultSet = new ArrayList<>();
        for(MethodCallExpr item : wholeSet)
            if(!subSet.contains(item))
                resultSet.add(item);
        return resultSet;
    }

    public List<MethodCallExpr> getMethodCallExprsListFromInnerMethods(Node method){
       List<MethodCallExpr> callExprsInInnerMethods = new ArrayList<>();
       List<Node> innerMethodsList = getInnerMethodsIn(method);
       for(Node innerMethod : innerMethodsList)
           innerMethod.findAll(MethodCallExpr.class).forEach(item->{
               callExprsInInnerMethods.add(item);
           });
       return callExprsInInnerMethods;
    }

    public List<MethodCallExpr> getMethodCallExprListCalledDirectlyBy(Node callerMethod){
       List<MethodCallExpr> callExprList = new ArrayList<>();
       List<MethodCallExpr> callExprsInInnerMethod = new ArrayList<>();
       callExprList = getMethodCallExprsListFrom(callerMethod);
       callExprsInInnerMethod = getMethodCallExprsListFromInnerMethods(callerMethod);
       return subtract(callExprList,callExprsInInnerMethod);
    }

    public String getArgument(Node methodCallExpr, int argIndex){
        return ((MethodCallExpr) methodCallExpr).getArgument(argIndex).toString();
   }


    private Node getParentNode(Node node){
        return node.getParentNode().get();
    }

    private String getClassName(Node node){
        return node.getClass().getName();
    }

    private boolean isClassOrInterfaceExpr(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"ClassOrInterface"))
            return true;
        return false;
    }

    private boolean isBlockStmt(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"BlockStmt"))
            return true;
        return false;
    }

    private boolean isOuterMethod(Node node) {
        Node ancientNode = getAncientNode( node);
        if(isVariableDeclarator(ancientNode))
            return true;
        return false;
    }

    private Node getAncientNode(Node node) {
        return getParentNode(getParentNode(node));
    }

    private boolean isAssignExpr(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"AssignExpr"))
            return true;
        return false;
    }

    private boolean isVariableDeclarator(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"VariableDeclarator"))
            return true;
        return false;
    }

    private boolean isMethodDeclartionExpr(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"MethodDeclaration"))
            return true;
        return false;
    }

    private boolean isClassMember(Node node){
        if(isClassOrInterfaceExpr(node.getParentNode().get()))
            return true;
        return false;
    }

    private boolean isMethodCallExpr(Node node){
        if(isPartialMatchWithPattern(getClassName(node),"MethodCallExpr"))
            return true;
        return false;
    }

    private boolean isMatchWithPatterns(List<String> patterns,String targetItem){
        if(patterns.contains(targetItem))
            return true;
        return false;
    }

    private boolean isMatchWithPattern(String pattern,String targetItem ){
        if(pattern.contentEquals(targetItem))
            return true;
        return false;
    }

    private boolean isPartialMatchWithPattern(List<String> patterns,String targetItem){
        for(String item : patterns)
            if(targetItem.contains(item))
                return true;
        return false;
    }

    private boolean isPartialMatchWithPattern(String pattern,String targetItem){
        if(targetItem.contains(pattern) || pattern.contains(targetItem))
            return true;
        return false;
    }
   //=========================================================================================

    private MethodDeclaration findMethodByName(MethodCallExpr methodCallExpr) {
        for(EventHandlerInformation method: calledMethodDeclarationList)
            if(methodCallExpr.getNameAsString().contentEquals(method.getAttachedMethod().getNameAsString()))
                return method.getAttachedMethod();
        return null;
    }

    public List<EventHandlerInformation> getExtractedEventHandlerList() {
        return eventHandlerInformationList;
    }

    public List<MethodCallExpr> getCalledMethodNames() {
        List<MethodCallExpr> calledMethodNames = new ArrayList<>();
        AST.findAll(MethodCallExpr.class).forEach(node -> {
            if (!node.getName().getParentNode().get().toString().contains("super." + node.getName().asString()))
                calledMethodNames.add(node);
        });
        return calledMethodNames;
    }

    private boolean isLocalMethod(MethodCallExpr calledMethodName) {
        AtomicBoolean result = new AtomicBoolean(false);
        if (calledMethodName.getScope().toString().equals("this")
                || calledMethodName.getScope().isEmpty()){
            activityNode.findAll(MethodDeclaration.class).forEach(methodItem->{
                if(isMatchWithPattern(methodItem.getNameAsString(),calledMethodName.getNameAsString()))
                   result.set(true);
            });
        }
        return result.get();
    }

    private List<EventHandlerInformation> getExtractedEventHandlerListOfMenuItems(
            EventHandlerInformation eventHandlerInformation) {
        List<EventHandlerInformation> extractedEventList = new ArrayList<>();
        boolean top = true;
        String variable = "";
        String methodName = "";
        BlockStmt prefix = new BlockStmt();
        BlockStmt postfix = new BlockStmt();

        MethodDeclaration method = eventHandlerInformation.getAttachedMethod().asMethodDeclaration();
        String parameterName = method.getParameter(0).getNameAsString();
        String parameterType = method.getParameter(0).getTypeAsString();
        int i = 0;
        while (i < method.getBody().get().getStatements().size()) {
            boolean flag = true;
            Statement statement = method.getBody().get().getStatement(i);
            if (statement.isIfStmt()) {
                String condition = statement.asIfStmt().getCondition().toString();
                if (Pattern.isMatch(condition, parameterName,parameterType, variable)) {
                    top = false;
                    flag = false;
                    extractedEventList.addAll(extractEventHandlerFromIfStmt(eventHandlerInformation, statement,
                            parameterName, parameterType, variable, methodName));
                }
            } else if (statement.isSwitchStmt()) {
                SwitchStmt switchStmt = (SwitchStmt) statement;
                String selector = switchStmt.getSelector().toString();
                if (Pattern.isMatch(selector, parameterName, parameterType,variable)) {
                    top = false;
                    flag = false;
                    extractedEventList.addAll(extractEventHandlerFromSwitchStmt(eventHandlerInformation,(SwitchStmt) statement,
                            parameterName, parameterType, variable, methodName));
                }
            }

            if (flag) {
                if (statement.getClass().getName().contains("ExpressionStmt"))
                    if (statement.toString().contains(parameterName + ".getItemId()")) {
                        variable = extractVariableFrom(statement.toString());
                        methodName = extractMethodPatternFrom(statement.toString());
                        i++;
                        continue;
                    }

                if(top)
                    prefix.addStatement(statement);
                else
                    postfix.addStatement(statement);
            }
            i++;
        }
        if(!extractedEventList.isEmpty())
            return preparedExtractedEventHandlers(eventHandlerInformation,extractedEventList,prefix,postfix);
        return extractedEventList;
    }

    private List<EventHandlerInformation> preparedExtractedEventHandlers(
                                      EventHandlerInformation eventHandler,
                                      List<EventHandlerInformation> extractedEventList,
                                      BlockStmt prefix, BlockStmt postfix) {
        for(EventHandlerInformation event : extractedEventList){
              if(!prefix.isEmpty())
                 appendBeginOfEventHandler(event,prefix);
              if(!postfix.isEmpty())
                  appendEndOfEventHandler(event,postfix);
              PsiMethod element = (PsiMethod) eventHandler.getPsiMethod().copy();
              PsiElementFactory factory = JavaPsiFacade.getInstance(element.getProject()).getElementFactory();
              PsiCodeBlock methodBody = factory.createCodeBlockFromText(
                    event.getAttachedMethod().getBody().get().toString(), null);
              element.getBody().replace(methodBody);
              event.setPsiMethod(element);
        }
        EventHandlerInformation tmpEvent = extractedEventList.remove(0);
        eventHandler.getAttachedMethod().setBody(tmpEvent.getAttachedMethod().getBody().get());
        eventHandler.setAttacheViewLabel(tmpEvent.getAttachedViewLable());
        eventHandler.setAttachedViewId(tmpEvent.getAttachedViewId());
        eventHandler.setAttachedViewType(tmpEvent.getAttachedViewType());
        eventHandler.setAttachedViewTag(tmpEvent.getAttachedViewTag());
        return extractedEventList;
    }

    private void appendEndOfEventHandler(EventHandlerInformation eventHandler, BlockStmt postfix) {
        int index = eventHandler.getAttachedMethod().getBody().get().getStatements().size();
        for(Statement stmt : postfix.getStatements()){
            eventHandler.getAttachedMethod().getBody().get().addStatement(index,stmt);
            index++;
        }
    }

    private void appendBeginOfEventHandler(EventHandlerInformation eventHandler, BlockStmt prefix) {
        BlockStmt block = new BlockStmt();
        int index = 0;
        for(Statement statement : prefix.getStatements()){
            eventHandler.getAttachedMethod().getBody().get().addStatement(index,statement);
            index++;
        }
    }

    private String extractMethodPatternFrom(String statement) {
        String result="";
        statement.trim();
        String[] stmtParts = statement.split("=");
        result = stmtParts[1].trim().replaceAll("\\s","");
        result = result.substring(result.indexOf('.')+1,stmtParts[1].indexOf(';')-1);
        return result;
    }

    private List<EventHandlerInformation> extractEventHandlerFromSwitchStmt(
            EventHandlerInformation eventHandlerInformation,
            SwitchStmt statement,
            String parameterName,
            String parameterType,
            String variable,
            String methodName) {
        List<EventHandlerInformation> extractedEventList = new ArrayList<>();
        for(int i = 0; i < statement.getEntries().size(); i++){
            SwitchEntry switchEntry = statement.getEntry(i);
            EventHandlerInformation newEvent = createEventHandler(eventHandlerInformation);
            collectOtherInformationFor(newEvent,switchEntry.getLabels().get(0).toString(),parameterName,
                    parameterType,variable,methodName);
            if(switchEntry.getStatements().size() -1 > 0){
                NodeList<Statement> statements = switchEntry.getStatements();
                statements.removeLast();
                newEvent.getAttachedMethod().setBody(new BlockStmt(switchEntry.getStatements()));
                extractedEventList.add(newEvent);
            }
       }
        return extractedEventList;
    }
    /*
         This method extract event handler from IfStmt.
     */

    private List<EventHandlerInformation> extractEventHandlerFromIfStmt(
            EventHandlerInformation eventHandlerInformation,
            Statement statement,
            String parameterName,
            String parameterType,
            String variable,
            String methodName) {
        List<EventHandlerInformation> extractedEventList = new ArrayList<>();
        boolean flag = true;

        while(flag){
            String condition = statement.asIfStmt().getCondition().toString();
            if(Pattern.isMatch(condition,parameterName,parameterType,variable)){
                EventHandlerInformation newEvent = createEventHandler(eventHandlerInformation);
                collectOtherInformationFor(newEvent,condition,parameterName,parameterType,variable,methodName);
                if (statement.asIfStmt().getThenStmt().isBlockStmt())
                    newEvent.getAttachedMethod().setBody(statement.asIfStmt().getThenStmt().asBlockStmt());
                else {
                    BlockStmt blockStatement = new BlockStmt();
                    blockStatement.addStatement(statement.asIfStmt().getThenStmt());
                    newEvent.getAttachedMethod().setBody(blockStatement);
                }

                extractedEventList.add(newEvent);
                if(!statement.asIfStmt().getElseStmt().isEmpty())
                   statement = statement.asIfStmt().getElseStmt().get();
                else
                    flag = false;
            }
        }

        return extractedEventList;
    }

    private void collectOtherInformationFor(EventHandlerInformation eventHandler, String content,
                                            String parameterName, String parameterType, String variable,
                                            String methodName) {
        switch(parameterType){
            case "View":
                           {
                              if(!content.contains("==")){
                                  if(content.startsWith("R.id"))
                                      eventHandler.setAttachedViewId(content);
                                  else
                                      eventHandler.setAttachedViewTag(content);

                              }else{
                                    if(!methodName.isEmpty()) {
                                        if (methodName.contains("getTag()"))
                                            eventHandler.setAttachedViewTag(extractTagValueFrom(content));
                                        else if (methodName.contains("getId()"))
                                            eventHandler.setAttachedViewId(extractViewIdFromContent(content));
                                    }else{
                                        if(content.contains("getTag"))
                                            eventHandler.setAttachedViewTag(extractTagValueFrom(content));
                                        else
                                            eventHandler.setAttachedViewId(extractViewIdFromContent(content));
                                    }

                               }
                           }
                           break;
            case "MenuItem" :
                             eventHandler.setAttachedViewType("MenuItem");
                             if(content.contains("=="))
                                 eventHandler.setAttachedViewId(extractViewIdFromContent(content));
                             else
                                 eventHandler.setAttachedViewId(content);
                             break;


        }
    }

    private String extractTagValueFrom(String content) {
        String result = "";
        content.trim();
        content = content.replaceAll("\\s","");
        String[] strParts = content.split("==");
        if(strParts[0].contains("getTag"))
            result = strParts[1].trim();
        else
            result = strParts[0].trim();
        return  result;
    }

    private String extractViewIdFromContent(String content) {
        String result = "";
        content.trim();
        content = content.replaceAll("\\s","");
        if(content.startsWith("R.id."))
            result = content.substring(0,content.indexOf("=")-1);
        else
            result = content.substring(content.lastIndexOf("==")+2);
        return result;
    }

    private EventHandlerInformation createEventHandler(EventHandlerInformation eventHandlerInformation) {
        EventHandlerInformation eventHandler = new EventHandlerInformation();
        eventHandler.setMethodDeclaration(new MethodDeclaration());
        eventHandler.setName(eventHandlerInformation.getName());
        eventHandler.getAttachedMethod().setName(eventHandlerInformation.getAttachedMethod().getName());
        eventHandler.getAttachedMethod().setType(eventHandlerInformation.getAttachedMethod().getType());
        eventHandler.getAttachedMethod().setModifiers(eventHandlerInformation.getAttachedMethod().getModifiers());
        eventHandler.getAttachedMethod().setParentNode(eventHandlerInformation.getAttachedMethod().getParentNode().get());
        eventHandler.getAttachedMethod().setRange(eventHandlerInformation.getAttachedMethod().getRange().get());
        eventHandler.getAttachedMethod().setParameters(eventHandlerInformation.getAttachedMethod().getParameters());
        return eventHandler;

    }

    private String extractVariableFrom(String statement) {
        statement.trim();
        String[] stmtParts = statement.split("=");
        stmtParts[0].trim();
        stmtParts = stmtParts[0].split("\\s");
        return stmtParts[1].trim();
    }

    private boolean isPossibleComplicatedMethod(MethodDeclaration method) {
        if(method.getParentNode().get().getClass().getName().contains("ClassOrInterface"))
            return true;
        return false;
    }

    private void extractActivityTitle(EventHandlerInformation eventHandlerInformation) {
        List<MethodCallExpr> calledMethods;
        String value = "";
        calledMethods = getDirectlyCalledMethodsBy(eventHandlerInformation.getAttachedMethod());
        for(MethodCallExpr calledMethod:calledMethods){
            if(calledMethod.getName().toString().contentEquals("setTitle")){
                String parameter = calledMethod.getArgument(0).toString();
                this.context.setTitle(extractLabelFromContent(parameter));
                break;
            }
        }

    }

    private void extractMenuLayoutFileName(MethodDeclaration method) {
        List<MethodCallExpr> calledMethods = getDirectlyCalledMethodsBy(method);
        MethodCallExpr resultMethodCallExpr = findMethodExprByName(calledMethods, "inflate");
        if (resultMethodCallExpr != null) {
            String menuFileName = resultMethodCallExpr.getArgument(0).toString();
            this.menuFileName = menuFileName.substring(menuFileName.lastIndexOf('.') + 1);
        }
    }

    private MethodCallExpr findMethodExprByName(List<MethodCallExpr> calledMethods, String calledMethodName) {
        MethodCallExpr result = null;
        for (MethodCallExpr item : calledMethods)
            if (item.getName().toString().equals(calledMethodName)) {
                result = item;
                break;
            }
        return result;
    }

    private boolean isAutomatedCalledMethod(String name) {
        boolean result = false;
        switch (name) {
            case "onCreate":
            case "onResume":
            case "onPause":
            case "onCreateOptionsMenu":
                result = true;
                break;
        }
        return result;
    }

    private MethodDeclaration getParentMethodDeclaration(MethodCallExpr callExpr){
        Node tmpNode = (Node) callExpr;
        while(!isMethodDeclartionExpr(tmpNode) && !isClassOrInterfaceExpr(tmpNode))
            tmpNode = getParentNode(tmpNode);
        if(isMethodDeclartionExpr(tmpNode))
            return (MethodDeclaration) tmpNode;
        return null;
    }

    private MethodDeclaration getParentMethodDeclaration(EventHandlerInformation eventHandlerInformation) {
        MethodDeclaration tmpNode;
        Node node = (Node) eventHandlerInformation.getAttachedMethod();
        boolean flag = true;
        while (node.getParentNode().isPresent()) {
            if (node.getParentNode().get().getClass().getName().contains("MethodDeclaration")) {
                break;
            }
            node = node.getParentNode().get();
        }
        tmpNode = (MethodDeclaration) node.getParentNode().get();
        return tmpNode;
    }

    private boolean isEventHandler(MethodDeclaration method) {
        for (EventHandlerInformation eventHandlerInformation : eventHandlerInformationList) {
            if (eventHandlerInformation.getAttachedMethod().getName() == method.getName())
                return true;
        }
        return false;
    }

    private EventHandlerInformation findEventHandlerByMethodDeclaration(MethodDeclaration method) {
        for (EventHandlerInformation eventHandlerInformation : eventHandlerInformationList) {
            if (eventHandlerInformation.getAttachedMethod().equals(method))
                return eventHandlerInformation;
        }
        return null;
    }

    public EventHandlerInformation findCalledMethodByName(String name) {
        for (EventHandlerInformation event : calledMethodDeclarationList) {
            if (event.getAttachedMethod().getName().toString().equals(name))
                return event;
        }
        return null;
    }

    private EventHandlerInformation findCallerEventHandler(String name) {
        if (isAutomatedCalledMethod(name) || calledDirectlyByAutomatedMethod(name))
            return null;
        EventHandlerInformation targetEventHandlerInformation = null;
        boolean findFlag = false;
        List<MethodCallExpr> calledDirectlyMethodNames = new ArrayList<>();
        for (EventHandlerInformation callerMethod : methodDeclarationList) {
            calledDirectlyMethodNames = getDirectlyCalledMethodsBy(callerMethod.getAttachedMethod());
            for (MethodCallExpr calledMethod : calledDirectlyMethodNames) {
                if ((calledMethod.getName().toString().contentEquals(name)) &&
                        (calledMethod.getScope().isEmpty()
                        )) {
                    if (isEventHandler(callerMethod.getAttachedMethod()))
                        targetEventHandlerInformation = findEventHandlerByMethodDeclaration(callerMethod.getAttachedMethod());
                    else
                        targetEventHandlerInformation = findCallerEventHandler(callerMethod.getName().toString());
                    findFlag = true;
                    break;
                }
            }
            if(findFlag)
                break;
        }
        return targetEventHandlerInformation;
    }

    private boolean calledDirectlyByAutomatedMethod(String name) {
        List<MethodCallExpr> calledDirectlyMethodNames = getDirectlyCalledMethodsBy(onCreate);
        for (MethodCallExpr calledMethod : calledDirectlyMethodNames)
            if ((calledMethod.getName().toString().contentEquals(name)) &&
                    (calledMethod.getScope().isEmpty()
                    ))
                return true;
        return false;
    }

    private List<MethodCallExpr> getDirectlyCalledMethodsBy(MethodDeclaration method) {
        List<MethodCallExpr> calledMethods = new ArrayList<>();
        List<MethodCallExpr> calledMethodsInInternalMethodDeclaration = new ArrayList<>();
        List<MethodDeclaration> methodDeclarationList = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(methodCallExpr->{
            if(isMatchWithPattern(getParentMethodDeclaration(methodCallExpr).getNameAsString(),method.getNameAsString()))
                calledMethods.add(methodCallExpr);
        });
        return calledMethods;
    }

    private String extractWidgetType(MethodDeclaration md, String objectName) {

        String widgetType = "";
        ArrayList<VariableDeclarator> variableList = new ArrayList<>();
        List<FieldDeclaration> classFieldList = new ArrayList<>();

        variableList.addAll(collectLocalVariables(md));
        AST.findAll(FieldDeclaration.class).forEach(item -> classFieldList.add(item));
        for (FieldDeclaration field : classFieldList)
            field.getVariables().forEach(var -> variableList.add(var));

        for (VariableDeclarator variable : variableList)
            if (variable.getName().toString().contentEquals(objectName))
                widgetType = variable.getType().toString();
        return widgetType;
    }

    private ArrayList<VariableDeclarator> collectLocalVariables(MethodDeclaration md) {
        ArrayList<VariableDeclarator> localVariable = new ArrayList<>();
        ArrayList<VariableDeclarator> internalMethodVariableList = new ArrayList<>();
        ArrayList<MethodDeclaration> internalMethodList = new ArrayList<>();

        md.getChildNodes().forEach(child -> child.findAll(MethodDeclaration.class).forEach(item -> internalMethodList.add(item)));
        md.findAll(VariableDeclarator.class).forEach(item -> localVariable.add(item));
        for (MethodDeclaration item : internalMethodList)
            item.findAll(VariableDeclarator.class).forEach(node -> internalMethodVariableList.add(node));
        localVariable.removeAll(internalMethodVariableList);

        return localVariable;

    }

    /********************************************************************
     *  This method tries to extract id of the input parameter
     *  it search the method that the input parameter is used or in the
     *  onCreate method
     */
    private String searchIdInParentsBlockPath(BlockStmt codeBlock, String objectName){
        String widgetId = "";
        codeBlock = getParentBlock(codeBlock);
        if(codeBlock != null){
            List<VariableDeclarator> localVariablesList = getLocalVariables(codeBlock);
            if(isLocalVariable(localVariablesList,objectName))
                widgetId = searchWidgetIdInBlockBody(codeBlock,objectName);
            else{
                widgetId = searchWidgetIdInBlockBody(codeBlock,objectName);
                if(widgetId.isEmpty())
                    widgetId = searchIdInParentsBlockPath(codeBlock,objectName);
            }
        }
        return widgetId;
    }

    private List<MethodDeclaration> getLocalMethods(){
        return subtractSets(methodDeclarationList,eventHandlerInformationList);
    }

    private List<MethodDeclaration> getLocalCalledMethodIn(BlockStmt codeBlock){
        List<MethodDeclaration> localCalledMethods = new ArrayList<>();
        List<MethodDeclaration> localMethods = getLocalMethods();
        List<MethodCallExpr> methodCallExprs = getMethodCallExprsCalledDirectlyBy(codeBlock);
        localMethods.forEach(method->{
            methodCallExprs.forEach(methodCallExpr -> {
                if(isMatchWithPattern(method.getNameAsString(),methodCallExpr.getNameAsString()) &&
                        !methodCallExpr.hasScope())
                    localCalledMethods.add(method);
            });
        });
        return localCalledMethods;
    }

    private String searchInPredefinedMethod(MethodDeclaration method,String objectName){
        String widgetId = "";
        widgetId = searchWidgetIdInBlockBody(method.getBody().get(),objectName);
        if(widgetId.isEmpty()){
            List<MethodDeclaration> localCalledMethods = getLocalCalledMethodIn(method.getBody().get());
            localCalledMethods.remove(method);
            for(MethodDeclaration methodItem: localCalledMethods){
                widgetId = searchWidgetIdInBlockBody(methodItem.getBody().get(),objectName);
                if(!widgetId.isEmpty())
                  break;
                widgetId = searchInPredefinedMethod(methodItem,objectName);
                if(!widgetId.isEmpty())
                    break;
            }
        }
        return widgetId;
    }

    private  String searchInPredefinedLocation(String objectName){
       String widgetId = "";
       widgetId = searchInPredefinedMethod(onCreate,objectName);
       if(widgetId.isEmpty() && onStart != null)
           widgetId = searchInPredefinedMethod(onStart,objectName);
       return widgetId;
    }

    private String  extractWidgetId(MethodDeclaration md, String objectName) {

        /* The following loop is used to search the WidgetId of input variable parameter (objectName) in the specified
        method declaration (md) */
        String widgetId = "";
        BlockStmt codeBlock = md.getBody().get();//getParentBlock(md);
        //==============================================================
        List<VariableDeclarator> localVariablesList = getLocalVariables(codeBlock);
        if(isLocalVariable(localVariablesList,objectName))
            widgetId = searchWidgetIdInBlockBody(codeBlock,objectName);
        else{
            widgetId = searchWidgetIdInBlockBody(codeBlock,objectName);
            if(widgetId.isEmpty()){
                widgetId = searchIdInParentsBlockPath(codeBlock,objectName);
                if(widgetId.isEmpty())
                    widgetId = searchInPredefinedLocation(objectName);
            }
        }
        return widgetId;
     }

    private List<MethodCallExpr> getMethodCallExprsListByNameFrom(Node node, String callExpr) {
        List<MethodCallExpr> callExprsList = new ArrayList<>();
        node.findAll(MethodCallExpr.class).forEach(item->{
            if(isMatchWithPattern(item.getNameAsString(),callExpr))
                callExprsList.add(item);
        });
        return callExprsList;
    }

    private List<MethodCallExpr> getMethodCallExprsListByNameFromInnerMethods(List<Node> innerMethodsList, String callExpr){
        List<MethodCallExpr> callExprsList = new ArrayList<>();
        for(Node node : innerMethodsList)
            callExprsList.addAll(getMethodCallExprsListByNameFrom(node,callExpr));
        return callExprsList;
    }

    private List<MethodCallExpr> getMethodCallExprsByNameCalledDirectlyBy(Node node,String callExpr){
        List<Node> InnerMethodList = getInnerMethodsIn(node);
        List<MethodCallExpr>  wholeCallExprsList = getMethodCallExprsListByNameFrom(node,callExpr);
        List<MethodCallExpr>  wholeInnerMethodCallExprs = getMethodCallExprsListByNameFromInnerMethods(InnerMethodList,callExpr);
        return subtract(wholeCallExprsList,wholeInnerMethodCallExprs);
    }

    private String searchWidgetIdInBlockBody(BlockStmt blockStmt,String objectName){
        String result = "";
        List<MethodCallExpr> bindingMethodCallExprs = getMethodCallExprsByNameCalledDirectlyBy(blockStmt,"findViewById");
        if(bindingMethodCallExprs.isEmpty())
            return result;
        for(MethodCallExpr callExpr : bindingMethodCallExprs){
            if(isMatchWithPattern(getBiningVariableName(callExpr),objectName))
                return callExpr.getArgument(0).toString();
        }
        return result;
    }

    private String getBiningVariableName(MethodCallExpr callExpr) {
        Node tmpNode  = callExpr;
        while(!isVariableDeclarator(tmpNode) && !isAssignExpr(tmpNode))
            tmpNode = getParentNode(tmpNode);
        if(isAssignExpr(tmpNode))
            return ((AssignExpr) tmpNode).getTarget().toString();
        else
           return ((VariableDeclarator) tmpNode).getNameAsString();
    }

    private boolean inspectPatterns(EventHandlerInformation eventHandler, String[] classPatterns, String[] methodPatterns) {
        boolean result = false;
        List<MethodCallExpr> calledMethods = getDirectlyCalledMethodsBy(eventHandler.getAttachedMethod());
        for (MethodCallExpr methodCallExpr : calledMethods) {
            if (!isLocalMethod(eventHandler.getAttachedMethod(),methodCallExpr)) {
                if(!methodCallExpr.getScope().isEmpty()){
                    String objectName = ASTUtils.getObjectName(methodCallExpr);
                    String containingClassName = resolveClassName(eventHandler.getAttachedMethod(), objectName);
                    if (Pattern.isMatch(eventHandler.getAttachedMethod(), methodCallExpr, containingClassName,methodPatterns, classPatterns)){
                        result = true;
                        break;
                    }
                }
            }
            else {
                EventHandlerInformation calledMethod = findCalledMethodByName(methodCallExpr.getNameAsString());
                if (calledMethod != null &&
                    !ASTUtils.areSameMethod(eventHandler.getAttachedMethod(),calledMethod.getAttachedMethod())) {
                    result = inspectPatterns(calledMethod,classPatterns,methodPatterns);
                        break;
                }
            }
        }
        return result;
    }

    public boolean isStartOrDestroyActivityBy(EventHandlerInformation eventHandler) {
        boolean result = false;
        String[] methodPatterns ={"startActivity","startActivityAsResult","finish", "show"};
        String[] classPatterns = {"Dialog"};
        if(inspectPatterns(eventHandler,classPatterns,methodPatterns)){
            result = true;
        }
        return result;
    }

    public boolean isUpdateGUIBy(EventHandlerInformation eventHandler){
        boolean result = false;
        String[] methodPatterns ={"setView", "addView","show", "setAdapter",
                                   "notifyDataSetChanged","replace"};
        String[] classPatterns = {"Dialog", "Layout","ListView","Adapter","FragmentTransaction" };
        return inspectPatterns(eventHandler,classPatterns,methodPatterns);

    }

    public boolean isReadOrWriteFromImportanceResourceBy(EventHandlerInformation eventHandler) {
        boolean result = false;
        String[] methodPatterns = {"read", "write", "getString", "putString",
                                   "insert","update","delete","rawQuery","query","execSQL"
        };
        String[] classPatterns = {
                "FileOutputStream", "FileInputStream","BufferedInputStream",
                "BufferedOutputStream","SharedPreferences","SharedPreferences.Editor",
                "SQLiteDatabase"
        };
        return inspectPatterns(eventHandler,classPatterns,methodPatterns);

    }

    public boolean isClosedActivityBy(MethodDeclaration method){
        AtomicBoolean result = new AtomicBoolean(false);
        MethodDeclaration localMethod;
        List<MethodCallExpr> methodCallExprList = getMethodCallExprListCalledDirectlyBy(method);
        methodCallExprList.forEach(item ->{
            if(!isLocalMethod(item))
                if(isMatchWithPattern(item.getNameAsString(),"finish"))
                    result.set(true);
        });
        if(result.get())
            return  true;

        for(MethodCallExpr callExpr: methodCallExprList)
            if(isLocalMethod(callExpr)){
                 localMethod = findMethodByName(callExpr);
                 if(!ASTUtils.areSameMethod(method,localMethod))
                      if(isClosedActivityBy(localMethod))
                        return true;
                }
        return false;
    }

    private Set<ObjectCreationExpr> getObjectCreationListBy(Node method) {
        Set<ObjectCreationExpr> set = new ArrayListSet<>();
        method.findAll(ObjectCreationExpr.class).forEach(objCreationExpr->{
             set.add(objCreationExpr);
        });
        return set;
    }

    private Set<ObjectCreationExpr> getObjectCreationListDirectlyByInnerMethods(Node method) {
      Set<ObjectCreationExpr> set = new ArrayListSet<>();
      List<Node> innerMethods = getInnerMethods(method);
      innerMethods.forEach(innerMethod->{
          set.addAll(getObjectCreationListDirectlyBy(innerMethod));
      });
      return set;
    }

    private Set<ObjectCreationExpr> subSet(Set<ObjectCreationExpr> source, Set<ObjectCreationExpr> target){
        Set<ObjectCreationExpr> result = new ArrayListSet<>();
        for(ObjectCreationExpr item : target)
            if(source.contains(item))
                source.remove(item);
        return source;
    }

    private Set<ObjectCreationExpr> getObjectCreationListDirectlyBy(Node method) {
        Set<ObjectCreationExpr> wholeSet, subSet;
        wholeSet = getObjectCreationListBy(method);
        subSet = getObjectCreationListDirectlyByInnerMethods(method);
        return subSet(wholeSet,subSet);
    }

    public boolean isOpenActivityBy(MethodDeclaration method) {
        AtomicBoolean result = new AtomicBoolean(false);
        method.findAll(ObjectCreationExpr.class).forEach(item->{
            if(isMatchWithPattern(item.getType().getNameAsString(),"Intent"))
                result.set(true);
        });

        if(result.get())
            return  true;
        List<MethodDeclaration> localMethods = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(item->{
            if(isLocalMethod(item))
                localMethods.add(findMethodByName(item));
        });
        for(MethodDeclaration localMethod: localMethods)
            if(!ASTUtils.areSameMethod(method,localMethod))
               if(isOpenActivityBy(localMethod))
                    return true;
        return false;
    }

    public String getActivityOpenedBy(MethodDeclaration method) {
        String activityName = "";
        boolean flag = false;
        List<ObjectCreationExpr> objectCreationExpr = new ArrayList<>();
        List<MethodCallExpr> calledMethods = getDirectlyCalledMethodsBy(method);
        method.findAll(ObjectCreationExpr.class).forEach(item -> {
            if(isMatchWithPattern(item.getType().getNameAsString(),"Intent"))
                 objectCreationExpr.add(item);
        });
        for (ObjectCreationExpr expr : objectCreationExpr) {
            String argument = "";
                switch (expr.getArguments().size()) {
                    case 0:
                        List<MethodCallExpr> calledIntentMethods =
                                getMethodCallExprsByNameCalledDirectlyBy(method,"setAction");
                        if(!calledIntentMethods.isEmpty()){
                            if(calledIntentMethods.get(0).hasScope()){
                                String className = resolveClassName(method,ASTUtils.getScope(calledIntentMethods.get(0)));
                                if(isMatchWithPattern(className,"Intent")){
                                    argument = calledMethods.get(0).getArgument(0).toString();
                                    argument = argument.substring(argument.lastIndexOf('.') + 1 );
                                    activityName = getActionView(argument);
                                    if(!activityName.isEmpty())
                                        flag = true;
                                    }
                            }
                        }
                        else{
                             calledIntentMethods =
                                     getMethodCallExprsByNameCalledDirectlyBy(method,"setClass");
                             if(!calledIntentMethods.isEmpty())
                                 if(calledIntentMethods.get(0).hasScope()){
                                 String className = resolveClassName(method,ASTUtils.getScope(calledIntentMethods.get(0)));
                                 if(isMatchWithPattern(className,"Intent")){
                                     argument = calledIntentMethods.get(0).getArgument(1).toString();
                                     activityName = argument.substring(0, argument.lastIndexOf('.'));
                                     flag = true;
                                 }
                             }
                        }
                        break;
                    case 1:
                        argument = expr.getArgument(0).toString();
                        argument = argument.substring(argument.lastIndexOf('.') + 1 );
                        activityName = getActionView(argument);
                        if(!activityName.isEmpty())
                            flag = true;
                        break;
                    case 2:
                        argument = expr.getArgument(0).toString();
                        if(argument.contains("Intent.ACTION_")){
                            argument = argument.substring(argument.lastIndexOf('.') + 1 );
                            activityName = getActionView(argument);
                            if(!activityName.isEmpty())
                                flag = true;
                        }
                        else{
                            argument = expr.getArgument(1).toString();
                            activityName = argument.substring(0, argument.lastIndexOf('.'));
                            flag = true;
                        }
                        break;
                }

            //}
        }

        if(!flag)
            for(MethodCallExpr calledMethod : calledMethods){
                //TODO:: In this block, we only inspect methods that are called in the called that have declared in
                //       this class.
                if(isLocalMethod(calledMethod)){
                    MethodDeclaration event = findMethodByName(calledMethod);
                    activityName = getActivityOpenedBy(event);
                    if(activityName != ""){
                       break;
                    }
                }
            }
        return activityName;
    }

    private String getActionView(String argument) {
        String activityName = "";
        switch (argument) {
            case "ACTION_CALL":
                activityName = "PhoneCall";
                break;
            case "ACTION_SENDTO":
                activityName = "SendEmail";
                break;
            case "ACTION_SEND":
                activityName = "ShareContent";
                break;
            case "ACTION_IMAGE_CAPTURE":
                activityName = "CaptureImage";
                break;
            case "ACTION_VIEW":
                activityName = "ViewWebPage";
                break;
        }
        return activityName;
    }

    //TODO: This method must be inspect concisely.
    public String resolveClassName(Node callerMethod, String objectName) {
        List<VariableDeclarator> localVariablesList;
        Node involvedMethod = callerMethod;
        do{
             localVariablesList = getLocalVariables(involvedMethod);
             if(isLocalVariable(localVariablesList,objectName))
                return getContainingClassName(localVariablesList,objectName);
             involvedMethod = getParentMethodDeclaration(involvedMethod);
        }while(involvedMethod != null);

        List<VariableDeclarator> globalVariablesList = getGlobalVariableList();
         if(isGlobalVariable(globalVariablesList,objectName))
             return getContainingClassName(globalVariablesList,objectName);

         return "";

    }
     private MethodDeclaration getParentMethodDeclaration(Node method){
        Node parentBlock = null;
        Node tmpNode = (Node) method;
        do{
            tmpNode = getParentNode(tmpNode);
            if(isMethodDeclartionExpr(tmpNode)){
                parentBlock = (MethodDeclaration) tmpNode;
                break;
            }
        }while(!isClassOrInterfaceExpr(tmpNode));
        return (MethodDeclaration) parentBlock;
     }

    private String getContainingClassName(List<VariableDeclarator> variablesList, String objectName) {

        for(VariableDeclarator variable : variablesList)
            if(variable.getName().toString().equals(objectName)){
                return variable.getType().toString();
            }
        return "";
    }

    private boolean isLocalVariable(List<VariableDeclarator> localVariablesList, String objectName){
        return isExistVariableInSet(localVariablesList,objectName);
    }

    private boolean isGlobalVariable(List<VariableDeclarator> globalVariablesList, String objectName) {
        return isExistVariableInSet(globalVariablesList,objectName);
    }

    //ToDo::This method requires to inspect concisely.
    private List<VariableDeclarator> getLocalVariables(Node callerMethod) {
        List<VariableDeclarator> localVariablesList = new ArrayList<>();
        callerMethod.findAll(VariableDeclarator.class).forEach(item->{
            Node tmpNode = (Node) item;
            while(!tmpNode.getParentNode().get().getClass().getName().contains("MethodDeclaration"))
                   tmpNode = tmpNode.getParentNode().get();
            if(!callerMethod.isAncestorOf(tmpNode.getParentNode().get()))
                 localVariablesList.add(item);

        });

        return localVariablesList;
    }

    private List<VariableDeclarator> getGlobalVariableList() {
        List<FieldDeclaration> fieldsList= new ArrayList<>();
        List<VariableDeclarator> globalVariablesList = new ArrayList<>();
        AST.findAll(FieldDeclaration.class).forEach(item->fieldsList.add(item));
        for(FieldDeclaration field:fieldsList)
            for(int i = 0; i<field.getVariables().size();i++)
                globalVariablesList.add(field.getVariable(i));

        return globalVariablesList;
    }

    private boolean isExistVariableInSet(List<VariableDeclarator> variablesSet, String objectName){
        boolean result = false;
        if(objectName.contains("."))
            objectName = objectName.substring(0,objectName.indexOf('.'));
        for(VariableDeclarator variable : variablesSet)
            if(variable.getName().toString().equals(objectName)){
                result = true;
            }
        return result;
    }

    private boolean isLocalMethod(MethodDeclaration callerMethod, MethodCallExpr calledMethod){

        boolean result = false;
        if(!calledMethod.getScope().isEmpty()) {
            String objectName = ASTUtils.getObjectName(calledMethod);
            String containingClassName = resolveClassName(callerMethod, objectName);
            List<PsiClass> classesList = this.context.getProjectInformation().getProjectJavaClassList();
            if(ClassFinder.getClassByName(classesList,containingClassName)!= null)
                result = true;
            else
                result = false;
        }
        else{
              if (hasDeclaration(calledMethod))
                 result = true;
              else
                result = false;
       }

       return result;
    }

    private boolean hasDeclaration(MethodCallExpr calledMethod) {
        boolean result = false;
        PsiMethod[] methodsList = this.context.getActivityClass().getMethods();
        if(MethodFinder.getMethodByName(methodsList,calledMethod.getNameAsString()) != null)
            result = true;

        return result;
    }

    private String generateLabelFor(EventHandlerInformation eventHandler){
        String label = "";
        List<Widget> orderedWidgetsList = null;
        List<Widget> usedWidgetsList = extractUsedWidgetsListBy(eventHandler, true);
        orderedWidgetsList = orderUsedWidgetsBy(usedWidgetsList,eventHandler);
        eventHandler.setUsedWidgets(orderedWidgetsList);

        for(int index = 0; index <orderedWidgetsList.size(); index++)
           label += orderedWidgetsList.get(index).generateLabelFor(eventHandler);
        return label;
    }

    private List<Widget> orderUsedWidgetsBy(List<Widget> usedWidgetsList, EventHandlerInformation eventHandler) {
       List<Widget> orderedWidgetsList = new ArrayList<>();
       //TODO: We must order widgets that are used in eventHandler based on their orders in layout.
       return usedWidgetsList;
    }


    private List<Widget> extractUsedWidgetsListBy(EventHandlerInformation eventHandler, boolean flag) {
        List<MethodCallExpr> calledMethods = getMethodCallExprListCalledDirectlyBy(eventHandler.getAttachedMethod());//getDirectlyCalledMethodsBy(eventHandler.getAttachedMethod());
        List<VariableDeclarator> localVariablesList = new ArrayList<>();
        List<Widget> usedWidgetsList = new ArrayList<>();
        Set<String> usedObjectInMethod = new HashSet<String>();

        localVariablesList = getLocalVariables(eventHandler.getAttachedMethod());
        for (MethodCallExpr calledMethod : calledMethods)
            if (!calledMethod.getScope().isEmpty()) {
                String objectName = ASTUtils.getObjectName(calledMethod);
                String className = resolveClassName(eventHandler.getAttachedMethod(), objectName);
                if (Widget.isWidget(className) /*&&!isLocalVariable(localVariablesList,objectName)*/) {
                    if (!usedObjectInMethod.contains(objectName)) {
                        if(WidgetVisitor.visit(className,calledMethod.getNameAsString())){
                            usedObjectInMethod.add(objectName);
                            Widget widget = new Widget();
                            widget.setWidgetType(className);
                            if(isMatchWithPattern(className,"EditTex"))
                                eventHandler.setAlternativePathFlag();
                            String widgetId = extractWidgetId(eventHandler.getAttachedMethod(), objectName);
                            if (!widgetId.isEmpty()) {
                                widget.setWidgetIdDescriptor(widgetId);
                                extractWidgetDescriptorsFromStaticLayout(eventHandler,widget, widgetId);
                                usedWidgetsList.add(widget);
                            } else if(extractWidgetDescriptorsFromDynamicLayout(eventHandler, widget, objectName)){
                                      if(isMatchWithPattern(widget.getWidgetType(),"EditText"))
                                          eventHandler.setAlternativePathFlag();
                                      usedWidgetsList.add(widget);
                            }
                       }
                   }
                }
            }else if (isLocalMethod(eventHandler.getAttachedMethod(), calledMethod)) {
                    EventHandlerInformation newEventHandler = findCalledMethodByName(calledMethod.getNameAsString());
                    if(!ASTUtils.areSameMethod(eventHandler.getAttachedMethod(),newEventHandler.getAttachedMethod())){
                        List<Widget> widgets = extractUsedWidgetsListBy(newEventHandler, false);
                        usedWidgetsList.addAll(widgets);
                        widgets.forEach(item->{
                            if(isMatchWithPattern(item.getWidgetType(),"EditText"))
                                eventHandler.setAlternativePathFlag();
                        });
                    }
            }
            if(flag)
                if(!usedObjectInMethod.contains(eventHandler.getBindingName())){
                    Widget widget = new Widget();
                    widget.setWidgetType(eventHandler.getAttachedViewType());
                    if(isMatchWithPattern(eventHandler.getAttachedViewType(),"MainMenuItem")){
                        if(eventHandler.isOptionMenuStyle())
                            widget.setWidgetOptionMenuDescription(true);
                        else
                           widget.setWidgetOptionMenuDescription(false);
                    }
                    if(!eventHandler.getAttachedViewLable().isEmpty())
                        widget.setWidgetLabelDescriptor(eventHandler.getAttachedViewLable());
                    if(!eventHandler.getAttachedViewId().isEmpty())
                        widget.setWidgetIdDescriptor(eventHandler.getAttachedViewId());
                    if(!eventHandler.getAttachedViewContentDescription().isEmpty())
                        widget.setWidgetContentDescription(eventHandler.getAttachedViewContentDescription());
                    usedWidgetsList.add(widget);
                    if(isMatchWithPattern(widget.getWidgetType(),"EditText"))
                        eventHandler.setAlternativePathFlag();
                }

            return usedWidgetsList;
    }



    private boolean extractWidgetDescriptorsFromDynamicLayout(EventHandlerInformation eventHandler, Widget widget,
                                                          String objectName){
        boolean result = false;
        String value;

        MethodDeclaration definedMethod = getDefinerMethodOfObject(eventHandler.getAttachedMethod(),objectName);

        if((eventHandler.getContext_title() == null) && (eventHandler.getContext_description() == null))
            extractContextLabelsForEventHandler(eventHandler,definedMethod);

        List<MethodCallExpr> calledMethodsList = getCalledMethodsOfObject(definedMethod,objectName);
        for(MethodCallExpr method:calledMethodsList){
           if(method.getNameAsString().contains("Hint")){
               value = extractDescriptorValue(method);
               if(!value.isEmpty()) {
                   widget.setWidgetHintDescriptor(value);
                   result = true;
               }
           }
           else if(method.getNameAsString().contains("Tag")){
               value = extractDescriptorValue(method);
               if(!value.isEmpty()) {
                   widget.setWidgetTagValueDescriptor(value);
                   result = true;
               }
           }
           else if(method.getNameAsString().contains("ContentDescription")){
               value = extractDescriptorValue(method);
               if(!value.isEmpty()){
                   widget.setWidgetContentDescription(value);
                   result = true;
               }
           }
           else if(method.getNameAsString().contains("Text")){
               value = extractDescriptorValue(method);
               if(!value.isEmpty()){
                   widget.setWidgetLabelDescriptor(value);
                   result = true;
               }
           }
       }
        return result;
    }

    private void extractContextLabelsForEventHandler(EventHandlerInformation eventHandler, MethodDeclaration definedMethod) {
        List<MethodCallExpr> calledMethods;
        boolean titleFlag = false;
        boolean descriptionFlag = false;
        calledMethods = getDirectlyCalledMethodsBy(definedMethod);
        for(MethodCallExpr calledMethod: calledMethods){
            if(!calledMethod.getScope().get().toString().isEmpty()){
                String className = resolveClassName(definedMethod,ASTUtils.getObjectName(calledMethod));
                if(className.contains("Dialog")){
                    if(calledMethod.getName().toString().contentEquals("setTitle") && eventHandler.getContext_title() == null){
                        String parameter = calledMethod.getArgument(0).toString();
                        if(parameter != ""){
                            String value = extractLabelFromContent(parameter);
                            eventHandler.setContext_title(value);
                            titleFlag = true;
                        }
                    }

                    if(calledMethod.getName().toString().contentEquals("setMessage") && eventHandler.getContext_description() == null){
                        String parameter = calledMethod.getArgument(0).toString();
                        if(parameter != ""){
                            String value = extractLabelFromContent(parameter);
                            eventHandler.setContext_description(value);
                            descriptionFlag = true;
                        }
                    }

                }
            }
            if(titleFlag && descriptionFlag)
                break;
        }
    }

    private String extractLabelFromContent(String content){
        String result = "";
        if(content.startsWith("getString("))
            content = content.substring(content.lastIndexOf('(') + 1, content.lastIndexOf(')'));

        if(content.startsWith("R.string.")){
            StringValueExtractor stringValueExtractor =
                        new StringValueExtractor(context.getProjectInformation(), "strings");
                result = stringValueExtractor.findViewLabelById(content);
        }

        if(content.startsWith("\"") && content.endsWith("\""))
            result = content.substring(1,content.length()-1);

        return result;
    }


    private List<MethodCallExpr> getCalledMethodsOfObject(MethodDeclaration md, String objectName) {
        List<MethodCallExpr> calledMethodsByObject = new ArrayList<>();
        md.findAll(MethodCallExpr.class).forEach(methodCallExpr ->{
            if(methodCallExpr.getNameAsString().startsWith("set"))
                if(!methodCallExpr.getScope().isEmpty()){
                    String scope = methodCallExpr.getScope().get().toString();
                    if(scope.contains("."))
                        scope = scope.substring(0,scope.indexOf('.'));
                    if(scope.equals(objectName))
                        calledMethodsByObject.add(methodCallExpr);

                }
        } );
        return calledMethodsByObject;
    }

    private MethodDeclaration getDefinerMethodOfObject(MethodDeclaration md, String objectName) {
        List<VariableDeclarator> variableDeclaratorList;

        MethodDeclaration involvedMethod = md;
        do{
            variableDeclaratorList = getLocalVariables(involvedMethod);
            if(isLocalVariable(variableDeclaratorList,objectName))
                return involvedMethod;
            involvedMethod = getParentMethodDeclaration(involvedMethod);

        }while(involvedMethod!=null);
        return null;
    }

    private String extractDescriptorValue(MethodCallExpr method) {
        String  value = "";
        StringValueExtractor stringValueExtractor = new StringValueExtractor(context.getProjectInformation(),"strings");
        if(!method.getArguments().isEmpty()){
            Expression argumentValue = method.getArgument(0);
            if(!(argumentValue instanceof MethodCallExpr)){
                if(!argumentValue.toString().contains("R.string.")){
                      value = argumentValue.toString();
                      if(value.startsWith("\""))
                          value = value.substring(1,value.length()-1);
                }
                else
                    value = extractLabelFromContent(argumentValue.toString());
            }
        }
        return value;
    }

    private List<MethodCallExpr> getMethodCallExprsListByNames(Node method,List<String> patterns){
        List<MethodCallExpr> methodCallExprsWithSimilarName = new ArrayList<>();
        List<MethodCallExpr> methodCallExprList = getMethodCallExprsListFrom(method);
        for(MethodCallExpr callExprItem : methodCallExprList)
            if(isMatchWithPatterns(patterns,callExprItem.getNameAsString()))
                methodCallExprsWithSimilarName.add(callExprItem);
        return methodCallExprsWithSimilarName;
    }

    private List<MethodCallExpr> getMethodCallExprsFromInnerMethods(List<Node> innerMethods){
        List<MethodCallExpr> callExprs = new ArrayList<>();
        for(Node node : innerMethods)
            callExprs.addAll(getMethodCallExprsListFrom(node));
        return callExprs;
    }

    private List<MethodCallExpr> getMethodCallExprsByNamesDirectlyBy(Node node,List<String> patterns){
        List<MethodCallExpr> callExprs = getMethodCallExprsListByNames(node,patterns);
        List<MethodCallExpr> innerCallExpr = getMethodCallExprsFromInnerMethods(getInnerMethodsIn(node));
        return subtract(callExprs,innerCallExpr);
    }

    private String getStaticLayout(Node method) {
        String layoutFileName = "";
        List<String> patterns = new ArrayList<>(){{
            add("inflate");
            add("setContentView");
        }};

        int argIndex = 0;
        List<MethodCallExpr> methodCallExprs = getMethodCallExprsByNamesDirectlyBy(method,patterns);
        for(MethodCallExpr methodCallExpr : methodCallExprs)
            if(isMatchWithPatterns(patterns,methodCallExpr.getNameAsString()))
                if(methodCallExpr.hasScope()){
                    layoutFileName = ASTUtils.getArgument(methodCallExpr,argIndex);
                    layoutFileName = layoutFileName.substring(layoutFileName.lastIndexOf('.')+1);
                    break;
                }
        return layoutFileName;
    }
    private List<MethodCallExpr> getMethodCallExprsCalledDirectlyBy(Node node){
        List<Node> InnerMethodList = getInnerMethodsIn(node);
        List<MethodCallExpr>  wholeCallExprsList = getMethodCallExprsListFrom(node);
        List<MethodCallExpr>  wholeInnerMethodCallExprs = getMethodCallExprsFromInnerMethods(InnerMethodList);
        return subtract(wholeCallExprsList,wholeInnerMethodCallExprs);
    }
    private boolean hasStaticLayout(Node node) {
        List<String> patterns = new ArrayList<>(){{
            add("inflate");
            add("setContentView");
        }};
        List<MethodCallExpr> methodCallExprs = getMethodCallExprsCalledDirectlyBy(node);
        for(MethodCallExpr methodCallExpr : methodCallExprs)
            if(isMatchWithPatterns(patterns,methodCallExpr.getNameAsString()))
                if(methodCallExpr.hasScope())
                    return  true;
        return false;
    }


    private void extractWidgetDescriptorsFromStaticLayout(EventHandlerInformation event,Widget widget, String widgetId) {
        String inputType = "";
        String layout = layoutFileName;
        if(isDialogMethod(event.getAttachedMethod()))
            if(event.hasParent()){
                if(hasStaticLayout(event.getParentEventHandlerInformation().getAttachedMethod()))
                    layout = getStaticLayout(event.getParentEventHandlerInformation().getAttachedMethod());
            }
            else{
                Node node = getIncludedBlockNode(event.getAttachedMethod());
                layout = getStaticLayout(node);

            }
        LayoutInformationExtractor layoutInformationExtractor =
                new LayoutInformationExtractor(context.getProjectInformation(),layout);
        layoutInformationExtractor.extractOtherWidgetDescriptorsById(widget,widgetId);

    }

    public boolean isDialogMethod(MethodDeclaration node) {
        if(isDefaultDialogMethodPattern(node))
            return true;
        else if(isCustomizedDialogMethodPattern(node))
            return true;
        return false;
    }

    private boolean isDefaultDialogMethodPattern(MethodDeclaration node){
        int indexParameter = 0;
        if(hasParameter(node,indexParameter)){
            String parameter = ASTUtils.getParameterFrom(node,indexParameter);
            if(isMatchWithPattern("DialogInterface",parameter))
                return true;
        }
        return false;
    }

    private boolean isCustomizedDialogMethodPattern(MethodDeclaration method){
        String bindingViewObjectName = getBindingViewObjectNameOfInnerMethod(method);
        if(bindingViewObjectName.isEmpty())
            return false;
        Node node = getIncludedBlockNode(method);
        List<MethodCallExpr> callExprsList = getViewBindingMethodCallExprsDirectlyBy(node);
        for(MethodCallExpr callExpr :callExprsList){
            String extractedViewObjectName = extractViewObjectNameFrom(callExpr);
            if(extractedViewObjectName.contentEquals(bindingViewObjectName))
                if(callExpr.hasScope()){
                    String object = ASTUtils.getObjectName(callExpr);
                    if(isPartialMatchWithPattern("Dialog",resolveClassNameFromNode(node,object)))
                        return true;
                }
                else
                    return false;
        }
        return false;
    }

    private String resolveClassNameFromNode(Node node, String objectName) {
        List<VariableDeclarator> localVariablesList;
        localVariablesList = getLocalVariablesFromNode(node);
        if(isLocalVariable(localVariablesList,objectName))
            return getContainingClassName(localVariablesList,objectName);
        return "";
    }

    private String extractViewObjectNameFrom(MethodCallExpr callExpr){
        String objectName = "";
        Node node = (Node) callExpr;
        while(!isVariableDeclarator(getParentNode(node)))
            node = getParentNode(node);
        return ((VariableDeclarator) getParentNode(node)).getNameAsString();
    }

    private List<MethodCallExpr> getViewBindingMethodCallExprsDirectlyBy(Node node){
        List<Node> innerMethodList = getInnerMethodsIn(node);
        List<MethodCallExpr>  callExprsList = new ArrayList<>();
        List<MethodCallExpr>  bindingViewCallExpr = getViewBindingCallExprFrom(node);
        for(Node innerMethod : innerMethodList)
            bindingViewCallExpr = subtract(bindingViewCallExpr, getViewBindingCallExprFrom(innerMethod));
        return bindingViewCallExpr;
    }

    private List<MethodCallExpr> getViewBindingCallExprFrom(Node node){
        List<MethodCallExpr> bindingViewCallExprs = getMethodCallExprsListByName(node,"findViewById");
        List<MethodCallExpr> targetCallExprs  = new ArrayList<>();
        for(MethodCallExpr callExpr :bindingViewCallExprs)
            if(callExpr.hasScope())
                targetCallExprs.add(callExpr);
        return targetCallExprs;
    }

    private List<Node> getInnerMethods(Node node){
        List<Node> innerMethodList = new ArrayList<>();
        node.findAll(MethodDeclaration.class).forEach(item->{
            if(!isOuterMethod(item))
                innerMethodList.add(item);
        });
        return innerMethodList;
    }

    private List<Node> getInnerMethodsIn(Node node){
        List<Node> innerMethodList = getInnerMethods(node);
        innerMethodList.remove(node);
        return innerMethodList;
    }

    private String getBindingViewObjectNameOfInnerMethod(Node node) {
        String bindingViewName = "";
        if(!isClassMember(node)){
            node = node.getParentNode().get();
            while(!(isMethodCallExpr(node) || isVariableDeclarator(node)))
                node = node.getParentNode().get();
            if(isMethodCallExpr(node))
                bindingViewName = ASTUtils.getScope((MethodCallExpr) node);
        }
        return bindingViewName;
    }

    private Node getIncludedBlockNode(Node node) {
        while(!isBlockStmt(getParentNode(node)))
            node = node.getParentNode().get();
        return node.getParentNode().get();
    }

    private boolean hasParameter(Node node,int index){
        if(isMethodDeclartionExpr(node))
            if(!((MethodDeclaration)node).getParameters().isEmpty())
                if(((MethodDeclaration)node).getParameter(index) != null )
                    return true;
        return false;
    }

//    public String getParameterFrom(Node node,int index){
//        if(isMethodDeclartionExpr(node))
//            if(!((MethodDeclaration)node).getParameters().isEmpty())
//                return ((MethodDeclaration)node).getParameter(index).getType().toString();
//        return "";
//    }

    public boolean cotainDialog(MethodDeclaration method) {
        boolean flag = false;
        List<VariableDeclarator> localVariables = getLocalVariables(method);
        for(VariableDeclarator variable : localVariables)
            if(isPartialMatchWithPattern(variable.getType().toString(),"Dialog"))
                flag = true;
        return flag;
    }

    public String extractLabelForDialogMethod(EventHandlerInformation event) {
        String dialogTitle = "", dialogMessage = "";
        Node method = event.getAttachedMethod();
        StringValueExtractor stringValueExtractor = new StringValueExtractor(
               context.getProjectInformation(),"strings");
        Node tmpNode = ASTUtils.getParentNode(method);
        while (!ASTUtils.getClassName(tmpNode).contains("MethodDeclaration"))
            tmpNode = ASTUtils.getParentNode(tmpNode);
        final MethodDeclaration methodDeclaration = (MethodDeclaration) tmpNode;
        List<MethodCallExpr> callExprs = new ArrayList<>();
        tmpNode.findAll(MethodCallExpr.class).forEach(node -> {
            if (node.getNameAsString().contentEquals("setTitle") || node.getNameAsString().contentEquals("setMessage"))
                callExprs.add(node);
        });

        for (MethodCallExpr expr : callExprs) {
            if (expr.getNameAsString().contentEquals("setTitle")) {
                String objectName = ASTUtils.getObjectName(expr);
                String containingClassName = resolveClassName(methodDeclaration, objectName);
                if (containingClassName.contains("Dialog")) {
                    if (expr.getArgument(0).toString().startsWith("R.string."))
                        dialogTitle = stringValueExtractor.findViewLabelById(ASTUtils.getArgument(expr,0));
                    else {
                        dialogTitle = ASTUtils.getArgument(expr,0);
                        dialogTitle = dialogTitle.substring(1,dialogTitle.length()-1);
                    }

                }
            } else if (expr.getNameAsString().contentEquals("setMessage")) {
                String objectName = ASTUtils.getObjectName(expr);
                String containingClassName = resolveClassName(methodDeclaration, objectName);
                if (containingClassName.contains("Dialog")) {
                    if (expr.getArgument(0).toString().startsWith("R.string."))
                        dialogMessage = stringValueExtractor.findViewLabelById(expr.getArgument(0).toString());
                    else{
                        dialogMessage = ASTUtils.getArgument(expr,0);
                        dialogMessage = dialogMessage.substring(1,dialogMessage.length()-1);

                    }
                }
            }
        }
        POSTagger posTagger = new POSTagger();
        String label = "";
        if (dialogTitle != "")
            label = posTagger.generateLabelforDialog(dialogTitle.toLowerCase());
        else if (dialogMessage != "")
            label = posTagger.generateLabelforDialog(dialogMessage);
        return label;
    }

    private List<VariableDeclarator> getLocalVariablesFromNode(Node node){
        List<VariableDeclarator> variableDeclaratorList = new ArrayList<>();
        node.findAll(VariableDeclarator.class).forEach(item->{
            variableDeclaratorList.add(item);
        });
        return variableDeclaratorList;
    }

    public boolean isPositiveButtonOnClick(MethodDeclaration method){
        boolean result = false;
        Node tmpNode = (Node) method;
        while (!ASTUtils.getClassName(ASTUtils.getParentNode(tmpNode)).contains("MethodCallExpr"))
            tmpNode = ASTUtils.getParentNode(tmpNode);
        tmpNode = ASTUtils.getParentNode(tmpNode);
        MethodCallExpr expr = (MethodCallExpr) tmpNode;
        if(expr.getName().toString().contains("setPositiveButton"))
            result = true;
        if(expr.hasScope())
            if(!(isPartialMatchWithPattern(ASTUtils.getScope(expr),"cancel")
                    || isPartialMatchWithPattern(ASTUtils.getScope(expr),"Cancel")))
                result = true;
        return result;
    }

    public String extractLabelForInnerMethod(MethodDeclaration method){
        String block = "";
        //block = "fwriter.append(\"ppt " + activityName.toString() + ".";
        Node tmpNode = (Node) method;
        while (!ASTUtils.getClassName(ASTUtils.getParentNode(tmpNode)).contains("MethodCallExpr"))
            tmpNode = ASTUtils.getParentNode(tmpNode);
        tmpNode = ASTUtils.getParentNode(tmpNode);
        MethodCallExpr expr = (MethodCallExpr) tmpNode;
        String methodName = ASTUtils.getScope(expr);
        block += methodName;
        if(!isMatchWithPattern(((MethodDeclaration)method).getNameAsString(),"onClick"))
            block += "_" + ((MethodDeclaration)method).getNameAsString();
        else{
            if(isPartialMatchWithPattern(expr.getNameAsString(),"On") && isPartialMatchWithPattern(expr.getNameAsString(),"Click"))
                block += "_" + expr.getNameAsString().substring(expr.getNameAsString().indexOf('O'), expr.getNameAsString().lastIndexOf('L'));
        }
        return block;
    }

    public String getContent(String content){
        if(content.startsWith("getString("))
            content = content.substring(content.lastIndexOf('(') + 1, content.lastIndexOf(')'));
        if(content.startsWith("R.string."))
            content = getString(content);
        if(content.startsWith("\"") && content.endsWith("\""))
            content = content.substring(1,content.length() - 1);
        return content;
    }

    public String getString(String valuId){
        StringValueExtractor stringValueExtractor =
                new StringValueExtractor(context.getProjectInformation(), "strings");
        return stringValueExtractor.findViewLabelById(valuId);
    }
}