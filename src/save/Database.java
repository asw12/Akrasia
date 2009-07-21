package save;

import com.mysql.jdbc.exceptions.*;
import com.mysql.jdbc.jdbc2.optional.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;

public class Database {
    public static void main(String[] args){
        Database adb = new Database();
        
        if(args[0] == "reinitialize") {
            adb.Armageddon();
        }
    }
    
    public Database() {
        props = new Properties();
        
        try{
            props.load(new BufferedReader(new FileReader("Akrasia.properties")));
        }
        catch(FileNotFoundException e){
            // Create the new file
            props.setProperty("DBTableName", Settings.DBTableNameDefault);
            props.setProperty("DBUser", Settings.DBUserDefault);
            props.setProperty("DBPassword", Settings.DBPasswordDefault);
            
            try{
                props.store(new BufferedWriter(new FileWriter("Akrasia.properties")), "Akrasia Server Side Properties \n Generated at:");
            } catch (IOException e2) { }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        try{
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
            // Localhost should probably always be the server
            dataSource.setServerName("localhost");
            dataSource.setPort(3306);
            
            dataSource.setDatabaseName(props.getProperty("DBTableName"));
            dataSource.setUser(props.getProperty("DBUser"));
            dataSource.setPassword(props.getProperty("DBPassword"));
            
            ds = dataSource;
            
            //TODO: perhaps add a test here to make sure the connection is valid?
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    
    DataSource ds = null;
    Properties props = null;
    
    private void ExecuteStatement(String statement) {
        Connection conn = null;
        Statement stmt = null;

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
                                + "\n\tTable: " + props.getProperty("DBTableName")
                                + "\n\tUser: " + props.getProperty("DBUser")
                                + "\n\tPassword: " + props.getProperty("DBPassword"));
            // This is a fatal error
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            /*
             * close any jdbc instances here that weren't
             * explicitly closed during normal code path, so
             * that we don't 'leak' resources...
             */

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlex) { }

                stmt = null;
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqlex) { }
                conn = null;
            }
        }
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
}