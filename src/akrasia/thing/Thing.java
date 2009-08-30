package akrasia.thing;

public class Thing
{
  public Thing()
  {
  }

  public Thing(int id)
  {
    this.id = id;
  }

  private int map;
  private int id = -1;
  private char c; //0x002E;

  public int GetId()
  {
    return id;
  }

  public int GetMap()
  {
    return map;
  }

  public char GetSymbol()
  {
    return c = '#';
  }
}
