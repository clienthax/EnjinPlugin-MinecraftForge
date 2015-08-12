package com.enjin.officialplugin.points;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class PlayerUtil
{
  public static EntityPlayer getPlayer(String playerName)
  {
    for (Iterator i$ = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator(); i$.hasNext(); ) { Object o = i$.next();
      EntityPlayer player = (EntityPlayer)o;
      if (player.getCommandSenderName().equalsIgnoreCase(playerName)) {
        return player;
      }
    }
    return null;
  }
}