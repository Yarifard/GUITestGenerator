package ir.ac.um.guitestgenerating.Util;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
//import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class Utils {
    private static ConsoleView consoleView;

    public static String capitalize(String text) {
        if (text != null && !text.isEmpty()) {
            text = "" + text.toUpperCase().charAt(0) + text.toLowerCase().substring(1);
        }
        return text;
    }

    public static String getTimestamp() {
        String pattern = "yyyy_MM_dd_HH_mm_ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    public static ConsoleView getConsoleView() {
        return Utils.consoleView;
    }

    public static void setConsoleView(ConsoleView consoleView) {
        Utils.consoleView = consoleView;
    }

    public static void showMessage(String message) {
        if (consoleView != null) {
            consoleView.print(String.format("%s%n", message),
                    ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }

    public static void showException(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        showMessage(writer.toString());
    }


    public static boolean hasTestData(String result) {
        if(result.contains("\"Data\""))
            return true;
        return false;
    }

    public static boolean isMatch(String source, String target) {
        if(source.contentEquals(target))
            return true;
        return false;
    }

    public static boolean isMatchWithPatterns(String[] patterns,String content){
        boolean result = false;
        for(int i = 0; i < patterns.length; i++ )
            if(patterns[i].toLowerCase().contentEquals(content.toLowerCase())){
                result = true;
                break;
            }
        return result;
    }

    public static boolean isMatchWithPatterns(List<String> patterns, String targetItem){
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

    public static boolean isPartialMatchWithPattern(String[] patterns,String targetItem){
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

    public static boolean isMatchWithConditionPatterns(String source) {
        return isPartialMatch(source, ".getId()") || isPartialMatch(source, ".getItemId()");

    }

    private static boolean isPartialMatch(String source, String template) {
        return source.contains(template);

    }
}
