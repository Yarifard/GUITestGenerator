package ir.ac.um.guitestgenerating.Database;

public class PointType {
    private int pointId;
    private String mainContext;
    private String parentContext;
    private String methodName;
    private String pointType;
    private String caption;
    private int packageId;

    public PointType(String mainContext,String parentContext,String methodName,String pointType,String caption){
        this.mainContext = mainContext;
        this.parentContext = parentContext;
        this.methodName = methodName;
        this.pointType = pointType;
        this.caption = caption;
    }

    public int getPackageId() {
        return packageId;
    }

    public int getPointId() {
        return pointId;
    }

    public String getMainContext(){
        return mainContext;
    }

    public String getParentContext(){
        return parentContext;
    }

    public String getMethodName(){
        return methodName;
    }

    public String getPointType(){
        return pointType;
    }

    public String getCaption(){
        return caption;
    }

    public void setPointId(int pointId){
        this.pointId = pointId;
    }

    public void setMainContext(String mainContext){
        this.mainContext = mainContext;
    }

    public void setParentContext (String parentContext){
        this.parentContext = parentContext;
    }

    public void setMethodName(String methodName){
        this.methodName = methodName;
    }

    public void setPointType(String pointType){
        this.pointType = pointType;
    }
     public void setCaption(String caption){
        this.caption = caption;
     }

   public void setPackageId(int packageId){
        this.packageId = packageId;
   }
}
