package com.enjin.officialplugin.points;

class PointsSyncClass
  implements Runnable
{
  String playername;
  int points;
  PointsAPI.Type type;

  public PointsSyncClass(String playername, int points, PointsAPI.Type type)
  {
    this.playername = playername;
    this.points = points;
    this.type = type;
  }

  @Override
  public synchronized void run()
  {
    try {
      PointsAPI.modifyPointsToPlayer(this.playername, this.points, this.type);
    }
    catch (NumberFormatException e)
    {
    }
    catch (PlayerDoesNotExistException e)
    {
    }
    catch (ErrorConnectingToEnjinException e)
    {
    }
  }
}