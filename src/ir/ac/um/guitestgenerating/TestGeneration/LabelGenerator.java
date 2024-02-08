package ir.ac.um.guitestgenerating.TestGeneration;

import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantProvider;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.WidgetDescriptor;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.ValidTestData.TestDataProvider;

import java.util.Random;

public class LabelGenerator {
    private InvariantProvider invariantProvider;
    private TestDataProvider testDataProvider;

    public LabelGenerator(InvariantProvider invariantProvider, TestDataProvider dataProvider){
        this.invariantProvider = invariantProvider;
        this.testDataProvider = dataProvider;
    }

    public String generateLabelFor(EventHandlerInformation eventHandler, Widget widget){
        String label  = "";
        if(!widget.getWidgetType().isEmpty()) {
            switch(widget.getWidgetType()){
                case "ListView":
                    label += "\t\tonData(anything())";
                    label += ".inAdapterView(" + getLocator(widget) + ").atPosition(0).";
                    break;
                default:
                    if(widget.getWidgetDescriptorsList().size() > 1)
                        label += "\t\tonView(anyOf(" + getLocator(widget) + ")).";
                    else
                        label += "\t\tonView(" + getLocator(widget) + ").";

            }

            label += "perform(" + getAction(eventHandler,widget) + ");\n";
        }

        return label;
    }

    private String getLocator(Widget widget){
        String locator ="";
        boolean flag = true;
        if(!widget.getWidgetDescriptorsList().isEmpty()){
            for(WidgetDescriptor descriptor : widget.getWidgetDescriptorsList()){
                switch (descriptor.getViewDescriptorType()){
                    case ViewId:
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

    private String getAction(EventHandlerInformation event,Widget widget){
        String action = "";
        Random random = new Random();
        String[] patterns = {"modify","edit","replace", "alter", "change"};
        switch(widget.getWidgetType()){
            case "EditText":
                if(Pattern.isMatch(event.getTitle(),patterns))
                    action += "replaceText(\"Data\" ),closeSoftKeyboard()";
                else
                    action += "typeText(\"Data\"),closeSoftKeyboard()";

                break;
            case "ListView":
            case "CheckBox":
            case "Button":
            case "RadioButton":
            case "FloatingActionButton":
            case "AlertDialog.Builder":
            case "RatingBar":
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
}
