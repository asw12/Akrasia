package akrasia.thing.unit;

import akrasia.Constant;

public class Player extends Unit{
    public Player() {
    }
    public Player(int i) {
        super(i);
    }

    char c  = 0x046A;

    public char GetSymbol(){
        return c;
    }

    public int GetTimeCost(Constant.ACTIONS action){
        return 40;
    }
}
