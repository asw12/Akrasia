package akrasia.thing;

import akrasia.Constant;

public class Wall extends Thing{
    public Wall(int i) {
        super(0);
    }

    public char GetSymbol(){
        return '#';
        //TODO: return special walls
    }
}
