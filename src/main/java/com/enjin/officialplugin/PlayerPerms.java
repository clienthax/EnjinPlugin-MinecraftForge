package com.enjin.officialplugin;

public class PlayerPerms
{
  String playername = "";
  String worldname = "";

  public PlayerPerms(String player, String world) {
    this.playername = player;
    this.worldname = world;
  }

  public String getPlayerName() {
    return this.playername;
  }

  public String getWorldName() {
    return this.worldname;
  }
}