package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.ConfigValueTypes;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.server.MinecraftServer;

public class Packet15RemoteConfigUpdate
{
  String toReviewer = "The only time enjin will send these values is atthe request of the server owner in the Enjin control panel";

  public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin)
  {
    try {
      String values = PacketUtilities.readString(in);
      EnjinMinecraftPlugin.debug("Changing these values in the config: \"" + values);
      String[] splitvalues = values.split(",");
      for (String value : splitvalues) {
        String[] split = value.split(":");	
        if (plugin.configvalues.containsKey(split[0].toLowerCase())) {
          ConfigValueTypes type = (ConfigValueTypes)plugin.configvalues.get(split[0].toLowerCase());
          switch (type.ordinal()) {
          case 1:
            if (split[1].equals("0")) {
              plugin.config.set(split[0].toLowerCase(), false);
              plugin.config.save();
            } else {
              plugin.config.set(split[0].toLowerCase(), true);
              plugin.config.save();
            }
            break;
          case 2:
            plugin.config.set(split[0].toLowerCase(), split[1]);
            plugin.config.save();
            break;
          case 3:
            try {
              double number = Double.parseDouble(split[1]);
              plugin.config.set(split[0].toLowerCase(), number);
              plugin.config.save();
            } catch (NumberFormatException e) {
              MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not a double.");
            }
          case 4:
            try
            {
              float number = Float.parseFloat(split[1]);
              plugin.config.set(split[0].toLowerCase(), number);
              plugin.config.save();
            } catch (NumberFormatException e) {
              MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not a float.");
            }
          case 5:
            try
            {
              int number = Integer.parseInt(split[1]);
              plugin.config.set(split[0].toLowerCase(), number);
              plugin.config.save();
            } catch (NumberFormatException e) {
              MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not an int.");
            }

          case 6:
            MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Enjin tried setting the value " + split[0] + " to " + split[1] + " but was forbidden!");
          }
        } else {
          MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Enjin tried setting the value " + split[0] + " to " + split[1] + " but it doesn't exist!");
        }
      }
      plugin.initFiles();
    }
    catch (Throwable t) {
      MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Failed to set config variables via 0x15, " + t.getMessage());
      t.printStackTrace();
    }
  }
}