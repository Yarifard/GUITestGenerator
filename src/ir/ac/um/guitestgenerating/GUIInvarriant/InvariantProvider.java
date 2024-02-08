package ir.ac.um.guitestgenerating.GUIInvarriant;

import ir.ac.um.guitestgenerating.Database.Invariant;
import ir.ac.um.guitestgenerating.Database.PointType;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Code.ASTUtils;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.WidgetDescriptor;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.Util.RelationOperator;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InvariantProvider {
    private static final int PostCondition = 1;
    private static final int PreCondittion = 0;
    private InvariantRepository invariantRepository;
    private ProjectInformation projectInformation;
    public InvariantProvider(ProjectInformation projectInformation){
       // this.invariantRepository = invariantRepository;
        this.invariantRepository = projectInformation.getGuiInvariantRepository();
        this.projectInformation = projectInformation;

    }

    public String getPreConditionAssertionFor(ActivityInformation currentActivity,
                                              EventHandlerInformation eventHandler){
        String invariant = "";
        String mainContext = "";
        String parentContext = "";
        String methodName = "";
        List<Invariant>  invariants = new ArrayList<>();
        int selectedInvarId;
        if(!isChoicedInvariant(eventHandler.getPreConditionInvarId()))
            do{
                selectedInvarId = 0;
                mainContext = "";
                parentContext = "";
                methodName = "";
                if(ASTUtils.isDialogMethod(eventHandler.getAttachedMethod())){
                    mainContext = currentActivity.getEventContext(eventHandler);
                    parentContext = currentActivity.getEventParenContext(eventHandler.getAttachedMethod());
                    PointType entryPointType = new PointType(mainContext,parentContext,"","OBJECT","");
                    invariants = invariantRepository.getInvariants(entryPointType);
                }
                else{
                     mainContext = currentActivity.getEventContext(eventHandler);
                     PointType entryPointType = new PointType(mainContext,"","","OBJECT","");
                     invariants = invariantRepository.getInvariants(entryPointType);
                }
                mainContext = currentActivity.getEventContext(eventHandler);
                parentContext = currentActivity.getEventParenContext(eventHandler.getAttachedMethod());
                methodName = currentActivity.getEventLabelFor(eventHandler);
                PointType entryPointType = new PointType(mainContext,parentContext,methodName,"ENTER","");
                invariants.addAll(invariantRepository.getInvariants(entryPointType));
                if(invariants.isEmpty()){
                    eventHandler.setPreConditionInvarId(0);
                    break;
                }
                Collections.sort(invariants,(d1,d2)->{
                    int item1 = (int)(d1.getPriority()*10);
                    int item2 = (int)(d2.getPriority()*10);
                    return item2 - item1;
                });

                selectedInvarId = getSelectedAssertion(currentActivity.getActivityName(),eventHandler.getTitle(),invariants,PreCondittion);
                if(selectedInvarId >= 0 && selectedInvarId <= invariants.size()){
                    if(selectedInvarId == 0)
                        eventHandler.setPreConditionInvarId(selectedInvarId);
                    else
                        eventHandler.setPreConditionInvarId(invariants.get(selectedInvarId-1).getId());
                    break;
                }
            }while(false);
        return generatePreConditionAssertionFor(eventHandler);
    }

    public String getPostConditionAssertionFor(ActivityInformation currentActivity,
                                                    EventHandlerInformation event,
                                                    String result){
        PointType pointType;
        List<Invariant>  invariants =  new ArrayList<>();
        int selectedInvarId;
        if(!isChoicedInvariant(event.getPostConditionInvarId()))
            do{
                selectedInvarId = 0;
                String mainContext = "";
                String parentContext = "";
                String methodName = "";
                if(showDialogBy(event)){
                     parentContext = currentActivity.getEventContext(event);
                     mainContext =   currentActivity.getEventContext(event.getChildEventHandlers().get(0));
                     pointType = new PointType(mainContext,parentContext,"","OBJECT","");
                     invariants = invariantRepository.getInvariants(pointType);
                }
                else{
                     mainContext = event.getTargetActivity();
                     pointType = new PointType(mainContext,"","","OBJECT","");
                     invariants = invariantRepository.getInvariants(pointType);

                     mainContext = currentActivity.getEventContext(event);
                     parentContext = currentActivity.getEventParenContext(event.getAttachedMethod());
                     //mainContext = event.getMainContext();
                     methodName = currentActivity.getEventLabelFor(event);
                     pointType = new PointType(mainContext,parentContext,methodName,"EXIT","");
                     invariants.addAll(invariantRepository.getInvariants(pointType));

                     Collections.sort(invariants,(d1,d2)->{
                        int item1 = (int)(d1.getPriority()*10);
                        int item2 = (int)(d2.getPriority()*10);
                        return item2 - item1;
                    });

               }
                if(invariants.isEmpty()){
                    event.setPostConditionInvarId(0);
                    break;
                }
                selectedInvarId = getSelectedAssertion(currentActivity.getActivityName(),event.getTitle(),invariants,PostCondition);
                if(selectedInvarId >= 0 && selectedInvarId <= invariants.size()){
                   if(selectedInvarId == 0){
                        event.setPostConditionInvarId(selectedInvarId);
                       // invariant = "";
                   }
                   else {
                          event.setPostConditionInvarId(invariants.get(selectedInvarId-1).getId());
                     //     invariant = invariants.get(selectedInvarId-1).getInvariant();
                   }
                   break;
                }
            }while(false);
       //    event.setPostConditionInvarId(0);

        return generatePostConditionAssertionFor(event,result);
    }

    private boolean showDialogBy(EventHandlerInformation event) {
        if(event.getTitle().startsWith("Show") && event.getTitle().endsWith("Dialog") &&
        !event.getTitle().contains("$"))
            return true;
        return false;
    }

    private String findAndInsertLocationFor(EventHandlerInformation eventHandler, String secondPartOfAssertion,
                                            String result,InvariantInformation invariant) {
        if(eventHandler.getMainContext().contentEquals(invariant.getDestinationContext()))
            result = replaceLast(result,"//tag\n","//tag\n\t\t " + secondPartOfAssertion);
        else{
             if(eventHandler.hasParent()){
                 result = replaceLast(result,"//tag","//tage");
                 result = findAndInsertLocationFor(eventHandler.getParentEventHandlerInformation(),secondPartOfAssertion,result,invariant);
                 result = replaceLast(result,"//tage","//tag");
             }
        }
        return result;
    }

    private String replaceLast(String result, String target, String replaceWith) {

    int lastIndex = result.lastIndexOf(target);
        if (lastIndex < 0) {
            return result;
        }
        return result.substring(0, lastIndex) + replaceWith + result.substring(lastIndex + target.length());
    }

    private List<Invariant> sortInvariants(List<Invariant> invariants) {
        List<Invariant> sortedInvariants = new ArrayList<>();
        int index;
        while(!invariants.isEmpty()){
            index = 0;
            Invariant invariant = invariants.get(0);
            for(int i = 1 ; i < invariants.size(); i++){
                if(invariants.get(i).getPriority() > invariant.getPriority()){
                    invariant = invariants.get(i);
                    index = i;
                }

            }
            sortedInvariants.add(invariant);
            invariants.remove(index);
        }
        return sortedInvariants;
    }

    private String generatePostConditionAssertionFor(EventHandlerInformation eventHanddler,String result) {
        if(eventHanddler.getPostConditionInvarId() == 0)
            return result;
        String secondPartOfAssertion = "", firstPartOfAssetion = "";
        String variableName = "";
        List<InvariantInformation> invariantList = invariantRepository.getInvariants(eventHanddler.getPostConditionInvarId());
        InvariantInformation invariant = invariantList.get(0);
        if(hasSecondPart(invariant)){
            secondPartOfAssertion =
                    generateSubOfAssertion(invariant);
            variableName = extractVariableName(secondPartOfAssertion);
            if(isSameWidgetAndSameAttribute(invariant)){
                secondPartOfAssertion  = "\n\t\t //Begin of first part of postcondition assertion \n" + secondPartOfAssertion;
                secondPartOfAssertion  = secondPartOfAssertion + "\n\t\t //End of fist part of postcondition assertion \n\t\t";
                result = findAndInsertLocationFor(eventHanddler,secondPartOfAssertion,result,invariant);
            }
        }
        firstPartOfAssetion += "onView(" + generateLocatorForAssertion(invariant.getSourceViewId()) +
                ").check(matches(" + generateViewAssertionMatcherFor(invariantList, variableName) + "));";
//            if(result.contains("//tag"))
//                result = result.replaceFirst("//tag\n","//tag\n\t\t" + secondPartOfAssertion);
        if(hasSecondPart(invariant) && !isSameWidgetAndSameAttribute(invariant))
             firstPartOfAssetion = secondPartOfAssertion + "\n" + firstPartOfAssetion;
//        Widget sourceWidget = new Widget();
//        // Widget destinationWidget = new Widget();
//        String firstPartOfAssetion = "";
//        firstPartOfAssetion += "\t\tonView(" + generateLocatorForAssertion(invariant.getSourceView(),invariant.getSourceViewId()) +
//                ").check(matches(" + generateViewAssertionMatcherFor(invariantList, variableName) + "));";
        if(result.contains("//Nothings"))
            result = result.replace("//Nothings",firstPartOfAssetion);
        return result;
    }

    private boolean isSameWidgetAndSameAttribute(InvariantInformation invariant) {
        boolean result = false;
        if(invariant.getSourceView().contentEquals(invariant.getDestinationView()) &&
           invariant.getSourceViewAttribute().contentEquals(invariant.getDestinationViewAttribute()) &&
           invariant.getSourceViewId() == invariant.getDestinationViewId())
             result = true;
        return result;
    }

    private String generatePreConditionAssertionFor(EventHandlerInformation eventHandler) {
        int invarId = eventHandler.getPreConditionInvarId();
        if(invarId == 0)
          return "";
        String varibaleName = "";
        String subPartOfAssertion = "";
        List<InvariantInformation> invariantList = invariantRepository.getInvariants(invarId);
        InvariantInformation invariant = invariantList.get(0);
        if(hasSecondPart(invariant)){
            subPartOfAssertion=
                    generateSubOfAssertion(invariant);
            varibaleName = extractVariableName(subPartOfAssertion);
        }

        String mainPartOfAssetion = "";
        mainPartOfAssetion += generateMainPartOfAssertion(invariantList,invariant.getSourceViewId(),
                                                          varibaleName);
//                "\t\tonView(" + generateLocatorForAssertion(invariant.getSourceViewId()) +
  //                              ").check(matches(" + generateViewAssertionMatcherFor(invariantList,varibaleName) + "));";
        String generatedAssertion = subPartOfAssertion + "\n" + mainPartOfAssetion;
        return generatedAssertion;
    }

    @NotNull
    private String generateSubOfAssertion(InvariantInformation invariant) {
        String assertion     = "";
        String viewType      = invariant.getDestinationView();
        int    viewId        = invariant.getDestinationViewId();
        String viewAttribute = invariant.getDestinationViewAttribute();
        Widget desWidget     = invariantRepository.getWidgetInfoById(invariant.getDestinationViewId());
        Widget srcWidget     = invariantRepository.getWidgetInfoById(invariant.getSourceViewId());
        int index            = projectInformation.getCounter();
        if(Pattern.isMatchWithPattern(viewType,"MainMenuItem"))
            if(desWidget.getWidgetOptionMenuDescriptorValue())
                assertion += "\n\t\tonView(withContentDescription(\"More options\")).perform(click());";
        switch(viewAttribute){
            case "visibility"   : assertion += "\n\t\tboolean boolValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tboolValue_" + index + " = " +
                        "VisibilityHelper.getVisibilityStatus(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t boolValue_" + index +" = false;" +
                        "\n\t\t}";
                break;

            case "enable"   : assertion += "\n\t\tboolean boolValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tboolValue_" + index + " = " +
                        "EnableHelper.getEnableStatus(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t boolValue_" + index +" = false;" +
                        "\n\t\t}";
                break;
            case "editable" : assertion += "\n\t\tboolean boolValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tboolValue_" + index + " = " +
                        " EditableHelper.getEditableStatus(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t boolValue_" + index +" = false;" +
                        "\n\t\t}";
                break;

            case "checked"  : assertion += "\n\t\tboolean boolValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tboolValue_" + index + " = " +
                        " CheckedHelper.getCheckedStatus(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t boolValue_" + index +" = false;" +
                        "\n\t\t}";
                break;

            case "text"     : assertion += "\n\t\tString strValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tstrValue_" + index + " = " +
                        " TextHelper.getText(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t strValue_" + index +" = \"\";" +
                        "\n\t\t}";
                break;
            case "length":
            case "size"  : assertion += "\n\t\tint intValue_" + index + ";";
                assertion +=  "\n\t\ttry{" +
                        "\n\t\t\tintValue_" + index + " = " +
                        " NumberHelper.getNumber(Espresso.onView(" +
                        generateLocatorForAssertion(viewId) + "));" +
                        "\n\t\t}catch(NoMatchingViewException e){" +
                        "\n\t\t\t intValue_" + index +" = 0;" +
                        "\n\t\t}";
                break;
        }
        if(Pattern.isMatchWithPattern(viewType,"MainMenuItem") && desWidget.getWidgetOptionMenuDescriptorValue())
            if(!srcWidget.getWidgetOptionMenuDescriptorValue())
                assertion +="\n\t\t pressBack();";
        return assertion;
    }

    private String generateMainPartOfAssertion(List<InvariantInformation> invariants,
                                               int viewId,String variableName) {
        String result = "";
        InvariantInformation invariant = invariants.get(0);
        Widget srcWidget = invariantRepository.getWidgetInfoById(viewId);
        Widget desWidget    = invariantRepository.getWidgetInfoById(invariant.getDestinationViewId());
        if(srcWidget.getWidgetOptionMenuDescriptorValue())
            if (!desWidget.getWidgetOptionMenuDescriptorValue())
                result += "\n\t\tonView(withContentDescription(\"More options\")).perform(click());";
        result += "\n\t\tonView(" + generateLocatorForAssertion(invariant.getSourceViewId()) +
                                      ").check(matches(" + generateViewAssertionMatcherFor(invariants,variableName) + "));";
        if(srcWidget.getWidgetOptionMenuDescriptorValue())
            result +="\n\t\tEspresso.pressBack();";
        return result;
    }

    @NotNull
    private String generateLocatorForAssertion(int viewId) {
        String locator = "";
      //  eventHandler = projectInformation.
        if (viewId == 17)
                System.out.println("OK");
        Widget widget = invariantRepository.getWidgetInfoById(viewId);
        String viewType = widget.getWidgetType();
        String bindingVariable = widget.getBindingVariableName();
        for(WidgetDescriptor descriptor : widget.getWidgetDescriptorsList()){
            switch (descriptor.getViewDescriptorType()){
                case ViewId:
                    if(Pattern.isMatchWithPattern(viewType,"MainMenuItem")){
                        if(widget.getWidgetOptionMenuDescriptorValue())
                            locator += "withId(android.R.id.title),";
                        else if(isHomeMenuItem(descriptor.getValue()))
                            locator += "withContentDescription(\"Navigate up\"),";
                        else if(descriptor.getValue().startsWith("R.id"))
                             locator += "withId(" + descriptor.getValue() + "),";
                    }
                    else if(!Pattern.isMatchWithPattern(widget.getBindingVariableName(),"dialogTitle"))
                         locator += "withId(" + descriptor.getValue() + "),";
                    break;
                case ViewHint:
                    locator += "withHint(\"" + descriptor.getValue() + "\"),";
                    break;
                case ViewLabel:
                    if(Pattern.isMatchWithPattern(viewType,"MainMenuItem")){
                        if(widget.getWidgetOptionMenuDescriptorValue())
                            locator += "withText(\"" + descriptor.getValue() +"\"),";
                        else
                            locator += "withContentDescription(\"" + descriptor.getValue() +"\"),";
                    }
                    else
                        if(!viewType.contentEquals("TextView") || bindingVariable.contentEquals("dialogTitle"))
                            locator += "withText(\"" + descriptor.getValue() +"\"),";
                    break;
                case ViewContentDescription:
                    locator += "withContentDescription(\"" + descriptor.getValue() +"\"),";
                    break;
                case ViewTagValue:
                    locator += "withTagValue(" + descriptor.getValue() + "),";
                    break;
            }
        }
        locator = locator.substring(0, locator.length() -1);
        if((locator.split("with")).length -1 > 1){
            locator = "allOf(" + locator + ")";
        }
        return locator;
    }

    private String generateViewAssertionMatcherFor(List<InvariantInformation> invariants,
                                                   String varibaleName) {
        return getCostumeViewAssertionMatcherFor(invariants, varibaleName);
    }

    private String getCostumeViewAssertionMatcherFor(List<InvariantInformation> invariants,
                                                     String variableName) {
        String viewMatcher = "";
        InvariantInformation invariant = invariants.get(0);
        Widget widget = invariantRepository.getWidgetInfoById(invariant.getSourceViewId());
        String viewAttribute = invariant.getSourceViewAttribute();
        String relationOpt = invariant.getRelationOperator();
        switch (viewAttribute){
            case "text":
                if(RelationOperator.isEqualTo(relationOpt)){
                    if(widget.getBindingVariableName().contentEquals("dialogTitle"))
                        viewMatcher ="isDisplayed()";
                    else
                        viewMatcher = "withTextEqualTo(value)";
                }
                else if(RelationOperator.isNotEqualTo(relationOpt))
                    viewMatcher = "withTextNotEqualTo(value)";
                else if(RelationOperator.isGreaterThan(relationOpt))
                    viewMatcher = "withTextGreaterThan(value)";
                else if(RelationOperator.isGreaterThanOrEqualTo(relationOpt))
                    viewMatcher = "withTextGreaterThanOrEqualTo(value)";
                else if(RelationOperator.isLessThan(relationOpt))
                    viewMatcher = "withTextLessThan(value)";
                else
                    viewMatcher = "withTextLessThanOrEqualTo(value)";
                break;
            case "length":
                if(RelationOperator.isEqualTo(relationOpt))
                    viewMatcher = "withTextLengthEqualTo(value)";
                else if(RelationOperator.isNotEqualTo(relationOpt))
                    viewMatcher = "withTextLengthNotEqualTo(value)";
                else if(RelationOperator.isGreaterThan(relationOpt))
                    viewMatcher = "withTextLengthGreaterThan(value)";
                else if(RelationOperator.isGreaterThanOrEqualTo(relationOpt))
                    viewMatcher = "withTextLengthGreaterThanOrEqualTo(value)";
                else if(RelationOperator.isLessThan(relationOpt))
                    viewMatcher = "withTextLengthLessThan(value)";
                else
                    viewMatcher = "withTextLengthLessThanOrEqualTo(value)";
                break;
            case "size" :
                if(RelationOperator.isEqualTo(relationOpt))
                    viewMatcher = "withSizeEqualTo(value)";
                else if(RelationOperator.isNotEqualTo(relationOpt))
                    viewMatcher = "withSizeNotEqualTo(value)";
                else if(RelationOperator.isGreaterThan(relationOpt))
                    viewMatcher = "withSizeGreaterThan(value)";
                else if(RelationOperator.isGreaterThanOrEqualTo(relationOpt))
                    viewMatcher = "withSizeGreaterThanOrEqualTo(value)";
                else if(RelationOperator.isLessThan(relationOpt))
                    viewMatcher = "withSizeLessThan(value)";
                else
                    viewMatcher = "withSizeLessThanOrEqualTo(value)";
                break;
            case "enable":
                viewMatcher = getViewEnableMatcher(invariant.getSourceView(),relationOpt);
                break;
            case "editable":
                if(RelationOperator.isEqualTo(relationOpt))
                    viewMatcher = "withEditable(value)";
                else
                    viewMatcher = "withEditable(not(value))";
                break;
            case "visibility":viewMatcher = getViewVisibilityMatcher(invariant.getSourceView(),relationOpt);
                break;
            case "checked" : if(RelationOperator.isEqualTo(relationOpt))
                viewMatcher = "withChecked(value)";
            else
                viewMatcher = "withChecked(not(value))";
                break;
        }
        if(hasMoreInvariants(invariants))
            return updateViewMathcerWithMultipeValues(invariants,viewMatcher);
        else
            return updateViewMatcherWithSingleValue(invariant,viewMatcher,variableName);
      //  return  viewMatcher;
    }

    private boolean hasMoreInvariants(List<InvariantInformation> invariants) {
        if(invariants.size() > 1)
            return true;
        return false;
    }

    private String updateViewMatcherWithSingleValue(InvariantInformation invariant, String viewMatcher, String variableName) {
        String value = "";
        switch(invariant.getContentType()){
            case 0: if(!invariant.getMathOperator().isEmpty()){
                switch(invariant.getMathOperator()){
                    case "-" : if(!invariant.getContent().isEmpty()){
                        if(invariant.getContent().contains("-"))
                            value = variableName + invariant.getContent();
                        else
                            value = variableName + "+" + invariant.getContent();
                    }
                    else
                        value = variableName;
                        break;
                    case "+" :  if(!invariant.getContent().isEmpty()){
                        if(invariant.getContent().contains("-"))
                            value = "-" + variableName + invariant.getContent();
                        else
                            value = "-" + variableName + "+" + invariant.getContent();
                    }
                    else
                        value = "-" + variableName;
                        break;

                }
            }
            else
                value = variableName;
                break;
            case 1: break;
            case 2: value = invariant.getContent().trim();break;
            case 3: value = invariant.getContent().trim();break;
            case 4: value = "\"" + invariant.getContent().trim() +"\"";break;
        }

        return viewMatcher.replace("value",value);

    }

    private String updateViewMathcerWithMultipeValues(List<InvariantInformation> invariants, String viewMatcher) {
        String values = "";
        for(InvariantInformation invariant: invariants)
            switch(invariant.getContentType()){
                case 2: values += " is(" + Integer.parseInt(invariant.getContent().trim()) + "),"; break;
                case 3: values += " is(" + Boolean.parseBoolean(invariant.getContent().trim()) + "),"; break;
                case 4: values += " is(\"" + invariant.getContent().trim() + "\"),"; break;
            }
        values = values.substring(0,values.lastIndexOf(','));
        values = "anyOf(" + values + ")";
        if(viewMatcher.contains("not(is(value))"))
            viewMatcher = viewMatcher.replace("is(value)",values);
        else
            viewMatcher = viewMatcher.replace("value",values);
        return viewMatcher;
    }


    private String extractVariableName(String content) {
        String items[] = content.split(";");
        if(content.contains("perform(click())")){
           String parts[] = items[1].split(" ");
            return parts[1];
        }
        else{
           String parts[] = items[0].split(" ");
            return parts[1];
        }
    }



    private String getViewVisibilityMatcher(String sourceView, String relationOpt) {
        String viewMatcher = "";
        switch (sourceView){
            case "TextView"             :
            case "EditText"             :
            case "Button"               :
            case "CheckBox"             :
            case "RadioButton"          :
            case "MainMenuItem"         :
            case "Switch"               :
            case "ToggleButton"         : viewMatcher = "withTextViewVisibility";
                                          break;
            case "ImageView"            :
            case "ImageButton"          :
            case "CircleImageView"      :
            case "FloatingActionButton" : viewMatcher = "withImageViewVisibility";
                                         break;
            case "ProgressBar"          :
            case "SeekBar"              :
            case "RatingBar"            : viewMatcher = "withProgressBarVisibility";
                                          break;
            case "ListView"             :
            case "CalendarView"         :
            case "WebView"              :
            case "DatePicker"           :
            case "TimePiacher"          :
            case "RadioGroup"           :
            case "NumberPiacker"        :
            case "SearchView"           :
            case "ScrollView"           :
            case "HorizentalScrollView" : viewMatcher = "withViewGroupVisibility";
                                          break;
        }
        if(RelationOperator.isEqualTo(relationOpt))
            viewMatcher += "(value)";
        else
            viewMatcher += "(not(value))";
        return viewMatcher;
    }

    private String getViewEnableMatcher(String sourceView, String relationOpt) {
        String viewMatcher = "";
        switch (sourceView){
            case "TextView"             :
            case "EditText"             :
            case "Button"               :
            case "CheckBox"             :
            case "RadioButton"          :
            case "MainMenuItem"         :
            case "Switch"               :
            case "ToggleButton"         : viewMatcher = "withTextViewEnable";
                                          break;
            case "ImageView"            :
            case "ImageButton"          :
            case "CircleImageView"      :
            case "FloatingActionButton" : viewMatcher = "withImageViewEnable";
                                          break;
            case "ProgressBar"          :
            case "SeekBar"              :
            case "RatingBar"            : viewMatcher = "withProgressBarEnable";
                                          break;
            case "ListView"             :
            case "CalendarView"         :
            case "WebView"              :
            case "DatePicker"           :
            case "TimePiacher"          :
            case "RadioGroup"           :
            case "NumberPiacker"        :
            case "SearchView"           :
            case "ScrollView"           :
            case "HorizentalScrollView" : viewMatcher = "withViewGroupEnable";
                                          break;
        }
        if(RelationOperator.isEqualTo(relationOpt))
            viewMatcher += "(value)";
        else
            viewMatcher += "(not(value))";
        return viewMatcher;
    }



    private String getSecondSectionValue(InvariantInformation invariant) {
        String variableName = "";
        switch (invariant.getSourceViewAttribute()){
            case "editable" :
            case "enable"   :
            case "visibiliy":
            case "checked"  : variableName = "boolValue";break;
            case "text"     : variableName = "strValue";break;
            case "length"   :
            case "size"     : variableName = "intValue";break;
        }
        return variableName;
    }


    private boolean isFirstIterate(int iterate) {
        if(iterate == 1)
            return true;
        return false;
    }

    private boolean hasSecondPart(InvariantInformation invariant) {
        if(invariant.getContentType() == 0)
            return true;
        return false;
    }

    private boolean isHomeMenuItem(String viewId ) {
        if (viewId.isEmpty())
             return false;
        if(viewId.contentEquals("android.R.id.home"))
            return true;
        return false;
    }

    private boolean hasMoreThanDescriptor(Widget widget) {
        if(widget.getWidgetDescriptorsList().size() > 1)
            return true;
        return false;
    }

    private boolean isPostCondition(int type) {
        if(type == PostCondition)
            return true;
        return false;
    }

    private boolean isChoicedInvariant(int postConditionInvarId) {
        if(postConditionInvarId == -1)
            return false;
        return true;
    }

    private int getSelectedAssertion(String activityName,String eventName,List<Invariant> invariants, int type){
          Window window = new Window();
          String patterns = initializePattern(eventName,invariants,type);
          return window.createLayout(activityName,eventName,invariants, patterns, type);
    }

    private String initializePattern(String eventName,List<Invariant> invariants, int type) {
        String pattern ="";
        if(isPostCondition(type))
            pattern += "Please Choice the appropriate postcondition invariant for\n";
        else
            pattern += "Please Choice the appropriate precondition invariant for\n";
        return pattern;
    }

    private List<Integer> extractInvarsIdFrom(List<String> invariants) {
        List<Integer> invarsIds = new ArrayList<>();
        for(int index = 0 ; index < invariants.size(); index++) {
            String invariant = invariants.get(index);
            String id = invariant.substring(invariant.lastIndexOf('.')+1);
            invarsIds.add(Integer.valueOf(id));
            invariant = invariant.substring(0,invariant.lastIndexOf('.'));
            invariants.remove(index);
            invariants.add(index,invariant);
        }
        return invarsIds;
    }

}
