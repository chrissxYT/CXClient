package de.chrissx.mods.chat;

import de.chrissx.mods.Mod;
import net.minecraft.util.IChatComponent;

public class RewiWords extends Mod {

	public RewiWords() {
		super("RewiWords", "rewiwords");
	}

	@Override
	public void onChatMessage(IChatComponent component) {
		String s = component.getUnformattedText();
		if(s.contains("GetDown") && s.contains("Welches Wort ist gesucht")) {
			System.out.println("Got RewiWords message: " + s);
		}
	}
}
