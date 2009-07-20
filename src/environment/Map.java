package akrasia.environment;

import akrasia.thing.Thing;

import akrasia.thing.Unit.Unit;

import java.awt.Point;

import java.util.HashMap;
import java.util.List;

/**
 * This is an interface of maps that is shared between the version the client views and what the
 * server uses to store.
 */
public interface Map {
    public HashMap<Integer, Unit> IDUnit = new HashMap<Integer, Unit>();
    public HashMap<Integer, Thing> IDThing = new HashMap<Integer, Thing>();
    public HashMap<Unit, Integer> UnitID = new HashMap<Unit, Integer>();
    public HashMap<Thing, Integer> ThingID = new HashMap<Thing, Integer>();
    
    public boolean MoveThing(Thing thing, Point point);
    
    public boolean CanMove(Unit unit, Point point);
    
    public boolean AddThing(Thing thing, Point point, int thingid);
    
    public boolean AddUnit(Unit unit, Point point, int id);
    
    public boolean RemoveThing(Thing thing);
    
    public Point GetLocationOfThing(Thing thing);
    
    public Thing[] LocationThings(Point point);
    
    public int height = 1000;
    public int width = 1000;
}
