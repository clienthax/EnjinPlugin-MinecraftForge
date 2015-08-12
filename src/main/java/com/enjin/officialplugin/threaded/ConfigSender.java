package com.enjin.officialplugin.threaded;

import java.io.BufferedInputStream;
import java.io.IOException;
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

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.ConfigValueTypes;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.PacketUtilities;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class ConfigSender implements Runnable {
	EnjinMinecraftPlugin plugin;

	public ConfigSender(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		if ((EnjinMinecraftPlugin.usingSSL) && (!this.plugin.testHTTPSconnection())) {
			EnjinMinecraftPlugin.usingSSL = false;
			MinecraftServer.getServer().logWarning(
					"SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
			EnjinMinecraftPlugin.enjinlogger.warning(
					"SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
		}

		StringBuilder builder = new StringBuilder();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin to send config...");
			URL enjinurl = getUrl();
			HttpURLConnection con;
			if (EnjinMinecraftPlugin.isMineshafterPresent())
				con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
			else {
				con = (HttpURLConnection) enjinurl.openConnection();
			}
			con.setRequestMethod("POST");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			Set<Map.Entry<String, ConfigValueTypes>> es = this.plugin.configvalues.entrySet();
			for (Map.Entry<String, ConfigValueTypes> entry : es) {
				switch (entry.getValue().ordinal()) {
				case 1:
					break;
				case 2:
					boolean istrue = this.plugin.config.getBoolean((String) entry.getKey());
					builder.append("&" + ((String) entry.getKey()).replaceAll("[.]", "_") + "=" + (istrue ? "1" : "0"));
					break;
				default:
					builder.append("&" + ((String) entry.getKey()).replaceAll("[.]", "_") + "="
							+ this.plugin.config.getString((String) entry.getKey()));
				}
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());

			InputStream in = con.getInputStream();
			String success = handleInput(in);
			if (success.equalsIgnoreCase("ok")) {
				if (this.plugin.unabletocontactenjin) {
					this.plugin.unabletocontactenjin = false;
					String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
					for (String player : players) {
						ArrayList<String> ops = new ArrayList<String>();
						for (String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames()) {
							ops.add(s);
						}
						if (ops.contains(player.toLowerCase())) {
							EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager()
									.getPlayerByUsername(player);
							rplayer.addChatMessage(new ChatComponentText(ChatColor.DARK_GREEN
									+ "[Enjin Minecraft Plugin] Connection to Enjin re-established!"));
							MinecraftServer.getServer()
							.logInfo("[Enjin Minecraft Plugin] Connection to Enjin re-established!");
						}
					}
				} else if (success.equalsIgnoreCase("auth_error")) {
					this.plugin.authkeyinvalid = true;
					EnjinMinecraftPlugin.enjinlogger.warning(
							"[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					MinecraftServer.getServer().logWarning(
							"[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					this.plugin.stopTask();
					String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
					for (String player : players) {
						ArrayList<String> ops = new ArrayList<String>();
						for (String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames()) {
							ops.add(s);
						}
						if (ops.contains(player.toLowerCase())) {
							EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager()
									.getPlayerByUsername(player);
							rplayer.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
									+ "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one."));
						}
					}
				} else if (success.equalsIgnoreCase("bad_data")) {
					EnjinMinecraftPlugin.enjinlogger.warning(
							"[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					MinecraftServer.getServer().logWarning(
							"[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				} else if (success.equalsIgnoreCase("retry_later")) {
					EnjinMinecraftPlugin.enjinlogger
					.info("[Enjin Minecraft Plugin] Enjin said to wait, will retry at next boot.");
					MinecraftServer.getServer()
					.logInfo("[Enjin Minecraft Plugin] Enjin said to wait, will retry at next boot.");
				} else if (success.equalsIgnoreCase("connect_error")) {
					EnjinMinecraftPlugin.enjinlogger.info(
							"[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
					MinecraftServer.getServer().logInfo(
							"[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
				} else {
					EnjinMinecraftPlugin.enjinlogger.info(
							"[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
					EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
					MinecraftServer.getServer().logInfo(
							"[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
					MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Response code: " + success);
				}
			}
		} catch (SocketTimeoutException e) {
			this.plugin.lasterror = new EnjinErrorReport(e, "Config sender. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			this.plugin.lasterror = new EnjinErrorReport(t, "config sender. Information sent:\n" + builder.toString());
			EnjinMinecraftPlugin.enjinlogger.warning(this.plugin.lasterror.toString());
		}
	}

	private String handleInput(InputStream in) {
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			return PacketUtilities.readString(bin);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "string_read_error";
	}

	private URL getUrl() throws Throwable {
		return new URL(
				(EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-config");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
	}
}