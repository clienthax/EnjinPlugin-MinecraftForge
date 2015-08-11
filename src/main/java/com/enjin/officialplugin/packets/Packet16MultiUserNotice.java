package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.EnjinConsole;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class Packet16MultiUserNotice
{
  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try
    {
      String players = PacketUtilities.readString(in);
      String message = PacketUtilities.readString(in);
      EnjinMinecraftPlugin.debug("Sending the following message to these users: " + players);
      EnjinMinecraftPlugin.debug(message);
      message = EnjinConsole.translateColorCodes(message);
      String[] splitvalues = players.split(",");
      for (String playerstring : splitvalues) {
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(playerstring);
        if (player != null)
          player.addChatMessage(new ChatComponentText(message));
      }
    }
    catch (Throwable t)
    {
      MinecraftServer.getServer().logWarning("Failed to send message to players., " + t.getMessage());
      t.printStackTrace();
    }
  }
}