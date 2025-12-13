package reporting;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import mod.TrafficLight;
import mod.Vehicle;
import org.eclipse.sumo.libtraci.Simulation;

public class CsvReportGenerator {
	
	private PrintWriter writer;
	private final String seperator = ";";
	private DecimalFormat df = new DecimalFormat("#.##");
	
	public CsvReportGenerator(String filePath, String header) throws IOException {
		
		this.writer = new PrintWriter(new FileWriter(filePath));
		writer.println(header);
	}
	
	public void writeVehicleData(Vehicle vehicle) {
		if(writer == null) return;
		double speed = vehicle.getcurrentSpeed();
		double[] location = vehicle.currentLocation();
		
		String lineVeh = 
				Simulation.getTime() + seperator + 
				vehicle.getId() + seperator +
				df.format(speed) + seperator +
				df.format(location[0]) + seperator +
				df.format(location[1]);
		
		writer.println(lineVeh);
	}
	
	public void writeTrafficLightData(TrafficLight tl) throws Exception {
		if(writer == null) return;
		
		String lineLight =
				Simulation.getTime() + seperator + 
				tl.getGuiId() + seperator +
				tl.getCurrentProgram() + seperator +
				tl.getCurrentPhase() + seperator + tl.getPhaseDuration();
		
		writer.println(lineLight);
	}
	
	public void close() {
		if(writer != null) {
			System.out.println("Csv Report saving: simulation_report.csv");
			writer.flush();
			writer.close();
		}
	}
}
