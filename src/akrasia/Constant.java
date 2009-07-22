package akrasia;

import akrasia.environment.RayCastFOV;

import java.awt.Graphics2D;
import java.awt.Point;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Contains final vars or shared procedures.
 *
 */
public class Constant {
    //Debug constant (for quick access)
    public final static int sleeptime = 20;
    
    public enum DIRECTIONS{ SOUTHWEST, SOUTH, SOUTHEAST,
                            WEST,      NONE,  EAST,
                            NORTHWEST, NORTH, NORTHEAST };
    public static boolean Diagonal(DIRECTIONS d){
        return d.ordinal()%2 == 0 && d.ordinal() != 4;
    }
    public static DIRECTIONS GetDirection(Point p1, Point p2){
        if(p1.x - p2.x != 0){
            if(p1.y - p2.y != 0){
                return p2.y - p1.y > 0 ? (p2.x - p1.x > 0 ? DIRECTIONS.SOUTHEAST : DIRECTIONS.SOUTHWEST)
                    : (p2.x - p1.x > 0 ? DIRECTIONS.NORTHEAST : DIRECTIONS.NORTHWEST);
            }
            else{
                return p2.x - p1.x > 0 ? DIRECTIONS.EAST : DIRECTIONS.WEST;
            }
        }
        else{
            if(p1.y - p2.y != 0){
                return p2.y - p1.y > 0 ? DIRECTIONS.SOUTH : DIRECTIONS.NORTH;
            }
            else{
                return DIRECTIONS.NONE;
            }
        }
    }
    
    /****************
     * Message Passing
     ****************/
    
    public static final byte RequiresThing =                      0x01;
    public static final byte RequiresThing2 =                     0x02;
    public static final byte RequiresDestination =                0x04;
    public static final byte RequiresString =                     0x08;
    public static final byte RequiresString2 =                    0x10;
    public static final byte RequiresOption =                     0x20;
    public static final byte RequiresOption2 =                    0x40;
    
    public enum OPCODES{
        //client to server or both
        MOVECARDINAL  {public byte getLogic(){return RequiresThing2 + RequiresOption;}},
        MOVE          {public byte getLogic(){return RequiresThing2 + RequiresDestination;}},
        START_CAST    {public byte getLogic(){return RequiresThing2 + RequiresOption;}}, 
        STOP_CAST     {public byte getLogic(){return 0;}}, 
        TALK          {public byte getLogic(){return RequiresThing2 + RequiresThing;}},  
        USEITEM       {public byte getLogic(){return RequiresOption;}}, 
        USEGOBJECT    {public byte getLogic(){return RequiresThing;}}, 
        EQUIP         {public byte getLogic(){return RequiresOption2 + RequiresOption;}}, //slot and item
        UNEQUIP       {public byte getLogic(){return RequiresOption;}}, 
        SKILL         {public byte getLogic(){return RequiresOption;}},
        DROP          {public byte getLogic(){return RequiresOption;}},   
        THROW         {public byte getLogic(){return RequiresOption + RequiresDestination;}},   
        
        JOIN          {public byte getLogic(){return RequiresString + RequiresString2;}},
        CHAT          {public byte getLogic(){return RequiresString;}},
        
        //server to client
        ACKNOWLEDGEJOIN {public byte getLogic(){return RequiresThing + RequiresDestination + RequiresOption;}},
        CONFIRM         {public byte getLogic(){return 0;}},
        REJECT          {public byte getLogic(){return RequiresString;}},
        APPEAR          {public byte getLogic(){return RequiresThing + RequiresDestination + RequiresOption;}},
        WALLAPPEAR      {public byte getLogic(){return RequiresThing + RequiresDestination;}},
        UNITAPPEAR      {public byte getLogic(){return RequiresThing + RequiresDestination + RequiresOption;}},
        REVEAL          {public byte getLogic(){return RequiresDestination + RequiresOption;}},
        DISAPPEAR       {public byte getLogic(){return RequiresThing;}},
        ADDSSTATUS      {public byte getLogic(){return RequiresThing;}}, 
        REMOVESTATUS    {public byte getLogic(){return RequiresThing;}};
         
        public abstract byte getLogic();
    };
    
    public enum ACTIONS{
        MOVE,
        REST,
        CAST;
    }
    
    public enum STATUS{  };
    
    public enum TARGETING{
        MANUAL,
        CLOSEST,
        FURTHEST,
        ASSIST,
        CLUSTER;
    }
    
    public enum ANIMATIONS{   }
    
    public enum STATS{
        SIGHT(12, 20),
        RANGE(0, 9);
        // should be handled by db?
        public int min;
        public int max;
        private STATS(int min, int max){
            this.min = min;
            this.max = max;
        }
    }
    
    /**
     * This class represents an object in the cell that is part of the structure of teh dungeon.
     * 
     * Structure of the dungeon: Wall or Floor.
     */
    public static class STRUCT{
        public enum FLOOR{
            NORMAL,
            WATER_S,
            WATER_D,
            PIT,
            ICE,
            LAVA,
            NONE;
            public boolean HasSpell(){
                return false;
            }
        }
        public enum WALL{
            ROCK,
            METAL;
        }
    }
    
    public static enum THINGTYPE{ //rename when used
        Potion,
        Weapon;
    }
    
    public static String PointToString(Point p){
        return p.x + "," + p.y;
    }
    public static Point StringToPoint(String s){
        String[] ss = s.split(",");
        return new Point(Integer.valueOf(ss[0]), Integer.valueOf(ss[1]));
    }
    
    /****************
     * Vision Methods
     ****************/
    
    /**
     *
     * @param x
     * @param y
     * @param radius
     * @return
     */
    public static ArrayList<Point> CircleOfPoints(int x, int y, int radius){
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(x, y));
        
        if(radius == 0){
            return points;
        }
        int r2 = radius*radius;
        
        for(int x2 = x-radius; x2 <= x+radius; x2++){
            int x22 = Math.max(Math.abs(x2-x) - 1, 0);
            x22 *= x22;
            
            for(int y2 = y - radius; y2 <= y + radius; y2++){
                int y22 = Math.max(Math.abs(y2-y) - 1, 0);
                y22 *= y22;
                
                int r3 = x22 + y22;
                
                int diff = Math.max(r2 - r3, 0);
                
                if(diff != 0){
                    points.add(new Point(x2, y2));
                }
            }
        }
        return points;
    }
    /**
     * Bresenham Line Algorithm
     * One on roguelike does not need hackish dx == 0 && dy == 0 check?
     * @param x0
     * @param x1
     * @param y0
     * @param y1
     * @return
     */
    public static ArrayList<Point> LineOfPoints(int x0, int x1, int y0, int y1){
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(x0, y0));
        
        int dx = x1 - x0;
        int dy = y1 - y0;
        if(dx == 0 && dy == 0){
            return points;
        }
        if(Math.abs(dx) >= Math.abs(dy)){
            int error = Math.abs(dy) + (Math.abs(dx) >> 1);
            for(; Math.signum(dx) * Math.signum(x1 - x0) >= 0; x0 += Math.signum(dx)){
                error -= Math.abs(dy);
                if(error < 0){
                    y0 += Math.signum(dy);
                    error += Math.abs(dx);
                }
                points.add(new Point(x0, y0));
            }
        }
        else{
            int error = Math.abs(dx) + (Math.abs(dy) >> 1);
            for(; Math.signum(dy) * Math.signum(y1 - y0) >= 0; y0 += Math.signum(dy)){
                error -= Math.abs(dx);
                if(error < 0){
                    x0 += Math.signum(dx);
                    error += Math.abs(dy);
                }
                points.add(new Point(x0, y0));
            }
        }
        
        return points;
    }
    public static ArrayList<Point> TolerantLineOfPoints(HashMap<Point, Integer> walls, int x0, int x1, int y0, int y1){
        ArrayList<Point> points = new ArrayList<Point>();
        
        int dx = x1 - x0;
        int dy = y1 - y0;
        if(dx == 0 && dy == 0){
            points.add(new Point(x0, y0));
            return points;
        }
        if(Math.abs(dx) >= Math.abs(dy)){
            int error = Math.abs(dy) + (Math.abs(dx) >> 1);
                for(; Math.signum(dx) * Math.signum(x1 - x0) >= 0; x0 += Math.signum(dx)){
                    error -= Math.abs(dy);
                    if(error < 0){
                        y0 += Math.signum(dy);
                        error += Math.abs(dx);
                    }
                    points.add(new Point(x0, y0));
                }
        }
        else{
            int error = Math.abs(dx) + (Math.abs(dy) >> 1);
                for(; Math.signum(dy) * Math.signum(y1 - y0) >= 0; y0 += Math.signum(dy)){
                    error -= Math.abs(dx);
                    if(error < 0){
                        x0 += Math.signum(dx);
                        error += Math.abs(dy);
                    }
                    points.add(new Point(x0, y0));
                }
        }
        
        return points;
    }
    public static ArrayList<Point> FieldOfView(HashMap<Point, Integer> walls, int x, int y, int radius){
        RayCastFOV raycast = new RayCastFOV(walls, x, y, radius);
        return raycast.castRays();
    }
}