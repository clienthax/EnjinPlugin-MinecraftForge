package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AsyncToSyncEventThrower
  implements Runnable
{
  ConcurrentLinkedQueue<Event> event = new ConcurrentLinkedQueue();
  boolean hasrun = false;
  EnjinMinecraftPlugin plugin;

  public AsyncToSyncEventThrower(EnjinMinecraftPlugin plugin)
  {
    this.plugin = plugin;
  }

  public AsyncToSyncEventThrower(Event event) {
    this.event.add(event);
  }

  public void addEvent(Event event) {
    this.event.add(event);
  }

  @Override
  public void run()
  {
    while (!this.event.isEmpty()) {
      Event ev = (Event)this.event.poll();
      MinecraftForge.EVENT_BUS.post(ev);
    }
  }

  boolean hasRun() {
    return this.event.isEmpty();
  }
}