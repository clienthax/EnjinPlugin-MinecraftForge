package com.enjin.officialplugin.listeners;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NewPlayerChatListener
{
  EnjinMinecraftPlugin plugin;

  public NewPlayerChatListener(EnjinMinecraftPlugin plugin)
  {
    this.plugin = plugin;
  }
  @SubscribeEvent
  public void playerChatEvent(ServerChatEvent event) {
    if (event.isCanceled())
      return;
  }
}