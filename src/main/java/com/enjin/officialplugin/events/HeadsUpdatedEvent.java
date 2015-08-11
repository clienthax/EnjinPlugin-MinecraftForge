package com.enjin.officialplugin.events;

import com.enjin.officialplugin.heads.HeadLocation;

import net.minecraftforge.fml.common.eventhandler.Event;

public class HeadsUpdatedEvent extends Event
{
  HeadLocation.Type type;
  String itemId;

  public HeadsUpdatedEvent(HeadLocation.Type type)
  {
    this.type = type;
  }

  public HeadsUpdatedEvent(HeadLocation.Type type, String itemId)
  {
    this.type = type;
    this.itemId = itemId;
  }

  public HeadLocation.Type getType() {
    return this.type;
  }

  public String getItemId() {
    return this.itemId;
  }
}