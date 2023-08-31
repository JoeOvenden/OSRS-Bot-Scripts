package gui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osbot.rs07.api.map.Position;

public class AgilityGUI extends GUI {
		
	enum Course {
		FALADOR,
		CANIFIS,
		VARROCK,
		AL_KHARID,
		DRAYNOR,
		GNOME_COURSE,
	}
	
	public AgilityGUI() {
		super();
		
		String[] courses = Stream.of(Course.values())
                .map(Enum::name)
                .collect(Collectors.toList()).toArray(new String[0]);
		addSelecter("Select course:", courses);
		
		packDialogue();
	}
	
	public String[] getObstacleNames() {
		Course course = Course.valueOf((String) selecters.get(0).getSelectedItem());
		if (course == Course.GNOME_COURSE) {
			return new String[] {"Log balance", "Obstacle net", "Tree branch", "Balancing rope", "Tree branch", "Obstacle net", "Obstacle pipe"};
		}
		else if (course == Course.DRAYNOR) {
			return new String[] {"Rough wall", "Tightrope", "Tightrope", "Narrow wall", "Wall", "Gap", "Crate"};
		}
		else if (course == Course.AL_KHARID) {
			return new String[] {"Rough wall", "Tightrope", "Cable", "Zip line", "Tropical Tree", "Roof top beams", "Tightrope", "Gap"};
		}
		else if (course == Course.VARROCK) {
			return new String[] {"Rough wall", "Clothes line", "Gap", "Wall", "Gap", "Gap", "Gap", "Ledge", "Edge"};
		}
		return null;
	}
	
	public Position getStartPosition() {
		Course course = Course.valueOf((String) selecters.get(0).getSelectedItem());
		if (course == Course.GNOME_COURSE) {
			return null;
		}
		else if (course == Course.DRAYNOR) {
			return new Position(3103, 3279, 0);
		}
		else if (course == Course.AL_KHARID) {
			return new Position (3273, 3195, 0);
		}
		else if (course == Course.VARROCK) {
			return new Position(3221, 3414, 0);
		}
		return null;
	}
	
	public int[] getExceptionIndex() {
		Course course = Course.valueOf((String) selecters.get(0).getSelectedItem());
		if (course == Course.AL_KHARID) {
			return new int[] {3};
		}
		else if (course == Course.VARROCK) {
			return new int[] {5, 6};
		}
		return new int[] {};
	}
	
	public Position[] getExceptionPositions() {
		Course course = Course.valueOf((String) selecters.get(0).getSelectedItem());
		if (course == Course.AL_KHARID) {
			return new Position[] {new Position(3301, 3163, 3)};
		}
		else if (course == Course.VARROCK) {
			return new Position[] {new Position(3208, 3402, 3),
					new Position(3232, 3402, 3)};
		}
		return null;
	}

}
