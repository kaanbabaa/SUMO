package mod;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sumo.libtraci.*; 
import exceptions.TrafficLightException;

public class TrafficLight {
	
	private  final String id;
	private final String guiId;
	private int currentPhase;
	private String currentProgram;
	
	private final double x;
	private final double y;
	
	private List<Integer> greenPhaseIndices = new ArrayList<>();
	public List<Integer> redPhaseIndices = new ArrayList<>();
	public List<Integer> yellowPhaseIndices = new ArrayList<>();
	public List<Integer> neutralPhaseIndices = new ArrayList<>();
	
	public static class SignalPoint {
		public double x, y;
		public int signalIndex;
		
		public SignalPoint(double x, double y, int signalIndex) {
			this.x = x;
			this.y = y;
			this.signalIndex = signalIndex;
		}
	}
	
	private List<SignalPoint> points = new ArrayList<>();
	
	public TrafficLight(String id, String guiId) throws TrafficLightException {
		this.id = id;
		this.guiId = guiId;
		try {
			this.updateState();
			StringVector junctions = org.eclipse.sumo.libtraci.TrafficLight.getControlledJunctions(id);
			if(junctions.size() > 0) {
				String junctionId = junctions.get(0);
				TraCIPosition position = org.eclipse.sumo.libtraci.Junction.getPosition(junctionId);
				this.x = position.getX();
				this.y = position.getY();
				calculateSignalPoint();
			} else {
				System.err.println("UYARI: TrafficLight " + id + " herhangi bir kavşağa bağlı değil!");
				this.x = 0;
				this.y = 0;
				
			}
			analyzePhases();
		} catch (Exception e) {
			throw new TrafficLightException("Traffic Light doesn`t exists: " + id, e);
		}
	}
	
	private void analyzePhases() throws TrafficLightException {
		try {
			TraCILogicVector logics = org.eclipse.sumo.libtraci.TrafficLight.getCompleteRedYellowGreenDefinition(id);
			if(logics.size() > 0) {
				TraCILogic logic = logics.get(0);
				List<Integer> fullRedIndices = new ArrayList<>();
				List<Integer> bigGreenIndices = new ArrayList<>();
				
				for(int i = 0; i < logic.getPhases().size(); i++) {
					TraCIPhase phase = logic.getPhases().get(i);
					String state = phase.getState();
			
					int yellow = calculateCharRepeat(state, 'y');
					int red = calculateCharRepeat(state, 'r');
					
					
					if(state.contains("G")) {
						greenPhaseIndices.add(i);
						bigGreenIndices.add(i);
					}
					else if(state.contains("g")) greenPhaseIndices.add(i);
					else if (state.length() == red) {
						redPhaseIndices.add(i);
						fullRedIndices.add(i);
					}
					else if(state.length() == yellow) yellowPhaseIndices.add(i);
					else if(red > yellow) redPhaseIndices.add(i);
					else if(yellow > red) yellowPhaseIndices.add(i);
					else neutralPhaseIndices.add(i);
					
				}
				
				if(!greenPhaseIndices.isEmpty() && !yellowPhaseIndices.isEmpty() && !redPhaseIndices.isEmpty()) return;
				else if(yellowPhaseIndices.isEmpty() && !redPhaseIndices.isEmpty() && !fullRedIndices.isEmpty()) {
					
					List<Integer> toMove = new ArrayList<>();
					
					for(int index : redPhaseIndices) {
						if(fullRedIndices.contains(index)) continue;
						toMove.add(index);
					}
					
					redPhaseIndices.removeAll(toMove);
					yellowPhaseIndices.addAll(toMove);	
				}
				else if(neutralPhaseIndices.size() > 0) {
					List<Integer> toMoveRed = new ArrayList<>();
					List<Integer> toMoveYellow = new ArrayList<>();
					for(int i = 0; i < neutralPhaseIndices.size(); i++) {
						if(i % 2 == 0) toMoveYellow.add(i);
						else toMoveRed.add(i);
					}
					yellowPhaseIndices.addAll(toMoveYellow);
					redPhaseIndices.addAll(toMoveRed);
					neutralPhaseIndices.removeAll(toMoveYellow);
					neutralPhaseIndices.removeAll(toMoveRed);
					
				}
				else if(yellowPhaseIndices.isEmpty()) {
					List<Integer> toMove = new ArrayList<>();
					
					for(int index : greenPhaseIndices) {
						if(bigGreenIndices.contains(index)) continue;
						toMove.add(index);
					}
					greenPhaseIndices.removeAll(toMove);
					yellowPhaseIndices.addAll(toMove);
				}
				
			}
		} catch (Exception e) {
			throw new TrafficLightException("Error during analzing the phases: " + id, e);
		}
	}
	
	public void updateState() throws TrafficLightException {
		try {
		this.currentPhase = org.eclipse.sumo.libtraci.TrafficLight.getPhase(id);
		this.currentProgram = org.eclipse.sumo.libtraci.TrafficLight.getProgram(id);

		} catch (Exception e) {
			throw new TrafficLightException("Trafic Light status not updated: " + id, e);
		}
	}
	
	private void calculateSignalPoint() throws TrafficLightException {
		try {
			TraCILinkVectorVector controlledLinks = org.eclipse.sumo.libtraci.TrafficLight.getControlledLinks(id);
			Set<String> processedLanes = new HashSet<>();
			for(int i = 0; i < controlledLinks.size(); i++) {
				TraCILinkVector links = controlledLinks.get(i);
				for(int j = 0; j < links.size(); j++) {
					TraCILink link = links.get(j);
					String laneId = link.getFromLane();
					
					if(processedLanes.contains(laneId)) continue;
					TraCIPositionVector rawShapes = org.eclipse.sumo.libtraci.Lane.getShape(laneId);
					TraCPositionVector shapes = rawShapes.getValue();
					if(shapes.size() > 0) {
						TraCIPosition endPos = shapes.get(shapes.size() - 1);
						points.add(new SignalPoint(endPos.getX(), endPos.getY(), i));
						processedLanes.add(laneId);
					}	
				}
			}
		} catch (Exception e) {
			throw new TrafficLightException("Error during calculating signal points: " + id, e);
		}
	}
	
	public List<SignalPoint> getSignalPoint() { return points; }
	
	public Color getColorForSignal(int index) throws TrafficLightException {
		try {
			String  state = org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id);
			if(index >= state.length()) return Color.BLACK;
			
			char colorChar = state.charAt(index);
			if(colorChar == 'G' || colorChar == 'g') return Color.GREEN;
			else if(colorChar == 'Y' || colorChar == 'y') return Color.YELLOW;
			else return Color.RED;
		} catch (Exception e) {
			throw new TrafficLightException("Error during getting signal color: " + id, e);
		}
	}
	
	
	
	public String getMostCongestedLane() throws TrafficLightException {
		String maxLaneId = null;
		int maxQueue = -1;
		Set<String> checkedLanes = new HashSet<>();
		
		try {
			TraCILinkVectorVector controlledLinks = org.eclipse.sumo.libtraci.TrafficLight.getControlledLinks(id);
			
			for(int i = 0; i < controlledLinks.size(); i++) {
				TraCILinkVector links = controlledLinks.get(i);
				for(int j = 0; j < links.size(); j++) {
					TraCILink link = links.get(j);
					String laneId = link.getFromLane();
					
					if(!checkedLanes.contains(laneId)) {
						int queue = org.eclipse.sumo.libtraci.Lane.getLastStepHaltingNumber(laneId);
						if(queue > maxQueue) {
							maxQueue = queue;
							maxLaneId = laneId;
						}
						checkedLanes.add(laneId);
					}
				}
			}
			if(maxQueue == 0) return null;
		} catch (Exception e) {
			throw new TrafficLightException("Error by calculating the queue length: " + id, e);
		}
		return maxLaneId;
	}
	
	public int getGreenPhaseForLane(String laneId) throws TrafficLightException {
		int signalIndex = -1;
		try {
			TraCILinkVectorVector controlledLinks = org.eclipse.sumo.libtraci.TrafficLight.getControlledLinks(id);
			searchLoop:
			for(int i = 0; i < controlledLinks.size(); i++) {
				TraCILinkVector links = controlledLinks.get(i);
				for(int j = 0; j < links.size(); j++) {
					if(links.get(j).getFromLane().equals(laneId)) {
						signalIndex = i;
						break searchLoop;
					}
				}
			}
			if(signalIndex == -1) return -1;
			
			TraCILogic logic = org.eclipse.sumo.libtraci.TrafficLight.getCompleteRedYellowGreenDefinition(id).get(0);
			
			for(int phaseIndex : greenPhaseIndices) {
				if(isLaneInGreenState(logic, phaseIndex, signalIndex)) return phaseIndex;
			}
			
			for(int phaseIndex : yellowPhaseIndices) {
				if(isLaneInGreenState(logic, phaseIndex, signalIndex)) return phaseIndex;
			}
			
			for(int phaseIndex : redPhaseIndices) {
				if(isLaneInGreenState(logic, phaseIndex, signalIndex)) return phaseIndex;
			}
		} catch (Exception e) {
			throw new TrafficLightException("Error during finding green phase: " + guiId, e);
		}
		return -1;
	}
	
	private boolean isLaneInGreenState(TraCILogic logic, int phaseIndex, int signalIndex) {
		String state = logic.getPhases().get(phaseIndex).getState();
		if(signalIndex < state.length()) {
			char c = state.charAt(signalIndex);
			return ( c == 'G' || c == 'g');
		}
		
		return false;
	}
	
	public void setPhase(int phaseIndex) throws TrafficLightException {
		try {
			org.eclipse.sumo.libtraci.TrafficLight.setPhase(id, phaseIndex);
			currentPhase = phaseIndex; 
		} catch (Exception e) {
			throw new TrafficLightException("Traffic light phase not updated: " + id, e);
		}
	}
	
	public void setPhaseDuration(double duration) throws TrafficLightException {
		try {
			org.eclipse.sumo.libtraci.TrafficLight.setPhaseDuration(id, duration);
		} catch (Exception e) {
			throw new TrafficLightException("Trafic light phase duration not updated: " + id, e);
		}
	}
	
	public String getId() {return id;}
	
	public int getCurrentPhase() {return currentPhase;}

	public String getCurrentProgram() {return currentProgram;}
	
	public double getPhaseDuration() throws TrafficLightException {
		try {
			return org.eclipse.sumo.libtraci.TrafficLight.getPhaseDuration(id);
		} catch (Exception e) {
			throw new TrafficLightException("Trafic light phase duration can`t pulled: " + id, e);
		}
	}
	
	public double getX() {return x;}

	public double getY() {return y;}
	
	public boolean isGreenPhase() {
		try {
			int phase = getCurrentPhase();
			if(greenPhaseIndices.contains(phase)) {
				//System.out.println("Green: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return true;
			}
			else {
				//System.out.println("NOT Green: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isRedPhase() {
		try {
			int phase = getCurrentPhase();
			if(redPhaseIndices.contains(phase)) {
				//System.out.println("Red: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return true;
			}
			else {
				//System.out.println("NOT Red: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isYellowPhase() {
		try {
			int phase = getCurrentPhase();
			if(yellowPhaseIndices.contains(phase)) {
				//System.out.println("Yellow: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return true;
			}
			else {
				//System.out.println("NOT Yellow: " + org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(id) + " " + guiId);
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public void switchToNextGreen() throws TrafficLightException {
		if(greenPhaseIndices.isEmpty()) return;
		
		for(int phaseIndex : greenPhaseIndices) {
			if (phaseIndex > currentPhase) {
				setPhase(phaseIndex);
				return;
			}
		}
		setPhase(greenPhaseIndices.get(0));
	}
	
	public void switchToNextYellow() throws TrafficLightException {
		if(yellowPhaseIndices.isEmpty()) return;
		
		for(int phaseIndex : yellowPhaseIndices) {
			if(phaseIndex > currentPhase) {
				setPhase(phaseIndex);
				return;
			}
		}
	}
	
	public void switchToNextRed() throws TrafficLightException {
		if(redPhaseIndices.isEmpty()) return;
		
		for(int phaseIndex : redPhaseIndices) {
			if(phaseIndex > currentPhase) {
				setPhase(phaseIndex);
				return;
			}
		}
		setPhase(redPhaseIndices.get(0));
	}
	
	public Color getPhaseColor() {
		if(redPhaseIndices.contains(getCurrentPhase())) return Color.RED;
		else if(greenPhaseIndices.contains(getCurrentPhase())) return Color.GREEN;
		else return Color.YELLOW;
	}
	
   
	public String getGuiId() {return guiId;	}
	
	public int calculateCharRepeat(String state, char controlChar) {
		int count = 0;
		for(int i = 0; i < state.length(); i++) {
			if((state.toLowerCase().charAt(i)) == controlChar) count++;
		}
		return count;
	}
	
	public String getProgramDefinition() {
		StringBuilder sb = new StringBuilder();
		
		TraCILogicVector logics = org.eclipse.sumo.libtraci.TrafficLight.getCompleteRedYellowGreenDefinition(id);
		if(logics.size() > 0) {
			TraCILogic logic = logics.get(0);
			sb.append("Traffic Light Id: " + getGuiId()).append("\n");
			sb.append("Current Phase: " + getCurrentPhase()).append("\n");
			sb.append("-------------------------------------------------\n");
			
			for(int i = 0; i < logic.getPhases().size(); i++) {
				TraCIPhase phase = logic.getPhases().get(i);
				String state = phase.getState();
				double duration = phase.getDuration();
				
				if(redPhaseIndices.contains(i)) {
					sb.append("Red Phase ").append(i).append(": [ ")
					.append(state).append(" ] ")
					.append("Default Duration: ").append(duration).append(" s");
				} else if(greenPhaseIndices.contains(i)) {
					sb.append("Green Phase ").append(i).append(": [ ")
					.append(state).append(" ] ")
					.append("Default Duration: ").append(duration).append(" s");
				} else {
					sb.append("Yellow Phase ").append(i).append(": [ ")
					.append(state).append(" ] ")
					.append("Default Duration: ").append(duration).append(" s");
				} 
				
				if(i == this.currentPhase) sb.append("<--- Active Phase");
				sb.append("\n");
			}
		}
	return sb.toString();
	}
	
}
