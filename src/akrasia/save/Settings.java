package save;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Properties;

/**
 * A class for storing Akrasia settings. Settings stored here should be considered temporary
 * as server-side settings will be phased into .properties files and client-side settings
 * will be phased into the MySQL database.
 */
public class Settings {
    private static boolean loaded = false;
    private static Properties props = null;

    // Database Settings
    public static boolean UseRelationalDB = true;

    // Network Settings

    // In-game Options

    // Key Bindings

    public static String getProperty(String str){
        if(!loaded){
            props = new Properties();
            try{
                props.load(new BufferedReader(new FileReader("Akrasia.properties")));
                loaded = true;
            }
            catch(FileNotFoundException e){
                // Create the new file
                //props.setProperty("DBTableName", Settings.DBTableNameDefault);
                loaded = true;
                try{
                    props.store(new BufferedWriter(new FileWriter("Akrasia.properties")), "Akrasia Server Side Properties \n Generated at:");
                } catch (IOException e2) { e2.printStackTrace(); }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        return props.getProperty(str);
    }
}