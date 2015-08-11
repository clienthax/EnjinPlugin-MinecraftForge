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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet15RemoteConfigUpdate;
import com.enjin.officialplugin.packets.Packet16MultiUserNotice;
import com.enjin.officialplugin.packets.Packet17AddWhitelistPlayers;
import com.enjin.officialplugin.packets.Packet18RemovePlayersFromWhitelist;
import com.enjin.officialplugin.packets.Packet1ABanPlayers;
import com.enjin.officialplugin.packets.Packet1BPardonPlayers;
import com.enjin.officialplugin.packets.Packet1DPlayerPurchase;
import com.enjin.officialplugin.packets.PacketUtilities;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class PeriodicEnjinTask implements Runnable {
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayerperms = new ConcurrentHashMap();
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap();
	HashMap<String, String> removedbans = new HashMap();
	HashMap<String, String> removedpardons = new HashMap();
	int numoffailedtries = 0;
	int statdelay = 0;
	int plugindelay = 60;
	boolean firstrun = true;

	public PeriodicEnjinTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	private URL getUrl() throws Throwable {
		return new URL(
				(EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-sync");
	}

	public void run() {
		if ((this.firstrun) && (EnjinMinecraftPlugin.usingSSL) && (!this.plugin.testHTTPSconnection())) {
			EnjinMinecraftPlugin.usingSSL = false;
			MinecraftServer.getServer().logWarning(
					"[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
			EnjinMinecraftPlugin.enjinlogger.warning(
					"SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
		}

		boolean successful = false;
		StringBuilder builder = new StringBuilder();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin...");
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
			if (this.firstrun) {
				builder.append("&maxplayers=" + encode(
						String.valueOf(MinecraftServer.getServer().getConfigurationManager().getMaxPlayers())));
				builder.append("&mc_version=" + encode(this.plugin.mcversion));
			}
			builder.append("&players=" + encode(
					String.valueOf(MinecraftServer.getServer().getConfigurationManager().getCurrentPlayerCount())));
			builder.append("&hasranks=" + encode("FALSE"));
			builder.append("&pluginversion=" + encode(this.plugin.getVersion()));

			if (this.plugindelay++ >= 59) {
				builder.append("&mods=" + encode(getMods()));
			}
			builder.append("&playerlist=" + encode(getPlayers()));
			builder.append("&worlds=" + encode(getWorlds()));
			builder.append("&tps=" + encode(getTPS()));
			builder.append("&time=" + encode(getTimes()));
			if (this.plugin.bannedplayers.size() > 0) {
				builder.append("&banned=" + encode(getBans()));
			}
			if (this.plugin.pardonedplayers.size() > 0) {
				builder.append("&unbanned=" + encode(getPardons()));
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());

			InputStream in = con.getInputStream();

			String success = handleInput(in);

			if (success.equalsIgnoreCase("ok")) {
				successful = true;
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
				successful = false;
			} else if (success.equalsIgnoreCase("bad_data")) {
				EnjinMinecraftPlugin.enjinlogger.warning(
						"[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				this.plugin.lasterror = new EnjinErrorReport("Enjin reported bad data",
						"Regular synch. Information sent:\n" + builder.toString());

				successful = false;
			} else if (success.equalsIgnoreCase("retry_later")) {
				EnjinMinecraftPlugin.enjinlogger
						.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");

				successful = false;
			} else if (success.equalsIgnoreCase("connect_error")) {
				EnjinMinecraftPlugin.enjinlogger.info(
						"[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
				this.plugin.lasterror = new EnjinErrorReport("Enjin said there's a connection error somewhere.",
						"Regular synch. Information sent:\n" + builder.toString());

				successful = false;
			} else if (success.startsWith("invalid_op")) {
				this.plugin.lasterror = new EnjinErrorReport(success,
						"Regular synch. Information sent:\n" + builder.toString());
				successful = false;
			} else {
				EnjinMinecraftPlugin.enjinlogger.info(
						"[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
				EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
				MinecraftServer.getServer().logInfo(
						"[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
				MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Response code: " + success);
				successful = false;
			}
			if (!successful) {
				if ((this.numoffailedtries++ > 5) && (!this.plugin.unabletocontactenjin)) {
					this.numoffailedtries = 0;
					this.plugin.noEnjinConnectionEvent();
				}
			} else
				this.numoffailedtries = 0;
		} catch (SocketTimeoutException e) {
			if (this.numoffailedtries++ > 5) {
				EnjinMinecraftPlugin.enjinlogger.warning(
						"[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				MinecraftServer.getServer().logWarning(
						"[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				this.numoffailedtries = 0;
				this.plugin.noEnjinConnectionEvent();
			}
			this.plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			if (this.numoffailedtries++ > 5) {
				EnjinMinecraftPlugin.enjinlogger.warning(
						"[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				MinecraftServer.getServer().logWarning(
						"[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				this.numoffailedtries = 0;
				this.plugin.noEnjinConnectionEvent();
			}
			if (EnjinMinecraftPlugin.debug) {
				t.printStackTrace();
			}
			this.plugin.lasterror = new EnjinErrorReport(t, "Regular synch. Information sent:\n" + builder.toString());
			EnjinMinecraftPlugin.enjinlogger.warning(this.plugin.lasterror.toString());
		}
		if (!successful) {
			EnjinMinecraftPlugin.debug("Synch unsuccessful.");
			this.statdelay += 1;
			Set<Map.Entry<String, String>> es = this.removedplayerperms.entrySet();
			for (Map.Entry entry : es) {
				if (!this.plugin.playerperms.containsKey(entry.getKey())) {
					this.plugin.playerperms.put((String) entry.getKey(), (String) entry.getValue());
				}
				this.removedplayerperms.remove(entry.getKey());
			}

			Set<Map.Entry<String, String>> voteset = this.removedplayervotes.entrySet();
			for (Map.Entry entry : voteset) {
				if (this.plugin.playervotes.containsKey(entry.getKey())) {
					String lists = (String) this.plugin.playervotes.get(entry.getKey()) + ","
							+ (String) entry.getValue();
					this.plugin.playervotes.put((String) entry.getKey(), lists);
				} else {
					this.plugin.playervotes.put((String) entry.getKey(), (String) entry.getValue());
				}
				this.removedplayervotes.remove(entry.getKey());
			}
			Set<Map.Entry<String, String>> banset = this.removedbans.entrySet();
			for (Map.Entry entry : banset) {
				this.plugin.bannedplayers.put((String) entry.getKey(), (String) entry.getValue());
			}
			banset.clear();
			Set<Map.Entry<String, String>> pardonset = this.removedpardons.entrySet();
			for (Map.Entry entry : pardonset) {
				this.plugin.pardonedplayers.put((String) entry.getKey(), (String) entry.getValue());
			}
			pardonset.clear();
		} else {
			this.firstrun = false;
			this.removedbans.clear();
			this.removedpardons.clear();
			EnjinMinecraftPlugin.debug("Synch successful.");
			if (this.plugindelay >= 59) {
				this.plugindelay = 0;
			}
		}
		if (this.plugin.collectstats)
			;
	}

	private String getTPS() {
		return String.valueOf(this.plugin.tpstask.getTPSAverage());
	}

	private String getPardons() {
		StringBuilder pardons = new StringBuilder();
		Set<Map.Entry<String, String>> pardonset = this.plugin.pardonedplayers.entrySet();
		for (Map.Entry pardon : pardonset) {
			if (pardons.length() > 0) {
				pardons.append(",");
			}
			if (((String) pardon.getValue()).equals(""))
				pardons.append((String) pardon.getKey());
			else {
				pardons.append((String) pardon.getValue() + ":" + (String) pardon.getKey());
			}
			this.plugin.pardonedplayers.remove(pardon.getKey());
			this.removedpardons.put((String) pardon.getKey(), (String) pardon.getValue());
		}
		return pardons.toString();
	}

	private String getBans() {
		StringBuilder bans = new StringBuilder();
		Set<Map.Entry<String, String>> banset = this.plugin.bannedplayers.entrySet();
		for (Map.Entry ban : banset) {
			if (bans.length() > 0) {
				bans.append(",");
			}
			if (((String) ban.getValue()).equals(""))
				bans.append((String) ban.getKey());
			else {
				bans.append((String) ban.getValue() + ":" + (String) ban.getKey());
			}
			this.plugin.bannedplayers.remove(ban.getKey());
			this.removedbans.put((String) ban.getKey(), (String) ban.getValue());
		}
		return bans.toString();
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
	}

	public String handleInput(InputStream in) throws IOException {
		String tresult = "Unknown Error";
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(2147483647);
		while (true) {
			int code = bin.read();
			switch (code) {
			case -1:
				EnjinMinecraftPlugin.debug("No more packets. End of stream. Update ended.");
				bin.reset();
				StringBuilder input = new StringBuilder();
				while ((code = bin.read()) != -1) {
					input.append((char) code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input.toString());
				return tresult;
			case 16:
				EnjinMinecraftPlugin.debug("Packet [0x10](Add Player Group) received.");
				Packet10AddPlayerGroup.handle(bin, this.plugin);
				break;
			case 17:
				EnjinMinecraftPlugin.debug("Packet [0x11](Remove Player Group) received.");
				Packet11RemovePlayerGroup.handle(bin, this.plugin);
				break;
			case 18:
				EnjinMinecraftPlugin.debug("Packet [0x12](Execute Command) received.");
				Packet12ExecuteCommand.handle(bin, this.plugin);
				break;
			case 19:
				EnjinMinecraftPlugin.debug("Packet [0x13](Execute command as Player) received.");
				Packet13ExecuteCommandAsPlayer.handle(bin, this.plugin);
				break;
			case 10:
				EnjinMinecraftPlugin.debug("Packet [0x0A](New Line) received, ignoring...");
				break;
			case 13:
				EnjinMinecraftPlugin.debug("Packet [0x0D](Carriage Return) received, ignoring...");
				break;
			case 20:
				EnjinMinecraftPlugin.debug("Packet [0x14](Newer Version) received.");
				Packet14NewerVersion.handle(bin, this.plugin);
				break;
			case 21:
				EnjinMinecraftPlugin.debug("Packet [0x15](Remote Config Update) received.");
				Packet15RemoteConfigUpdate.handle(bin, this.plugin);
				break;
			case 22:
				EnjinMinecraftPlugin.debug("Packet [0x16](Multi-user Notice) received.");
				Packet16MultiUserNotice.handle(bin, this.plugin);
				break;
			case 23:
				EnjinMinecraftPlugin.debug("Packet [0x17](Add Whitelist Players) received.");
				Packet17AddWhitelistPlayers.handle(bin, this.plugin);
				break;
			case 24:
				EnjinMinecraftPlugin.debug("Packet [0x18](Remove Players From Whitelist) received.");
				Packet18RemovePlayersFromWhitelist.handle(bin, this.plugin);
				break;
			case 25:
				EnjinMinecraftPlugin.debug("Packet [0x19](Enjin Status) received.");
				tresult = PacketUtilities.readString(bin);
				break;
			case 26:
				EnjinMinecraftPlugin.debug("Packet [0x1A](Ban Player) received.");
				Packet1ABanPlayers.handle(bin, this.plugin);
				break;
			case 27:
				EnjinMinecraftPlugin.debug("Packet [0x1B](Pardon Player) received.");
				Packet1BPardonPlayers.handle(bin, this.plugin);
				break;
			case 29:
				EnjinMinecraftPlugin.debug("Packet [0x1D](Player Purchase) received.");
				Packet1DPlayerPurchase.handle(bin, this.plugin);
				break;
			case 60:
				EnjinMinecraftPlugin.debug("Packet [0x3C](Enjin Maintenance Page) received. Aborting sync.");
				bin.reset();
				StringBuilder input1 = new StringBuilder();
				while ((code = bin.read()) != -1) {
					input1.append((char) code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input1.toString());
				return "retry_later";
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 11:
			case 12:
			case 14:
			case 15:
			case 28:
			case 30:
			case 31:
			case 32:
			case 33:
			case 34:
			case 35:
			case 36:
			case 37:
			case 38:
			case 39:
			case 40:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
			case 48:
			case 49:
			case 50:
			case 51:
			case 52:
			case 53:
			case 54:
			case 55:
			case 56:
			case 57:
			case 58:
			case 59:
			default:
				EnjinMinecraftPlugin.debug("[Enjin] Received an invalid opcode: " + code);
				bin.reset();
				StringBuilder input2 = new StringBuilder();
				while ((code = bin.read()) != -1) {
					input2.append((char) code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input2.toString());
				return "invalid_op\nRaw data received:\n" + input2.toString();
			}
		}
	}

	private String getMods() {
		StringBuilder builder = new StringBuilder();
		List<ModContainer> modlist = Loader.instance().getActiveModList();
		for (ModContainer p : modlist) {
			builder.append("," + p.getName());
		}
		if (builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}

	private String getPlayers() {
		StringBuilder builder = new StringBuilder();
		for (String p : MinecraftServer.getServer().getConfigurationManager().getAllUsernames()) {
			builder.append(',');
			builder.append(p);
		}
		if (builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}

	private String getWorlds() {
		StringBuilder builder = new StringBuilder();
		WorldServer[] worldservers = MinecraftServer.getServer().worldServers;
		LinkedList worlds = new LinkedList();
		for (WorldServer world : worldservers) {
			if (!worlds.contains(world.getWorldInfo().getWorldName())) {
				builder.append(',');
				builder.append(world.getWorldInfo().getWorldName());
				worlds.add(world.getWorldInfo().getWorldName());
			}
		}
		if (builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}

	private String getTimes() {
		StringBuilder builder = new StringBuilder();
		LinkedList worlds = new LinkedList();
		for (WorldServer w : MinecraftServer.getServer().worldServers) {
			WorldInfo worldinfo = w.getWorldInfo();
			if ((w.provider.getDimensionId() == 0) && (!worlds.contains(worldinfo.getWorldName()))) {
				if (builder.length() > 0) {
					builder.append(";");
				}
				worlds.add(worldinfo.getWorldName());
				builder.append(
						w.getWorldInfo().getWorldName() + ":" + Long.toString(w.getWorldInfo().getWorldTime()) + ",");
				int moonphase = (int) (worldinfo.getWorldTotalTime() / 24000L % 8L);
				builder.append(Integer.toString(moonphase) + ",");
				if (worldinfo.isRaining()) {
					if (worldinfo.isThundering())
						builder.append("2");
					else
						builder.append("1");
				} else {
					builder.append("0");
				}
			}
		}
		return builder.toString();
	}
}