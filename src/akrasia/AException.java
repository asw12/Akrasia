package akrasia;

/**
 * An exception specific to Akrasia's properties.
 */
public class AException extends Exception{
    public AException(){
        this(ExceptionType.DEFAULT);
    }

    public AException(ExceptionType type){
        super(type.GetString());
    }

    public static enum ExceptionType{
        DEFAULT("Default statement"),
        OUT_OF_MAP_BOUNDS("Coordinates are outside of the map's bounds"),
        INVALID_DIMENSIONS("Dimensions are invalid");
        
        private String statement;
        private ExceptionType(String statement){ this.statement = statement; }
        String GetString(){ return "Akrasia: " + statement; }
    }
}
