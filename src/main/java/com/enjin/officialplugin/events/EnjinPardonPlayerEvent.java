package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class EnjinPardonPlayerEvent extends Event
{
  boolean iscanceled = false;
  String[] players;

  public EnjinPardonPlayerEvent(String[] players)
  {
    this.players = players;
  }

  public String[] getPardonedPlayers() {
    return this.players;
  }

  public void setPardonedPlayers(String[] players) {
    this.players = players;
  }

  public boolean isCancelled() {
    return this.iscanceled;
  }

  public void setCancelled(boolean cancel) {
    this.iscanceled = cancel;
  }
}