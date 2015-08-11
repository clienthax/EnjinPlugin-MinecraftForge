package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.server.MinecraftServer;

public class Packet13ExecuteCommandAsPlayer
{
  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try
    {
      String name = PacketUtilities.readString(in);
      String command = PacketUtilities.readString(in);
    }
    catch (Throwable t)
    {
      MinecraftServer.getServer().logWarning("Failed to dispatch command via 0x13, " + t.getMessage());
      t.printStackTrace();
    }
  }
}