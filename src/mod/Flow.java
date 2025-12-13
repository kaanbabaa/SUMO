package mod;

import exceptions.FlowClassException;
import exceptions.VehicleClassException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.sumo.libtraci.TraCIColor;
public class Flow {
	private final String flowId;
	private final String routeId;
	private final String vehType;
	private final int batchSize;

	public Flow(String flowId, Route routeId, String vehType, int batchSize) {
		this.flowId = flowId;
		this.routeId = routeId.getId();
		this.vehType = vehType;
		this.batchSize = batchSize;
	}
	
	public Flow(String flowId, String routeId, String vehType, int batchSize) {
		this.flowId = flowId;
		this.routeId = routeId;
		this.vehType = vehType;
		this.batchSize = batchSize;
	}
	
	public List<Vehicle> injectBatch() throws FlowClassException {
		
		List<Vehicle> injectedVehicles = new ArrayList<>();
		
		for(int i = 0; i < batchSize; i++) {	
			String newId = "v_" + flowId + "_" + (injectedVehicles.size()+1);
			try {
				
				Vehicle newVehicle = new Vehicle(newId, routeId, vehType);
				injectedVehicles.add(newVehicle);
				
			} catch (VehicleClassException e) {
				throw new FlowClassException("Flow can`t be created: " + flowId, e);
			} catch (Exception ex) {
				throw new FlowClassException("Unexpected error during flow injection: " + ex.getMessage(), ex);
			}
		}
		return injectedVehicles;
	}
	public List<Vehicle> injectBatch(TraCIColor color) throws FlowClassException {
		
		List<Vehicle> injectedVehicles = new ArrayList<>();
		
		for(int i = 0; i < batchSize; i++) {	
			String newId = "v_" + flowId + "_" + (injectedVehicles.size()+1);
			try {
				
				Vehicle newVehicle = new Vehicle(newId, routeId, vehType, color);
				injectedVehicles.add(newVehicle);
				
			} catch (VehicleClassException e) {
				throw new FlowClassException("Flow can`t be created: " + flowId, e);
			} catch (Exception ex) {
				throw new FlowClassException("Unexpected error during flow injection: " + ex.getMessage(), ex);
			}
		}
		return injectedVehicles;
	}

}
