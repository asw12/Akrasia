package akrasia.network;

import akrasia.thing.Unit.Player;
import akrasia.thing.Unit.Unit;

/**
 * This is the version of clients that the Server keeps track of.
 */
public class PseudoClient extends Thread{
    public Unit controlledUnit = new Player();
    
    public void ReceiveMsg(String str){
        
    }
}
