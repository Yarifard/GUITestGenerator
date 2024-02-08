package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Method;


public class MethodComplexity {
    private double cyclomaticComplexity;
    private double calledMethodComplexity;
    private double intentComplexity;
    private double asyncComplexity;
    private double halstedComplexity;
    public MethodComplexity(){
        cyclomaticComplexity   = 0.0;
        calledMethodComplexity = 0.0;
        intentComplexity = 0.0;
        asyncComplexity  = 0.0;
        halstedComplexity = 0.0;
    }
    public void setCyclomaticComplexity(double cyclomaticComplexity){
        this.cyclomaticComplexity = cyclomaticComplexity;
    }
    public void setCalledMethodComplexity(double calledMethodComplexity){
        this.calledMethodComplexity= calledMethodComplexity;
    }
    public void setIntentComplexity(double intentComplexity){
        this.intentComplexity = intentComplexity;
    }
    public void setAsyncComplexity(double asyncComplexity){
        this.asyncComplexity = asyncComplexity;
    }
    public void setHalstedComplexity(double halstedComplexity){
        this.halstedComplexity = halstedComplexity;
    }
    public double getTotalComplexity(){
        return ((halstedComplexity + cyclomaticComplexity) / 2)
                + asyncComplexity + intentComplexity + calledMethodComplexity;
    }
}
