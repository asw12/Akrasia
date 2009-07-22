package akrasia.graphics;

import akrasia.Constant;

import akrasia.thing.Thing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import java.util.Set;

import javax.swing.JPanel;

/**
 * A panel for displaying the main screen of Akrasia
 */
public class APanel extends JPanel {
    public APanel(SimpleGUI gui) {
        this.gui = gui;
    }
    
    int fontsize = 12;
    SimpleGUI gui;
    public boolean drawing = false;
    boolean circletest = false;//TODO: temp
    boolean ChangedThings = true;
    boolean ChangedUnits = true;
    //public Font font = new Font("Arial", Font.CENTER_BASELINE, fontsize);
    public Font font = new Font("Courier New", Font.CENTER_BASELINE, fontsize);
    
    public void paint(Graphics g){
        drawing = true;
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gui.dimensions.width, gui.dimensions.height);
        g2.setClip(0, 0, 600, 500);
        
        if(ChangedThings){
            ChangedThings = false;
            gui.thingsimage = new BufferedImage(gui.dimensions.width, gui.dimensions.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g3 = gui.thingsimage.createGraphics();
            g3.setFont(font);
            for(Thing t : gui.client.map.GetThings()){
                Point p = gui.client.map.GetLocationOfThing(t);
                if(p!=null){
                    g3.drawChars(new char[]{t.GetSymbol()}, 0, 1, fontsize*p.x + (int)((fontsize - g3.getFontMetrics().charWidth(t.GetSymbol()))/2 + 1), fontsize*(p.y+1) - 1);
                }
            }
            
            for(Point p : gui.client.map.GetAllFloorTiles().keySet()){
                int index = gui.client.map.FloorTiles.get(p);
                if(index < 0){
                    g3.setColor(Color.WHITE);
                    g3.drawChars(new char[]{'#'}, 0, 1, fontsize*p.x + (int)((fontsize - g3.getFontMetrics().charWidth('.'))/2 + 1), fontsize*(p.y+1) - 1);
                }
                else{
                    g3.setColor(Color.WHITE);
                    g3.drawChars(new char[]{'.'}, 0, 1, fontsize*p.x + (int)((fontsize - g3.getFontMetrics().charWidth('.'))/2 + 1), fontsize*(p.y+1) - 1);
                }
            }
            
            g3.dispose();
        }
        
        g2.drawImage(gui.thingsimage, 0, 0, null);
        
        g2.setFont(font);
        for(Thing t : gui.client.map.GetUnits()){
            Point p = gui.client.map.GetLocationOfThing(t);
            if(p != null){ //TODO: Somewhat hackish
                g2.setColor(Color.BLACK);
                g2.fillRect(fontsize*p.x+1, fontsize*p.y+1, fontsize-1, fontsize-1);
                g2.setColor(Color.WHITE);
                g2.drawChars(new char[]{t.GetSymbol()}, 0, 1, fontsize*p.x + (int)((fontsize - g2.getFontMetrics().charWidth(t.GetSymbol()))/2 + 1), fontsize*(p.y+1) - 1);
            }
        }
        
        if(gui.cursor != null){
            Point p = (Point)gui.client.map.GetLocationOfThing(gui.client.map.GetUnits().get(1)).clone(); //TODO: add real unit ref
            if(!circletest){
                if(gui.cursorover != null){
                    BresenhamLineDraw(g2, p.x, gui.cursor.x, p.y, gui.cursor.y, 200, 100, 100, 100, 255);
                }
                else{
                    BresenhamLineDraw(g2, p.x, gui.cursor.x, p.y, gui.cursor.y, 100, 200, 200, 100, 255);
                }
            }
            else{
                CircleDraw(g2, p.x, p.y, Math.abs(gui.cursor.x - p.x), 255, 0, 0, 50, 200);
            }
        }
        
        g2.dispose();
        
        ChangedUnits = false;
        
        drawing = false;
        if(ChangedThings){
            repaint();
        }
    }
    
    /*public void Plot(Graphics2D g2, int x, int y, int alpha){
        g2.setColor(new Color(40,150,0,alpha));
        g2.fillRect(fontsize*x+1, fontsize*y+1, fontsize-1, fontsize-1);
    }
    public void Plot(Graphics2D g2, int x, int y, int r, int g, int b, int alpha){
        g2.setColor(new Color(r,g,b,alpha));
        g2.fillRect(fontsize*x+1, fontsize*y+1, fontsize-1, fontsize-1);
    }*/
    public void Plot(Graphics2D g2, Point p, Color c){
        g2.setColor(c);
        g2.fillRect(fontsize*p.x+1, fontsize*p.y+1, fontsize-1, fontsize-1);
    }
    
    
    public void CircleDraw(Graphics2D g2, int x, int y, int radius, int r, int g, int b, int minAlpha, int maxAlpha){
        Color c = new Color(r, g, b, minAlpha);
        if(radius == 0){
            Plot(g2, new Point(x, y), new Color(r, g, b, maxAlpha));
            return;
        }
        int r2 = radius*radius;
        
        double dAlpha = Math.floor((maxAlpha - minAlpha)/radius);
        
        for(int x2 = x-radius; x2 <= x+radius; x2++){
            int x22 = Math.max(Math.abs(x2-x) - 1, 0);
            x22 *= x22;
            
            for(int y2 = y - radius; y2 <= y + radius; y2++){
                int y22 = Math.max(Math.abs(y2-y) - 1, 0);
                y22 *= y22;
                
                int r3 = x22 + y22;
                
                int diff = Math.max(r2 - r3, 0);
                
                if(diff != 0){
                    Plot(g2, new Point(x2, y2), new Color(r, g, b, Math.min(maxAlpha, minAlpha+(int)((radius-Math.sqrt(r3)) * dAlpha))));
                }
            }
        }
    }
    public void BresenhamLineDraw(Graphics2D g2, int x0, int x1, int y0, int y1, int r, int g, int b, int minAlpha, int maxAlpha){
        //ArrayList<Point> points = Constant.LineOfPoints(x0, x1, y0, y1);
        ArrayList<Point> points = Constant.TolerantLineOfPoints(gui.client.map.GetAllFloorTiles(), x0, x1, y0, y1);
        int dalpha = Math.max((maxAlpha-minAlpha)/points.size(), 1);
        int alpha = minAlpha;
        Color c;
        for(Point p : points){
            c = new Color(r, g, b, alpha = Math.min(alpha+dalpha, maxAlpha));
            Plot(g2, p, c);
        }
    }
}