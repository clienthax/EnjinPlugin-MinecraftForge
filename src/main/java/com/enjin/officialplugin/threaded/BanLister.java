package com.enjin.officialplugin.threaded;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.server.MinecraftServer;

public class BanLister
  implements Runnable
{
  ConcurrentHashMap<String, String> currentbannedplayers = new ConcurrentHashMap();
  EnjinMinecraftPlugin plugin;

  public BanLister(EnjinMinecraftPlugin plugin)
  {
    this.plugin = plugin;
    String[] bannedplayerlist = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getKeys();
    for (String player : bannedplayerlist)
      this.currentbannedplayers.put(player.toLowerCase(), "");
  }

  @Override
  public void run()
  {
    EnjinMinecraftPlugin.debug("Scanning banned player list");
    String[] bannedplayerlist = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getKeys();
    HashMap lowercasebans = new HashMap();

    for (String player : bannedplayerlist) {
      lowercasebans.put(player.toLowerCase(), "");
      if (!this.currentbannedplayers.containsKey(player.toLowerCase())) {
        this.currentbannedplayers.put(player.toLowerCase(), "");
        this.plugin.bannedplayers.put(player.toLowerCase(), "");
        EnjinMinecraftPlugin.debug("Adding banned player " + player);
      }
    }

    Set<String> keys = this.currentbannedplayers.keySet();
    for (String player : keys)
      if (!lowercasebans.containsKey(player)) {
        this.currentbannedplayers.remove(player);
        this.plugin.pardonedplayers.put(player, "");
        EnjinMinecraftPlugin.debug(player + " was pardoned. Adding to pardoned list.");
      }
  }

  public synchronized void addBannedPlayer(String name)
  {
    this.currentbannedplayers.put(name.toLowerCase(), "");
  }

  public synchronized void pardonBannedPlayer(String name)
  {
    this.currentbannedplayers.remove(name.toLowerCase());
  }

  public synchronized boolean playerIsBanned(String name)
  {
    return this.currentbannedplayers.containsKey(name.toLowerCase());
  }
}