package com.enjin.officialplugin.threaded;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.ReverseFileReader;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class ReportMakerThread
  implements Runnable
{
  EnjinMinecraftPlugin plugin;
  StringBuilder builder;
  EntityPlayerMP sender;

  public ReportMakerThread(EnjinMinecraftPlugin plugin, StringBuilder builder, EntityPlayerMP sender)
  {
    this.plugin = plugin;
    this.builder = builder;
    this.sender = sender;
  }

  public synchronized void run()
  {
    this.builder.append("\nLast Severe error message: \n");
    File serverloglocation = this.plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
    try {
      ReverseFileReader rfr = new ReverseFileReader(serverloglocation.getAbsolutePath() + File.separator + "server.log");
      LinkedList errormessages = new LinkedList();
      String line = "";
      boolean errorfound = false;
      while (((line = rfr.readLine()) != null) && (!errorfound)) {
        if (errormessages.size() >= 40) {
          errormessages.removeFirst();
        }
        errormessages.add(line);
        if (!line.contains("[SEVERE]"))
          continue;
        boolean severeended = false;
        while (((line = rfr.readLine()) != null) && (!severeended)) {
          if (line.contains("[SEVERE]")) {
            if (errormessages.size() >= 40) {
              errormessages.removeFirst();
            }
            errormessages.add(line); continue;
          }
          severeended = true;
        }

        for (int i = errormessages.size(); i > 0; i--) {
          this.builder.append((String)errormessages.get(i - 1) + "\n");
        }
        errorfound = true;
      }

      rfr.close();
    } catch (Exception e) {
      if (EnjinMinecraftPlugin.debug) {
        e.printStackTrace();
      }
    }
    if (this.plugin.lasterror != null) {
      this.builder.append("\nLast Enjin Plugin Severe error message: \n");
      this.builder.append(this.plugin.lasterror.toString());
    }
    this.builder.append("\n=========================================\nEnjin HTTPS test: " + (this.plugin.testHTTPSconnection() ? "passed" : "FAILED!") + "\n");
    this.builder.append("Enjin HTTP test: " + (this.plugin.testHTTPconnection() ? "passed" : "FAILED!") + "\n");
    this.builder.append("Enjin web connectivity test: " + (this.plugin.testWebConnection() ? "passed" : "FAILED!") + "\n");
    this.builder.append("Is mineshafter present: " + (EnjinMinecraftPlugin.isMineshafterPresent() ? "yes" : "no") + "\n=========================================\n");
    String fullreport;
    if (EnjinMinecraftPlugin.hash.length() > 0)
      fullreport = this.builder.toString().replaceAll(EnjinMinecraftPlugin.hash, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    else {
      fullreport = this.builder.toString();
    }
    System.out.println(fullreport);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    Date date = new Date();
    BufferedWriter outChannel = null;
    try {
      outChannel = new BufferedWriter(new FileWriter(serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt"));
      outChannel.write(fullreport);
      outChannel.close();
      if (this.sender == null)
        MinecraftServer.getServer().logInfo("Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!");
      else
        this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.GOLD + "Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!"));
    }
    catch (IOException e) {
      if (outChannel != null)
        try {
          outChannel.close();
        }
        catch (Exception e1) {
        }
      if (this.sender == null)
        MinecraftServer.getServer().logInfo("Unable to write enjin debug report!");
      else {
        this.sender.addChatComponentMessage(new ChatComponentText(ChatColor.DARK_RED + "Unable to write enjin debug report!"));
      }
      e.printStackTrace();
    }
  }
}