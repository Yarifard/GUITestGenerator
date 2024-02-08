package ir.ac.um.guitestgenerating.Database;

public class InvariantFirstPart {

        public String sourceView;
        public String sourceContext;
        public String sourceViewAttribute;
        public String relationOperator;
        public String mathOpertaor;
        public String content;
        public boolean flag;
        public int sourceViewId;
        public int rowId;

        public InvariantFirstPart(){
            sourceContext = "";
            sourceView = "";
            sourceViewAttribute = "";
            sourceViewId = -1;
            relationOperator = "";
            mathOpertaor = "";
            content = "";
            flag = false;
            rowId = -1;
        }
        public InvariantFirstPart(String context,String view,String attribute,String relationOperator,
                                  String mathOperrator,String content,int id,int viewId,boolean flag){
            sourceContext = context;
            sourceView = view;
            sourceViewAttribute = attribute;
            sourceViewId = viewId;
            rowId = id;
            this.content = content;
            this.flag = flag;
            this.mathOpertaor = mathOperrator;
            this.relationOperator = relationOperator;
        }

    public void setContent(String content) {
        this.content = content;
    }
    public void setSourceView(String sourceView){
            this.sourceView = sourceView;
    }
    public  void setSourceContext(String context){
            this.sourceContext = context;
    }
    public void setSourceViewAttribute(String attribute){
            this.sourceViewAttribute = attribute;
    }
    public void setRelationOperator(String relationOperator){
            this.relationOperator = relationOperator;
    }
    public void setMathOpertaor(String mathOpertaor){
            this.mathOpertaor = mathOpertaor;
    }
    public void setFlag(boolean flag){
            this.flag = flag;
    }
    public void setSourceViewId(int id){
            this.rowId = id;
    }
    public void setViewId(int viewId){
            this.sourceViewId = viewId;
    }
    public String getSourceView(){
            return sourceView;
    }
    public String getSourceContext(){
            return sourceContext;
    }
    public String getSourceViewAttribute(){
            return sourceViewAttribute;
    }
    public String getRelationOperator(){
            return relationOperator;
    }
    public String getMathOpertaor(){
            return mathOpertaor;
    }
    public String getContent(){
            return content;
    }
    public int getSourceViewId(){
            return sourceViewId;
    }
    public int getRowId(){
            return rowId;
    }
    public boolean getFlag(){
            return flag;
    }
}
