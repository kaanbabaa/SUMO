package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.WindowAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import exceptions.SimulationManagerException;
import mod.TrafficLight;
import simulation.SimulationManager;
import simulation.SimulationThread;
import org.eclipse.sumo.libtraci.TraCIColor;

import utils.Colors;

public class MainFrame {

	private JFrame frame;
	private SimulationManager manager;
	private SimulationThread simThread;
	
	private MapPanel mapPanel;
	private JPanel controlPanel;
	private JPanel actionPanel;
	
	private JButton startButton;
	private JButton closeButton;
	private JButton addVehicle;
	private JButton changeLight;
	private JCheckBox filterCheckBox;
	private JButton triggerFlowButton;
	private JButton randomVehicleButton;
	private JButton stressTestButton;
	
	private Map<String, String> typeMap;
	private Map<String, String> currentSpeedMap; 
	private String[] tlActions;
	
	public MainFrame(SimulationManager manager) {
		this.manager = manager;
		initVehType();
		initCurrentSpeedMap();
		initTlActions();
		initialize();
	}
	
	private void initTlActions() {
		tlActions = new String[] {
				"Switch to RED", 
				"Switch to GREEN",
				"Switch to YELLOW"
			};
	}
	
	private void initVehType() {
		typeMap = new LinkedHashMap<>();
		typeMap.put("Car", "DEFAULT_VEHTYPE");
		typeMap.put("Bike", "DEFAULT_BIKETYPE");
		typeMap.put("Taxi", "DEFAULT_TAXITYPE");
	}
	
	public void initCurrentSpeedMap() {
		currentSpeedMap = new LinkedHashMap<>();
		currentSpeedMap.put("All", "All");
		currentSpeedMap.put("0 - 5", "0-5");
		currentSpeedMap.put("5 - 10", "5-10");
		currentSpeedMap.put("10 - 15", "10-15");
		currentSpeedMap.put("15 - 20", "15-20");
		currentSpeedMap.put("20+", "20+");
	}
	
	public void initialize() {
		
		frame = new JFrame("SUMO Traffic Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 800);
		frame.setLayout(new BorderLayout());
		frame.setLocationRelativeTo(null);

		mapPanel = new MapPanel(manager);
		frame.add(mapPanel, BorderLayout.CENTER);
		
		controlPanel = new JPanel();
		actionPanel = new JPanel();
		
		actionPanel.setLayout(new GridLayout(0, 1, 5, 10)); 
		actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
		
		startButton = new JButton("Start Simulation");
		closeButton = new JButton("Close Simulation");
		addVehicle = new JButton("Add Vehicle");
		changeLight = new JButton("Change Light");
		filterCheckBox = new JCheckBox("Filter Vehicles");
		triggerFlowButton = new JButton("Trigger Flow");
		randomVehicleButton = new JButton("Add Random Vehicle");
		stressTestButton = new JButton("Stress Test");
		
		
		controlPanel.add(startButton);
		controlPanel.add(closeButton);
		actionPanel.add(addVehicle);
		actionPanel.add(changeLight);
		actionPanel.add(triggerFlowButton);
		actionPanel.add(randomVehicleButton);
		actionPanel.add(stressTestButton);
		actionPanel.add(filterCheckBox);
		
	
		
		frame.add(controlPanel, BorderLayout.SOUTH);
		frame.add(actionPanel, BorderLayout.EAST);
		
		toggleSimulationState(false);
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSimulationThread();
				toggleSimulationState(true);
			}
		});
		
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeSimulationThread();
				toggleSimulationState(false);
				frame.dispose();
			}
		});
		
		addVehicle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddVehicleDialog();
			}
		});
		
		changeLight.addActionListener(new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				openChangeLightDialog();
			}
		});
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(simThread != null && simThread.isAlive()) {
					simThread.stopSimulation();
				}
			}
		});
		
		filterCheckBox.addActionListener(e -> {
			if(filterCheckBox.isSelected()) {
				openFilterDialog();
				filterCheckBox.setSelected(false);
			} else mapPanel.applyFilters(false, null, null);
		});
		
		triggerFlowButton.addActionListener(e -> {
			openTriggerFlowDialog();
		});
		
		randomVehicleButton.addActionListener(e -> {
			try {
				manager.createRandomVehicle();
			} catch(SimulationManagerException ex) {
				JOptionPane.showMessageDialog(frame,"Error during creating the random vehicle", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		stressTestButton.addActionListener(e -> {
			openStressTestDialog();
		});
	}
	
	private void toggleSimulationState(boolean isRunning) {
		startButton.setEnabled(!isRunning);
		closeButton.setEnabled(isRunning);
		addVehicle.setEnabled(isRunning);
		changeLight.setEnabled(isRunning);
		triggerFlowButton.setEnabled(isRunning);
		filterCheckBox.setEnabled(isRunning);
		randomVehicleButton.setEnabled(isRunning);
		stressTestButton.setEnabled(isRunning);
		}
	
	private void openAddVehicleDialog() {
		
		JDialog dialog = new JDialog(frame, "Create a new vehicle ", true);
		dialog.setSize(350, 280);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		int x = frame.getX() + frame.getWidth();
		int y = frame.getY();
		dialog.setLocation(x + 5, y);
	
		JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	
		JTextField idField = new JTextField();
		
		Object[] routeIds = manager.getActiveRoutes();
		if(routeIds.length == 0) {
			routeIds = new String[] {"Route doesnt exists"};
		}
		
		JComboBox<String> typeBox = new JComboBox<>(typeMap.keySet().toArray(new String[0]));
		JComboBox<String> colorBox = new JComboBox<>(Colors.COLOR_MAP.keySet().toArray(new String[0]));
		JComboBox<Object> routeBox = new JComboBox<>(routeIds);
		
		colorBox.setSelectedIndex(0);
		typeBox.setSelectedIndex(0);
		inputPanel.add(new JLabel("Vehicle ID:"));
		inputPanel.add(idField);
		inputPanel.add(new JLabel("Route ID:"));
		inputPanel.add(routeBox);
		inputPanel.add(new JLabel("Vehicle Typ:"));
		inputPanel.add(typeBox);
		inputPanel.add(new JLabel("Vehicle Color:"));
		inputPanel.add(colorBox);
    
		dialog.add(inputPanel, BorderLayout.CENTER);
    
		JButton okButton = new JButton("Add");
		JButton cancelButton = new JButton("Cancel");
    
		JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 10));
    
		okButton.setPreferredSize(new Dimension(90, 90));
		cancelButton.setPreferredSize(new Dimension(90, 90));
    
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
    
		dialog.add(btnPanel, BorderLayout.SOUTH);
    
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = idField.getText().trim();
				String route = (String) routeBox.getSelectedItem();
    		
				String selectedTypeName = (String) typeBox.getSelectedItem();
				String selectedColorName = (String) colorBox.getSelectedItem();
    		
				TraCIColor selectedColor = Colors.COLOR_MAP.get(selectedColorName);
				String realTypeId = typeMap.get(selectedTypeName);
    		
				if(id.isEmpty() || route.isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "Please enter Id and Route!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
    		
				try {
					manager.addVehicle(id, route, realTypeId, selectedColor);
					System.out.println("GUI: Vehicle added -> " + id + " (" + selectedColorName + ")");
					dialog.dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
    
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
    
		dialog.pack();
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setVisible(true);
    
	}
	
	private void openChangeLightDialog() {
		
		JDialog dialog = new JDialog(frame, "Control Traffic Lights ", true);
		dialog.setSize(350, 280);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		int x = frame.getX() + frame.getWidth();
		int y = frame.getY();
		dialog.setLocation(x + 5, y);
		
		JPanel inputPanel = new JPanel(new GridLayout(6, 1, 10, 10));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
	
		JTextField idField = new JTextField();
		JComboBox phaseBox = new JComboBox<String>(tlActions);
		JTextField phaseDurationField = new JTextField();
		
		
		inputPanel.add(new JLabel("TrafficLight-Id: "));
		inputPanel.add(idField);
		inputPanel.add(new JLabel("New Phase: "));
		inputPanel.add(phaseBox);
		inputPanel.add(new JLabel("Phase Duration: "));
		inputPanel.add(phaseDurationField);
     

		
		dialog.add(inputPanel, BorderLayout.CENTER);
		
		JButton okButton = new JButton("Add");
		JButton cancelButton = new JButton("Cancel");
		JButton inspectButton = new JButton("Inspect Traffic Light");
		JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 10));
    
		okButton.setPreferredSize(new Dimension(90, 90));
		cancelButton.setPreferredSize(new Dimension(90, 90));
    
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
		btnPanel.add(inspectButton);
		
		dialog.add(btnPanel, BorderLayout.SOUTH);
    
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = idField.getText().trim();
				String selectedPhase = (String) phaseBox.getSelectedItem(); 
				String durationStr = phaseDurationField.getText().trim();

				if (id.isEmpty() || selectedPhase.isEmpty() || durationStr.isEmpty()) {
		            JOptionPane.showMessageDialog(dialog, "Please fill every blank!", "Missing Information Error", JOptionPane.WARNING_MESSAGE);
		            return;
		        }
				try {
					double duration = Double.parseDouble(durationStr);
					TrafficLight tl = manager.getActiveLights().get(id);
					if(tl == null) {
						JOptionPane.showMessageDialog(frame, "Traffic light doesnt exists", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if("Switch to RED".equals(selectedPhase)) {
						tl.switchToNextRed();
						manager.setPhaseDuration(id, duration);
					} else if ("Switch to YELLOW".equals(selectedPhase)) {
						tl.switchToNextYellow();
						manager.setPhaseDuration(id, duration);

					} else if("Switch to GREEN".equals(selectedPhase)){
						tl.switchToNextGreen();
						manager.setPhaseDuration(id, duration);

					}
			
					System.out.println("GUI: Traffic Light updated!" + id);
					dialog.dispose();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(dialog, "Enter only number to phase and duration sections.", "Format Error", JOptionPane.ERROR_MESSAGE);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(dialog, "Error: " + exc.getMessage(), "Process Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
			
		});
		
		inspectButton.addActionListener(e -> {
			String guiId = idField.getText().trim();
			if(guiId.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please enter the id", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				String pd = manager.getTlProgramDefinition(guiId);
				JOptionPane.showMessageDialog(dialog, pd, "Phase Details", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "Error during inspecting: " + ex.getMessage());
			}
			
		});
		
		dialog.pack();
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setVisible(true);
	}
	
	private void openFilterDialog() {
		JDialog dialog = new JDialog(frame, "Filter Visible Vehicles", true);
		dialog.setSize(300, 250);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		int x = frame.getX() + frame.getWidth();
		int y = frame.getY();
		dialog.setLocation(x + 5, y);
		
		JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));		
		
		
		JComboBox<String> currentSpeedBox = new JComboBox(currentSpeedMap.keySet().toArray(new String[0]));
		
		List<String> filterColorList = new ArrayList<>();
		filterColorList.add("All");
		filterColorList.addAll(Colors.COLOR_MAP.keySet());
		JComboBox<String> colorBox = new JComboBox<>(filterColorList.toArray(new String[0]));
	
		currentSpeedBox.setEnabled(true);
		colorBox.setEnabled(true);
		
		panel.add(new javax.swing.JSeparator());
		panel.add(new JLabel("Speed Range (m/s):"));
        panel.add(currentSpeedBox);
        panel.add(new JLabel("Vehicle Color:"));
        panel.add(colorBox);
        
        dialog.add(panel, BorderLayout.CENTER);
        
        JButton saveButton = new JButton("Apply Filters");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 10));
        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);
        
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        saveButton.addActionListener(e -> {
        	
        	String selectedSpeed = (String) currentSpeedBox.getSelectedItem();
        	String selectedColorName = (String) colorBox.getSelectedItem();
        	
        	java.awt.Color targetColor = null;
        	
        	if(!"All".equals(selectedColorName)) {
        		TraCIColor tc = Colors.COLOR_MAP.get(selectedColorName);
        		if(tc != null) {
        			targetColor = new java.awt.Color(tc.getR(), tc.getG(), tc.getB(), tc.getA());
        		}
        	}
        	
        	mapPanel.applyFilters(true, selectedSpeed, targetColor);
  
        	dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
        	dialog.dispose();
        });
		dialog.getRootPane().setDefaultButton(saveButton);

        dialog.setVisible(true);
	}
	
	private void openTriggerFlowDialog() {
		JDialog dialog = new JDialog(frame, "Trigger Flow", true);
		dialog.setSize(300, 250);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		int x = frame.getX() + frame.getWidth();
		int y = frame.getY();
		dialog.setLocation(x + 5, y);
		
		JPanel inputPanel = new JPanel(new GridLayout(6, 1, 5, 5));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));		
		
		JTextField idField = new JTextField();
		JTextField numOfVeh = new JTextField();
	
		Object[] routeIds = manager.getActiveRoutes();
		if(routeIds.length == 0) {
			routeIds = new String[] {"Route doesnt exists"};
		}
		
		JComboBox<String> typeBox = new JComboBox<>(typeMap.keySet().toArray(new String[0]));
		JComboBox<String> colorBox = new JComboBox<>(Colors.COLOR_MAP.keySet().toArray(new String[0]));
		JComboBox<Object> routeBox = new JComboBox<>(routeIds);
		
		colorBox.setSelectedIndex(0);
		typeBox.setSelectedIndex(0);
		inputPanel.add(new JLabel("Vehicle ID:"));
		inputPanel.add(idField);
		inputPanel.add(new JLabel("Route ID:"));
		inputPanel.add(routeBox);
		inputPanel.add(new JLabel("Vehicle Typ:"));
		inputPanel.add(typeBox);
		inputPanel.add(new JLabel("Vehicle Color:"));
		inputPanel.add(colorBox);
		inputPanel.add(new JLabel("#Vehicle: "));
		inputPanel.add(numOfVeh);
		
    
		dialog.add(inputPanel, BorderLayout.CENTER);
    
		JButton okButton = new JButton("Add");
		JButton cancelButton = new JButton("Cancel");
    
		JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 10));
    
		okButton.setPreferredSize(new Dimension(90, 90));
		cancelButton.setPreferredSize(new Dimension(90, 90));
    
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
    
		dialog.add(btnPanel, BorderLayout.SOUTH);
    
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = idField.getText().trim();
				String route = (String) routeBox.getSelectedItem();
				String numOfVehStr = numOfVeh.getText().trim();
				
				
				String selectedTypeName = (String) typeBox.getSelectedItem();
				String selectedColorName = (String) colorBox.getSelectedItem();
				int selectedVehicleCount = Integer.parseInt(numOfVehStr);
				
				TraCIColor selectedColor = Colors.COLOR_MAP.get(selectedColorName);
				String realTypeId = typeMap.get(selectedTypeName);
    		
				if(id.isEmpty() || route.isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "Please enter Id and Route!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
    		
				if(selectedVehicleCount <= 0) {
					JOptionPane.showMessageDialog(dialog, "Please enter valid #Vehicle(>0)!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				try {
					manager.triggerFlow(id, route, realTypeId, selectedVehicleCount, selectedColor);
					System.out.println("GUI: Vehicle added -> " + id + " (" + selectedColorName + ")");
					dialog.dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
    
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
    
		dialog.pack();
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setVisible(true);
		
	}
	
	private void openStressTestDialog() {
		JDialog dialog = new JDialog(frame, "Stress Test", true);
		dialog.setSize(300, 250);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
		int x = frame.getX() + frame.getWidth();
		int y = frame.getY();
		dialog.setLocation(x + 5, y);
		
		JPanel inputPanel = new JPanel(new GridLayout(6, 1, 5, 5));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));		
		
		JTextField numOfVeh = new JTextField();
		
		inputPanel.add(new JLabel("#Vehicle: "));
		inputPanel.add(numOfVeh);
		
		dialog.add(inputPanel, BorderLayout.CENTER);
		
		JButton okButton = new JButton("Start test");
		JButton cancelButton = new JButton("Cancel test");
    
		JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 10));
    
		okButton.setPreferredSize(new Dimension(90, 90));
		cancelButton.setPreferredSize(new Dimension(90, 90));
    
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
    
		dialog.add(btnPanel, BorderLayout.SOUTH);
		
		okButton.addActionListener(e -> {
			String numOfVehStr = numOfVeh.getText().trim();
			int vehCountInput= Integer.parseInt(numOfVehStr);
			try {
				manager.startStressTest(vehCountInput);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "Error during starting the Stress test", "Error", JOptionPane.ERROR_MESSAGE);
			}
			dialog.dispose();
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
    
		dialog.pack();
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setVisible(true);		
	}
	
	public void startSimulationThread() {
		if(simThread == null || !simThread.isAlive()) {
			simThread = new SimulationThread(manager, mapPanel);
			simThread.start();
			System.out.println("GUI: Simulation thread start command forwarded correctly...");
			
		} else {
			System.out.println("GUI: Simulation alreay started");
		}
	}
	
	public void closeSimulationThread() {
		if(simThread != null && simThread.isAlive()) {	
			simThread.stopSimulation();
		}
	}
	
	
	
	public void setVisible(boolean visible ) {
		frame.setVisible(visible);
	}
}
