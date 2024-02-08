package ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel;

public class ViewInformation {
    private String title;
    private String viewId;
    private String viewTag;
    private String viewType;
    private String bindingName;
    private boolean optionMenu;
    private String contentDesciption;
    public ViewInformation(){
        this.title = "";
        this.viewId = "";
        this.viewType = "";
        this.viewTag = "";
        bindingName = "";
        optionMenu = false;
        contentDesciption = "";
    };

    public String getTitle() {
        return title;
    }

    public String getViewId() {
        return viewId;
    }

    public String getViewType() {
        return viewType;
    }

    public String getTag(){ return viewTag;}

    public boolean isOptionMenuStyle(){ return  optionMenu; }

    public String getContentDesciption(){ return  contentDesciption;}

    public String getBindingName(){
        return this.bindingName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public void setViewTag(String viewTag){ this.viewTag = viewTag;}

    public void setBindingName(String bindingName){
        this.bindingName = bindingName;
    }
    public void setOptionMenuStyle(boolean boolValue) { this.optionMenu = boolValue;}

    public void setContentDesciption(String contentDesciption) { this.contentDesciption = contentDesciption;}
}
