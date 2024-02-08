package ir.ac.um.guitestgenerating.GUIInvarriant;


import ir.ac.um.guitestgenerating.Database.Invariant;
import ir.ac.um.guitestgenerating.Database.PointType;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Patterns.Pattern;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;
import ir.ac.um.guitestgenerating.Util.Utils;
import ir.ac.um.guitestgenerating.Database.DatabaseAdapter;
import ir.ac.um.guitestgenerating.Project.ProjectInformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public  class InvariantRepository {
    private String packageName;
    private DatabaseAdapter adapter;
    private ProjectInformation projectInformation;

    public InvariantRepository(ProjectInformation projectInformation) {

        this.projectInformation = projectInformation;
        adapter = projectInformation.getDbAdapter();
    }

    public boolean collectGUIInvariants(String folderName){
        String mainContext = "";
        String parentContext = "";
        String methodName = "";
        String pointType = "";
        packageName = folderName;
        String filePath = "E:\\daikon-5.8.10\\examples\\java-examples\\" + packageName;
        File file = new File(filePath,"GUIInvariant.txt");
        int i =0;
        try{
            if(!file.exists()) return false;
            Scanner sc = new Scanner(file);
            sc.nextLine();
            String line = sc.nextLine();
           // packageName = line.substring(0,line.indexOf("."));
           adapter = projectInformation.getDbAdapter();//new DatabaseAdapter(packageName);
            if(!adapter.prepareDatabase()){
                Utils.showMessage("GUI invariants database for this project is not prepared successfully!!!!");
                return false;
            }
            int pointTypeId = -1;
            do{
                if(!line.contains("=====")){
                    line = (String) removePackageName(line);
                    if(line.contains(":::")){
                        mainContext = getMainContext(line);
                        parentContext = getParentContext(line);
                        methodName = getMethodName(line);
                        pointType = getPointType(line);
                        pointTypeId = adapter.InsertPointType(mainContext, parentContext,methodName,pointType,line);
                        if(pointTypeId == -1)
                            break;
                    }
                    else
                        processInvariant(line,pointTypeId,mainContext,parentContext,methodName);
                    i++;

                }
                if(!sc.hasNext())
                    break;
                line = sc.nextLine();
            }while(true);

            StorInvariantsIntoFile();

        } catch (IOException ioe){
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    private String getPointType(String content) {
        content = content.substring(content.indexOf(".") +1);
        String pointType = content.substring(content.indexOf(":::") + 3);
        return pointType;
    }

    private void StorInvariantsIntoFile() throws IOException {
        String filePath = "E:\\daikon-5.8.10\\examples\\java-examples\\" + packageName;
        File file = new File(filePath,"PriorityGUIInvariants.txt");
        if(!file.exists())
            file.createNewFile();
        FileWriter fileWriter = new FileWriter(file,true);
        List<String> captions = adapter.getPointTypeCaptions();
        for(String caption : captions){
            int pointTypeId = getPointTypeId(caption);
            String pointTypeCaption = getPointTypeCaption(caption);
            fileWriter.write("=======================================================================\n");
            fileWriter.write(pointTypeCaption + "\n");
            List<Invariant> invariants = adapter.getInvariants(pointTypeId);
            for(Invariant invarinat: invariants)
                fileWriter.write(invarinat.getInvariant() + "\n");
        }
        fileWriter.flush();
        fileWriter.close();
    }

    private String getPointTypeCaption(String caption) {
        return caption.substring(0,caption.lastIndexOf('.'));
    }

    private int getPointTypeId(String caption) {
        int pointTypeId = Integer.valueOf(caption.substring(caption.lastIndexOf('.')+1));
        return  pointTypeId;

    }

    private void processInvariant(String invariant,int pointTypeId,String mainContext,
                                  String parentContext,String methodName){
        String[] subInvariants;
        Invariant newInvariant = new Invariant(pointTypeId,invariant);
        int invarintId = adapter.insertInvariant(newInvariant);
        if(hasMultipleVariants(invariant))
            subInvariants = getSubInvariants(invariant);
        else{
            subInvariants = new String[1];
            subInvariants[0] = invariant;
        }
        for(String subInvariant : subInvariants){
            if(isStringInvariant(subInvariant)){
                processStringInvariant(invarintId,pointTypeId,subInvariant,mainContext);
            }
            else{
                processOtherTypeInvariant(invarintId,pointTypeId,subInvariant,mainContext);
            }

        }
    }

    private void processOtherTypeInvariant(int invarinatId,int pointTypeId,String originalInvariant,String mainContext) {
        float priority = 0.0f;
        String firstSection = "";
        String secondSection = "";
        String sourceContext = "";
        Widget sourceWidget  = new Widget();
        String destinationContext = "";
        Widget destinationWidget = new Widget();
        String content = "";
        String relationOperator= "";
        String mathOperator = "";
        int flag = 0;
        boolean useOriginMethod = false;

        relationOperator = getRelationOperation(originalInvariant);
        //System.out.println(originalInvariant);
        String[] subsections = splitInvariantBasedOnRelationOperator(originalInvariant);
        if(hasMathOperator(subsections[0])){
            String[] subItems = subsections[0].split(" ");
            firstSection = subItems[0].trim();
            mathOperator = subItems[1].trim();
            secondSection = subItems[2].trim();
            if(subItems.length > 3){
                //flag =  2;
                // means the secondsecction of the invariant is include an integer constant.
                if(subItems[3].contentEquals("+"))
                    content = "-" + subItems[4];
                else
                    content = subItems[4];
            }
        }
        else{
            firstSection = subsections[0].trim();
            secondSection = subsections[1].trim();
        }

        sourceContext   = getContext(mainContext,firstSection);
        if(includedView(firstSection)){
           // sourceWidget = new Widget();
            sourceWidget.setWidgetType(getView(firstSection));
            sourceWidget.setWidgetDatabaseId(getViewId(firstSection));
            sourceWidget.setWidgetAttr(getViewAttr(firstSection));
        }
        else
            return;

        int firstPartId = adapter.addFirstPartOfInvariant(originalInvariant,sourceContext,sourceWidget,relationOperator,mathOperator,content,flag,priority,invarinatId);
        if(secondSection.contentEquals("null")){
                flag = 1;
                content = secondSection.trim();
                adapter.updateContent(firstPartId,content,flag);

        } else if(secondSection.contentEquals("true") || secondSection.contentEquals("false")){
                flag = 3;
                content = secondSection.trim();
                adapter.updateContent(firstPartId,content,flag);
        } else if(startWithDigit(secondSection)){
                flag = 2;
                content = secondSection.trim();
                adapter.updateContent(firstPartId,content,flag);
        } else {
                if (secondSection.startsWith("\\old(")) {
                    secondSection = secondSection.substring(secondSection.indexOf("(") + 1, secondSection.lastIndexOf(")"));
                    useOriginMethod = true;
                }
                  // flag = 0; // means that the secondSection of invariant is a view attribute.
                destinationContext  = getContext(mainContext,secondSection);
                if(includedView(secondSection)){
               //     destinationWidget = new Widget();
                    destinationWidget.setWidgetType(getView(secondSection));
                    destinationWidget.setWidgetDatabaseId(getViewId(secondSection));
                    destinationWidget.setWidgetAttr(getViewAttr(secondSection));
                }
                adapter.addSecondPartOfInvariant(destinationContext,destinationWidget,useOriginMethod,firstPartId);
        }
        priority += callculateInvariantPriority(sourceContext,sourceWidget,destinationContext,destinationWidget,flag,useOriginMethod,mathOperator,
                relationOperator,content);
        adapter.updateInvariantPriority(firstPartId,priority);
    }

    private void processStringInvariant(int invarinatId,int pointTypeId,String originalInvariant,String mainContext) {
        String firstSection = "";
        String secondSection = "";
        String relationOpertor = "";
        String sourceContext = "";
        String mathOperator = "";
        float priority = 0.0f;
        Widget sourceWidget = new Widget();
        String destinationContext = "";
        Widget destinationWidget = new Widget();
        String content = "";
        int flag = 0;
        boolean useOriginMethod = false;

        if(originalInvariant.contains(".equals(")){
            firstSection = originalInvariant.substring(0,originalInvariant.indexOf(".equals("));
            firstSection = firstSection.replace(".toString()","");
            secondSection = originalInvariant.substring(originalInvariant.indexOf("equals(")).trim();
            secondSection = secondSection.replace("equals(","");
            secondSection = secondSection.substring(0,secondSection.lastIndexOf(')'));
            secondSection = secondSection.replace(".toString()","");
            if(originalInvariant.startsWith("!")){
                relationOpertor = "!=";
                firstSection = firstSection.replace("!","");
                secondSection = secondSection.replace("!","");
            }
            else
                relationOpertor = "==";
            if(secondSection.startsWith("\"")){
                flag = 4; //means that secondsection of invariant is a constant string.
                content = secondSection.substring(1,secondSection.length()-1);
            }
            else if(secondSection.startsWith("\\old(")){
                secondSection = secondSection.substring(secondSection.indexOf("(") + 1 ,secondSection.lastIndexOf(")"));
                useOriginMethod = true;
                // flag = 0; // means that the secondSection of invariant is a view attribute.
            }
        }
        else if(originalInvariant.contains(".compareTo(")){
            firstSection = originalInvariant.substring(0,originalInvariant.indexOf(".compareTo("));
            firstSection = firstSection.replace(".toString()","");
            secondSection = originalInvariant.substring(originalInvariant.indexOf(".compareTo("));
            secondSection = secondSection.replace(".compareTo(","");
            relationOpertor = secondSection.substring(secondSection.lastIndexOf(")")+1);
            relationOpertor = relationOpertor.replace("0","").trim();
            secondSection = secondSection.substring(0,secondSection.lastIndexOf(')'));
            secondSection = secondSection.replace(".toString()","");
            if(secondSection.startsWith("\"")){
                flag = 4;
                content = secondSection.substring(1,secondSection.length()-1);
            }
            else if(secondSection.startsWith("\\old(")){
                secondSection = secondSection.substring(secondSection.indexOf("(") + 1,secondSection.lastIndexOf(")"));
                useOriginMethod = true;
                // flag = 0; // means that the secondSection of invariant is a view attribute.
            }
        }

        sourceContext = getContext(mainContext,firstSection);
        if(includedView(firstSection)){
            sourceWidget.setWidgetType(getView(firstSection));
            sourceWidget.setWidgetDatabaseId(getViewId(firstSection));
            sourceWidget.setWidgetAttr(getViewAttr(firstSection));
        }

        int firstPartId = adapter.addFirstPartOfInvariant(originalInvariant,sourceContext,sourceWidget,
                relationOpertor,mathOperator,content,flag,priority,invarinatId);

        if(flag == 0){
            destinationContext  = getContext(mainContext,secondSection);
            if(includedView(secondSection)){
                destinationWidget.setWidgetType(getView(secondSection));
                destinationWidget.setWidgetDatabaseId(getViewId(secondSection));
                destinationWidget.setWidgetAttr(getViewAttr(secondSection));
            }
            adapter.addSecondPartOfInvariant(destinationContext,destinationWidget,useOriginMethod,firstPartId);
        }
        priority += callculateInvariantPriority(sourceContext,sourceWidget,destinationContext,destinationWidget,flag,useOriginMethod,mathOperator,
                relationOpertor,content);
        adapter.updateInvariantPriority(firstPartId,priority);
    }

    private boolean startWithDigit(String str) {
        if(str.charAt(0)>='0' && str.charAt(0) <= '9')
            return true;
        return  false;
    }

    private String[] splitInvariantBasedOnRelationOperator(String originalInvariant) {
        String[] subsections;
        String[] relationOperatorPattern = { "==" , "!=" , ">" , ">=" , "<" ,"<="};
        int matchedPattern = Pattern.findMathOperator(relationOperatorPattern,originalInvariant);
        if(matchedPattern >= 0){
            subsections = originalInvariant.split(relationOperatorPattern[matchedPattern]);
        }
        else{
            subsections = new String[1];
            subsections[0] = originalInvariant;
        }
        return subsections;

    }

    private String getRelationOperation(String originalInvariant){
        String relationOperation = "";
        String[] relationOperatorPattern = { "==" , "!=" , ">" , ">=" , "<" ,"<="};
        int matchedPattern = Pattern.findMathOperator(relationOperatorPattern,originalInvariant);
        if(matchedPattern >= 0)
            relationOperation =relationOperatorPattern[matchedPattern];
        return relationOperation;
    }

    private boolean hasMathOperator(String subsection) {
        if(subsection.contains("-") || subsection.contains("+"))
            return true;
        return false;
    }

    private float callculateInvariantPriority(String sourceContext,Widget sourceWidget, String destinationContext,Widget destinationWidget,
                                               int flag, boolean useOrigin, String mathOperator,
                                               String relationOpertor,String content) {
        float priority = 0.0f;

        if(!mathOperator.contentEquals("")){
            priority += 0.8;
            return priority;
        }
        else {
              if(destinationWidget.getWidgetType().contentEquals("")){
                  if(flag == 1){
                    // (flag == 1) means that the first invariant attribute compare with null.
                     if(sourceWidget != null){
                          priority += 0.1;
                          return priority;
                     }
                  }else if(flag == 2){
                     // (flag == 2) means that the first invariant attribute compare with a constant int.
                        if(sourceWidget.getWidgetAttr().contentEquals( "size"))
                            priority += 0.7 ;
                        else
                            priority += 0.3;
                        return priority;
                  }else if(flag == 3){
                      // (flag == 3) means that the first invariant attribute compare with boolean value.
                      priority += 0.2;
                      return priority;
                  } else if(flag == 4){
                      //(flag == 4) means that the first invariant attribute compare with a constant string.
                       if(content.length()>0)
                           if(sourceWidget.getWidgetType().contentEquals("TextView")){
                               priority += 0.6;
                               return priority;
                           }
                  }
              }
              else{
                    if(sourceWidget.getWidgetDatabaseId() == destinationWidget.getWidgetDatabaseId()){
                        if(sourceWidget.getWidgetAttr().contentEquals(destinationWidget.getWidgetAttr()))
                             if(sourceWidget.getWidgetType().contentEquals("ListView")){
                                 priority += 0.6;
                                 return priority;
                             } else {
                                 priority += 0.2;
                                 return priority;
                             }
                        }
                    else{
                          if(!sourceWidget.getWidgetType().contentEquals(destinationWidget.getWidgetType()))
                              priority += retrivePriority1(sourceContext,sourceWidget,destinationContext,destinationWidget);
                          else{
                              priority += retrivePriority2(sourceContext,sourceWidget,destinationContext,destinationWidget);
                          }
                    }
              }
        }
        return priority;
    }

    private float retrivePriority1(String sourceContext,Widget sourceWidget, String destinationContext,Widget destinationWidget) {
        float priority = 0.0f;
        if(sourceContext.contentEquals(destinationContext)){
            if(sourceWidget.getWidgetAttr().contentEquals(destinationWidget.getWidgetAttr()))
                priority += 0.1;
            else{
                  if(sourceWidget.getWidgetAttr().contentEquals("Checked") ||
                     destinationWidget.getWidgetAttr().contentEquals("Checked"))
                      priority += 0.2;
                  else if((sourceWidget.getWidgetAttr().contentEquals("size") && destinationWidget.getWidgetAttr().contentEquals("length"))||
                          (sourceWidget.getWidgetAttr().contentEquals("length")||destinationWidget.getWidgetAttr().contentEquals("size")))
                      priority += 0.0;
                  else
                      priority += 0.3;

            }
        }
        else{
            if(sourceWidget.getWidgetAttr().contentEquals(destinationWidget.getWidgetAttr()))
                priority += 0.2;
            else{
                if(sourceWidget.getWidgetAttr().contentEquals("Checked") ||
                        destinationWidget.getWidgetAttr().contentEquals("Checked"))
                    priority += 0.3;
                else if((sourceWidget.getWidgetAttr().contentEquals("size") && destinationWidget.getWidgetAttr().contentEquals("length"))||
                        (sourceWidget.getWidgetAttr().contentEquals("length")||destinationWidget.getWidgetAttr().contentEquals("size")))
                    priority += 0.0;
                else
                    priority += 0.4;
           }
        }
//             switch (sourceWidget.getWidgetAttr()){
//                 case "enable":  priority += 0.3;break;
//                 case "text":    priority += 0.4; break;
//                 case "toString()": priority  += 0.4;break;
//                 case "length" : if(destinationWidget.getWidgetAttr().contentEquals("length"))
//                                    priority += 0.3;
//                                 else
//                                     priority += 0.1;
//                                 break;
//                 case "checked": if(destinationWidget.getWidgetAttr().contentEquals("editable"))
//                                    priority += 0.4;
//                                 else
//                                     priority += 0.2;
//                                 break;
//                 case "editable": if(destinationWidget.getWidgetAttr().contentEquals("checked"))
//                                     priority +=0.4;
//                                  else
//                                      priority += 0.2;
//                                  break;
//                 case "size" :   break;
//             }
        return priority;
    }

    private float retrivePriority2(String sourceContext,Widget sourceWidget, String destinationContext,Widget destinationWidget) {
        float priority = 0.0f;
        if(sourceContext.contentEquals(destinationContext)){
            if(sourceWidget.getWidgetAttr().contentEquals(destinationWidget.getWidgetAttr()))
                if(sourceWidget.getWidgetType().contentEquals("Button") ||
                   sourceWidget.getWidgetType().contentEquals("FloatingActionButton"))
                      priority += 0.1;

        }

//        switch (sourceWidget.getWidgetAttr()){
//            case "enable":  if(sourceWidget.getWidgetType().contentEquals("Button") ||
//                               sourceWidget.getWidgetType() .contentEquals("FloatingActionButton"))
//                                  priority += 0.3;
//                             else
//                                  priority += 0.1;
//                             break;
//            case "text":
//            case "toString()": if(!sourceWidget.getWidgetType().contains("Button"))
//                                   priority += 0.2;
//                               break;
//
//            case "checked": if(destinationWidget.getWidgetAttr().contentEquals("checked"))
//                                priority += 0.2;
//                            break;
//            case "editable":
//            case "length" :
//            case "size" :   break;
//        }
        return priority;
    }

    private String getViewAttr(String subInvariant) {
        String viewAttr = "";
        subInvariant = subInvariant.substring(subInvariant.indexOf(".") + 1);
        viewAttr = subInvariant.substring(subInvariant.lastIndexOf("_") + 1);
        return viewAttr;

    }

    private int getViewId(String subInvariant) {
        String viewId = "";
        subInvariant = subInvariant.substring(subInvariant.indexOf(".") + 1);
        if(subInvariant.startsWith("this."))
            subInvariant = subInvariant.substring(subInvariant.indexOf(".") + 1);
        subInvariant = subInvariant.substring(subInvariant.indexOf("_") +1);
        viewId = subInvariant.substring(0,subInvariant.indexOf('_'));
        return Integer.parseInt(viewId);
    }

    private String getView(String subInvariant) {
        String view = "";
        subInvariant = subInvariant.substring(subInvariant.indexOf(".") + 1);
        if(subInvariant.startsWith("this."))
            subInvariant = subInvariant.substring(subInvariant.indexOf(".") + 1);
        view = subInvariant.substring(0,subInvariant.indexOf("_"));
        return view;
    }

    private boolean includedView(String invariant) {
        String tmp =  invariant.substring(invariant.indexOf(".") + 1);
        if(tmp.contains("_"))
            return true;
        return false;
    }

    private String getContext(String mainContext, String subInvariant) {
        String context = "";
        subInvariant = subInvariant.trim();

        if(subInvariant.startsWith("this.") || subInvariant.contentEquals("this"))
            context = mainContext;
        else{
            Utils.showMessage(subInvariant);
            context = subInvariant.substring(0,subInvariant.indexOf("."));
        }
        return context;
    }

    private boolean isStringInvariant(String subInvariant) {
        if(subInvariant.contains(".toString()"))
            return true;
        return false;
    }

    private Object removePackageName(String subInvariant) {
        return subInvariant.replaceAll(packageName + ".","");
    }

    private boolean hasPackageName(String subInvariant) {
        return subInvariant.contains(packageName);
    }

    private boolean hasStringRelationOperator(String item) {
        if((item.contains(".equals(") || item.contains(".compareTo(")) && item.contains(".toString()"))
            return true;
        return false;
    }

    private String[] getSubInvariants(String invariant) {
        String[] invariants = invariant.split("\\|\\|");
        return invariants;
    }

    private boolean hasMultipleVariants(String invariant) {
        if(invariant.contains("||"))
            return  true;
        return false;
    }

    private String getMainContext(String content){
        String mainContext = "";
        String context = "";
        String methodName = content.substring(content.indexOf(".") +1);
        String pointType = methodName.substring(methodName.indexOf(":::") + 3);
        if(content.contains("."))
            context = content.substring(0,content.indexOf("."));
        else
            context = content.substring(0,content.indexOf(":::"));
        // String context = content.substring(content.indexOf(".") + 1, content.indexOf(":::"));

        if(pointType.contentEquals("OBJECT")){
            if(context.contains("$"))
                mainContext = context.substring(context.indexOf("$")+1);
            else
                mainContext = context;
        }
        else{
            if(context.contains("$"))
                mainContext = context.substring(context.indexOf("$")+1);
            else
                mainContext = context;
        }
        return mainContext;
    }

    private String getParentContext(String content){
        String parentContext = "";
        //content = content.substring(content.indexOf(".") +1);
        String pointType = content.substring(content.indexOf(":::") + 3);
        String context = content.substring(0, content.indexOf(":::"));
        if(context.contains("$"))
            parentContext = context.substring(0,context.indexOf("$"));
        //
        //        if(pointType.contentEquals("OBJECT")){
        //            if(context.contains("$"))
        //                parentContext = context.substring(0,context.indexOf("$"));
        //
        //        }
        //        else{
        //            if(context.contains("$"))
        //                parentContext = context.substring(0,context.indexOf("$"));
        //
        //        }
        return parentContext;
    }

    private String getMethodName(String content){
        String methodName = "";
        String pointType = content.substring(content.indexOf(":::") + 3);
        String context = content.substring(0, content.indexOf(":::"));
        if(!pointType.contentEquals("OBJECT")){
            methodName = context.substring(context.indexOf('.') + 1,context.indexOf("("));
            if(methodName.contentEquals("menu.findItem")){
                String label = context.substring(context.indexOf("(") + 1,context.indexOf(")"));
                if(label.startsWith("R.id"))
                    label = label.substring(label.lastIndexOf('.') + 1);
                methodName = label + context.substring(context.lastIndexOf('_'));
            }

        }
        return methodName;
    }

    public List<Invariant> getInvariants(PointType pointType){
        int pointId = adapter.getPointIdBy(pointType);
        List<Invariant> invariants = adapter.getInvariants(pointId);
        return invariants;
    }

    public List<InvariantInformation> getInvariants(int invarId){
        List<InvariantInformation> invariants = adapter.getInvariantDetailsBy(invarId);
        return invariants;
    }

//    public int getPackageId() {
//        return adapter.getPackageId();
//    }

    public String getInvariantById(int invarId) {
        return adapter.getInvariantBy(invarId);
    }

    public Widget getWidgetInfoById(int viewId) {
        Widget widget = adapter.getWidgetInfoById(viewId);
        widget.setWidgetDescriptorsList(adapter.getWidgetDescriptorListByWidgetId(viewId));
        return widget;
    }

    public void setAdapter(DatabaseAdapter adapter){
        this.adapter = adapter;
    }

    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
}