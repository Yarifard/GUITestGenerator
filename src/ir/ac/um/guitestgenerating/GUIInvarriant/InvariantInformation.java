package ir.ac.um.guitestgenerating.GUIInvarriant;

public class InvariantInformation {

    private int rowId;
    private String sourceContext;
    private String sourceView;
    private String sourceViewAttribute;
    private int    sourceViewId;
    private String relationOperator;
    private String mathOperator;
    private String content;
    private int contentType;
    private String destinationContext;
    private String destinationView;
    private String destinationViewAttribute;
    private int destinationViewId;
    private boolean isUsedOrigMethod;

    public InvariantInformation(){
        rowId = -1;
        sourceContext = "";
        sourceView = "";
        sourceViewAttribute = "";
        sourceViewId = -1;
        relationOperator = "";
        mathOperator = "";
        content = "";
        contentType = -1;
        destinationContext = "";
        destinationView = "";
        destinationViewAttribute = "";
        destinationViewId = -1;
        isUsedOrigMethod = false;

    }
    public void setRowId(int rowId){
        this.rowId = rowId;
    }

    public void setSourceContext(String context){
        this.sourceContext = context;
    }
    public void setSourceView(String sourceView){
        this.sourceView = sourceView;
    }
    public void setSourceViewAttribute(String attribute){
        this.sourceViewAttribute = attribute;
    }
    public void setSourceViewId(int viewId){
        this.sourceViewId = viewId;
    }
    public void setRelationOperator(String relationOperator){
        this.relationOperator = relationOperator;
    }
    public void setContentType(int contentType){
        this.contentType = contentType;
    }
    public void setDestinationContext(String context){
        this.destinationContext = context;
    }
    public void setDestinationView(String destinationView){
        this.destinationView = destinationView;
    }
    public void setDestinationViewAttribute(String attribute){
        this.destinationViewAttribute = attribute;
    }
    public void setMathOperator(String mathOperator){
        this.mathOperator = mathOperator;
    }
    public void setDestinationViewId(int viewId){
        this.destinationViewId = viewId;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setIsUsedOrigMethod(int isUsedOrigMethod){
        if(isUsedOrigMethod == 1)
            this.isUsedOrigMethod = true;
        else
            this.isUsedOrigMethod = false;
    }

    public String getSourceContext(){ return sourceContext; }
    public String getSourceView(){ return sourceView; }
    public String getSourceViewAttribute(){ return sourceViewAttribute; }
    public String getRelationOperator(){ return relationOperator; }
    public String getContent(){ return content; }
    public String getDestinationView(){ return destinationView; }
    public String getDestinationContext(){ return destinationContext; }
    public String getDestinationViewAttribute(){ return destinationViewAttribute; }
    public String getMathOperator(){ return mathOperator; }
    public int getSourceViewId(){ return sourceViewId; }
    public int getDestinationViewId(){ return destinationViewId; }
    public int getContentType(){ return contentType; }
    public int getRowId(){ return rowId; }
    public boolean isUsedOrigMethod(){ return isUsedOrigMethod; }

}
