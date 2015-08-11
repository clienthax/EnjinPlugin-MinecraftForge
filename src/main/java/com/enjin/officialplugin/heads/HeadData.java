package com.enjin.officialplugin.heads;

public class HeadData
{
  String playername;
  String[] signdata;
  HeadLocation.Type type;
  int ranking = 0;
  String itemID = "";

  public HeadData(String playername, String[] signdata, HeadLocation.Type type, int ranking)
  {
    this.playername = playername;
    this.signdata = signdata;
    this.type = type;
    this.ranking = ranking;
  }

  public HeadData(String playername, String[] signdata, HeadLocation.Type type, int ranking, String itemID)
  {
    this.playername = playername;
    this.signdata = signdata;
    this.type = type;
    this.ranking = ranking;
    this.itemID = itemID;
  }

  public int getRanking()
  {
    return this.ranking;
  }

  public void setRanking(int ranking)
  {
    this.ranking = ranking;
  }

  public String getPlayername()
  {
    return this.playername;
  }

  public String[] getSigndata()
  {
    return this.signdata;
  }

  public void setSignData(String[] signdata)
  {
    this.signdata = signdata;
  }

  public HeadLocation.Type getType()
  {
    return this.type;
  }

  public void incrementRanking()
  {
    this.ranking += 1;
  }

  public String getItemID()
  {
    return this.itemID;
  }
}