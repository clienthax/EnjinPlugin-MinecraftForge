package com.enjin.officialplugin.packets;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.CommandExecuter;
import java.io.BufferedInputStream;
import net.minecraft.server.MinecraftServer;

public class Packet12ExecuteCommand
{
  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try
    {
      String command = PacketUtilities.readString(in);
      EnjinMinecraftPlugin.debug("Executing command \"" + command + "\" as console.");
      plugin.commandqueue.addCommand(command);
    }
    catch (Throwable t) {
      MinecraftServer.getServer().logWarning("Failed to dispatch command via 0x12, " + t.getMessage());
      t.printStackTrace();
    }
  }
}