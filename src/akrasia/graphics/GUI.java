package akrasia.graphics;

import akrasia.Constant;

import akrasia.environment.DisplayedMap;

import akrasia.network.Client;

import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public abstract class GUI {
    public GUI(){
        console = new JFrame("Akrasia");
    }

    Dimension defaultDim;
    KeyListener keyListener;
    JFrame console;

    public Client client;

    //public boolean ChangedThings = true;
    //public boolean ChangedUnits = true;

    void DisplayMenu(){}
    void Prompt(){}
    void Repaint(){}
    void LoadSettings(){}
    public void MovedSelf(Constant.DIRECTIONS direction){}
    public void HandleRejectMessage(String s){}
    public abstract void EditedInanimate();
    public abstract void EditedAnimate();


    /**
     * Attempts to move the cursor or character controlled in the direction specified.
     * Returned boolean is true if the screen is changed in anyway.
     * @param direction
     * @return
     */
    boolean MoveCardinal(Constant.DIRECTIONS direction){
        return false;
    }

    void NextPage(){}
    void PreviousPage(){}

    public void AddClient(Client client){
        this.client = client;
        client.gui = this;
    }

    public void Initialize(){}

    public void Tick() {}
}
