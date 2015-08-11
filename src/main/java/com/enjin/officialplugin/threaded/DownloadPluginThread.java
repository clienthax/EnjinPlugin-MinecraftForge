package com.enjin.officialplugin.threaded;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import net.minecraft.server.MinecraftServer;

public class DownloadPluginThread
  implements Runnable
{
  String downloadlocation = "";
  File destination;
  EnjinMinecraftPlugin plugin;
  String versionnumber;

  public DownloadPluginThread(String downloadlocation, String versionnumber, File destination, EnjinMinecraftPlugin plugin)
  {
    this.downloadlocation = downloadlocation;
    this.versionnumber = versionnumber;
    this.destination = destination;
    this.plugin = plugin;
  }

  public void run()
  {
    File tempfile = new File(this.downloadlocation + File.separator + "EnjinMinecraftPlugin.zip.part");
    try
    {
      EnjinMinecraftPlugin.debug("Connecting to url http://resources.guild-hosting.net/1/downloads/emp/" + this.versionnumber + "/EnjinMinecraftPlugin.zip");
      URL website = new URL("http://resources.guild-hosting.net/1/downloads/emp/" + this.versionnumber + "/EnjinMinecraftPlugin.zip");
      ReadableByteChannel rbc = Channels.newChannel(website.openStream());
      FileOutputStream fos = new FileOutputStream(tempfile);
      fos.getChannel().transferFrom(rbc, 0L, 16777216L);
      fos.close();
      if ((this.destination.delete()) && (tempfile.renameTo(this.destination))) {
        this.plugin.hasupdate = true;
        this.plugin.newversion = this.versionnumber;
        MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Enjin Minecraft plugin was updated to version " + this.versionnumber + ". Please restart your server.");
        return;
      }
      this.plugin.updatefailed = true;
      MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to update to new version. Please update manually!");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    this.plugin.hasupdate = false;
  }
}