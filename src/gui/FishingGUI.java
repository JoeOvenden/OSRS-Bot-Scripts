package gui;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osbot.rs07.api.ui.Skill;

public class FishingGUI extends GUI {
	
	Fishing type = Fishing.SMALL_NET_FISHING;
	
	enum Fishing {
		SMALL_NET_FISHING,
		BAIT_FISHING,
		FLY_FISHING,
		BARBARIAN_FISHING,
	}
	
	public FishingGUI() {
		super();
		
		String[] types = Stream.of(Fishing.values())
                .map(Enum::name)
                .collect(Collectors.toList()).toArray(new String[0]);
		addSelecter("Select fishing type", types);
		
		packDialogue();
	}
	
	public void getFishingType() {
		type = Fishing.valueOf((String) selecters.get(0).getSelectedItem());
	}
	
	public String[] getRequiredItems() {
		getFishingType();
		if (type == Fishing.SMALL_NET_FISHING) {
			return new String[] {"Small fishing net"};
		}
		else if (type == Fishing.BAIT_FISHING) {
			return new String[] {"Fishing rod", "Fishing bait"};
		}
		else if (type == Fishing.FLY_FISHING) {
			return new String[] {"Fly fishing rod", "Feather"};
		}
		else if (type == Fishing.BARBARIAN_FISHING) {
			return new String[] {"Barbarian rod", "Feather"};
		}
		return new String[] {};
	}
	
	public Skill[] getSkills() {
		getFishingType();
		if (type == Fishing.BARBARIAN_FISHING) {
			return new Skill[] {Skill.FISHING, Skill.STRENGTH, Skill.AGILITY};
		}
		return new Skill[] {Skill.FISHING};
	}
	
	public String getSpotText() {
		Fishing[] exceptions = {Fishing.FLY_FISHING};
		getFishingType();
		
		if (Arrays.asList(exceptions).contains(type)) {
			if (type == Fishing.FLY_FISHING) {
				return "Rod Fishing spot";
			}
		}
		return "Fishing spot";
	}
	
	public String getLureText() {
		getFishingType();
		if (type == Fishing.SMALL_NET_FISHING) {
			return "Small net";
		}
		else if (type == Fishing.BAIT_FISHING) {
			return "Bait";
		}
		else if (type == Fishing.FLY_FISHING) {
			return "Lure";
		}
		else if (type == Fishing.BARBARIAN_FISHING) {
			return "Use-rod";
		}
		return "";
	}

}
