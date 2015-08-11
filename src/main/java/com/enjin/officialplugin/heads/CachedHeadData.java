package com.enjin.officialplugin.heads;

import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;

import net.minecraftforge.common.MinecraftForge;

public class CachedHeadData
{
  EnjinMinecraftPlugin plugin;
  ConcurrentHashMap<HeadLocation.Type, ConcurrentHashMap<Integer, HeadData>> headdata = new ConcurrentHashMap();
  ConcurrentHashMap<String, ConcurrentHashMap<Integer, HeadData>> itemheaddata = new ConcurrentHashMap();

  public CachedHeadData(EnjinMinecraftPlugin plugin) {
    this.plugin = plugin;
  }

  public void addToHead(HeadData head, boolean callUpdate)
  {
    if (head.getType() == HeadLocation.Type.RecentItemDonator) {
      ConcurrentHashMap mapdata = (ConcurrentHashMap)this.itemheaddata.get(head.getItemID().toLowerCase());

      if (mapdata == null) {
        mapdata = new ConcurrentHashMap();
        mapdata.put(Integer.valueOf(0), head);
        this.itemheaddata.put(head.getItemID().toLowerCase(), mapdata);
        if (callUpdate) {
          MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
        }
        return;
      }

      for (int i = 3; i >= 0; i--) {
        HeadData data = (HeadData)mapdata.get(Integer.valueOf(i + 1));
        if (data != null) {
          data.incrementRanking();
          data.setSignData(this.plugin.cachedItems.updateSignData(data.getSigndata(), data.getType(), data.getRanking()));
          mapdata.put(Integer.valueOf(i + 1), data);
        }
      }

      mapdata.put(Integer.valueOf(0), head);
      if (callUpdate)
        MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
    }
    else {
      ConcurrentHashMap mapdata = (ConcurrentHashMap)this.headdata.get(head.getType());

      if (mapdata == null) {
        mapdata = new ConcurrentHashMap();
        mapdata.put(Integer.valueOf(0), head);
        this.headdata.put(head.getType(), mapdata);
        if (callUpdate) {
          MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
        }
        return;
      }

      for (int i = 9; i >= 0; i--) {
        HeadData data = (HeadData)mapdata.get(Integer.valueOf(i));
        if (data != null) {
          data.incrementRanking();
          data.setSignData(this.plugin.cachedItems.updateSignData(data.getSigndata(), data.getType(), data.getRanking()));
          mapdata.put(Integer.valueOf(i + 1), data);
        }
      }

      mapdata.put(Integer.valueOf(0), head);
      if (callUpdate)
        MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
    }
  }

  public void setHead(HeadData head, boolean callUpdate)
  {
    if (head.getType() == HeadLocation.Type.RecentItemDonator) {
      ConcurrentHashMap mapdata = (ConcurrentHashMap)this.itemheaddata.get(head.getItemID().toLowerCase());

      if (mapdata == null) {
        mapdata = new ConcurrentHashMap();
        this.itemheaddata.put(head.getItemID().toLowerCase(), mapdata);
      }
      mapdata.put(Integer.valueOf(head.getRanking()), head);
      if (callUpdate) {
        MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
      }
      return;
    }
    ConcurrentHashMap mapdata = (ConcurrentHashMap)this.headdata.get(head.getType());

    if (mapdata == null) {
      mapdata = new ConcurrentHashMap();
      this.headdata.put(head.getType(), mapdata);
    }
    mapdata.put(Integer.valueOf(head.getRanking()), head);
    if (callUpdate)
      MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(head.getType()));
  }

  public void setHeads(HeadLocation.Type type, HeadData[] head)
  {
    if (type == HeadLocation.Type.RecentItemDonator) {
      ConcurrentHashMap mapdata = new ConcurrentHashMap();
      for (int i = 0; i < head.length; i++) {
        mapdata.put(Integer.valueOf(i), head[i]);
      }
      this.itemheaddata.put(head[0].getItemID().toLowerCase(), mapdata);
    } else {
      ConcurrentHashMap mapdata = new ConcurrentHashMap();
      for (int i = 0; i < head.length; i++) {
        mapdata.put(Integer.valueOf(i), head[i]);
      }
      this.headdata.put(type, mapdata);
    }
    MinecraftForge.EVENT_BUS.post(new HeadsUpdatedEvent(type));
  }

  public HeadData getHead(HeadLocation.Type type, int rank, String itemId)
  {
    if (type == HeadLocation.Type.RecentItemDonator) {
      ConcurrentHashMap typelist = (ConcurrentHashMap)this.itemheaddata.get(itemId.toLowerCase());
      if (typelist == null) {
        return null;
      }
      return (HeadData)typelist.get(Integer.valueOf(rank));
    }
    ConcurrentHashMap typelist = (ConcurrentHashMap)this.headdata.get(type);
    if (typelist == null) {
      return null;
    }
    return (HeadData)typelist.get(Integer.valueOf(rank));
  }

  public void clearHeadData()
  {
    this.headdata.clear();
    this.itemheaddata.clear();
  }

  public void clearHeadData(HeadLocation.Type type)
  {
    if (type == HeadLocation.Type.RecentItemDonator)
      this.itemheaddata.clear();
    else
      this.headdata.remove(type);
  }
}