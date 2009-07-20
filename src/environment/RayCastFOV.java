package akrasia.environment;

import java.util.LinkedList;
import java.util.Queue;

import java.awt.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Casts rays from a single point through a {@link World} object, which describes
 * impassable locations. Fills in a 2D array of {@link RayData} objects,
 * describing the visibility status of each point in the world as viewed from the
 * defined origin point.
 *
 * Rays take full advantage of work done by previous ray casts, and do not get
 * cast into occluded areas, making for near-optimal efficiency.
 *
 * The results array is not necessary for the algorithm's operation - it is
 * generated for external use.
 *
 */
public class RayCastFOV {
	
	private HashMap<Point, Integer> walls; // holds obstruction data
	private Point origin; // the point at which the rays will be cast from
        private int radius;
        private int radius2;
	private Queue<RayData> perimeter; // rays currently on the search frontier
	private RayData[][] results; // stores calculated data for external use
        public HashSet<Point> showWalls; // Walls that are shown
	
	
	public RayCastFOV(HashMap<Point, Integer>  walls, int originX, int originY, int radius) {
		this.walls = walls; 
		this.origin = new Point(originX, originY);
                this.radius = radius;
                this.radius2 = radius * radius;
		this.perimeter = new LinkedList<RayData>();
		this.results = new RayData[radius * 2 + 1][radius * 2 + 1]; 
                this.showWalls = new HashSet<Point>();
	}
	
	public Point getOrigin() { return this.origin; }
        public HashSet<Point> getWalls() { return showWalls; }
	
	/**
	 * Executes the ray casting operation by running a breadth-first traversal 
	 * (flood) of the world, beginning at the origin. 
	 */
	public ArrayList<Point> castRays() {
		expandPerimeterFrom(new RayData(0, 0));
		RayData currentData;
		while(!perimeter.isEmpty()) {
			currentData = perimeter.remove();
			
			// since we are traversing breadth-first, all inputs are guaranteed 
			// to be added to current data by the time it is removed.
			mergeInputs(currentData);
			
			if(!currentData.ignore) expandPerimeterFrom(currentData);
		}
                
                ArrayList<Point> points = new ArrayList<Point>();
                
                for( RayData[] ra : results ){
                    for( RayData r : ra ){
                        if(r != null && !r.ignore && !r.obscure()){
                            points.add(new Point(origin.x + r.xLoc, origin.y + r.yLoc) );
                        }
                    }
                }
                
                return points;
	}
	

	// Expands by the unit length in each component's current direction.
	// If a component has no direction, then it is expanded in both of its 
	// positive and negative directions.
	private void expandPerimeterFrom(RayData from) {
		if(from.xLoc >= 0) 
			processRay(new RayData(from.xLoc + 1, from.yLoc), from);
		if(from.xLoc <= 0) 
			processRay(new RayData(from.xLoc - 1, from.yLoc), from);
		if(from.yLoc >= 0) 
			processRay(new RayData(from.xLoc, from.yLoc + 1), from);
		if(from.yLoc <= 0) 
			processRay(new RayData(from.xLoc, from.yLoc - 1), from);
	}
	

	// Does bounds checking, marks obstructions, assigns inputs, and adds the 
	// ray to the perimeter if it is valid.
	private void processRay(RayData newRay, RayData inputRay) {
		int mapX = (radius + newRay.xLoc);
		int mapY = (radius + newRay.yLoc);

		// bounds check
		if(newRay.xLoc * newRay.xLoc + newRay.yLoc * newRay.yLoc >= radius2) return;
		
		// Since there are multiple inputs to each new ray, we need to check if 
		// the new ray has already been set up.
		// Here we use the results table as lookup, but we could easily use 
		// a different structure, such as a hashset keyed point data.
		if(results[mapX][mapY] != null) newRay = results[mapX][mapY];
		
		// Setting the reference from the new ray to this input ray.
		boolean isXInput = (newRay.yLoc == inputRay.yLoc);
		if(isXInput) newRay.xInput = inputRay;
		else newRay.yInput = inputRay;
		
		// Adding the new ray to the perimeter if it hasn't already been added.
		if(!newRay.added) {
			perimeter.add(newRay);
			newRay.added = true;
			results[radius + newRay.xLoc][radius + newRay.yLoc] = newRay;
		}
	}
	

	// Once all inputs are known to be assigned, mergeInputs performs the key 
	// task of populating the new ray with the correct data. 
	private void mergeInputs(RayData newRay) {
		// Obstructions must propagate obscurity.
                Point p = new Point((origin.x + newRay.xLoc), 
			   				    (origin.y + newRay.yLoc));
		if( walls.containsKey(p) && walls.get( p ) < 0 ) {
                        showWalls.add(p);
			int absXLoc = Math.abs(newRay.xLoc);
			int absYLoc = Math.abs(newRay.yLoc);
			newRay.xObsc = absXLoc;
			newRay.yObsc = absYLoc;
			newRay.xErrObsc = newRay.xObsc;
			newRay.yErrObsc = newRay.yObsc;
			return; 
		}
                p = null;
		
		RayData xInput = newRay.xInput;
		RayData yInput = newRay.yInput;
		boolean xInputNull = (xInput == null);
		boolean yInputNull = (yInput == null);
		
		// Process individual input information.
		if(!xInputNull) processXInput(newRay, xInput);
		if(!yInputNull) processYInput(newRay, yInput);

		// Culling handled here.
		// If both inputs are null, the point is never checked, so ignorance 
		// is propagated trivially in that case.
		if(xInputNull) {
			// cut point (inside edge)
			if(yInput.obscure()) newRay.ignore = true;
		}
		else if(yInputNull) {
			// cut point (inside edge)
			if(xInput.obscure()) newRay.ignore = true;
		}
		else { // both y and x inputs are valid
			// cut point (within arc of obscurity)
			if(xInput.obscure() && yInput.obscure()) {
				newRay.ignore = true;
				return;
			}
		}
	} // END mergeInputs(RayData)
	

	// The X input can provide two main pieces of information: 
	// 1. Progressive X obscurity.
	// 2. Recessive Y obscurity.
	private void processXInput(RayData newRay, RayData xInput) {
		if((xInput.xObsc == 0) && (xInput.yObsc == 0)) return;
		
		// Progressive X obscurity
		if(xInput.xErrObsc > 0) {
			if(newRay.xObsc == 0) { // favouring recessive input angle
				newRay.xErrObsc = (xInput.xErrObsc - xInput.yObsc);
				newRay.yErrObsc = (xInput.yErrObsc + xInput.yObsc);
				newRay.yObsc = xInput.yObsc;
				newRay.xObsc = xInput.xObsc;
			}
		}
		// Recessive Y obscurity
		if(xInput.yErrObsc <= 0) {
			if((xInput.yObsc > 0) && (xInput.xErrObsc > 0)) { 
				newRay.yErrObsc = (xInput.yObsc + xInput.yErrObsc);
				newRay.xErrObsc = (xInput.xErrObsc - xInput.yObsc);
				newRay.xObsc = xInput.xObsc;
				newRay.yObsc = xInput.yObsc;
			}
		}
	}
	
	
	// The Y input can provide two main pieces of information: 
	// 1. Progressive Y obscurity.
	// 2. Recessive X obscurity.
	private void processYInput(RayData newRay, RayData yInput) {
		if((yInput.xObsc == 0) && (yInput.yObsc == 0)) return;

		// Progressive Y obscurity
		if(yInput.yErrObsc > 0) {
			if(newRay.yObsc == 0) { // favouring recessive input angle
				newRay.yErrObsc = (yInput.yErrObsc - yInput.xObsc);
				newRay.xErrObsc = (yInput.xErrObsc + yInput.xObsc);
				newRay.xObsc = yInput.xObsc;
				newRay.yObsc = yInput.yObsc;
			}
		}
		// Recessive X obscurity
		if(yInput.xErrObsc <= 0) {
			if((yInput.xObsc > 0) && (yInput.yErrObsc > 0)) { 
				newRay.xErrObsc = (yInput.xObsc + yInput.xErrObsc);
				newRay.yErrObsc = (yInput.yErrObsc - yInput.xObsc);
				newRay.xObsc = yInput.xObsc;
				newRay.yObsc = yInput.yObsc;
			}
		}
	}
}

/**
 * A RayData object encapsulates information regarding a 2D ray from the origin, 
 * where the 'Loc' fields represent their respective components of the ray.
 * 
 * A RayData object also encodes data allowing it to propagate visibility 
 * (or lack of) to other rays. The obscurity effect vector is carried by the 
 * {@code xObsc} and {@code yObsc}. Information about the error from the vector 
 * is carried by the {@code xErrObsc} and {@code yErrObsc} fields. 
 * 
 * The Input fields store references to the input data, from which the rest of 
 * the data can be generated. These aren't necessary, since one could look them up 
 * elsewhere (perhaps from an array), but they are convenient.
 * 
 * The {@link #method obscure()} method contains the visibility (obscurity) function
 * which is somewhat arbitrary. If the {@code ignore} flag is true, then this object 
 * should also be treated as non-visible. 
 * 
 */
class RayData {
	int xLoc;
	int yLoc;
	
	int xObsc;
	int yObsc;
	int xErrObsc;
	int yErrObsc;
	
	RayData xInput;
	RayData yInput;

	boolean added; // true if we have added this to the perimeter
	boolean ignore; // true if there is no need to expand this ray

	
	public RayData(int xLoc, int yLoc) {
		this.xLoc = xLoc;
		this.yLoc = yLoc;
	}
	
	
	public boolean obscure() {
		return ((xErrObsc > 0) && (xErrObsc <= xObsc)) || 
		       ((yErrObsc > 0) && (yErrObsc <= yObsc));
	}
	
	
	public String toString() {
		StringBuilder data = new StringBuilder();
		data.append("(" + xLoc + "," + yLoc + ") ");
		data.append(": " + xObsc + "|" + yObsc + "|" + xErrObsc + "|" + yErrObsc);
		return data.toString();
	}
	
	
	/**
	 * <p>A useful method for printing results in text form.</p>
	 * 
	 * @return A character representing the status of this object. 
	 * 'I' -> ignored
	 * 'X' -> obscured from x only
	 * 'Y' -> obscured from y only
	 * 'Z' -> obscured from both x and y
	 * 'A' -> not obscure with recessive x obscurity
	 * 'B' -> not obscure with recessive y obscurity
	 * 'C' -> not obscure with both recessive x and y obscurity
	 */
	public char toChar() {
		boolean xObscure = ((xErrObsc > 0) && (xErrObsc <= xObsc));
		boolean yObscure = ((yErrObsc > 0) && (yErrObsc <= yObsc));
		if(ignore) return 'I';
		else if(xObscure && yObscure) return 'Z';
		else if(xObscure) return 'X';
		else if(yObscure) return 'Y';
		else {
			boolean xRecessive = (xErrObsc <= 0) && (xObsc > 0);
			boolean yRecessive = (yErrObsc <= 0) && (yObsc > 0);
			if(xRecessive && yRecessive) return 'C';
			else if(xRecessive) return 'A';
			else if(yRecessive) return 'B';
			else return 'O';
		}
	}

}