package com.enjin.officialplugin.heads;

import net.minecraft.world.World;

public class Location
{
  private int x;
  private int y;
  private int z;
  private String world;
  private World worldObj;

  public Location(String world, int x, int y, int z)
  {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Location(World worldObj, int x, int y, int z)
  {
    this.world = worldObj.getWorldInfo().getWorldName();
    this.worldObj = worldObj;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int getX()
  {
    return this.x;
  }

  public int getY()
  {
    return this.y;
  }

  public int getZ()
  {
    return this.z;
  }

  public String getWorld()
  {	
    return this.world;
  }

  public World getWorldObj() {
    if (this.worldObj == null) {
      return this.worldObj = HeadUtils.getWorldByName(this.world);
    }
    return this.worldObj;
  }
}