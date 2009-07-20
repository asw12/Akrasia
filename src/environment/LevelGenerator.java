package akrasia.environment;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.ArrayList;

/**
 * This ia a random map generator.
 */
public class LevelGenerator {
    public LevelGenerator(LevelMap m, int x, int w, int y, int h) {
        map = m;
        x_offset = x;
        y_offset = y;
        width = w;
        height = h;
        
        Generate();
    }
    
    int x_offset;
        int width;
    int y_offset;
        int height;
    
    LevelMap map;
    
    ArrayList<Rectangle> rooms = new ArrayList<Rectangle>();

    private void Generate() {
        for(int i = 0; i < 10; i++){
            
        }
    }
    
    public void RectangularRoomTest(int x, int y, int w, int h){
        ArrayList<Point> points = new ArrayList<Point>();
        for(int xx = x; xx <= xx+w; xx++){
            for(int yy = y; yy <= yy+h; yy++){
                points.add(new Point(xx,yy));
            }
        }
        
        for(Point p : points){
            if(map.FloorTiles.containsKey(p)){
                System.out.println("Intersection");
            }
            else{
                map.FloorTiles.put(p, -1);
            }
            
        }
    }
}
