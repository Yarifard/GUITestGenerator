package ir.ac.um.guitestgenerating.Util;

public class RelationOperator {

    public static boolean isEqualTo(String opt){
        if(opt.contentEquals("=="))
            return true;
        return false;
    }

    public static boolean isNotEqualTo(String opt){
        if(opt.contentEquals("!="))
            return true;
        return false;
    }

    public static boolean isLessThan(String opt){
        if(opt.contentEquals("<"))
            return true;
        return false;
    }

    public static boolean isLessThanOrEqualTo(String opt){
        if(opt.contentEquals("<="))
            return true;
        return false;
    }

    public static boolean isGreaterThan(String opt){
        if(opt.contentEquals(">"))
            return true;
        return false;
    }

    public static boolean isGreaterThanOrEqualTo(String opt){
        if(opt.contentEquals(">="))
            return true;
        return false;
    }
}
