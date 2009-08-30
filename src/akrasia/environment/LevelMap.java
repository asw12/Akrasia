package akrasia.environment;

import akrasia.Constant;

import akrasia.thing.Thing;

import akrasia.thing.unit.Mob;
import akrasia.thing.unit.Unit;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;


/**
 * This map represents the server side map. It essentially controls all the actors.
 */
public class LevelMap
  implements AMap
{
  public LevelMap()
  {
    activeMobs = new TreeMap<Long, List<Mob>>();
    activeMobsHash = new HashSet<Mob>();

    // Generate random room
    (new LevelGenerator(this)).RectangularRoomTest(10, 10, 10, 10);
  }

  public TreeMap<Long, List<Mob>> activeMobs;
  public HashSet<Mob> activeMobsHash;

  HashMap<Thing, Point> ThingLocation = new HashMap<Thing, Point>();
  HashMap<Point, List<Thing>> LocationThings =
    new HashMap<Point, List<Thing>>();

/********************
 * BASIC FUNCTIONALITY
 ********************/

  /**
   *This hash map contains all the floor tile details.
   * Zero+ : Floor
   * Negative : Wall ( values().get(-(i + 1)) )
   */
  public HashMap<Point, Integer> FloorTiles =
    new HashMap<Point, Integer>();

  public Point StartLocation = new Point(4, 4);

  public Point GetLocationOfThing(Thing thing)
  {
    if (ThingLocation.containsKey(thing))
    {
      return ThingLocation.get(thing);
    }
    return null;
  }

  public Thing[] LocationThings(Point p)
  {
    Thing[] things =
      new Thing[LocationThings.get(p) != null? LocationThings.get(p).size():
                0];
    if (things.length > 0)
    {
      synchronized (LocationThings)
      { //debug
        return LocationThings.get(p).toArray(things);
      } // */
      //return LocationThings.get(p).toArray(things);
    }
    else
    {
      return things;
    }
  }

  public boolean PlaceThing(Thing thing, Point point)
  {
    ThingLocation.put(thing, point);

    if (LocationThings.containsKey(point))
    {
      LocationThings.get(point).add(thing);
    }
    else
    {
      ArrayList<Thing> al = new ArrayList<Thing>();
      al.add(thing);
      LocationThings.put(point, al);
    }

    return true;
  }

  public boolean AddThing(Thing thing, Point point, int thingid)
  {
    PlaceThing(thing, point);

    IDThing.put(thingid, thing);

    return true;
  }

  public boolean AddUnit(Unit unit, Point point, int id)
  {
    PlaceThing(unit, point);

    IDUnit.put(id, unit);
    UnitID.put(unit, id);

    return true;
  }

  public boolean MoveThing(Thing thing, Point point)
  {
    boolean flag =
      LocationThings.get(ThingLocation.get(thing)).remove(thing);
    if (flag)
    {
      if (LocationThings.get(ThingLocation.get(thing)).size() == 0)
      {
        LocationThings.remove(ThingLocation.get(thing));
      }
      ThingLocation.remove(thing);
    }

    PlaceThing(thing, point);

    return true;
  }

  public boolean MoveUnit(Unit unit, Point point)
  {
    if (!CanMove(unit, point) || !MoveThing(unit, point))
      return false;

    unit.delay =
        System.currentTimeMillis() + (int) (unit.GetTimeCost(Constant.ACTIONS.MOVE) *
                                            (Constant.Diagonal(Constant.GetDirection(GetLocationOfThing(unit), point))?
                                             1.414: 1));
    return true;
  }

  /**
   *.
   * @param unit
   * @param point
   * @return
   */
  public boolean CanMove(Unit unit, Point point)
  {
    boolean b = true;
    Thing[] things = LocationThings(point);
    for (Thing thing : things)
    {
      if (thing instanceof Unit)
      {
        b = false;
        break;
      }
    }
    return (b &&
            (!FloorTiles.containsKey(point) || FloorTiles.get(point) >= 0));
  }

  public ArrayList<Thing> GetAllThings()
  {
    return new ArrayList<Thing>(ThingLocation.keySet());
  }

  public ArrayList<Thing> GetThings()
  {
    Thing[] clone = new Thing[IDThing.size()];
    IDThing.values().toArray(clone);
    return new ArrayList<Thing>(Arrays.asList(clone));
  }

  public ArrayList<Unit> GetUnits()
  {
    return new ArrayList<Unit>(IDUnit.values());
  }

  public boolean RemoveThing(Thing thing)
  {
    if (thing == null)
    {
      System.out.println("Error - LevelMap:RemoveThing : thing is null");
      return false;
    }

    if (thing instanceof Unit)
    {
      Unit unit = (Unit) thing;
      IDUnit.remove(UnitID.get(unit));
      UnitID.remove(unit);
    }
    else
    {
      IDThing.remove(ThingID.get(thing));
      ThingID.remove(thing);
    }

    //TODO: Caution, ThingLocation can be getting null!
    boolean flag =
      LocationThings.get(ThingLocation.get(thing)).remove(thing);
    if (flag)
    {
      if (LocationThings.get(ThingLocation.get(thing)).size() == 0)
      {
        LocationThings.remove(ThingLocation.get(thing));
      }
      ThingLocation.remove(thing);
    }
    return flag;
  }

/********************
 * MAP <-> SERVER
 ********************/

  public void Tick(long time)
  {
    while (activeMobs.size() > 0 && activeMobs.firstKey() < time)
    {
      synchronized (activeMobs)
      {
        List<Mob> mobs = activeMobs.remove(activeMobs.firstKey());
        for (Mob mob: mobs)
        {
          mob.TickAI(this);
          if (activeMobs.containsKey(mob.delay))
            activeMobs.get(mob.delay).add(mob);
          else
          {
            activeMobs.put(mob.delay, new ArrayList<Mob>());
            activeMobs.get(mob.delay).add(mob);
          }
        }
      }
    }
  }

  
}
