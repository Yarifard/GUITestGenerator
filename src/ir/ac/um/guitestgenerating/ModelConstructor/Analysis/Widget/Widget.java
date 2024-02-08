package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget;

import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.DescriptorType.OptionMenuDescription;
import static ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.DescriptorType.ViewId;

enum WidgetType {TextView, EditText, Button, ImageButton, FloatingActionButton, RadioButton,ToggleButton,
                 CheckBox,RadioGroup, ImageView,CircleImageView,CalendarView,WebView,VideoView,ProgressBar,SeekBar,
                 RatingBar,ListView, MenuItem,Switch,Spinner,None }

//enum direction { Horizontal, Vertical}
//
//enum  TextInputType { PlainText, Password, Numeric_Password, Time, Date, Email, Phone, Number, Signed_Number, Decimal_Number,
//   Postal_Address, MultiLineText, Checked_TextView }

public class Widget {
    private int orderInLayout;
    private String bindingVariableName;
    private String widgetType;
    private int id;
    private boolean optionMenuStyle;
    private String widgetAttr;
    private List<WidgetDescriptor> widgetDescriptorsList;

    public Widget(){
        this.orderInLayout = -1;
        widgetType = "";
        bindingVariableName = "";
        widgetAttr = "";
        optionMenuStyle = true;
        this.widgetDescriptorsList = new LinkedList<>();
    }

    public static boolean isWidget(String className){
        boolean result = false;
        WidgetType widgetType = getWidgetType(className);
        if(widgetType != WidgetType.None)
            result = true;
        return result;
    }

    public static WidgetType getWidgetType(String widgetName){
        WidgetType result = WidgetType.None;
        switch(widgetName){
            case "TextView"    : result = WidgetType.TextView;break;
            case "EditText"    : result = WidgetType.EditText;break;
            case "Button"      : result = WidgetType.Button;break;
            case "ImageButton" : result = WidgetType.ImageButton;break;
            case "CircleImageView" : result = WidgetType.CircleImageView;break;
            case "FloatingActionButton" : result = WidgetType.FloatingActionButton; break;
            case "RadioButton" : result = WidgetType.RadioButton; break;
            case "ToggleButton": result = WidgetType.ToggleButton; break;
            case "CheckBox": result = WidgetType.CheckBox;break;
            case "RadioGroup": result = WidgetType.RadioGroup; break;
            case "ImageView" : result = WidgetType.ImageView; break;
            case "CalendarView": result = WidgetType.CalendarView; break;
            case "WebView": result = WidgetType.WebView;break;
            case "VideoView":result = WidgetType.VideoView; break;
            case "ProgressBar": result = WidgetType.ProgressBar;break;
            case "SeekBar": result = WidgetType.SeekBar; break;
            case "RatingBar": result = WidgetType.RatingBar;break;
            case "ListView": result = WidgetType.ListView;break;
            case "MenuItem": result = WidgetType.MenuItem;break;
            case "Switch"  : result = WidgetType.Switch;break;
            case "Spinner" : result = WidgetType.Spinner;break;

        }
        return result;
    }

    public void setWidgetType(String widgetType){
        this.widgetType = widgetType;
    }
    public void setWidgetAttr(String widgetAttr){this.widgetAttr = widgetAttr;}
    public void setWidgetDatabaseId(int id){ this.id = id;}
    public void setOptionMenuStyle(boolean bool){this.optionMenuStyle = bool; }
    public void setBindingVariable(String variable){this.bindingVariableName = variable;}

    public void setWidgetDescriptorsList(List<WidgetDescriptor> descriptorsList){
        this.widgetDescriptorsList = descriptorsList;
    }
    public String getBindingVariableName(){return bindingVariableName;}
    public int getWidgetDatabaseId(){return id;}
    public boolean getOptionMenuStyle(){ return optionMenuStyle;}
    public String getWidgetAttr(){return widgetAttr;}

    public void setWidgetIdDescriptor(String value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(ViewId,value);
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public void setWidgetInputTypeDecsriptor(String value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(DescriptorType.ViewInputType,value);
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public void setWidgetLabelDescriptor(String value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(DescriptorType.ViewLabel,value);
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public void setWidgetTagValueDescriptor(String value){
         WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
         widgetDescriptor.SetDescriptor(DescriptorType.ViewTagValue,value);
         widgetDescriptorsList.add(widgetDescriptor);
     }

    public void setWidgetHintDescriptor(String value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(DescriptorType.ViewHint,value);
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public void setWidgetOptionMenuDescription(boolean value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(DescriptorType.OptionMenuDescription,String.valueOf(value));
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public void setWidgetContentDescription(String value){
        WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
        widgetDescriptor.SetDescriptor(DescriptorType.ViewContentDescription,value);
        widgetDescriptorsList.add(widgetDescriptor);
    }

    public String getWidgetIdDescriptorValue(){
        String viewId = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == ViewId)
                viewId = descriptor.getValue();
        return viewId;
    }

    public String getWidgetLabelDescriptorValue(){
        String viewLabel = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewLabel)
                viewLabel = descriptor.getValue();
        return viewLabel;
    }

    public String getWidgetTagDescriptorValue(){
        String viewTag = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewTagValue)
                viewTag = descriptor.getValue();
        return viewTag;
    }

    public String getWidgetHintDescriptorValue(){
        String viewHint = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewHint)
                viewHint = descriptor.getValue();
        return viewHint;
    }

    public String getWidgetInputTypeDescriptorValue(){
        String inputType = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewInputType)
                inputType = descriptor.getValue();
        return inputType;
    }

    public boolean getWidgetOptionMenuDescriptorValue(){
        boolean result = false;
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.OptionMenuDescription)
                result = Boolean.parseBoolean(descriptor.getValue());
        return result;
    }

    public String getWidgetContentDescriptorValue(){
        String viewContent = "";
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewContentDescription)
                viewContent = descriptor.getValue();
        return viewContent;
    }

    public List<WidgetDescriptor> getWidgetDescriptorsList(){
        return widgetDescriptorsList;
    }
    public String getWidgetType(){
        return widgetType;
    }

    public String generateLabelFor(EventHandlerInformation eventHandler){
        String label  = "";
        if(!widgetType.isEmpty()) {
            switch(widgetType){
                case "MainMenuItem":
                    if(isHomeMenuItem())
                        label += "\n\t\tonView(withContentDescription(\"Navigate up\")).perform(click());";
                    else if(eventHandler.isOptionMenuStyle()){
                        label += "\n\t\tonView(withContentDescription(\"More options\")).perform(click());";
                        label += "\n\t\tonView(allOf(withId(android.R.id.title),";
                        if(!getWidgetLabelDescriptorValue().isEmpty())
                             label += "withText(\"" + getWidgetLabelDescriptorValue() + "\")";
                        else if(!getWidgetContentDescriptorValue().isEmpty())
                            label += "withText(\"" + getWidgetContentDescriptorValue() + "\")";
                        label +=")).perform(" + getAction(eventHandler) + ");";
                    }
                    else
                        label += "\n\t\tonView(allOf(" + getLocator() + ")).perform(" + getAction(eventHandler) + ");";
                    break;
                case "ListView":
                    label += "\n\t\tonData(anything())";
                    label += ".inAdapterView(" + getLocator() + ").atPosition(0).perform(" + getAction(eventHandler) + ");";
                    break;
                case "Spinner":
                        label +="\n\t\tonView(" + getLocator() + ").perform(click());";
                        label += "\n\t\tonData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(0).perform("
                                + getAction(eventHandler) + ");";
                        break;
                default:
                    if(widgetDescriptorsList.size() > 1)
                        label += "\n\t\tonView(anyOf(" + getLocator() + ")).perform(" + getAction(eventHandler) + ");";
                    else
                        label += "\n\t\tonView(" + getLocator() + ").perform(" + getAction(eventHandler) + ");";
            }
        }
        return label;
    }

    private boolean isHomeMenuItem() {
        if(!widgetDescriptorsList.isEmpty()){
            for(WidgetDescriptor descriptor :widgetDescriptorsList)
                if(descriptor.getViewDescriptorType() == ViewId)
                    if(descriptor.getValue().contentEquals("android.R.id.home"))
                        return true;
        }
        return false;
    }

    private String getLocator(){
        String locator ="";
        boolean flag = true;
        if(!widgetDescriptorsList.isEmpty()){
            for(WidgetDescriptor descriptor :widgetDescriptorsList){
                switch (descriptor.getViewDescriptorType()){
                    case ViewId:
                        if(descriptor.getValue().startsWith("R.id") ||
                               descriptor.getValue().startsWith("android.R.id"))
                                  locator += "withId(" + descriptor.getValue() + "),";
                        break;
                    case ViewHint:
                        locator += "withHint(\"" + descriptor.getValue() + "\"),";
                        break;
                    case ViewTagValue:
                        locator += "withTagValue(\"" + descriptor.getValue() +"\"),";
                        break;
                    case ViewLabel:
                        locator += "withText(\"" + descriptor.getValue() + "\"),";
                        break;
                    case ViewContentDescription:
                        locator += "withContentDescription(\"" + descriptor.getValue() + "\"),";
                        break;
                }
            }
            locator = locator.substring(0,locator.lastIndexOf(','));
        }
        return locator;
    }

    private String getAction(EventHandlerInformation event){
        String action = "";
        Random random = new Random();
        String[] replacePattern = {"modify", "edit", "replace", "alter", "change"};
        String[] errorPattern = {"Error"};
        switch(widgetType){
            case "EditText":
                String data = getIntialValue();
                if(Utils.isPartialMatchWithPattern(replacePattern,event.getTitle()))
                    action += "replaceText(\"" + data +"\" ),closeSoftKeyboard()";
                else{
                    if(Utils.isPartialMatchWithPattern(errorPattern,event.getTitle()))
                        action += "typeText(\"\"),closeSoftKeyboard()";
                    else
                        action += "typeText(\"" + data + "\"),closeSoftKeyboard()";

                }
                break;
            case "Spinner":
            case "MainMenuItem":
                           action +="click()";
                           break;
            case "TextView":
            case "ListView":
            case "CheckBox":
            case "Button":
            case "RadioButton":
            case "FloatingActionButton":
            case "ImageButton":
            case "AlertDialog.Builder":
            case "RatingBar":
            case "Switch":
            case "CircleImageView":
                if(event.getName().contains("Long"))
                    action += "longClick()";
                else if(event.getName().contains("double"))
                    action += "doubleClick()";
                else
                    action +="click()";
                break;
        }
        return action;
    }

    private String getIntialValue() {
        String value = "intial data";
        if(hasInputTypeDescriptor()){
            switch (getWidgetInputTypeDescriptorValue()){
                case "number"           :  value = "6546784";break;
                case "numberPassword"   :  value = "12345678";break;
                case "textEmailAddress" : value = "test@mail.com";break;
                case "textPersonName"   : value = "javad";break;
                case "textPassword"     : value = "SamplePassword";break;
                case "phone"            : value = "9153518713";break;
                case "textPostalAddress": value = "No.430, Passdaran St., Tehran, Iran";break;
                case "textMultiLine"    : value = "Test line 1\nTest line2";break;
                case "time"             :break;
                case "date"             :break;
                case "numberSigned":   value = "-12";break;
                case "numberDecimal":  value = "43";break;

            }
        }
        return value;
    }

    private boolean hasInputTypeDescriptor() {
        boolean result = false;
        for(WidgetDescriptor descriptor:widgetDescriptorsList)
            if(descriptor.getViewDescriptorType() == DescriptorType.ViewInputType)
                result = true;
        return result;
    }
}
