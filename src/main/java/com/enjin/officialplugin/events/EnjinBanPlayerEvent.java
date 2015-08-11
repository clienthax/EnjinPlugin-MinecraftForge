package com.enjin.officialplugin.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class EnjinBanPlayerEvent extends Event
{
  String[] players;
  boolean iscanceled = false;

  public EnjinBanPlayerEvent(String[] players) {
    this.players = players;
  }

  public String[] getBannedPlayers() {
    return this.players;
  }

  public void setBannedPlayers(String[] players) {
    this.players = players;
  }

  public boolean isCancelled() {
    return this.iscanceled;
  }

  public void setCancelled(boolean cancel) {
    this.iscanceled = cancel;
  }
}