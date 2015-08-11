package com.enjin.officialplugin.heads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.yaml.InvalidYamlConfigurationException;
import com.enjin.officialplugin.yaml.YamlConfigSection;
import com.enjin.officialplugin.yaml.YamlConfigWriter;

public class HeadLocations
{
  ConcurrentHashMap<HeadLocation.Type, ArrayList<HeadLocation>> headlist = new ConcurrentHashMap();
  ConcurrentHashMap<String, HeadLocation> locheadlist = new ConcurrentHashMap();

  public void outputHeads() {
    EnjinMinecraftPlugin.debug("<Head Database Start>");

    for (Map.Entry entry : this.headlist.entrySet()) {
      EnjinMinecraftPlugin.debug(((HeadLocation.Type)entry.getKey()).name() + ":");
      for (HeadLocation headLoc : (ArrayList<HeadLocation>)entry.getValue()) {
        EnjinMinecraftPlugin.debug("Sign: " + signLocationToString(headLoc));
        if (headLoc.hasHead()) {
          EnjinMinecraftPlugin.debug("Head: " + signLocationToString(headLoc));
        }
      }
    }

    EnjinMinecraftPlugin.debug("<Head Database End>");
  }

  public void addHead(HeadLocation head)
  {
    ArrayList heads = (ArrayList)this.headlist.get(head.getType());
    if (heads == null) {
      heads = new ArrayList();
      this.headlist.put(head.getType(), heads);
    }
    heads.add(head);
    this.locheadlist.put(signLocationToString(head), head);
    if (head.hasHead())
      this.locheadlist.put(headLocationToString(head), head);
  }

  public void removeHead(Location loc)
  {
    HeadLocation head = (HeadLocation)this.locheadlist.get(locationToString(loc));
    if (head != null) {
      ArrayList heads = (ArrayList)this.headlist.get(head.getType());
      if (heads != null) {
        heads.remove(head);
      }
      this.locheadlist.remove(signLocationToString(head));
      if (head.hasHead())
        this.locheadlist.remove(headLocationToString(head));
    }
  }

  public ArrayList<HeadLocation> getHeads(HeadLocation.Type type)
  {
    ArrayList heads = (ArrayList)this.headlist.get(type);
    if (heads == null) {
      return new ArrayList();
    }
    return heads;
  }

  public boolean hasHeadHere(Location loc)
  {
    return this.locheadlist.containsKey(locationToString(loc));
  }

  public String locationToString(Location loc)
  {
    return loc.getWorld() + "." + loc.getX() + "." + loc.getY() + "." + loc.getZ();
  }

  public String headLocationToString(HeadLocation head) {
    return head.getWorld() + "." + head.getHeadx() + "." + head.getHeady() + "." + head.getHeadz();
  }

  public String signLocationToString(HeadLocation sign) {
    return sign.getWorld() + "." + sign.getSignx() + "." + sign.getSigny() + "." + sign.getSignz();
  }

  public void loadHeads()
  {
    this.headlist.clear();
    this.locheadlist.clear();
    File headsfile = new File(EnjinMinecraftPlugin.datafolder, "heads.yml");
    YamlConfigSection headsconfig = new YamlConfigSection();
    try {
      headsconfig.load(headsfile);
      YamlConfigSection headsection = headsconfig.getConfigSection("heads");
      if (headsection != null) {
        Set<String> keys = headsection.getValues(false).keySet();
        for (String key : keys) {
        YamlConfigSection theheads = headsection.getConfigSection(key);
          HeadLocation.Type type = HeadLocation.Type.valueOf(key);
          if (theheads != null) {
            Set<String> theheadids = theheads.getValues(false).keySet();
            for (String headid : theheadids) {
              boolean hashead = theheads.getBoolean(headid + ".hashead", false);
              String worldString = theheads.getString(headid + ".world", "world");
              int signx = theheads.getInt(headid + ".signx", 0);
              int signy = theheads.getInt(headid + ".signy", 0);
              int signz = theheads.getInt(headid + ".signz", 0);
              int position = theheads.getInt(headid + ".position", 0);
              HeadLocation hl;
              if (hashead) {
                int headx = theheads.getInt(headid + ".headx", 0);
                int heady = theheads.getInt(headid + ".heady", 0);
                int headz = theheads.getInt(headid + ".headz", 0);
                hl = new HeadLocation(worldString, headx, heady, headz, signx, signy, signz, type, position);
              }
              else {
                hl = new HeadLocation(worldString, signx, signy, signz, type, position);
              }
              if (type == HeadLocation.Type.RecentItemDonator) {
                String itemid = theheads.getString(headid + ".itemid", "");
                hl.setItemid(itemid);
              }
              addHead(hl);
            }
          }
        }
      }
    }
    catch (FileNotFoundException e)
    {
    }
    catch (IOException e)
    {
    }
    catch (InvalidYamlConfigurationException e)
    {
      YamlConfigSection headsection;
      YamlConfigSection theheads;
      HeadLocation.Type type;
      e.printStackTrace();
    }
  }

  public void saveHeads()
  {
    File headsfile = new File(EnjinMinecraftPlugin.datafolder, "heads.yml");
    YamlConfigWriter headsconfig = new YamlConfigWriter();
    Enumeration theheads = this.locheadlist.elements();
    int headid = 0;
    while (theheads.hasMoreElements()) {
      HeadLocation head = (HeadLocation)theheads.nextElement();
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".hashead", Boolean.valueOf(head.hasHead()));
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".world", head.getWorld());
      if (head.hasHead()) {
        headsconfig.set("heads." + head.getType().toString() + "." + headid + ".headx", Integer.valueOf(head.getHeadx()));
        headsconfig.set("heads." + head.getType().toString() + "." + headid + ".heady", Integer.valueOf(head.getHeady()));
        headsconfig.set("heads." + head.getType().toString() + "." + headid + ".headz", Integer.valueOf(head.getHeadz()));
      }
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signx", Integer.valueOf(head.getSignx()));
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signy", Integer.valueOf(head.getSigny()));
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signz", Integer.valueOf(head.getSignz()));
      headsconfig.set("heads." + head.getType().toString() + "." + headid + ".position", Integer.valueOf(head.getPosition()));
      if (head.getType() == HeadLocation.Type.RecentItemDonator) {
        headsconfig.set("heads." + head.getType().toString() + "." + headid + ".itemid", head.getItemid());
      }
      headid++;
    }
    try {
      headsconfig.save(headsfile);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}