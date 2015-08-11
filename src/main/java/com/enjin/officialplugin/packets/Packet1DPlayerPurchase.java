package com.enjin.officialplugin.packets;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ShopListener;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Packet1DPlayerPurchase
{
  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try
    {
      String players = PacketUtilities.readString(in);
      EnjinMinecraftPlugin.debug("Removing these player's buffered buy lists: " + players);
      String[] msg = players.split(",");
      if (msg.length > 0)
        for (int i = 0; i < msg.length; i++)
          plugin.shoplistener.removePlayer(msg[i]);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}