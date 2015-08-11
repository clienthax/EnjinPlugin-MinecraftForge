package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Date;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;

public class Packet1BPardonPlayers
{
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
	{
		try
		{
			String players = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Removing these players from the banlist: " + players);
			String[] msg = players.split(",");

			if (msg.length > 0)
				for (int i = 0; i < msg.length; i++) {
					GameProfile gameprofile = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(msg[i]);
					UserListBansEntry userlistbansentry = new UserListBansEntry(gameprofile, (Date)null, msg[i], (Date)null, "Banned from website.");
					MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().removeEntry(userlistbansentry);
					plugin.banlistertask.pardonBannedPlayer(msg[i]);
				}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}