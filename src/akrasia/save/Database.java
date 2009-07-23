package save;

// temporary libraries
import com.mysql.jdbc.exceptions.*;
import com.mysql.jdbc.jdbc2.optional.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.sql.*;
import javax.naming.*;
import java.sql.*;
import java.util.*;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.tools.sessionconfiguration.XMLSessionConfigLoader;
import oracle.toplink.tools.sessionmanagement.SessionManager;

public class Database {
    public static void main(String[] args){
        Database adb = new Database();
        
        if(args.length > 0 && args[0] == "reinitialize") {
            adb.Armageddon();
        }
        
        XMLSessionConfigLoader loader = new XMLSessionConfigLoader(getSessionsXmlPath());
        SessionManager mgr = oracle.toplink.tools.sessionmanagement.SessionManager.getManager();
        DatabaseSession session = (DatabaseSession)mgr.getSession(loader, getSessionName(), Thread.currentThread().getContextClassLoader(), true, true);

        /* ResultSet rs = adb.ExecuteStatement("SELECT * FROM `creatureinstance`;");
        try{
            while(rs.next()){
                
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }*/      
    }
    
    public Database() {
        try{
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
            // Localhost should probably always be the server
            dataSource.setServerName("localhost");
            dataSource.setPort(3306);
            
            dataSource.setDatabaseName(Settings.getProperty("DBTableName"));
            dataSource.setUser(Settings.getProperty("DBUser"));
            dataSource.setPassword(Settings.getProperty("DBPassword"));
            
            ds = dataSource;
            
            //TODO: perhaps add a test here to make sure the connection is valid?
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    
    DataSource ds = null;
    
    private ResultSet ExecuteStatement(String statement) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.execute(statement);
            
            if(stmt.getWarnings() != null)
                System.out.println("SQLWarning: " + stmt.getWarnings().getMessage());
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Using settings: "
                                + "\n\tTable: " + Settings.getProperty("DBTableName")
                                + "\n\tUser: " + Settings.getProperty("DBUser")
                                + "\n\tPassword: " + Settings.getProperty("DBPassword"));
            // This is a fatal error
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (stmt != null) {
                try {
                    rs = stmt.getResultSet();
                    
                    /*
                     * Note: When a Statement object is 
                     * closed, its current ResultSet object, if one exists, is 
                     * also closed.  
                     */
                    //stmt.close();
                } catch (SQLException sqlex) { }

                stmt = null;
            }

            if (conn != null) {
                /* try {
                    // See above
                    //conn.close();
                } catch (SQLException sqlex) { } */
                conn = null;
            }
        }
        return rs;
    }
    
    /**
     * Runs the necessary querries for setting up a table for Akrasia.
     * All the included .SQL query files should use INSERT IGNORE or CREATE *** IF NOT EXISTS
     * statements exclusively, as this method is not intended to overwrite anything unless 
     * used with Armageddon().
     * 
     * This method is only intended to be used on a clean Database.
     */
    private void InitializeDatabase() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("SQL/Akrasia.sql"));
            StringBuffer sb = new StringBuffer();
            while (in.readLine() != null) {
                sb.append(in.readLine() + "\n");
            }
            in.close();
            if(sb.toString() != "")
                ExecuteStatement(sb.toString());
            else
                System.err.println("ERROR: Akrasia.sql is emptry");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes all tables associated with Akrasia, then reinitializes it.
     */
    private void Armageddon() {
        //TODO: Write and replace with SQL statement that drops all tables
        ExecuteStatement("DROP TABLE IF EXISTS `akrasia`.`dungeonlevel`;");
        
        InitializeDatabase();
    }
    
    /**
     * Retrieves the String of the location of the sessions settings.
     * @return String
     */
    protected static String getSessionsXmlPath() {
            return "META-INF/sessions.xml";
    }
    protected static String getSessionName() {
            return "Akrasia";
    }
}