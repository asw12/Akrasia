package akrasia.thing.unit;

import akrasia.Constant;

import akrasia.environment.LevelMap;

import akrasia.network.Server;

import java.awt.Point;

import java.util.ArrayList;

/**
 * A unit that has AI.
 *
 */
public class Mob extends Unit{
    public Mob() {
    }
    public Mob(int i) {
        super(i);
    }
    
    public int entry;
    ArrayList<Integer> players = new ArrayList<Integer>();
    static int ticks = 0;
    enum AITypes{
        Default,
        Melee,
        Ranged,
        Caster,
        Assist
    }
    
    public AITypes ai = AITypes.Default;
    
    public void TickAI(Server server, LevelMap map){
        Point p = map.GetLocationOfThing(this);
        
        Point p2 = null;
        while(p2 == null){
            p2 = map.GetLocationOfThing(server.clients.get(0).controlledUnit);
        }
        if(!server.UnitMove(this, new Point(p.x + (int)Math.signum(p2.x - p.x), p.y + (int)Math.signum(p2.y - p.y)))){
            delay += 500;
        } // */
        /*int dx = 0;
        int dy = 0;
        while(dx == 0 && dy == 0){
            dx = (int)(3.*Math.random()) - 1;
            dy = (int)(3.*Math.random()) - 1;
        }
        if(((p.x + dx < 0 || p.x + dx > 60 || p.y + dy < 0 || p.y + dy > 40)) || !server.UnitMove(this, new Point(p.x + dx, p.y + dy))){
            //TickAI(server, map);
            delay += 10;
        } // */
    }
    public void AddVision(int i){
        players.add(i);
    }
    public void RemoveVision(int i){
        players.remove(i);
    }
    public ArrayList<Integer> SpottedBy(){
        return players;
    }
}