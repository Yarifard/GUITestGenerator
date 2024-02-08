package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ASTUtils {

    public static Node getParentNode(Node node){
        return node.getParentNode().get();
    }

    public static Node getAncientNode(Node node){
        return getParentNode(getParentNode(node));
    }

    public static String getScope(Node callExpr){
        return ((MethodCallExpr)callExpr).getScope().get().toString();
    }

    public static String getClassName(Node node){
        return node.getClass().getName();
    }

    public static Node getIncludedBlockNode(Node node) {
        while(!isBlockStmt(getParentNode(node)))
            node = getParentNode(node);
        return getParentNode(node);
    }

    public static List<VariableDeclarator> getLocalVariablesFromNode(Node node){
        List<VariableDeclarator> variableDeclaratorList = new ArrayList<>();
        node.findAll(VariableDeclarator.class).forEach(item->{
            variableDeclaratorList.add(item);
        });
        return variableDeclaratorList;
    }

    public static List<Node> getInnerMethods(Node node){
        List<Node> innerMethodList = new ArrayList<>();
        node.findAll(MethodDeclaration.class).forEach(item->{
            if(!isOuterMethod(item))
                innerMethodList.add(item);
        });
        return innerMethodList;
    }

    public static List<Node> getInnerMethodsIn(Node node){
        List<Node> innerMethodList = getInnerMethods(node);
        innerMethodList.remove(node);
        return innerMethodList;
    }

    public static String extractViewObjectNameFrom(Node node){
        while(!isVariableDeclarator(getParentNode(node)))
            node = getParentNode(node);
        return ((VariableDeclarator) getParentNode(node)).getNameAsString();
    }

    public static String getParameterFrom(Node node,int index){
        if(isMethodDeclartionExpr(node))
            if(!((MethodDeclaration)node).getParameters().isEmpty())
                return ((MethodDeclaration)node).getParameter(index).getType().toString();
        return "";
    }

    public static List<MethodCallExpr> getViewBindingCallExprFrom(Node node){
        List<MethodCallExpr> bindingViewCallExprs = getMethodCallExprsListByName(node,"findViewById");
        List<MethodCallExpr> targetCallExprs  = new ArrayList<>();
        for(MethodCallExpr callExpr :bindingViewCallExprs)
            if(callExpr.hasScope())
                targetCallExprs.add(callExpr);
        return targetCallExprs;
    }

    public static List<MethodCallExpr> getMethodCallExprsListFrom(Node node){
        List<MethodCallExpr> wholeCallExprsList = new ArrayList<>();
        node.findAll(MethodCallExpr.class).forEach(item->{
            wholeCallExprsList.add(item);
        });
        return wholeCallExprsList;
    }

    public static List<MethodCallExpr> getMethodCallExprsListByName(Node method,String pattern){
        List<MethodCallExpr> methodCallExprsWithSimilarName = new ArrayList<>();
        List<MethodCallExpr> methodCallExprList = getMethodCallExprsListFrom(method);
        for(MethodCallExpr callExprItem : methodCallExprList)
            if(isMatchWithPattern(pattern,callExprItem.getNameAsString()))
                methodCallExprsWithSimilarName.add(callExprItem);
        return methodCallExprsWithSimilarName;
    }

    public static boolean isClassMember(Node node){
        return isClassOrInterfaceExpr(getParentNode(node));
    }

    public static boolean isAssignExpr(Node node){
        return node.getClass().getName().contains("AssignExpr");

    }

    public static boolean isMethodDeclartionExpr(Node node){
        return getClassName(node).contains("MethodDeclaration");
    }


    public static boolean isMethodCallExpr(Node node){
        return getClassName(node).contains("MethodCallExpr");
    }

    public static boolean isClassOrInterfaceExpr(Node node){
       return getClassName(node).contains("ClassOrInterface");
    }

    public static boolean isVariableDeclarator(Node node){
        return getClassName(node).contains("VariableDeclarator");
    }

    public static boolean isLocalVariable(List<VariableDeclarator> localVariablesList, String objectName){
        return isExistVariableInSet(localVariablesList,objectName);
    }

    public static boolean isExistVariableInSet(List<VariableDeclarator> variablesSet, String objectName){
        boolean result = false;
        if(objectName.contains("."))
            objectName = objectName.substring(0,objectName.indexOf('.'));
        for(VariableDeclarator variable : variablesSet)
            if(variable.getName().toString().equals(objectName)){
                result = true;
            }
        return result;
    }

    public static boolean isMethodDeclarationExpr(Node node){
        return getClassName(node).contains("MethodDeclaration");
    }

    public static boolean isBlockStmt(Node node){
        return getClassName(node).contains("BlockStmt");
    }

    public static boolean isOuterMethod(Node node) {
        return isVariableDeclarator(getAncientNode(node));
    }

    public static boolean hasParameter(Node node, int index){
        if(isMethodDeclarationExpr(node))
            if(!((MethodDeclaration)node).getParameters().isEmpty())
                if(((MethodDeclaration)node).getParameter(index) != null )
                    return true;
        return false;
    }
    public static boolean containDialog(EventHandlerInformation eventHandler) {
        boolean flag = false;
        List<EventHandlerInformation> childsEventHandlerList = eventHandler.getChildEventHandlers();
        for(EventHandlerInformation eventHandlerItem : childsEventHandlerList)
            if(isDialogMethod(eventHandlerItem.getAttachedMethod())){
                flag = true;
                break;
            }
        return flag;
    }
    public static boolean isDialogMethod(MethodDeclaration node) {
        if(isDefaultDialogMethodPattern(node))
            return true;
        else if(isCustomizedDialogMethodPattern(node))
            return true;
        return false;
    }
    public static boolean isDefaultDialogMethodPattern(MethodDeclaration node){
        int indexParameter = 0;
        if(hasParameter(node,indexParameter)){
            String parameter = getParameterFrom(node,indexParameter);
            if(isMatchWithPattern("DialogInterface",parameter))
                return true;
        }
        return false;
    }

    public static String getArgument(Node methodCallExpr, int argIndex){
        return ((MethodCallExpr) methodCallExpr).getArgument(argIndex).toString();
    }


    public static BlockStmt getParentBlock(Node node){
        BlockStmt parentBlock = null;
        Node tmpNode = node;
        do{
            tmpNode = getParentNode(tmpNode);
        }while(!(isBlockStmt(tmpNode) || isClassOrInterfaceExpr(tmpNode)));

        if(isBlockStmt(tmpNode))
            parentBlock = (BlockStmt) tmpNode;
        return parentBlock;

    }


    public static boolean isCustomizedDialogMethodPattern(Node method){
        String bindingViewObjectName = getBindingViewObjectNameOfInnerMethod(method);
        if(bindingViewObjectName.isEmpty())
            return false;
        Node node = getIncludedBlockNode(method);
        List<MethodCallExpr> callExprsList = getViewBindingMethodCallExprsDirectlyBy(node);
        for(MethodCallExpr callExpr :callExprsList){
            String extractedViewObjectName = extractViewObjectNameFrom(callExpr);
            if(extractedViewObjectName.contentEquals(bindingViewObjectName))
                if(callExpr.hasScope()){
                    String object = getScope(callExpr);
                    if(isPartialMatchWithPattern("Dialog",resolveClassNameFromNode(node,object)))
                        return true;
                }
                else
                    return false;
        }
        return false;
    }

    public static List<MethodCallExpr> getViewBindingMethodCallExprsDirectlyBy(Node node){
        List<Node> innerMethodList = getInnerMethodsIn(node);
        List<MethodCallExpr>  callExprsList = new ArrayList<>();
        List<MethodCallExpr>  bindingViewCallExpr = getViewBindingCallExprFrom(node);
        for(Node innerMethod : innerMethodList)
            bindingViewCallExpr = subtract(bindingViewCallExpr, getViewBindingCallExprFrom(innerMethod));
        return bindingViewCallExpr;
    }


    public static String getBindingViewObjectNameOfInnerMethod(Node node) {
        String bindingViewName = "";
        if(!isClassMember(node)){
            node = getParentNode(node);
            while(!(isMethodCallExpr(node) || isVariableDeclarator(node)))
                node = getParentNode(node);
            if(isMethodCallExpr(node))
                bindingViewName = getScope((MethodCallExpr) node);
        }
        return bindingViewName;
    }

    public static String resolveClassNameFromNode(Node node, String objectName) {
        List<VariableDeclarator> localVariablesList;
        if(objectName.contains("."))
            objectName = objectName.substring(0,objectName.indexOf('.'));
        localVariablesList = getLocalVariablesFromNode(node);
        if(isLocalVariable(localVariablesList,objectName))
            return getContainingClassName(localVariablesList,objectName);
        return "";
    }

    public static String getContainingClassName(List<VariableDeclarator> variablesList, String objectName) {

        for(VariableDeclarator variable : variablesList)
            if(isMatchWithPattern(variable.getNameAsString(),objectName)){
                return variable.getType().toString();
            }
        return "";
    }

    public static List<MethodCallExpr> subtract(List<MethodCallExpr> wholeSet, List<MethodCallExpr> subSet){
        List<MethodCallExpr> resultMethodCallExprsList = new ArrayList<>();
        for(int index = 0 ; index < wholeSet.size(); index++)
            if(!subSet.contains(wholeSet.get(index)))
                resultMethodCallExprsList.add(wholeSet.get(index));
        return resultMethodCallExprsList;
    }

    public static boolean hasIntentPattern(MethodDeclaration attachedMethod) {
        List<String> patterns = new ArrayList<>();
        patterns.add("Intent");
        List<ObjectCreationExpr> objectCreationExprs = new ArrayList<>();
        attachedMethod.findAll(ObjectCreationExpr.class).forEach(item->{
            if(isMatchWithPatterns(patterns,item.getType().toString()))
                objectCreationExprs.add(item);
        });
        if(objectCreationExprs.size() > 0)
            return true;
        return false;
    }

    public static boolean isMatchWithPatterns(List<String> patterns,String targetItem){
        if(patterns.contains(targetItem))
            return true;
        return false;
    }

    public static boolean isMatchWithPattern(String pattern,String targetItem ){
        if(pattern.contentEquals(targetItem))
            return true;
        return false;
    }

    public static boolean isPartialMatchWithPattern(List<String> patterns,String targetItem){
        for(String item : patterns)
            if(targetItem.contains(item))
                return true;
        return false;
    }

    public static boolean isPartialMatchWithPattern(String pattern,String targetItem){
        if(targetItem.contains(pattern) || pattern.contains(targetItem))
            return true;
        return false;
    }

    public static boolean isPlacedInSameBlock(BlockStmt parentBlock, BlockStmt childBlock) {
        if(isMatchWithPattern(parentBlock.toString(),childBlock.toString()))
            return true;
        return false;
    }

    public static boolean isPlacedInConditionBlock(Node node) {
        while(!((node instanceof IfStmt || node instanceof SwitchStmt || node instanceof MethodDeclaration )))
            node = getParentNode(node);
        if(node instanceof IfStmt)
            return  true;
        else if(node instanceof SwitchStmt)
            return true;
        else
            return false;
    }

    public static boolean areSameMethod(MethodDeclaration method, MethodDeclaration targetMethod) {
        if(isMatchWithPattern(method.toString(),targetMethod.toString()))
            return true;
        return false;
    }

    public static MethodCallExpr getParentMethodCallExpr(Node node) {
        while(node.getClass() != MethodCallExpr.class)
            node = getParentNode(node);
        return (MethodCallExpr) node;
    }

    public static boolean isObjectCreationMethod(Node node) {
        while(!(isAssignExpr(node) ||isClassOrInterfaceExpr(node) || isVariableDeclarator(node)))
            node = getParentNode(node);
        if(isAssignExpr(node) || isVariableDeclarator(node))
            return true;
        return false;
    }


    public static List<MethodCallExpr> getMethodCallExprsCalledDirectlyBy(Node node){
        List<Node> InnerMethodList = getInnerMethodsIn(node);
        List<MethodCallExpr>  wholeCallExprsList = getMethodCallExprsListFrom(node);
        List<MethodCallExpr>  wholeInnerMethodCallExprs = getMethodCallExprsFromInnerMethods(InnerMethodList);
        return subtract(wholeCallExprsList,wholeInnerMethodCallExprs);
    }

    public static List<MethodCallExpr> getMethodCallExprsFromInnerMethods(List<Node> innerMethods){
        List<MethodCallExpr> callExprs = new ArrayList<>();
        for(Node node : innerMethods)
            callExprs.addAll(getMethodCallExprsListFrom(node));
        return callExprs;
    }

    public static boolean isLocalMethod(Node node){
        node = getParentNode(node);
        if(Utils.isPartialMatchWithPattern(getClassName(node),"ClassOrInterfaceDeclaration"))
            return true;
        else
            return false;

    }

    public static String getObjectName(MethodCallExpr calledMethod) {
        String objectName = getScope(calledMethod);
        if(objectName.contains("."))
            objectName = objectName.substring(0,objectName.lastIndexOf('.'));
        return objectName;
    }


}
