package com.enjin.officialplugin.shop;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class PlayerHistoryGetter
  implements Runnable
{
  EntityPlayerMP player;
  ShopListener listener;
  String historyplayer;

  public PlayerHistoryGetter(ShopListener listener, EntityPlayerMP player, String historyplayer)
  {
    this.listener = listener;
    this.player = player;
    this.historyplayer = historyplayer;
  }

  public void run()
  {
    StringBuilder builder = new StringBuilder();
    try {
      EnjinMinecraftPlugin.debug("Connecting to Enjin for shop history data for player...");
      URL enjinurl = getUrl();
      HttpURLConnection con;
      if (EnjinMinecraftPlugin.isMineshafterPresent())
        con = (HttpURLConnection)enjinurl.openConnection(Proxy.NO_PROXY);
      else {
        con = (HttpURLConnection)enjinurl.openConnection();
      }
      con.setRequestMethod("POST");
      con.setReadTimeout(15000);
      con.setConnectTimeout(15000);
      con.setDoInput(true);
      con.setDoOutput(true);
      con.setRequestProperty("User-Agent", "Mozilla/4.0");
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
      builder.append("&player=" + encode(this.historyplayer));
      con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
      EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
      con.getOutputStream().write(builder.toString().getBytes());

      InputStream in = con.getInputStream();

      String json = parseInput(in);
      EnjinMinecraftPlugin.debug("Output of history:\n" + json);
      ArrayList page = ShopUtils.parseHistoryJSON(json, this.historyplayer);
      if (page == null) {
        this.player.addChatMessage(new ChatComponentText(ChatColor.RED + "There was a problem loading the player history, please try again later."));
        return;
      }
      ShopListener.sendPlayerPage(this.player, page);
      return;
    } catch (SocketTimeoutException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    this.player.addChatMessage(new ChatComponentText(ChatColor.RED + "There was a problem loading the player history, please try again later."));
  }

  public static String parseInput(InputStream in) throws IOException {
    byte[] buffer = new byte[1024];
    int bytesRead = in.read(buffer);
    StringBuilder builder = new StringBuilder();
    while (bytesRead > 0) {
      builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
      bytesRead = in.read(buffer);
    }
    return builder.toString();
  }

  private URL getUrl() throws Throwable {
    return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-purchases");
  }

  private String encode(String in) throws UnsupportedEncodingException {
    return URLEncoder.encode(in, "UTF-8");
  }
}