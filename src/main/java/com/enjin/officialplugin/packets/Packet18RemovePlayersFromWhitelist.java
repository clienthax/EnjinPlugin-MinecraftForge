package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;

public class Packet18RemovePlayersFromWhitelist {
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String players = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Removing these players from the whitelist: " + players);
			String[] msg = players.split(",");

			if (msg.length > 0) {
				for (int i = 0; i < msg.length; i++) {
					GameProfile playerprofile = null;
					for (GameProfile profile : MinecraftServer.getServer().getGameProfiles()) {
						if (profile.getName().equals(msg[i])) {
							playerprofile = profile;
							break;
						}
					}
					MinecraftServer.getServer().getConfigurationManager().removePlayerFromWhitelist(playerprofile);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}