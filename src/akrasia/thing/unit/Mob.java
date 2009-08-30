package akrasia.thing.unit;

import akrasia.Constant;

import akrasia.environment.LevelMap;

import java.awt.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *A unit that has AI.
 *
 */
public class Mob
  extends Unit
{
  public Mob()
  {
  }

  public Mob(int i)
  {
    super(i);
  }

  public int entry;
  ArrayList<Integer> players = new ArrayList<Integer>();

  // represents the reactions of this mob to other units
  HashMap<Integer, Unit> attitudeList = new HashMap<Integer, Unit>();

  enum AITypes
  {
    Default,
    Melee,
    Ranged,
    Caster,
    Assist;
  }

  public AITypes ai = AITypes.Default;

  public void TickAI(LevelMap map)
  {
    Point p = map.GetLocationOfThing(this);
    Point p2 = null;

    // TODO: do something ai-ey
    Iterator<Map.Entry<Integer, Unit>> i =
      attitudeList.entrySet().iterator();
    while (i.hasNext())
    {
      Map.Entry<Integer, Unit> e =
        i.next(); // guaranteed by iter to be not null.
      p2 = map.GetLocationOfThing(e.getValue());
    }

    if (p2 != null)
      map.MoveUnit(this,
                   new Point(p.x + (int) Math.signum(p2.x - p.x), p.y +
                             (int) Math.signum(p2.y - p.y)));
  }

  public void AddVision(int i)
  {
    players.add(i);
  }

  public void RemoveVision(int i)
  {
    players.remove(i);
  }

  public ArrayList<Integer> SpottedBy()
  {
    return players;
  }
}
