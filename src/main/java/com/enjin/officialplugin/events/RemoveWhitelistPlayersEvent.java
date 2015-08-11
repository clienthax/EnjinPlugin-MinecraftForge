package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class RemoveWhitelistPlayersEvent extends Event
{
  String[] players;

  public RemoveWhitelistPlayersEvent(String[] players)
  {
    this.players = players;
  }

  public String[] getPlayers() {
    return this.players;
  }
}