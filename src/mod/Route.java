package mod;

import org.eclipse.sumo.libtraci.*;
import java.util.List;
import org.eclipse.sumo.libtraci.TraCIColor;
import exceptions.RouteClassException;
import org.eclipse.sumo.libtraci.Lane;

public class Route {
	private final String routeId;
	private final List<String> edges;
	private final int repeat;
	double length;
	
	public Route(String id) throws RouteClassException {
		this.routeId = id;
		this.repeat = 0;
		try {
			this.edges = org.eclipse.sumo.libtraci.Route.getEdges(routeId);
			
			if(this.edges == null || this.edges.isEmpty()) {
				throw new RouteClassException("Route is founded in SUMO (" + routeId + ") but its empty");
			}
		
			this.length = 0.0;
			
			for(String edgeId : this.edges) {
				
				int laneCount = Edge.getLaneNumber(edgeId);
				
				if(laneCount <= 0) {
					throw new RouteClassException("Route doesn`t exists");
				}
				
				String firstLaneId = edgeId + "_0";
				this.length += Lane.getLength(firstLaneId);
			}
		} catch (Exception e) {
			throw new RouteClassException("Route doesn`t exists or lengt can not calcualted: " + routeId, e);
		}
	}
	
	public void setColor(TraCIColor color) throws RouteClassException {
		try {
			String colorString = color.getR() + "," + color.getG() + "," + color.getB();
			org.eclipse.sumo.libtraci.Route.setParameter(routeId, "color", colorString);
		} catch (Exception e) {
			throw new RouteClassException("Error during color adjustment(with setparameter): " + routeId, e);
		}
	}
	
	public String getId() {
		return routeId;
	}
	
	public double getLength() {
		return length;
	}
		
	public List<String> getEdges() {
		return edges;
	}
	
	public int getRepeat() {
		return repeat;
	}
	
	public TraCIColor getColor() throws RouteClassException {
	    try {
	       
	        String colorStr = org.eclipse.sumo.libtraci.Route.getParameter(routeId, "color");
	        
	        if (colorStr == null || colorStr.isEmpty()) {
	            return utils.Colors.WHITE; 
	        }

	      
	        String[] parts = colorStr.split(",");
	        int r = Integer.parseInt(parts[0].trim());
	        int g = Integer.parseInt(parts[1].trim());
	        int b = Integer.parseInt(parts[2].trim());
	        int a = (parts.length == 4) ? Integer.parseInt(parts[3].trim()) : 255;

	        return new TraCIColor((byte)r, (byte)g, (byte)b, (byte)a);

	    } catch (Exception e) {
	        throw new RouteClassException("Error retrieving color for route: " + routeId, e);
	    }
	}
}


