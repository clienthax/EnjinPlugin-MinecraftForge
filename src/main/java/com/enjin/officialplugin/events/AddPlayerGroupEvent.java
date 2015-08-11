package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class AddPlayerGroupEvent extends Event
{
  String player;
  String groupname;
  String world;

  public AddPlayerGroupEvent(String player, String groupname, String world)
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