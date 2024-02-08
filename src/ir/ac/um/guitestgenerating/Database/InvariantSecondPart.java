package ir.ac.um.guitestgenerating.Database;

public class InvariantSecondPart{
    public String  destinationContext;
    public String  destinationView;
    public String  destinationViewAttribute;
    public int     destinationViewId;
    public boolean useOrigMethod;

    public InvariantSecondPart(){
        destinationContext = "";
        destinationView    = "";
        destinationViewAttribute = "";
        destinationViewId = -1;
        useOrigMethod     = false;
    }
    public InvariantSecondPart(String context,String view,String attribute,int viewId,boolean origMethod){
        destinationContext = context;
        destinationView = view;
        destinationViewAttribute = attribute;
        destinationViewId = viewId;
        useOrigMethod = origMethod;
    }

    public void setDestinationContext(String contex){
        this.destinationContext = contex;
    }
    public void setDestinationView(String view){
        this.destinationView = view;
    }
    public void setDestinationViewAttribute(String attribute){
        this.destinationViewAttribute = attribute;
    }
    public void setDestinationViewId(int viewId){
        this.destinationViewId = viewId;
    }
    public void setUseOrigMethod(boolean origMethod){
        this.useOrigMethod = origMethod;
    }
    public String getDestinationContext(){
        return this.destinationContext;
    }
    public String getDestinationView(){
        return this.destinationView;
    }
    public String getDestinationViewAttribute(){
        return this.destinationViewAttribute;
    }
    public int getDestinationViewId(){
        return destinationViewId;
    }
    public boolean isUsedOrigMethod(){
        return useOrigMethod;
    }

}