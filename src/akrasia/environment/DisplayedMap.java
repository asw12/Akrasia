package akrasia.environment;

import akrasia.thing.Thing;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This is the restricted map that the client sees.
 */
public class DisplayedMap extends LevelMap{
    public int GetFloorTile(Point p){
        if(FloorTiles.containsKey(p)){
            return FloorTiles.get(p);
        }
        return 0;
    }

    //TODO: Fix potential concurrent mod error
    public HashMap<Point, Integer> GetAllFloorTiles(){
        synchronized(FloorTiles){
            return (HashMap<Point, Integer>)FloorTiles.clone();
        }
    }

    public boolean MoveThing(Thing thing, Point point, Point oldpoint){
        boolean flag = LocationThings.get(ThingLocation.get(thing)).remove(thing);
        if(flag){
            if(LocationThings.get(ThingLocation.get(thing)).size() == 0){
                LocationThings.remove(ThingLocation.get(thing));
            }
            ThingLocation.remove(thing);
        }

        oldpoint.x = point.x; oldpoint.y = point.y;

        PlaceThing(thing, oldpoint);

        return true;
    }
}
