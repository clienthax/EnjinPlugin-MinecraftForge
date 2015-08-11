package com.enjin.officialplugin.packets;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Packet11RemovePlayerGroup
{
  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try
    {
      String instring = PacketUtilities.readString(in);
      EnjinMinecraftPlugin.debug("Read string: " + instring);
      String[] msg = instring.split(",");
      if ((msg.length == 2) || (msg.length == 3)) {
        String playername = msg[0];
        String groupname = msg[1];
        String world = msg.length == 3 ? msg[2] : null;
        if ("*".equals(world)) {
          world = null;
        }

      }

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}