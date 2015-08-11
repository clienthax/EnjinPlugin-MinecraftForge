package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class AddWhitelistPlayersEvent extends Event
{
  String[] players;

  public AddWhitelistPlayersEvent(String[] players)
  {
    this.players = players;
  }

  public String[] getPlayers() {
    return this.players;
  }
}