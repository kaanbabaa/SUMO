package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mod.TrafficLight;
import mod.Vehicle;
import mod.SumoPolygon;
import simulation.SimulationManager;


public class MapPanel extends JPanel {

	private BufferedImage staticImage = null;
	private SimulationManager manager;
	private double zoomFactor = 1.0;
	private double camX = 0;
    private double camY = 0;
    private int lastMouseX, lastMouseY;
	
    private boolean filterEnabled = false;
    private double minSpeed = 0;
    private double maxSpeed = Double.MAX_VALUE;
    private Color filterColor = null;
	
	
	public MapPanel(SimulationManager manager) {
	    this.manager = manager;
	    setBackground(Color.LIGHT_GRAY);
	    
	    this.addMouseWheelListener(new MouseWheelListener() {
	        @Override
	        public void mouseWheelMoved(MouseWheelEvent e) {
	        	double rotation = e.getPreciseWheelRotation();
	        	if(rotation == 0) return;
	            if(rotation < 0) {zoomFactor *= 1.05;}
	            else {zoomFactor /= 1.05;}
	            staticImage = null;
	            repaint(); 
	        }
	    }); 
	    
	    this.addMouseListener(new MouseAdapter() {
	    	@Override
	    	public void mousePressed(MouseEvent e) {
	    		lastMouseX = e.getX();
	    		lastMouseY = e.getY();
	    	}
	    });
	    
	    this.addMouseMotionListener(new MouseAdapter() {
	    	@Override
	    	public void mouseDragged(MouseEvent e) {
	    		int dx = e.getX() - lastMouseX;
	    		int dy = e.getY() - lastMouseY;
	    		
	    		camX += dx;
	    		camY+= dy;
	    		
	    		staticImage = null;
	    		
	    		lastMouseX = e.getX();
	    		lastMouseY = e.getY();
	    		repaint();
	    	}
	    });
	    
	}
	
	public void applyFilters(boolean isActive, String speedRange, Color color) {
		this.filterEnabled = isActive;
		if(!isActive) {
			repaint();
			return;
		}
		
		this.filterColor = color;
		
		if(speedRange == null || speedRange.equals("All")) {
			this.minSpeed = 0;
			this.maxSpeed = Double.MAX_VALUE;
		} else if(speedRange.contains("-")) {
			try {
				String[] parts = speedRange.split("-");
				this.minSpeed = Double.parseDouble(parts[0].trim());
				this.maxSpeed = Double.parseDouble(parts[1].trim());
			} catch (NumberFormatException e) {
				System.err.println("Speed Range Error: " + speedRange);
			}
		} else if(speedRange.contains("+")) {
			try {
				String val = speedRange.replace("+", "").trim();
				this.minSpeed = Double.parseDouble(val);
				this.maxSpeed = Double.MAX_VALUE;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Speed range format error: " + speedRange,
						"Filter Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (staticImage == null || staticImage.getWidth() != getWidth() || staticImage.getHeight() != getHeight()) {
			renderStaticMap();
		}
		else if(staticImage != null)
		{
			g2d.drawImage(staticImage, 0,  0,  null);
		}
		drawVehiclesAndLights(g2d);
	}
	
	private void renderStaticMap() {
		staticImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gCache = staticImage.createGraphics();
		
		gCache.setColor(Color.LIGHT_GRAY);
		gCache.fillRect(0,  0,  getWidth(),  getHeight());
		
		double minX = manager.getMinX();
		double minY = manager.getMinY();
		double mapWidth = manager.getMaxX() - minX;
		double mapHeight = manager.getMaxY() - minY;
		
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		
		double scaleX = panelWidth/ mapWidth;
		double scaleY = panelHeight/ mapHeight;
		
		double scale = Math.min(scaleX, scaleY) * zoomFactor;
		
		double offsetX = (panelWidth - (mapWidth * scale)) / 2 + camX;
		double offsetY = (panelHeight - (mapHeight * scale)) / 2 + camY;
		
		List<SumoPolygon> polygons = manager.getMapPolygons();
		
		if(polygons != null) {
			for(SumoPolygon poly : polygons) {
				int[] xPoints = new int[poly.getShape().size()];
				int[] yPoints = new int[poly.getShape().size()];
				
				for(int i = 0; i < poly.getShape().size(); i++) {
					double[] p = poly.getShape().get(i);
					xPoints[i] = (int) (offsetX + (p[0] - minX) * scale);
                    yPoints[i] = (int) (offsetY + (mapHeight - (p[1] - minY)) * scale); 
				}
				gCache.setColor(poly.getColor());
				
				if(poly.isFilled()) {
					gCache.fillPolygon(xPoints, yPoints, xPoints.length);
				} else {
					gCache.drawPolygon(xPoints, yPoints, xPoints.length);
				}
			}
		}
		
		List<List<double[]>> lanes = manager.getLaneShapes();
		
		gCache.setColor(Color.BLACK);
		if(lanes != null) {
			
			for(List<double[]> lane : lanes) {
				int[] xPoints = new int[lane.size()];
				int[] yPoints = new int[lane.size()];
				
				for(int i = 0; i < lane.size(); i++) {
					double[] p = lane.get(i);
					xPoints[i] = (int) (offsetX + (p[0] - minX) * scale);
                    yPoints[i] = (int) (offsetY + (mapHeight - (p[1] - minY)) * scale);
				}
				gCache.drawPolyline(xPoints, yPoints, lane.size());
			}
		}
		gCache.dispose();
		
		System.out.println("GUI: Static map created and its in the Cache");
	}
	
	private void drawVehiclesAndLights(Graphics2D g2d) {
		
		double minX = manager.getMinX();
		double minY = manager.getMinY();
		double mapWidth = manager.getMaxX() - minX;
		double mapHeight = manager.getMaxY() - minY;
		
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		
		double scaleX = panelWidth/ mapWidth;
		double scaleY = panelHeight/ mapHeight;
		
		double scale = Math.min(scaleX, scaleY) * zoomFactor;
		
		double offsetX = (panelWidth - (mapWidth * scale)) / 2 + camX;
		double offsetY = (panelHeight - (mapHeight * scale)) / 2 + camY;
		
		Map<String, Vehicle> vehicles = manager.getActiveVehicles();
		Map<String, TrafficLight> lights = manager.getActiveLights();
		
		for(TrafficLight tl : lights.values()) {
			
			List<TrafficLight.SignalPoint> points = tl.getSignalPoint();
			
			for(TrafficLight.SignalPoint p : points) {
				int x = (int) (offsetX + (p.x - minX) * scale);
				int y = (int) (offsetY + (mapHeight - (p.y - minY)) * scale); // Flip Y
				try {
					g2d.setColor(tl.getColorForSignal(p.signalIndex));
				} catch (Exception e) {
					System.err.println("Error during drawing the signal points: " + tl.getGuiId());
				}
				g2d.fillOval(x-3, y-3, 6, 6);
			}
			
			int x = (int) (offsetX + (tl.getX() - minX) * scale);
			int y = (int) (offsetY + (mapHeight - (tl.getY() - minY)) * scale); // Flip Y	
		
			g2d.setColor(tl.getPhaseColor());
			g2d.drawString(tl.getGuiId(), x-5, y-5);
			
			g2d.fillRect(x, y, 5, 5);
		}
		
		for(Vehicle veh: vehicles.values()) {
			
			if(filterEnabled) {
				double vehCurrentSpeed = veh.getcurrentSpeed();
				if(vehCurrentSpeed < minSpeed || vehCurrentSpeed >= maxSpeed) {
					continue;
				}
				
				if(filterColor != null) {
					if(!veh.getColor().equals(filterColor)) {
						continue;
					}
				}
			}
			
			int x = (int) (offsetX + (veh.currentLocation()[0] - minX)*scale);
			int y = (int) (offsetY + (mapHeight - (veh.currentLocation()[1] - minY)) * scale);
			
			g2d.setColor(veh.getColor());
			g2d.fillOval(x-5, y-5, 8, 8);
		}
	}
	
	public void reloadStaticMap() {
		staticImage = null;
		repaint();
		System.out.println("GUI: Static map created and its in the Cache");
	}
}
