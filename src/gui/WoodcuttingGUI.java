package gui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;

public class WoodcuttingGUI extends GUI {
	
	enum Location {
		WOODCUTTING_GUILD,
		DRAYNOR,
		CAMELOT,
		HOSIDIUS,
		VARROCK_WEST
	}
	
	enum Tree {
		NORMAL,
		OAK,
		WILLOW,
		MAPLE,
		YEW,
		MAGIC,
		REDWOOD
	}
	
	public WoodcuttingGUI() {
		super();

		String[] locations = Stream.of(Location.values())
                .map(Enum::name)
                .collect(Collectors.toList()).toArray(new String[0]);
		String[] trees = Stream.of(Tree.values())
                .map(Enum::name)
                .collect(Collectors.toList()).toArray(new String[0]);
		
		addSelecter("Select location:", locations);
		addSelecter("Select tree:", trees);
		addCheckbox("Afk:");
		addCheckbox("Powerchop:");
		
		packDialogue();
	}
	
	public Location getLocation() {
		return Location.valueOf((String) selecters.get(0).getSelectedItem());
	}
	
	public Tree getTree() {
		return Tree.valueOf((String) selecters.get(1).getSelectedItem());
	}
	
	public Area getTreeArea() {
		Location loc = getLocation();
		Tree tree = getTree();
		if (loc == Location.DRAYNOR) {
			if (tree == Tree.WILLOW) {
				return new Area(3088, 3239, 3091, 3230);
			}
		}
		else if (loc == Location.WOODCUTTING_GUILD) {
			if (tree == Tree.MAPLE) {
				return new Area(1608, 3497, 1612, 3492);
			}
			else if (tree == Tree.YEW) {
				return new Area(1591, 3484, 1598, 3489);
			}
		}
		else if (loc == Location.VARROCK_WEST) {
			if (tree == Tree.NORMAL) {
				return new Area(3168, 3415, 3171, 3418);
			}
			else if (tree == Tree.OAK) {
				return new Area(3170, 3419, 3166, 3423);
			}
		}
		else if (loc == Location.CAMELOT) {
			if (tree == Tree.MAPLE) {
				return new Area(2720, 3499, 2732, 3503);
			}
		}
		else if (loc == Location.HOSIDIUS) {
			if (tree == Tree.OAK) {
				return new Area(1729, 3602, 1737, 3610);
			}
		}
		return null;
	}
	
	public Area getBankArea() {
		Location loc = getLocation();
		if (loc == Location.CAMELOT) {
			return new Area(2722, 3493, 2728, 3493);
		}
		else if (loc == Location.WOODCUTTING_GUILD) {
			return new Area(1591, 3476, 1593, 3476);
		}
		else if (loc == Location.VARROCK_WEST) {
			return new Area(3184, 3436, 3185, 3437);
		}
		else if (loc == Location.DRAYNOR) {
			return new Area(3092, 3245, 3092, 3243);
		}
		else if (loc == Location.HOSIDIUS) {
			return Banks.HOSIDIUS_HOUSE;
		}
		return null;
	}
	
	public String getLogName() {
		Tree tree = getTree();
		String logName = String.valueOf(tree).toLowerCase();
		if (tree == Tree.NORMAL) {
			logName = "Logs";
		}
		else {
			logName += " logs";
		}
		return logName;
	}
	
	public String getTreeName() {
		Tree tree = getTree();
		String logName = String.valueOf(tree).toLowerCase();
		if (tree == Tree.NORMAL) {
			logName = "Tree";
		}
		else {
			logName += " tree";
		}
		return logName;
	}
	
	public boolean getAFK() {
		return isCheckboxChecked(0);
	}
	
	public boolean getPowerChop() {
		return isCheckboxChecked(1);
	}
}
