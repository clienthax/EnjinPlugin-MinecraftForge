package com.enjin.officialplugin.heads;

import net.minecraft.world.World;

public class HeadLocation
{
  Location signLoc;
  Location headLoc;
  Type type;
  int position = 0;
  String itemid = "";

  public HeadLocation(World worldObj, int signx, int signy, int signz, Type type, int position)
  {
    this.signLoc = new Location(worldObj, signx, signy, signz);
    this.type = type;
    this.position = position;
  }

  public HeadLocation(String world, int signx, int signy, int signz, Type type, int position)
  {
    this.signLoc = new Location(world, signx, signy, signz);
    this.type = type;
    this.position = position;
  }

  public HeadLocation(World worldObj, int headx, int heady, int headz, int signx, int signy, int signz, Type type, int position)
  {
    this.signLoc = new Location(worldObj, signx, signy, signz);
    this.headLoc = new Location(worldObj, headx, heady, headz);
    this.type = type;
    this.position = position;
  }

  public HeadLocation(String world, int headx, int heady, int headz, int signx, int signy, int signz, Type type, int position)
  {
    this.signLoc = new Location(world, signx, signy, signz);
    this.headLoc = new Location(world, headx, heady, headz);
    this.type = type;
    this.position = position;
  }

  public Location getSignLocation()
  {
    return this.signLoc;
  }

  public Location getHeadLocation()
  {
    return this.headLoc;
  }

  public Type getType()
  {
    return this.type;
  }

  public int getPosition()
  {
    return this.position;
  }

  public boolean hasHead()
  {
    return this.headLoc != null;
  }

  public String getWorld() {
    return this.signLoc.getWorld();
  }

  public World getWorldObj() {
    return this.signLoc.getWorldObj();
  }

  public int getHeadx() {
    return this.headLoc.getX();
  }

  public int getHeady() {
    return this.headLoc.getY();
  }

  public int getHeadz() {
    return this.headLoc.getZ();
  }

  public int getSignx() {
    return this.signLoc.getX();
  }

  public int getSigny() {
    return this.signLoc.getY();
  }

  public int getSignz() {
    return this.signLoc.getZ();
  }

  public String getItemid() {
    return this.itemid;
  }

  public void setItemid(String itemid)
  {
    this.itemid = itemid;
  }

  public static enum Type
  {
    TopDailyVoter, 
    TopWeeklyVoter, 
    TopMonthlyVoter, 
    RecentVoter, 
    RecentDonator, 
    RecentItemDonator, 
    TopPlayer, 
    TopPoster, 
    TopLikes, 
    LatestMembers, 
    TopPoints, 
    TopPointsMonth, 
    TopPointsWeek, 
    TopPointsDay, 
    TopDonators, 
    TopDonatorsDay, 
    TopDonatorsWeek, 
    TopDonatorsMonth, 
    TopPointsDonators, 
    TopPointsDonatorsDay, 
    TopPointsDonatorsWeek, 
    TopPointsDonatorsMonth;
  }
}