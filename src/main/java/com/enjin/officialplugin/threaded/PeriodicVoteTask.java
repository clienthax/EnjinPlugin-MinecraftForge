package com.enjin.officialplugin.threaded;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class PeriodicVoteTask implements Runnable {
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap();
	int numoffailedtries = 0;
	boolean firstrun = true;

	public PeriodicVoteTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl
				+ "minecraft-votifier");
	}

	@Override
	public void run()
	{
		if (this.plugin.playervotes.size() > 0)
		{
			if ((this.firstrun) && (EnjinMinecraftPlugin.usingSSL) && 
					(!this.plugin.testHTTPSconnection())) {
				EnjinMinecraftPlugin.usingSSL = false;
				MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
				EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
			}

			boolean successful = false;
			StringBuilder builder = new StringBuilder();
			try {
				EnjinMinecraftPlugin.debug("Connecting to Enjin to send votes...");
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
				builder.append("&votifier=" + encode(getVotes()));
				builder.append("&accepts-packets=true");
				con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
				EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
				con.getOutputStream().write(builder.toString().getBytes());

				InputStream in = con.getInputStream();

				String success = this.plugin.getTask().handleInput(in);

				if (success.equalsIgnoreCase("ok")) {
					successful = true;
					if (this.plugin.unabletocontactenjin) {
						this.plugin.unabletocontactenjin = false;
						String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
						for (String player : players)
						{
							ArrayList<String> ops = new ArrayList<String>();
							for(String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
							{
								ops.add(s);
							}

							if (ops.contains(player.toLowerCase())) {
								EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(player);
								rplayer.addChatMessage(new ChatComponentText(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!"));
								MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Connection to Enjin re-established!");
							}
						}
					}
				}
				else if (success.equalsIgnoreCase("auth_error")) {
					this.plugin.authkeyinvalid = true;
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					this.plugin.stopTask();
					String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
					for (String player : players)
					{
						ArrayList<String> ops = new ArrayList<String>();
						for(String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
						{
							ops.add(s);
						}

						if (ops.contains(player.toLowerCase())) {
							EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(player);
							rplayer.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one."));
						}
					}
					successful = false;
				} else if (success.equalsIgnoreCase("bad_data")) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					this.plugin.lasterror = new EnjinErrorReport("Enjin reported bad data", "Vote synch. Information sent:\n" + builder.toString());

					successful = false;
				} else if (success.equalsIgnoreCase("retry_later")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");

					successful = false;
				} else if (success.equalsIgnoreCase("connect_error")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
					this.plugin.lasterror = new EnjinErrorReport("Enjin reported a connection issue.", "Vote synch. Information sent:\n" + builder.toString());

					successful = false;
				} else if (success.startsWith("invalid_op")) {
					this.plugin.lasterror = new EnjinErrorReport(success, "Vote synch. Information sent:\n" + builder.toString());
					successful = false;
				} else {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on vote sync, if you continue to see this error please report it to enjin.");
					EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
					MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Something happened on vote sync, if you continue to see this error please report it to enjin.");
					MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Response code: " + success);
					successful = false;
				}
			} catch (SocketTimeoutException e) {
				this.plugin.lasterror = new EnjinErrorReport(e, "Vote synch. Information sent:\n" + builder.toString());
			} catch (Throwable t) {
				if (EnjinMinecraftPlugin.debug) {
					t.printStackTrace();
				}
				this.plugin.lasterror = new EnjinErrorReport(t, "Votifier sync. Information sent:\n" + builder.toString());
				EnjinMinecraftPlugin.enjinlogger.warning(this.plugin.lasterror.toString());
			}
			if (!successful) {
				EnjinMinecraftPlugin.debug("Vote sync unsuccessful.");

				Set<Map.Entry<String, String>> voteset = this.removedplayervotes.entrySet();
				for (Map.Entry entry : voteset)
				{
					if (this.plugin.playervotes.containsKey(entry.getKey()))
					{
						String lists = (String)this.plugin.playervotes.get(entry.getKey()) + "," + (String)entry.getValue();
						this.plugin.playervotes.put((String) entry.getKey(), lists);
					} else {
						this.plugin.playervotes.put((String) entry.getKey(), (String) entry.getValue());
					}
					this.removedplayervotes.remove(entry.getKey());
				}
			} else {
				EnjinMinecraftPlugin.debug("Vote sync successful.");
				this.firstrun = false;
			}
		}
	}

	private String getVotes() {
		this.removedplayervotes.clear();
		StringBuilder votes = new StringBuilder();
		Set<Map.Entry<String, String>> voteset = this.plugin.playervotes.entrySet();
		for (Map.Entry entry : voteset) {
			String player = (String) entry.getKey();
			String lists = (String) entry.getValue();
			if (votes.length() != 0) {
				votes.append(";");
			}
			votes.append(player + ":" + lists);
			this.removedplayervotes.put(player, lists);
			this.plugin.playervotes.remove(player);
		}
		return votes.toString();
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
	}
}