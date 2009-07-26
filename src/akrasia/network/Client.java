package akrasia.network;

import akrasia.Constant;

import akrasia.environment.DisplayedMap;

import akrasia.graphics.GUI;

import akrasia.graphics.SimpleGUI;

import akrasia.thing.Thing;
import akrasia.thing.unit.Mob;
import akrasia.thing.unit.Player;
import akrasia.thing.unit.Unit;

import java.awt.Point;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.InetAddress;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Client extends PseudoClient{
    public Client(GUI gui) {
        this.gui = gui;
    }

    public GUI gui;

    //Server server;
        int port = 6112;
        Socket socket;
            BufferedWriter output;
            BufferedReader input;

            Deque<String> outputMsgs = new LinkedBlockingDeque<String>();

    long CurrentTime;

    String name = "";
    public DisplayedMap map;

    Point cursor;

    public int clientid;
    long debug;

    public void run(){
        CurrentTime = System.currentTimeMillis();

        try{
            while(socket.isConnected()){
                gui.Tick();

                if(input.ready()){
                    ProcessMsg(input.readLine());
                }

                if(!outputMsgs.isEmpty()){
                    //output.write(outputMsgs.pollFirst());

                    String str = outputMsgs.pollFirst();
                    if(str.charAt(0) == '0'){ debug = System.currentTimeMillis(); }
                    output.write(str);

                    output.newLine();
                    output.flush();
                }

                int diff = Math.min(Constant.sleeptime, (int)(System.currentTimeMillis()-CurrentTime));

                if(!input.ready() && outputMsgs.isEmpty()){
                    sleep(Constant.sleeptime - diff);
                }

                CurrentTime = System.currentTimeMillis();
            }
        }
        catch(Exception e ){
            e.printStackTrace();
        }
    }

    public void JoinServer(InetAddress address, int port){
        this.port = port;
        try{
            socket = new Socket(address, port);

            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        map = new DisplayedMap();

        gui.Initialize();

        SMSG_Join();
        start();
    }

    public void ReceiveMsg(String str){
        ProcessMsg(str);
    }

    void ProcessMsg(String str){
        String[] strs = str.split(":");
        Constant.OPCODES opcode;

        try{
            opcode = Constant.OPCODES.values()[Integer.valueOf(strs[0])];
        }
        catch(NumberFormatException e){
            System.out.println(str);
            e.printStackTrace();

            opcode = Constant.OPCODES.values()[Integer.valueOf(strs[0])];
        }

        switch(opcode){
            case APPEAR : RMSG_Appear(Integer.valueOf(strs[1]), Constant.StringToPoint(strs[2]), Integer.valueOf(strs[3])); break;
            case WALLAPPEAR : RMSG_WallAppear(Integer.valueOf(strs[1]), Constant.StringToPoint(strs[2])); break;
            case UNITAPPEAR : RMSG_UnitAppear(Integer.valueOf(strs[1]), Constant.StringToPoint(strs[2]), Integer.valueOf(strs[3])); break;
            case DISAPPEAR : RMSG_Disappear(Integer.valueOf(strs[1])); break;
            case MOVE : RMSG_Move(Integer.valueOf(strs[1]), Constant.StringToPoint(strs[2])); break;
            case ACKNOWLEDGEJOIN : RMSG_AcknowledgeJoin(Integer.valueOf(strs[1]), Constant.StringToPoint(strs[2]), Integer.valueOf(strs[3])); break;
            case REVEAL : RMSG_Reveal(Constant.StringToPoint(strs[1]), Integer.valueOf(strs[2])); break;
            case CONFIRM : RMSG_Confirm(); break;
            case REJECT : RMSG_Reject(strs[1]); break;
            default : System.out.println("Error - Client:ProcessMsg : " + opcode.toString()); break;
        }
    }

    void SendMsg(Constant.OPCODES opcode, String[] options){
        outputMsgs.addLast(MakeMsg(opcode, options));
    }

    private String MakeMsg(Constant.OPCODES opcode, String[] options){
        String s = String.valueOf(opcode.ordinal());
        int o = 0;
        int logic = opcode.getLogic();

        while(logic!=0){
            if((logic & 1) != 0)
                o++;
            logic >>= 1;
        }

        if(options.length != o){
            System.out.println("Error - MakeMsg : number of options provided");
        }

        for(int i = 0; i< o; i++){
            s+=":" + options[i];
        }

        return s;
    }

    public void SMSG_Join(){
        SendMsg(Constant.OPCODES.JOIN, new String[]{ "hi" , "hi" });
    }

    public void SMSG_MoveCardinal(Constant.DIRECTIONS direction){
        SendMsg(Constant.OPCODES.MOVECARDINAL, new String[]{ "-1", String.valueOf(direction.ordinal()) });
    }

    public void SMSG_Chat(String str){
        SendMsg(Constant.OPCODES.CHAT, new String[]{str});
    }

    /**
     * Process the message received when another client joins.
     */
    public void RMSG_Join(){

    }
    /**
     * Process the message received when this client joins.
     */
    public void RMSG_AcknowledgeJoin(int i, Point p, int ClientID){
        Player unit = new Player(i);
        map.AddUnit(unit, p, ClientID); //TODO: replace how players are added? maybe change how ids are used?
        clientid = ClientID;

        gui.EditedAnimate();
    }

    /**
     * Adds a blank floor to indicate a place has been uncovered. More things can appear on top of this floor.
     * @param p
     * @param r
     */
    public void RMSG_Reveal(Point p, int r){
        //TODO: REMOVE DEBUG
        /*ArrayList<Point> debug = new ArrayList<Point>();
        for(Point p2 : map.FloorTiles.keySet()){
            if(map.FloorTiles.get(p2) == 0){
                debug.add(p2);
            }
        }
        try{
            while(((SimpleGUI)gui).panel.drawing){
                Thread.currentThread().sleep(5);
            }
        }
        catch(Exception e){}
        for(Point p2 : debug){map.FloorTiles.remove(p2); } debug.clear(); // */

        for(Point p2 : Constant.FieldOfView(map.FloorTiles, p.x, p.y, r)){
            if(!map.FloorTiles.containsKey(p2)){
                synchronized(map.FloorTiles){
                    map.FloorTiles.put(p2, 0);
                }
            }
        }

        gui.EditedInanimate();
    }
    public void RMSG_Disappear(int i){
        if(map.IDThing.containsKey(i)){
            Thing thing = map.IDThing.get(i);
            map.RemoveThing(thing);
        }
        gui.EditedInanimate();
    }
    public void RMSG_UnitDisappear(int i){
        if(map.IDUnit.containsKey(i)){
            Unit unit = map.IDUnit.get(i);
            map.RemoveThing(unit);
        }
        //gui.EditedInanimate();
    }
    public void RMSG_Appear(int i, Point p, int thingid){
        Thing thing;
        if(map.IDThing.containsKey(thingid)){
            thing = map.IDThing.get(thingid);
        }
        else{
            thing = new Thing(i);
        }
        map.AddThing(thing, p, thingid);
        gui.EditedInanimate();
    }
    private void RMSG_WallAppear(int i, Point point) {
        synchronized(map.FloorTiles){
            map.FloorTiles.put(point, -i - 1);
        }
    }
    public void RMSG_UnitAppear(int i, Point p, int uniqueid){
        Unit unit;
        if(map.IDUnit.containsKey(uniqueid)){
            unit = map.IDUnit.get(uniqueid);
        }
        else{
            unit = new Mob(i);
        }
        map.AddUnit(unit, p, uniqueid);
        gui.EditedAnimate();
    }
    public void RMSG_Move(int i, Point p){
        Unit unit = map.IDUnit.get(i);

        Point oldp = map.GetLocationOfThing(unit);

        if(i == clientid){
            map.MoveThing(unit, p);
            gui.MovedSelf(Constant.GetDirection(oldp, p));
        }
        else{
            map.MoveThing(unit, p, oldp);
        }

        gui.EditedAnimate();


        /*Unit unit = map.IDUnit.get(i);

        Point oldp = map.GetLocationOfThing(unit);

        map.MoveThing(unit, p);

        if(i == clientid){
            gui.MovedSelf(Constant.GetDirection(oldp, p));
        }

        gui.EditedAnimate();*/
    }

    private void RMSG_Reject(String s) {
        gui.HandleRejectMessage(s);
    }
    private void RMSG_Confirm() {
    }
}