package com.enjin.officialplugin.threaded;

import com.enjin.officialplugin.EMPListener;

public class DelayedPlayerPermsUpdate implements Runnable
{
  EMPListener listener;
  String player;

  public DelayedPlayerPermsUpdate(EMPListener listener, String player)
  {
    this.player = player;
    this.listener = listener;
  }

  public void run()
  {
    this.listener.updatePlayerRanks(this.player);
  }
}