package akrasia.graphics;

import akrasia.network.*;
import akrasia.Constant;
import akrasia.environment.DisplayedMap;
import akrasia.thing.Thing;
import akrasia.thing.unit.Unit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import java.net.InetAddress;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.util.LinkedList;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import sun.awt.Graphics2Delegate;

public class SimpleGUI extends GUI{
    public static void main(String[] args) {
        /*
         * 1) Starts with a SimpleGUI
         * 2) SimpleGUI joins server
         * 3) SimpleGUI changed to in-game mode
         */
        
        SimpleGUI simple = new SimpleGUI();
        simple.AddClient(new Client(simple));
 
        // Uncomment to run two servers       
        /*SimpleGUI simple2 = new SimpleGUI();
        simple2.AddClient(new Client(simple2)); // */
        
        Server server = new Server(6112);
        
        // Blank window will appear if no server can be found
        try{
            simple.client.JoinServer(InetAddress.getByName("67.180.54.71"), 6112);
            
            //simple2.client.JoinServer(InetAddress.getLocalHost(), 6112);
        }
        catch(Exception e){
            e.printStackTrace(); 
        }
    }
    
    public APanel panel;
    
    BufferedImage thingsimage;
    Dimension dimensions;
    
    Point cursor = null;
        Thing cursorover = null;
        boolean cursorlock = false;
    long CurrentTime;
        long cursorTime;
        int cursorSpeed = 0;
        int cursorSlowSpeed = 160;
    
    LinkedList<Byte> moveKeys = new LinkedList<Byte>(); //TODO: this should just be an array
        boolean moveReady = true; //should send a move message
    
    public SimpleGUI() {
        super();
        dimensions = new Dimension(800, 640);
        console.setSize(dimensions);
        console.setResizable(false);
        console.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        console.setVisible(true);
    }
    
    public void Initialize(){
        keyListener = new KeyListener(){
                public void keyTyped(KeyEvent e) {
                }
                
                public void mvkey(int key, byte modifiers){
                    for(int i = 0; i < moveKeys.size(); i++){
                        if((moveKeys.get(i) & 0xf) == key){
                            return;
                        }
                    }
                    if(moveKeys.size() < 3){
                        moveKeys.addFirst((byte)(key + modifiers));
                    }
                }
                public void rmkey(int key){
                    for(int i = 0; i < moveKeys.size(); i++){
                        if((moveKeys.get(i) & 0xf) == key){
                            moveKeys.remove(i);
                            return;
                        }
                    }
                }
                
                public void keyPressed(KeyEvent e) {
                    Byte front =  moveKeys.peekFirst();
                    byte modifiers = (byte)((e.isShiftDown() ? 0x80 : 0) + (e.isControlDown() ? 0x40 : 0) + (e.isMetaDown() ? 0x20 : 0) + (e.isAltDown() ? 0x10 : 0));
                    
                    switch(e.getKeyCode()){
                        case 'Y': mvkey(7, modifiers); break;
                        case 'K': mvkey(8, modifiers); break;
                        case 'U': mvkey(9, modifiers); break;
                        case 'H': mvkey(4, modifiers); break;
                        case 'L': mvkey(6, modifiers); break;
                        case 'B': mvkey(1, modifiers); break;
                        case 'J': mvkey(2, modifiers); break;
                        case 'N': mvkey(3, modifiers); break;
                    
                        case 'T': Debug(2); break;
                        case 'G': Debug(5); break;
                        case 'R': Debug(0); break;
                        case 'E': Debug(3); break;
                        case 'W': Debug(4); break;
                        case 'V': Debug(1); break;
                        case 'Q': Debug(6); break;
                    
                        case 16: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) | 0x80)); break;
                        case 17: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) | 0x40)); break;
                        case 18: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) | 0x10)); break;
                        default : break;
                    }
                }

                public void keyReleased(KeyEvent e) {
                    switch(e.getKeyCode()){
                        case 'Y': rmkey(7); break;
                        case 'K': rmkey(8); break;
                        case 'U': rmkey(9); break;
                        case 'H': rmkey(4); break;
                        case 'L': rmkey(6); break;
                        case 'B': rmkey(1); break;
                        case 'J': rmkey(2); break;
                        case 'N': rmkey(3); break;
                    
                        case 16: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) ^ 0x80)); break;
                        case 17: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) ^ 0x40)); break;
                        case 18: if(moveKeys.size()>0) moveKeys.set(0, (byte)(moveKeys.get(0) ^ 0x10)); break;
                        default :
                            break;
                    }
                }
            };

        console.addKeyListener(keyListener);
        
        panel = new APanel(this);
        
        console.add(panel);
        
        console.paintComponents(console.getGraphics());
        
        panel.repaint();
    }
    
    public void Tick(){
        CurrentTime = System.currentTimeMillis();
        
        if(!moveKeys.isEmpty()){
            if(client.map.IDUnit.get(client.clientid).delay < System.currentTimeMillis()){
                try{
                    byte a = moveKeys.get(0); //debug
                    
                    byte modifiers = (byte)(a & 0xf0);
                    int b = (moveKeys.get(0) & 0xf);
                    //System.out.println(moveKeys.get(0) + " " + modifiers + " " + b);
                    switch(b){
                        case 7: MoveCardinal(Constant.DIRECTIONS.NORTHWEST, modifiers); break;
                        case 8: MoveCardinal(Constant.DIRECTIONS.NORTH, modifiers); break;
                        case 9: MoveCardinal(Constant.DIRECTIONS.NORTHEAST, modifiers); break;
                        case 4: MoveCardinal(Constant.DIRECTIONS.WEST, modifiers); break;
                        case 6: MoveCardinal(Constant.DIRECTIONS.EAST, modifiers); break;
                        case 1: MoveCardinal(Constant.DIRECTIONS.SOUTHWEST, modifiers); break;
                        case 2: MoveCardinal(Constant.DIRECTIONS.SOUTH, modifiers); break;
                        case 3: MoveCardinal(Constant.DIRECTIONS.SOUTHEAST, modifiers); break;
                        default: System.out.println("Error : Tick : Unknown direction " + b); break;
                    }
                }
                catch(IndexOutOfBoundsException e){
                    // expected seldomly
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    //TODO: move to APanel
    public void EditedInanimate() {
        panel.ChangedThings = true;
        if(!panel.drawing){
            panel.repaint();
        }
        else if(!panel.ChangedThings){
            System.out.println("Error - SimpleGUI:EditedAnimate : drawing not synchronized");
            panel.ChangedThings = true;
        }
    }
    public void EditedAnimate() {
        panel.ChangedUnits = true;
        if(!panel.drawing){
            panel.repaint();
        }
    }
    
    public boolean MoveCardinal(Constant.DIRECTIONS direction, byte modifiers){
        if(cursor == null){
            if(moveReady){
                moveReady = false;
                client.SMSG_MoveCardinal(direction);
            }
        }
        //You are targetting something
        else{
            //TODO: something like shift is held (override auto targetting key)
            
            if((modifiers & 0x80) != 0){ //shift
                cursor = (Point)cursor.clone();
                cursorlock = false;
            }
            if(!cursorlock){
                if(cursorTime < System.currentTimeMillis()){
                    cursorTime = System.currentTimeMillis() + cursorSpeed + (((modifiers & 0x40) != 0) ? cursorSlowSpeed : 0);
                    switch(direction){
                        case NORTHWEST: case WEST: case SOUTHWEST: cursor.x--; break;
                        case NORTHEAST: case EAST: case SOUTHEAST: cursor.x++; break;
                    }
                    switch(direction){
                        case NORTHWEST: case NORTH: case NORTHEAST:  cursor.y--; break;
                        case SOUTHWEST: case SOUTH: case SOUTHEAST:  cursor.y++; break;
                    }
                    
                    Thing[] things = client.map.LocationThings(cursor);
                    
                    if(things.length > 0){
                        cursorover = things[0];
                    }
                    else{
                        cursorover = null;
                    }
                    panel.repaint();
                }
            }
            else{
                //Target  
            }
        }
        return true;
    }

    public void MovedSelf(Constant.DIRECTIONS direction){
        Unit unit = client.map.IDUnit.get(client.clientid);
        unit.delay = System.currentTimeMillis() + (long)((Constant.Diagonal(direction) ? 1.414 : 1) * unit.GetTimeCost(Constant.ACTIONS.MOVE) - 20); //replace with latency
        moveReady = true;
    }
    public void HandleRejectMessage(String s){
        moveReady = true;
        System.out.println(s);
    }
    
    public Point Target(Constant.TARGETING targetting){
        Point p;
        switch(targetting){
            case CLOSEST: 
                cursorover = client.map.GetUnits().get(1);
                cursorlock = true;
                p = client.map.GetLocationOfThing(cursorover); break;
            case MANUAL:
                //cursorover = client.map.GetUnits().get(0);
                p = (Point)client.map.GetLocationOfThing(client.map.GetUnits().get(0)).clone(); break;
            default:
                p = (Point)client.map.GetLocationOfThing(client.map.GetUnits().get(1)).clone(); break;
        }
        return p;
    }
    public void TargetOff(){
        cursor = null;
        cursorlock = false;
        cursorover = null;
    }
    
    /**
     *Continues the targetting algorithm, starting at the Point given.
     * @param targetting
     * @return
     */
    public Point Target(Constant.TARGETING targetting, Point p){
        return (Point)p.clone();
    }

    int test = 10;
    public void Debug(int i){
        switch(i)
        {
        case 0:
            thingsimage.getGraphics().setColor(Color.CYAN);
            thingsimage.getGraphics().drawRect(0, 0, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(0, panel.fontsize, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(0, 2*panel.fontsize, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(panel.fontsize, 0, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(panel.fontsize, panel.fontsize, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(panel.fontsize, 2*panel.fontsize, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(2*panel.fontsize, 0, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(2*panel.fontsize, panel.fontsize, panel.fontsize, panel.fontsize);
            thingsimage.getGraphics().drawRect(2*panel.fontsize, 2*panel.fontsize, panel.fontsize, panel.fontsize);
            break;
        case 1:
            client.SMSG_Chat("asdf");
            break;
        case 2:
            if(cursor == null){
                //TODO: add real unit ref
                cursor = Target(Constant.TARGETING.CLOSEST);
            }
            else{
                TargetOff();
            }
            panel.repaint();
            break;
        case 5:
            if(cursor == null){
                cursor = Target(Constant.TARGETING.MANUAL);
            }
            else{
                TargetOff();
            }
            panel.repaint();
            break;
        case 3:
            panel.circletest = !panel.circletest;
            panel.repaint();
            break;
        case 4:
            Point p = client.map.GetLocationOfThing(client.map.GetUnits().get(0));
            for(Point p2 : Constant.CircleOfPoints(p.x, p.y, 
                                                   client.map.GetUnits().get(0).GetStat(Constant.STATS.SIGHT))){
                client.map.FloorTiles.put(p2, 0);
            }
            client.gui.EditedInanimate();
            break;
        case 6:
            System.out.println("Move ready? " + moveReady);
            break;
        }
    }
}