package main;

import simulation.SimulationManager;
import javax.swing.SwingUtilities;
import gui.MainFrame;


public class MainApp {
	
	public static void main(String[] args) {
		
        SimulationManager manager = null;
       
        
		try {
			org.eclipse.sumo.libtraci.Simulation.preloadLibraries();
			manager = new SimulationManager();
			final SimulationManager finalManager = manager;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainFrame frame = new MainFrame(finalManager);
					frame.setVisible(true);
				}
			});
			
		} catch (Exception e) {
			System.err.println("Critical error in MainApp: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
