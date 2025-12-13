package mod;

import java.awt.Color;
import java.util.List;

public class SumoPolygon {
	private final String id;
	private final List<double[]> shape;
	private final Color color;
	private final boolean isFilled;
	
	public SumoPolygon(String id, List<double[]> shape, Color color, boolean isFilled) {
		this.id = id;
		this.shape = shape;
		this.color = color;
		this.isFilled = isFilled;
	}
	
	public List<double[]> getShape() { return shape;}
	
	public Color getColor() { return color;}
	
	public boolean isFilled() { return isFilled;}
}
