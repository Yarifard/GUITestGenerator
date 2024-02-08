package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiStatement;

/*
  Author : Ali Asghar Yarifard
  Description: This class provides methods for calculating cyclomatic complexity of the input
  method. In this class, only we evaluated the input's method instructions and counts its branches.
  It should be noted that, this class does not consider the complexity of API and other methods that are used in it.
 */

public class CyclomaticComplexityAnalyzer {
    public int getComplexity(MethodDeclaration method) {
        BlockStmt methodBody=new BlockStmt();
        if(method.getBody().isPresent())
           methodBody = method.getBody().get();

        //TODO modify the implementation
        /* for a simple method that has if-then-else statement, complexity should be 2, but 3 is returned.
         the reason is that the default value of 1 is added to the complexity of the method.
         if a method has no branch, its complexity should be 1, but if it has some branches, the initial value of complexity should be 0.
        */
        int cyclomatic = 1;
        cyclomatic = getCyclomaticComplexityOfBlockExpression(methodBody,cyclomatic);
        return cyclomatic;

    }

    private boolean isBranch(Statement st){
        boolean branchStatement = false;
        if(st.isIfStmt())
            branchStatement = true;
        else if(st.isForStmt())
            branchStatement = true;
        else if(st.isTryStmt())
            branchStatement = true;
        else if(st.isForEachStmt())
            branchStatement = true;
        else if(st.isDoStmt())
            branchStatement = true;
        else if(st.isWhileStmt())
            branchStatement = true;
        else if(st.isSwitchStmt())
            branchStatement = true;
        return branchStatement;
    }


    private int getCyclomaticComplexityOfBlockExpression(BlockStmt codeBlock, int cyclomatic) {
        if (codeBlock.isEmpty() || codeBlock.getStatements().size() == 0) {
            // System.out.println(method.getText());
            return cyclomatic;
        }

        for (int i=0;i<codeBlock.getStatements().size();i++) {
            Statement statement = codeBlock.getStatements().get(i);
            if (isBranch(statement))
                cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);
        }
        return cyclomatic;
    }


    private int getCyclomaticComplexityOfBranchExpression(Statement statement, int cyclomatic){
        if(statement.isIfStmt())
             cyclomatic = getCyclomaticComplexityOfIfExpression(statement,++cyclomatic);
        else if(statement.isSwitchStmt())
             cyclomatic = getCyclomaticComplexityOfSwitchExpression(statement,cyclomatic);
        else if(statement.isWhileStmt())
             cyclomatic = getCyclomaticComplexityOfWhileExpression(statement,++cyclomatic);
        else if(statement.isDoStmt())
            cyclomatic = getCyclomaticComplexityOfDoExpression(statement,++cyclomatic);
        else if(statement.isForStmt())
            cyclomatic = getCyclomaticComplexityOfForExpression(statement,++cyclomatic);
        else if(statement.isForEachStmt())
            cyclomatic = getCyclomaticComplexityOfForEachExpression(statement,++cyclomatic);
        else
            cyclomatic = getCyclomaticComplexityOfBlockExpression(((TryStmt) statement).getTryBlock(),cyclomatic);
        return cyclomatic;
    }

    private int getCyclomaticComplexityOfForEachExpression(Statement statement, int cyclomatic) {
        if (((ForEachStmt) statement).getBody().isBlockStmt()) {
            BlockStmt codeBlock;
            codeBlock = ((ForEachStmt) statement).getBody().asBlockStmt();
            cyclomatic = getCyclomaticComplexityOfBlockExpression(codeBlock, cyclomatic);
        } else {
            statement = ((ForEachStmt) statement).getBody();
            if(isBranch(statement))
                cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);
        }
        return cyclomatic;
    }

    private int getCyclomaticComplexityOfForExpression(Statement statement, int cyclomatic) {
        if (((ForStmt) statement).getBody().isBlockStmt()) {
            BlockStmt codeBlock;
            codeBlock = ((ForStmt) statement).getBody().asBlockStmt();
            cyclomatic = getCyclomaticComplexityOfBlockExpression(codeBlock, cyclomatic);
        } else {
            statement = ((ForStmt) statement).getBody();
            if(isBranch(statement))
                cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);
        }
        return cyclomatic;
    }

    private int getCyclomaticComplexityOfDoExpression(Statement statement, int cyclomatic) {
        if (((DoStmt) statement).getBody().isBlockStmt()) {
            BlockStmt codeBlock;
            codeBlock = ((DoStmt) statement).getBody().asBlockStmt();
            cyclomatic = getCyclomaticComplexityOfBlockExpression(codeBlock, cyclomatic);
        } else {
            statement = ((DoStmt) statement).getBody();
            if(isBranch(statement))
                cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);
        }
        return  cyclomatic;
    }

    private int getCyclomaticComplexityOfWhileExpression(Statement statement, int cyclomatic) {

        if (((WhileStmt) statement).getBody().isBlockStmt()) {
            BlockStmt codeBlock;
            codeBlock = ((WhileStmt) statement).getBody().asBlockStmt();
            cyclomatic = getCyclomaticComplexityOfBlockExpression(codeBlock, cyclomatic);
        } else {
            statement = ((WhileStmt) statement).getBody();
            if(isBranch(statement))
                cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);
        }
        return cyclomatic;
    }

    private int getCyclomaticComplexityOfSwitchExpression(Statement statement, int cyclomatic) {

        for(SwitchEntry entry:((SwitchStmt) statement).getEntries()){
            cyclomatic++;
            for(Statement stmt:entry.getStatements()){
                if(stmt.isBlockStmt()){
                    cyclomatic = getCyclomaticComplexityOfBlockExpression((BlockStmt) stmt,cyclomatic);
                }
                else{
                    if(isBranch(stmt))
                        cyclomatic = getCyclomaticComplexityOfBranchExpression(stmt,cyclomatic);
                }
            }
        }
        if(statement.toString().contains(("default:")))
            cyclomatic--;
        return cyclomatic;
    }

    private int getCyclomaticComplexityOfIfExpression(Statement statement,int cyclomatic){
        if(statement.asIfStmt().getThenStmt().isBlockStmt())
            cyclomatic = getCyclomaticComplexityOfBlockExpression(statement.asIfStmt().getThenStmt().asBlockStmt(),cyclomatic);
        else {
             // statement = statement.asIfStmt().getThenStmt();
              if(isBranch(statement.asIfStmt().getThenStmt()))
                  cyclomatic = getCyclomaticComplexityOfBranchExpression(statement.asIfStmt().getThenStmt(),cyclomatic);
        }

        if(statement.asIfStmt().getElseStmt().isPresent()){
           statement = statement.asIfStmt().getElseStmt().get();
           if(statement.isBlockStmt())
               cyclomatic = getCyclomaticComplexityOfBlockExpression((BlockStmt) statement,cyclomatic);
           else if(isBranch(statement))
               cyclomatic = getCyclomaticComplexityOfBranchExpression(statement,cyclomatic);

        }
        return cyclomatic;
    }
}
