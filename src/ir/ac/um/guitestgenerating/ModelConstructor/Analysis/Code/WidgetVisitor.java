package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code;

public class WidgetVisitor {
    public static boolean visit(String className, String methodName){
        boolean result = false;
        switch (className){
            case "EditText":
                            result = visit_EditText(methodName);
                            break;
            case "Spinner":
                             result = visit_Spinner(methodName);
                             break;
            case "CheckBox":
                             result = visit_CheckBox(methodName);
                             break;
       }
        return result;
    }

    private static boolean visit_CheckBox(String methodName){
        boolean result = false;
        if(methodName.contentEquals("isChecked"))
            result = true;
        return result;
    }

    private static  boolean visit_EditText(String methodName){
        boolean result = false;
        if(methodName.contentEquals("getText"))
            result = true;
        return result;
    }

    private static boolean visit_Spinner(String methodName){
        boolean result = false;
        if(methodName.contentEquals("getSelectedItem") || methodName.contentEquals("getSelectedItemId"))
            result = true;
        return result;
    }
}
