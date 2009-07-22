package akrasia.thing;

public class Thing {
    public Thing() {
    }
    public Thing(int id) {
        this.id = id;
    }

    
    public int id = -1;
    
    char c; //0x002E;
    
    public char GetSymbol(){
        return c = '#';
    }
}