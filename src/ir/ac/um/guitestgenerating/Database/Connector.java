package ir.ac.um.guitestgenerating.Database;

import ir.ac.um.guitestgenerating.Util.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector extends Object{

    private String     db_userName;
    private String     db_password;
    private String     db_url;
    private Connection db_connection;

    public Connector(String databaseName){
        db_userName = "root";
        db_password = "";
        db_url = "jdbc:mysql://localhost:3306/" + databaseName + "?useSSL=false";
        db_connection = null;
    }


    public Connection createConnection(){

        try {
              Class.forName("com.mysql.jdbc.Driver");
              db_connection = DriverManager.getConnection(db_url, db_userName, db_password);
        } catch (SQLException | ClassNotFoundException ioe) {
            Utils.showMessage(ioe.getMessage());
        }
        return db_connection;
    }

    public boolean closeConnection(){
        boolean status = false;
        try {
            db_connection.close();
            status = true;
        } catch (SQLException ioe){
            Utils.showMessage(ioe.getMessage());
        }
        return  status;
    }

    public Connection getConnection(){
        return  db_connection;
    }

}
