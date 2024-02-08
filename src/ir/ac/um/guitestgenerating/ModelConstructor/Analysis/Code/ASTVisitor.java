package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ASTVisitor {
    static Node onCreateOptionsMenuMethod = null;
    static Node onCreateContextMenuMethod = null;
    static Node onOptionsItemSelectedMethod = null;
    static Node onContextItemSelectedMethod = null;


    public static void visitMainMenuEelement(Node root){
        onCreateOptionsMenuMethod = null;
        root.findAll(MethodDeclaration.class).forEach(item->{
            if(Utils.isMatch("onCreateOptionsMenu",item.getNameAsString()))
            onCreateOptionsMenuMethod = item;
        });
    }

    public static void visitContextMenuElement(Node root){
        root.findAll(MethodDeclaration.class).forEach(method->{
            if(Utils.isMatch("onCreateContextMenu",method.getNameAsString()))
                onCreateContextMenuMethod = method;
        });
    }
    public static boolean isExistMainMenuElement(Node root){
        onCreateOptionsMenuMethod = null;
        visitMainMenuEelement(root);
        return onCreateOptionsMenuMethod != null;
    }

    public static boolean isExistContextMenuElement(Node root) {
        onCreateContextMenuMethod = null;
        visitContextMenuElement(root);
        return onCreateContextMenuMethod != null;
    }

    public static boolean isStandardMainMenu(Node root) {
        AtomicReference<Boolean> flag = new AtomicReference<>(true);
        root.findAll(MethodDeclaration.class).forEach(method->{
            if(Utils.isMatch(method.getNameAsString(),"onOptionsItemSelected")
                    && Utils.isMatch(method.getParameter(0).getType().toString(),"MenuItem")){
                onOptionsItemSelectedMethod = method;
                flag.set(false);
            }
        });
        return flag.get();
    }

    public static boolean isStandardContextMenu(Node root) {
        AtomicReference<Boolean> flag = new AtomicReference<>(true);
        root.findAll(MethodDeclaration.class).forEach(method->{
            if(Utils.isMatch(method.getNameAsString(),"onContextItemSelected")
                    && Utils.isMatch(method.getParameter(0).getType().toString(),"MenuItem")){
                onContextItemSelectedMethod = method;
                flag.set(false);
            }
        });
        return flag.get();
    }

    public static boolean visitSwitchStmt(Node body){
        AtomicBoolean flag = new AtomicBoolean(false);
        body.findAll(SwitchStmt.class).forEach(switchStmt ->{
            if(Utils.isMatchWithConditionPatterns(switchStmt.getSelector().toString()))
                flag.set(true);
        });
        return flag.get();
    }

    public static List<CodeBlock> visitSwitchStmtEntries(Node body){
        List<CodeBlock> codeBlocks = new ArrayList<>();
        List<SwitchEntry> entryList  = new ArrayList<>();
        body.findAll(SwitchStmt.class).forEach(switchStmt ->{
            if(Utils.isMatchWithConditionPatterns(switchStmt.getSelector().toString()))
                for(SwitchEntry entry:switchStmt.getEntries()){
                    NodeList<Statement> statements = entry.getStatements();
                    statements.remove(statements.size()-1);
                    codeBlocks.add(new CodeBlock(entry.getLabels().get(0).toString(),statements.toString()));
                }
        });
        return  codeBlocks;
    }

    public static List<CodeBlock> visitIfStmtSections(Node body) {
        List<CodeBlock> codeBlocks = new ArrayList<>();
        List<SwitchEntry> entryList  = new ArrayList<>();
        body.findAll(IfStmt.class).forEach(ifStmt ->{
            if(Utils.isMatchWithConditionPatterns(ifStmt.getCondition().toString()))
                    codeBlocks.add(new CodeBlock(ifStmt.getCondition().toString(),ifStmt.getThenStmt().toString()));
        });
        return  codeBlocks;
    }

    public static boolean visitIfStmt(Node body){
        AtomicBoolean flag = new AtomicBoolean(false);
        body.findAll(IfStmt.class).forEach(ifItem ->{
            if(Utils.isMatchWithConditionPatterns(ifItem.getCondition().toString()))
                flag.set(true);
        });
        return flag.get();
    }

    public static Node getOnCreateOptionsMenuMethod(){ return onCreateOptionsMenuMethod; }
    public static Node getOnOptionsItemSelectedMethod(){ return onOptionsItemSelectedMethod;}
    public static Node getonCreateContextMenu() { return onCreateContextMenuMethod;}
    public static Node getonContextItemSelected() { return onContextItemSelectedMethod; }
}

