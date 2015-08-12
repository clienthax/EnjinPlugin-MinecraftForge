package com.enjin.officialplugin.threaded;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class NewKeyVerifier
  implements Runnable
{
  EnjinMinecraftPlugin plugin;
  String key;
  EntityPlayerMP sender;
  public boolean completed = false;
  public boolean pluginboot = true;

  public NewKeyVerifier(EnjinMinecraftPlugin plugin, String key, EntityPlayerMP sender, boolean pluginboot) {
    this.plugin = plugin;
    this.key = key;
    this.sender = sender;
    this.pluginboot = pluginboot;
  }

  @Override
  public synchronized void run()
  {
    if (this.pluginboot)
    {
      int i = 0;
      while (!this.plugin.testWebConnection())
      {
        i++; if (i > 5) {
          MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to connect to the internet to verify your key! Please check your internet connection.");
          EnjinMinecraftPlugin.enjinlogger.warning("Unable to connect to the internet to verify your key! Please check your internet connection.");
          i = 0;
        }
        try
        {
          wait(60000L);
        }
        catch (InterruptedException e)
        {
        }
      }
      int validation = keyValid(false, this.key);
      if (validation == 1) {
        this.plugin.authkeyinvalid = false;
        EnjinMinecraftPlugin.debug("Key valid.");
        this.plugin.startTask();
        this.plugin.registerEvents();
      } else if (validation == 0) {
        this.plugin.authkeyinvalid = true;
        MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Invalid key! Please regenerate your key and try again.");
        EnjinMinecraftPlugin.enjinlogger.warning("Invalid key! Please regenerate your key and try again.");
      } else {
        this.plugin.authkeyinvalid = true;
        MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
        EnjinMinecraftPlugin.enjinlogger.warning("There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
      }
      this.completed = true;
    } else {
      if (this.key.equals(EnjinMinecraftPlugin.getHash())) {
        if (this.sender == null)
          MinecraftServer.getServer().logInfo("The specified key and the existing one are the same!");
        else {
          this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.YELLOW + "The specified key and the existing one are the same!"));
        }
        this.completed = true;
        return;
      }
      int validation = keyValid(true, this.key);
      if (validation == 0) {
        if (this.sender == null)
          MinecraftServer.getServer().logInfo("That key is invalid! Make sure you've entered it properly!");
        else {
          this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "That key is invalid! Make sure you've entered it properly!"));
        }
        this.plugin.stopTask();
        this.plugin.unregisterEvents();
        this.completed = true;
        return;
      }if (validation == 2) {
        if (this.sender == null)
          MinecraftServer.getServer().logInfo("There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
        else {
          this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)"));
        }
        this.plugin.stopTask();
        this.plugin.unregisterEvents();
        this.completed = true;
        return;
      }
      this.plugin.authkeyinvalid = false;
      EnjinMinecraftPlugin.setHash(this.key);
      EnjinMinecraftPlugin.debug("Writing hash to file.");
      this.plugin.config.set("authkey", this.key);
      this.plugin.config.save();
      if (this.sender == null)
        MinecraftServer.getServer().logInfo("Set the enjin key to " + this.key);
      else {
        this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Set the enjin key to " + this.key));
      }
      this.plugin.stopTask();
      this.plugin.unregisterEvents();
      this.plugin.startTask();
      this.plugin.registerEvents();
      this.completed = true;
    }
    this.completed = true;
  }

  private int keyValid(boolean save, String key)
  {
    if ((EnjinMinecraftPlugin.usingSSL) && (!this.plugin.testHTTPSconnection())) {
      EnjinMinecraftPlugin.usingSSL = false;
      MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
      EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
    }
    try {
      if (key == null) {
        return 0;
      }
      if (key.length() < 2) {
        return 0;
      }
      if (save) {
        return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", new String[] { "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport, "save=1" });
      }
      return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", new String[] { "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport });
    }
    catch (Throwable t) {
      MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
      t.printStackTrace();
      this.plugin.lasterror = new EnjinErrorReport(t, "Verifying key when error was thrown:");
      EnjinMinecraftPlugin.enjinlogger.warning("There was an error synchronizing game data to the enjin server." + this.plugin.lasterror.toString());
    }return 2;
  }
}