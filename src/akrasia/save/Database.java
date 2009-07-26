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
    // Do not use
    /*public static void main(String[] args){
        if(args.length > 0 && args[0] == "reinitialize") {
            adb.Armageddon();
        }
    }*/

    protected static DatabaseSession session;

    public Database() {
        if(Settings.UseRelationalDB)
        {
            XMLSessionConfigLoader loader = new XMLSessionConfigLoader(getSessionsXmlPath());
            SessionManager mgr = oracle.toplink.tools.sessionmanagement.SessionManager.getManager();
            session = (DatabaseSession)mgr.getSession(loader, getSessionName(), Thread.currentThread().getContextClassLoader(), true, true);
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
    }

    /**
     * Deletes all tables associated with Akrasia, then reinitializes it.
     */
    private void Armageddon() {
        //TODO: Write and replace with TopLink statement that drops all tables
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