package mod;

import org.eclipse.sumo.libtraci.*;

import exceptions.VehicleClassException;
import org.eclipse.sumo.libtraci.TraCIColor;

import java.awt.Color; 

public class Vehicle {
	
	private final String id;
	private final String vehTyp;
	private final String routeId;
	
	private double currentSpeed;
	private double currentX;
	private double currentY;
	
	public Vehicle(String id, String routeId, String vehTyp) throws VehicleClassException {
		this.id = id;
		this.routeId = routeId;
		this.vehTyp = vehTyp;
	
		try {
			org.eclipse.sumo.libtraci.Vehicle.add(id, routeId, vehTyp);
		} catch(Exception e) {
			throw new VehicleClassException("Vehicle isn`t added to SUMO: " + id, e);
		}
	}
	
	public Vehicle(String id, String routeId, String vehTyp, TraCIColor color) throws VehicleClassException {
		this(id,routeId,vehTyp);
		try {
			org.eclipse.sumo.libtraci.Vehicle.setColor(id, color);
		} catch (Exception e) {
			throw new VehicleClassException("Car is added to SUMO but color is not adjusted: " + id, e);
		}
	}

	public Vehicle(String id, Route routeId, String vehType) throws VehicleClassException {
		this(id,routeId.getId(),vehType);
		
	}
	
	public Vehicle(String id, Route routeId, String vehType, TraCIColor color) throws VehicleClassException {
		this(id,routeId.getId(),vehType);
		try {
			org.eclipse.sumo.libtraci.Vehicle.setColor(id ,color);
		} catch (Exception e) {
			throw new VehicleClassException("Car is added to simulation but color can`t be changed: " + id, e);
		}
	}
	
	public void updateTelemetry() throws VehicleClassException {
		try {
			double speedFromSumo = org.eclipse.sumo.libtraci.Vehicle.getSpeed(id);
			if(speedFromSumo < -1000) {
				throw new VehicleClassException("SUMO returned error value for getSpeed");
			}
			TraCIPosition position = org.eclipse.sumo.libtraci.Vehicle.getPosition(id);
			currentX = position.getX();
			currentY = position.getY();
			currentSpeed = speedFromSumo;
		} catch(Exception ex) {
			throw new VehicleClassException("Telemetry could not pulled(Probably vehicle left: " + id, ex);
		}
	}

	public void setColor(TraCIColor color) throws VehicleClassException {
		try {
			org.eclipse.sumo.libtraci.Vehicle.setColor(id, color);
		} catch (Exception e) {
			throw new VehicleClassException("Car color can`t be adjusted: " + id, e);
		}
	}
	public void setSpeed(double speed) throws VehicleClassException {
		try {
			org.eclipse.sumo.libtraci.Vehicle.setSpeed(id, speed);
		} catch(Exception e) {
			throw new VehicleClassException("Vehicle speed isn`t changed: " + id, e);
		}
	}
	
	public Color getColor() {
		TraCIColor color = org.eclipse.sumo.libtraci.Vehicle.getColor(id);
		
        if (color == null) {
            return Color.WHITE; 
        }
        int r = color.getR();
        int g = color.getG();
        int b = color.getB();
        int a = color.getA();
      
        return new Color(r, g, b, a);
	}
	
	public String getId() { return id;}
	
	public String getRouteId() { return routeId;}
	
	public String getVehicleType() { return vehTyp;}
	
	public double[] currentLocation() { return new double[] {this.currentX, this.currentY};	}
	
	public double getcurrentSpeed() { return currentSpeed;}
}