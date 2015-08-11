package com.enjin.officialplugin.points;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.UpdateHeadsThread;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PointsAPI
{
  public static void modifyPointsToPlayerAsynchronously(String player, int amount, Type type)
  {
    PointsSyncClass mthread = new PointsSyncClass(player, amount, type);
    Thread dispatchThread = new Thread(mthread);
    dispatchThread.start();	
  }

  public static int modifyPointsToPlayer(String player, int amount, Type type)
    throws NumberFormatException, PlayerDoesNotExistException, ErrorConnectingToEnjinException
  {
    try
    {
      if (amount < 1) {
        throw new NumberFormatException("The amount cannot be negative or 0!");
      }
      EnjinMinecraftPlugin.debug("Connecting to Enjin for action " + type.toString() + " for " + amount + " points for player " + player);
      URL enjinurl;
      switch (type.ordinal()) {
      case 1:
        enjinurl = getAddPointsUrl();
        break;
      case 2:
        enjinurl = getRemovePointsUrl();
        break;
      case 3:
        enjinurl = getSetPointsUrl();
        break;
      default:
        enjinurl = getAddPointsUrl();
      }
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

      StringBuilder builder = new StringBuilder();
      builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
      builder.append("&points=" + amount);
      builder.append("&player=" + player);
      con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
      EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
      con.getOutputStream().write(builder.toString().getBytes());

      InputStream in = con.getInputStream();

      String json = UpdateHeadsThread.parseInput(in);

      EnjinMinecraftPlugin.debug("Content of points query for type " + type.toString() + ":\n" + json);

      JSONParser parser = new JSONParser();

      JSONObject array = (JSONObject)parser.parse(json);
      String success = array.get("success").toString();
      if (success.equalsIgnoreCase("true")) {
        String spoints = array.get("points").toString();
        int points = Integer.parseInt(spoints);
        return points;
      }
      String error = array.get("error").toString();
      throw new PlayerDoesNotExistException(error);
    }
    catch (SocketTimeoutException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
    } catch (UnsupportedEncodingException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
    } catch (PlayerDoesNotExistException e) {
      throw e;
    } catch (NumberFormatException e) {
      throw e;
    } catch (IOException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points."); } catch (Throwable e) {
    }
    throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
  }

  public static int getPointsForPlayer(String player)
    throws PlayerDoesNotExistException, ErrorConnectingToEnjinException
  {
    try
    {
      EnjinMinecraftPlugin.debug("Connecting to Enjin to retrieve points balance for player " + player);
      URL enjinurl = getPointsUrl();
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

      StringBuilder builder = new StringBuilder();
      builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
      builder.append("&player=" + player);
      con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
      EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
      con.getOutputStream().write(builder.toString().getBytes());

      InputStream in = con.getInputStream();

      String json = UpdateHeadsThread.parseInput(in);

      EnjinMinecraftPlugin.debug("Content of points query:\n" + json);

      JSONParser parser = new JSONParser();

      JSONObject array = (JSONObject)parser.parse(json);
      String success = array.get("success").toString();
      if (success.equalsIgnoreCase("true")) {
        String spoints = array.get("points").toString();
        int points = Integer.parseInt(spoints);
        return points;
      }
      String error = array.get("error").toString();
      throw new PlayerDoesNotExistException(error);
    }
    catch (SocketTimeoutException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
    } catch (UnsupportedEncodingException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
    } catch (IOException e) {
      throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
    } catch (PlayerDoesNotExistException e) {
      throw e; } catch (Throwable e) {
    }
    throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
  }

  private static URL getAddPointsUrl()
    throws Throwable
  {
    return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "add-points");
  }

  private static URL getRemovePointsUrl() throws Throwable {
    return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "remove-points");
  }

  private static URL getSetPointsUrl() throws Throwable {
    return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "set-points");
  }

  private static URL getPointsUrl() throws Throwable {
    return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "get-points");
  }

  private static String encode(String in) throws UnsupportedEncodingException {
    return URLEncoder.encode(in, "UTF-8");
  }

  public static enum Type
  {
    AddPoints, 
    RemovePoints, 
    SetPoints;
  }
}