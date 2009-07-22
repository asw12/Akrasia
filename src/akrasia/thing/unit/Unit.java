package akrasia.thing.unit;

import akrasia.Constant;

import akrasia.thing.Thing;

public abstract class Unit extends Thing{
    public Unit(){
        super(0);
        
        //TODO: fill with real data
        for(int i = 0; i < Constant.STATS.values().length; i++){
            stats[i] = Constant.STATS.values()[i].min;
        }
    }
    public Unit(int i){
        super(i);
        
        for(int n = 0; n < Constant.STATS.values().length; n++){
            stats[n] = Constant.STATS.values()[n].min;
        }
    }
    
    ///public int id = 0;
    public int id;
    public int[] stats = new int[Constant.STATS.values().length];
    
    char c = '@';
    
    public char GetSymbol(){
        return c;
    }
    
    public long delay = 0;
    
    public int GetTimeCost(Constant.ACTIONS action){
        return Math.max(10, 10); //actions should be capped for monsters
    }
    
    public int GetStat(Constant.STATS stat){
        //System.out.println(stat.toString() + " " + stats[stat.ordinal()]);
        return stats[stat.ordinal()];
    }
}