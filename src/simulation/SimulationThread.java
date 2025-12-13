package simulation;

import javax.swing.SwingUtilities;

import gui.MapPanel;

public class SimulationThread extends Thread {
	
	private SimulationManager manager;
	private volatile boolean isRunning = true;
	private MapPanel mapPanel;
	
	public SimulationThread(SimulationManager manager, MapPanel mapPanel) {
		this.manager = manager;
		this.mapPanel= mapPanel; 
	}
	
	@Override
	public void run() {
		try {
			System.out.println("THREAD: SUMO starting...");
			manager.startSimulation();
			manager.calculateMapBounds();
			manager.importRoadNetwork();
			manager.importPolygons();
			manager.loadAllEdges();
			System.out.println("THREAD: Routes and Traffic lights are downloading...");
			manager.importRoutes();
			manager.importTrafficLights();
			mapPanel.reloadStaticMap();
			
			System.out.println("THREAD: Simulation cycle starting...");
			
			while(isRunning) {
				
				manager.runStep();
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override 
					public void run() {
						mapPanel.repaint();
					}
				});
				
				Thread.sleep(250);
				}	
		} catch(InterruptedException e) {
			System.out.println("Simulation Thread stopped (interrupt).");
			isRunning = false;
		} catch (Exception e) {
			System.out.println("Simulation Thread Error: " + e.getMessage());
			isRunning = false;
			e.printStackTrace();
		}finally {
            if (manager != null) {
            	try {
            		manager.closeSimulation();
            	} catch(Exception closeException) {
            		System.err.println("Error during closing the simulation (ihmal edilebilir): " + closeException.getMessage());
            	}
            }
        }     
		}
	
	public void stopSimulation() {
		this.isRunning = false;
		this.interrupt();
	}
}
