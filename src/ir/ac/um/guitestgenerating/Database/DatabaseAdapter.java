package ir.ac.um.guitestgenerating.Database;

import ir.ac.um.guitestgenerating.GUIInvarriant.InvariantInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.DescriptorType;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.Widget;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Widget.WidgetDescriptor;
import ir.ac.um.guitestgenerating.Util.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {
    private String databaseName;
    private int packageId;
    private Connector connector;
    private Connection connection;

    public DatabaseAdapter(String databaseName){
        this.databaseName = databaseName;
    }

    public boolean prepareDatabase(){
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int InsertPointType(String mainContext,String parentContext,String methodName, String pointType,String caption) {
        int rowId;
        caption = caption.substring(caption.indexOf(".") +1);
        try {
                Connector connector = new Connector(databaseName);
                Connection connection = connector.createConnection();
                Statement stmt = connection.createStatement();
                 String sql = "INSERT INTO POINTSTYPETABLE (pointType, ParentContext,Context,MethodName,caption) VALUES ('"
                         + pointType + "','" + parentContext + "','" + mainContext + "','" + methodName + "','" + caption + "')";
                 stmt.executeUpdate(sql);
                 sql = "SELECT max(pointId) as pointId FROM POINTSTYPETABLE WHERE 1";
                 ResultSet resultSet = stmt.executeQuery(sql);
                 resultSet.next();
                 rowId = resultSet.getInt("pointId");
                 stmt.close();
                 connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 1");
            return -1;
        }
        return rowId;
    }

    public int insertInvariant(Invariant invariant){
        int rowId = -1;
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "INSERT INTO invariantstable (invariant,pointTypeId) VALUES ('"
                    + invariant.getInvariant() + "'," + invariant.getPointTypeId() + ")";
            stmt.executeUpdate(sql);
            sql = "SELECT max(invarId) as invarId FROM invariantstable WHERE 1";
            ResultSet resultSet = stmt.executeQuery(sql);
            resultSet.next();
            rowId = resultSet.getInt("invarId");
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return rowId;
    }

    public int addFirstPartOfInvariant(String subInvariant, String souceContext, Widget sourceWidget,
                                       String relationOperator, String mathOperator,String content,
                                       int flag, float priority, int invarId){
        int rowId;
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            if(subInvariant.contains("'"))
                subInvariant = subInvariant.replaceAll("'","''");
            if(content.contains("'"))
                content = content.replaceAll("'","''");

            String sql = "INSERT INTO firstpartofinvariantdetail (originalinvariant,sourceContext,sourceView,sourceViewAttribute," +
                    "relationOperator,mathOperator,content,Flag, priority,invarId,viewId) VALUES ('"
                    + subInvariant + "','" + souceContext + "','" + sourceWidget.getWidgetType() + "','"
                    + sourceWidget.getWidgetAttr() + "','" + relationOperator + "','" + mathOperator +"','"
                    + content + "','" + flag + "','" + priority + "','" + invarId + "','" + sourceWidget.getWidgetDatabaseId() + "')";
            stmt.executeUpdate(sql);
            sql = "SELECT firstPartId FROM firstpartofinvariantdetail WHERE originalinvariant ='" + subInvariant + "' and invarId = " +
                    invarId ;
            ResultSet resultSet = stmt.executeQuery(sql);
            resultSet.next();
            rowId = resultSet.getInt("firstPartId");
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 2-" + subInvariant);
            return -1;
        }
        return rowId;
    }

    public void updateContent(int firstPartId, String content,int flag){

        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            if(content.contains("'"))
                content = content.replaceAll("'","''");
            String sql = "UPDATE firstpartofinvariantdetail set content='" + content + "',flag =" + flag + " where firstPartId=" + firstPartId;
            int count = stmt.executeUpdate(sql);
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 3");
        }

    }

    public boolean addSecondPartOfInvariant(String destinationContext,Widget destinationWidget, boolean useOriginMethod,int firstPartId){
        int flag;
        if(useOriginMethod)
            flag = 1;
        else
            flag = 0;
        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "INSERT INTO secondpartofinvariantdetail (destinationContext,destinationView,destinationViewAttribute," +
                    "useOrigMethod,firstPartId,viewId) VALUES ('" + destinationContext + "','"
                    + destinationWidget.getWidgetType() + "','" + destinationWidget.getWidgetAttr() + "',"
                    + flag + "," + firstPartId + "," + destinationWidget.getWidgetDatabaseId() + ")";
            stmt.executeUpdate(sql);
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 4");
            return false;
        }
        return true;
    }

    public void updateInvariantPriority(int firstPartId, float priority) {
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "UPDATE firstpartofinvariantdetail set priority='" + priority + "' where firstPartId=" + firstPartId;
            int count = stmt.executeUpdate(sql);
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 5");
        }

    }

    public List<String> getPointTypeCaptions() {
        List<String> captions = new ArrayList<>();
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM pointstypetable WHERE 1";
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()){
              //  String pointTypeCaption = "";
                String pointType = resultSet.getString("pointType");
                String parentContext = resultSet.getString("ParentContext");
                String context = resultSet.getString("Context");
                String caption = resultSet.getString("caption");
                if(pointType.contentEquals("OBJECT"))
                    captions.add(databaseName + "." + caption + "." + resultSet.getInt("pointId") );
                else{
                     if(!parentContext.contentEquals(""))
                        captions.add(databaseName + "." + parentContext + "$" + context + "."
                                + caption + "." + resultSet.getInt("pointId"));
                     else
                         captions.add(databaseName + "." + context + "." + caption + "."
                                 + resultSet.getInt("pointId"));
                }
            }
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 6");
        }
       return captions;
    }

    public int getPointTypeId(String caption) {
        int pointTypeId = 0;
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
             String sql = "SELECT pointId FROM pointstypetable WHERE caption ='" + caption + "'";
            ResultSet resultSet = stmt.executeQuery(sql);
            if(resultSet.next())
                pointTypeId = resultSet.getInt("pointId");
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 7");
        }
        return pointTypeId;
    }

    public List<Invariant> getInvariants(int pointId) {
        List<Invariant> invariants = new ArrayList<>();
        try {
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT DISTINCT invariantstable.invarId,invariantstable.invariant, firstpartofinvariantdetail.priority FROM invariantstable,firstpartofinvariantdetail WHERE invariantstable.pointTypeId =" + pointId +
                    " AND firstpartofinvariantdetail.invarId = invariantstable.invarId ORDER BY firstpartofinvariantdetail.priority DESC ";
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()){
                Invariant invariant = new Invariant(pointId,resultSet.getString("invariant"));
                invariant.setInvardId(resultSet.getInt("invarId"));
                invariant.setPriority(resultSet.getFloat("priority"));
                invariants.add(invariant);
            }
            stmt.close();
            connection.close();
        }catch (SQLException ioe){
            ioe.printStackTrace();
            Utils.showMessage("There is a problem in working with the database!!! 8");
        }
        return invariants;
    }

 //   public int getPackageId(){return  packageId;}

    public int getPointIdBy(PointType pointType) {
        int pointId = -1;
        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT pointId FROM pointstypetable WHERE pointType = '" + pointType.getPointType() + "' AND"
                      + " ParentContext = '" + pointType.getParentContext() + "' AND Context ='" + pointType.getMainContext() +
                      "' AND MethodName ='" + pointType.getMethodName() +"'";
            ResultSet resultSet = stmt.executeQuery(sql);
            if(resultSet.next())
                pointId = resultSet.getInt("pointId");
            connection.close();
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return pointId;
    }

    public String getInvariantBy(int invarId) {
        String invariant = "";
        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT invariant FROM invariantstable WHERE invarId =" + invarId;
            ResultSet resultSet = stmt.executeQuery(sql);
            if(resultSet.next())
                invariant = resultSet.getString("invariant");
            connection.close();
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return invariant;
    }

    public List<InvariantInformation> getInvariantDetailsBy(int invarId){
        List<InvariantInformation> invariants = new ArrayList<>();

        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT  firstpartofinvariantdetail.* , secondpartofinvariantdetail.* FROM  firstpartofinvariantdetail " +
                    "LEFT JOIN secondpartofinvariantdetail " +
                    "ON firstpartofinvariantdetail.firstPartId = secondpartofinvariantdetail.firstPartId " +
                    "WHERE   firstpartofinvariantdetail.invarId =" + invarId;
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()){
                String tmpStr = "";
                InvariantInformation invariantInfo = new InvariantInformation();
                invariantInfo.setRowId(resultSet.getInt("firstPartId"));
                invariantInfo.setSourceContext(resultSet.getString("sourceContext"));
                invariantInfo.setSourceView(resultSet.getString("sourceView"));
                invariantInfo.setSourceViewAttribute(resultSet.getString("SourceViewAttribute"));
                invariantInfo.setRelationOperator(resultSet.getString("relationOperator"));
                invariantInfo.setMathOperator(resultSet.getString("mathOperator"));
                invariantInfo.setContent(resultSet.getString("content"));
                invariantInfo.setContentType(resultSet.getInt("Flag"));
                invariantInfo.setSourceViewId(resultSet.getInt("viewId"));
                tmpStr = resultSet.getString("destinationContext");
                if(!resultSet.wasNull())
                    invariantInfo.setDestinationContext(tmpStr);
                tmpStr = resultSet.getString("destinationView");
                if(!resultSet.wasNull())
                    invariantInfo.setDestinationView(tmpStr);
                tmpStr = resultSet.getString("destinationViewAttribute");
                if(!resultSet.wasNull())
                    invariantInfo.setDestinationViewAttribute(tmpStr);
                int tmpInt = resultSet.getInt("useOrigMethod");
                if(!resultSet.wasNull())
                    invariantInfo.setIsUsedOrigMethod(tmpInt);
                tmpInt = resultSet.getInt("secondpartofinvariantdetail.viewId");
                if(!resultSet.wasNull())
                    invariantInfo.setDestinationViewId(tmpInt);
                invariants.add(invariantInfo);
            }

            connection.close();
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return invariants;
    }


    public List<WidgetDescriptor> getWidgetDescriptorListByWidgetId(int widgetId) {
        List<WidgetDescriptor> widgetDescriptors = new ArrayList<>();
        String descriptor = "";
        String value = "";
        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM descriptor WHERE widgetId =" + widgetId;
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()){
                WidgetDescriptor widgetDescriptor = new WidgetDescriptor();
                descriptor = resultSet.getString("descriptor");
                value      = resultSet.getString("value");
                switch (descriptor){
                    case "ViewId"   : widgetDescriptor.SetDescriptor(DescriptorType.ViewId,value);
                                      break;
                    case "ViewLabel": widgetDescriptor.SetDescriptor(DescriptorType.ViewLabel,value);
                                      break;
                    case "ViewHint" : widgetDescriptor.SetDescriptor(DescriptorType.ViewHint,value);
                                      break;
                    case "ViewContentDescription" : widgetDescriptor.SetDescriptor(DescriptorType.ViewContentDescription,value);
                                         break;
                    case "ViewTagValue" : widgetDescriptor.SetDescriptor(DescriptorType.ViewTagValue,value);
                                          break;
                    case "OptionMenuDescription" : widgetDescriptor.SetDescriptor(DescriptorType.OptionMenuDescription,value);
                                                   break;
                }

                widgetDescriptors.add(widgetDescriptor);
            }

            connection.close();
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return widgetDescriptors;
    }

    public Widget getWidgetInfoById(int widgetId) {
        Widget widget = new Widget();
        try{
            Connector connector = new Connector(databaseName);
            Connection connection = connector.createConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM widget WHERE widgetId =" + widgetId;
            ResultSet resultSet = stmt.executeQuery(sql);
            if(resultSet.next()){
                widget.setWidgetType(resultSet.getString("WidgetType"));
                widget.setBindingVariable(resultSet.getString("bindingVariable"));
            }

            connection.close();
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return widget;

    }
}
