package akrasia.network;

import akrasia.Constant;

import akrasia.environment.LevelMap;

import akrasia.environment.RayCastFOV;

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
import java.net.ServerSocket;
import java.net.Socket;

import java.net.SocketTimeoutException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import save.Database;

public class Server
  extends Thread
{
  public static void main(String[] args)
  {
    /*
         * 1) Starts with a SimpleGUI
         * 2) SimpleGUI joins server
         * 3) SimpleGUI changed to in-game mode
         */

    SimpleGUI simple = new SimpleGUI();
    simple.AddClient(new Client(simple));

    /* // Uncomment to run two servers
        SimpleGUI simple2 = new SimpleGUI();
        simple2.AddClient(new Client(simple2)); // */

    Server server = new Server(6112);

    // Blank window will appear if no server can be found
    try
    {
      simple.client.JoinServer(InetAddress.getLocalHost(), 6112);
      //Thread.sleep(1000);
      //simple2.client.JoinServer(InetAddress.getLocalHost(), 6112);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  ServerSocket socket;

  Database AkrasiaDB;
  ArrayList<ServerClient> connections;
  ArrayList<PseudoClient> clients;
  ArrayList<HashMap<Point, Integer>> clientfloormaps; // TODO: move this to LevelMap
  ArrayList<LevelMap> maps = new ArrayList<LevelMap>();
  int joinMap = 0; // map that players join at
  HashSet<LevelMap> activeMaps = new HashSet<LevelMap>();

  Long currentTime;

  /********************
   * Basic Network Functions
   ********************/

  public Server(int port)
  {
    AkrasiaDB = new Database();

    clients = new ArrayList<PseudoClient>();
    connections = new ArrayList<ServerClient>();
    clientfloormaps = new ArrayList<HashMap<Point, Integer>>();
    maps.add(new LevelMap());

    try
    {
      socket = new ServerSocket(port)
        {
        };
      socket.setSoTimeout(1000);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    start();
  }

  public void run()
  {
    Thread socketThread = new Thread()
    {
      Server server = Server.this;

      public void run()
      {
        while (!socket.isClosed())
        {
          try
          {
            Socket newSocket;
            newSocket = socket.accept();
            if (newSocket != null)
            {
              ServerClient newServerClient =
                new ServerClient(newSocket, server, connections.size());
              newServerClient.start();
              connections.add(newServerClient);
            }
          }
          catch (SocketTimeoutException e)
          {
            //ignore
          }
          catch (Exception e)
          {
          }
        }

        System.out.println("Socket Closed");
      }
    };
    socketThread.start();

    while (connections.size() == 0)
    {
      try
      {
        sleep(100);
      }
      catch (InterruptedException e)
      {

      }
    }

    // TODO: Turn this into an in-game option
    // try{socket.close();} catch(Exception e){ e.printStackTrace(); }

    while (Tick(System.currentTimeMillis()))
    {
      try
      {
        sleep(20);
      }
      catch (InterruptedException e)
      {

      }
    }
  }

  public void AddClient(PseudoClient client)
  {
    clients.add(client);
    clientfloormaps.add(new HashMap<Point, Integer>());
  }

  /**
   * This class handles each connection between server and client.
   */
  class ServerClient
    extends Thread
  {
    ServerClient(Socket socket, Server server, int clientid)
    {
      this.clientid = clientid;
      this.socket = socket;
      this.server = server;
      try
      {
        output =
            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        input =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    int clientid;

    Server server;
    Socket socket;
    BufferedWriter output;
    BufferedReader input;

    public void run()
    {
      try
      {
        long diff = System.currentTimeMillis();
        while (socket.isConnected())
        {
          if (input.ready())
          {
            server.ProcessMsg(clientid, input.readLine());
          }
          else
          {
            int i =
              Constant.sleeptime - (int) (System.currentTimeMillis() -
                                          diff);
            sleep(i > 0? i: Constant.sleeptime);
            diff = System.currentTimeMillis();
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  /********************
   * Message Passing System and Opcodes
   ********************/

  void ProcessMsg(int c, String str)
  {
    currentTime = System.currentTimeMillis();

    String[] strs = str.split(":");
    Constant.OPCODES opcode =
      Constant.OPCODES.values()[Integer.valueOf(strs[0])];

    switch (opcode)
    {
      case MOVECARDINAL:
        RMSG_MoveCardinal(c, strs);
        break;
      case MOVE:
        RMSG_Move(c, strs);
        break;
      case JOIN:
        RMSG_Join(c, strs);
        break;
      case CHAT:
        RMSG_Chat(c, strs);
        break;
      default:
        System.out.println("Error - Server:ProcessMsg : " +
                           opcode.toString() + " from " + c);
        break;
    }
  }

  void SendMsg(int client, Constant.OPCODES opcode, String[] options)
  {
    String str = MakeMsg(opcode, options);

    try
    {
      connections.get(client).output.write(str + "\n");
      //            connections.get(client).output.newLine();
      connections.get(client).output.flush();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  void RMSG_Move(int c, String[] strs)
  {
    /*Point p = map.GetLocationOfThing(map.IDUnit.get(c));
        if(map.CanMove(clients.get(c).controlledUnit, p)){

        }
        else{
            if(false){
                //attack
            }
            else{
                SMSG_Reject(0, "YOU FOOL!");
            }
        }*/
  }

  void RMSG_MoveCardinal(int c, String[] strs)
  {
    Unit unit = clients.get(c).controlledUnit;
    if (unit.delay > currentTime)
    {
      SMSG_Reject(c,
                  "Error - RMSG_MoveCardinal - client cannot move yet!"); //TODO: return error;
      return;
    }

    LevelMap map = maps.get(unit.GetMap());

    int x = map.GetLocationOfThing(unit).x;
    int y = map.GetLocationOfThing(unit).y;

    switch (Constant.DIRECTIONS.values()[Integer.valueOf(strs[2])])
    {
      case NORTH:
        y--; break;
      case EAST:
        x++; break;
      case SOUTH:
        y++; break;
      case WEST:
        x--; break;
      case NORTHEAST:
        x++; y--; break;
      case SOUTHEAST:
        x++; y++; break;
      case SOUTHWEST:
        x--; y++; break;
      case NORTHWEST:
        x--; y--; break;
    }

    Point p = new Point(x, y);

    if (map.MoveUnit(unit, p))
    {
      SMSG_Move(c, unit.GetId(), p);
      Reveal(c);
    }
    else
      SMSG_Reject(c, "can't move there");
  }

  void RMSG_Join(int c, String[] strs)
  {
    AddClient(new PseudoClient());
    LevelMap map = maps.get(joinMap);
    activeMaps.add(map);
    Point p = map.StartLocation;

    //TODO: Debug walls
    /*for(int i = 0; i < 100; i++){
            CreateWall(new Point((int)(Math.random()*60), (int)(Math.random()*60)), Constant.STRUCT.WALL.ROCK);
        } // */

    map.AddUnit(clients.get(c).controlledUnit, p, c);
    SMSG_AcknowledgeJoin(clients.get(c).controlledUnit.GetId(), new Mob(), p,
                         c);

    /*for(int i = 0; i < 10; i++){
            Point pp = new Point((int)(Math.random() * 40), (int)(Math.random() * 40));
            while(map.LocationThings(pp).length!=0){
                pp = new Point((int)(Math.random() * 40), (int)(Math.random() * 40));
            }
            map.AddUnit(new Mob(1), pp, 10 + i);
        }*/

    Reveal(c);
  }

  private void RMSG_Chat(int c, String[] strs)
  {

  }

  private void SMSG_AcknowledgeJoin(int c, Thing thing, Point p,
                                    int thingid)
  {
    SendMsg(c, Constant.OPCODES.ACKNOWLEDGEJOIN, new String[]
        { String.valueOf(thing.GetId()), Constant.PointToString(p),
          String.valueOf(thingid) });
  }

  private void SMSG_Reveal(int c, Point p, int radius)
  {
    SendMsg(c, Constant.OPCODES.REVEAL, new String[]
        { Constant.PointToString(p), String.valueOf(radius) });
  }

  private void SMSG_Appear(int c, Thing thing, Point p, int thingid)
  {
    SendMsg(c, Constant.OPCODES.APPEAR, new String[]
        { String.valueOf(thing.GetId()), Constant.PointToString(p),
          String.valueOf(thingid) });
  }

  private void SMSG_WallAppear(int c, int wall, Point p)
  {
    SendMsg(c, Constant.OPCODES.WALLAPPEAR, new String[]
        { String.valueOf(wall), Constant.PointToString(p) });
  }

  private void SMSG_UnitAppear(int c, Unit unit, Point p, int unitid)
  {
    SendMsg(c, Constant.OPCODES.UNITAPPEAR, new String[]
        { String.valueOf(unit.GetId()), Constant.PointToString(p),
          String.valueOf(unitid) });
  }

  private void SMSG_Move(int c, int i, Point p)
  {
    SendMsg(c, Constant.OPCODES.MOVE, new String[]
        { String.valueOf(i), Constant.PointToString(p) });
  }

  private void SMSG_Confirm(int c)
  {
    SendMsg(c, Constant.OPCODES.CONFIRM, new String[]
        { });
  }

  private void SMSG_Reject(int c, String str)
  {
    SendMsg(c, Constant.OPCODES.REJECT, new String[]
        { str });
  }

  private String MakeMsg(Constant.OPCODES opcode, String[] options)
  {
    String s = String.valueOf(opcode.ordinal());
    int o = 0;
    int logic = opcode.getLogic();

    while (logic != 0)
    {
      if ((logic & 1) != 0)
        o++;
      logic >>= 1;
    }

    if (options.length != o)
    {
      System.out.println("Error - MakeMsg : number of options provided : " +
                         opcode.toString());
    }

    for (int i = 0; i < o; i++)
    {
      s += ":" + options[i];
    }

    return s;
  }

  /**
   *Reveals a radius around the a point for the client. The client automatically fills the circle
   * as empty tiles. All objects and special tiles must be revealed here.
   * @param c The client's id
   */
  private void Reveal(int c)
  {
    Unit controlled = clients.get(c).controlledUnit;
    LevelMap map = maps.get(controlled.GetMap());
    Point p = map.GetLocationOfThing(controlled);
    int radius = controlled.GetStat(Constant.STATS.SIGHT);
    controlled = null;
    RayCastFOV raycast = new RayCastFOV(map.FloorTiles, p.x, p.y, radius);
    for (Point p2: raycast.castRays())
    {
      //List<Thing> ls = map.LocationThings(p2);
      Thing[] ls = map.LocationThings(p2);
      //if(ls != null){
      for (Thing thing: ls)
      {
        if (thing instanceof Mob)
        {
          Mob mob = (Mob) thing;
          SMSG_UnitAppear(c, mob, map.GetLocationOfThing(mob),
                          map.UnitID.get(mob));
          synchronized (map.activeMobs)
          {
            if (!map.activeMobsHash.contains(mob))
            {
              mob.delay = System.currentTimeMillis();
              if (map.activeMobs.containsKey(mob.delay))
              {
                map.activeMobs.get(mob.delay).add(mob);
              }
              else
              {
                ArrayList<Mob> list = new ArrayList<Mob>();
                list.add(mob);
                map.activeMobs.put(mob.delay, list);
              }
              map.activeMobsHash.add(mob);
            }
          }
          mob.AddVision(c);
        }
        else
        {
          SMSG_Appear(c, thing, map.GetLocationOfThing(thing),
                      map.ThingID.get(thing));
        }
      }
      //}
    }

    for (Point p2: raycast.getWalls())
    {
      clientfloormaps.get(c).put(p2, map.FloorTiles.get(p2));
      SMSG_WallAppear(c, Constant.STRUCT.WALL.ROCK.ordinal(), p2);
    }
    SMSG_Reveal(c, p, radius);
  }

  /**
   * @param time The distance elapsed since the last tick
   * @return False if the server has stopped.
   */
  private boolean Tick(long time)
  {
    for(LevelMap map : activeMaps)
      map.Tick(time);

    return true;
  }
}
