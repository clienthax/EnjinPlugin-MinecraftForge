package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class RemovePlayerGroupEvent extends Event
{
  String player;
  String groupname;
  String world;

  public RemovePlayerGroupEvent(String player, String groupname, String world)
  {
    this.player = player;
    this.groupname = groupname;
    this.world = world;
  }

  public String getPlayer() {
    return this.player;
  }

  public String getGroupname() {
    return this.groupname;
  }

  public String getWorld() {
    return this.world;
  }
}