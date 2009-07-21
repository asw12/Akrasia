package save;

/**
 * A class for storing Akrasia settings. Settings stored here should be considered temporary
 * as server-side settings will be phased into .properties files and client-side settings
 * will be phased into the MySQL database.
 */
public class Settings {
    // Database Settings ( //TODO: to be moved to a .properties file )
    public final static String DBTableNameDefault = "akrasia";
    public final static String DBUserDefault      = "akrasia";
    public final static String DBPasswordDefault  = "admin";

    // Network Settings

    // In-game Options

    // Key Bindings
}
