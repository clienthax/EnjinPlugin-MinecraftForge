package com.enjin.officialplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import com.enjin.officialplugin.heads.CachedHeadData;
import com.enjin.officialplugin.heads.HeadListener;
import com.enjin.officialplugin.heads.HeadLocations;
import com.enjin.officialplugin.listeners.CommandListener;
import com.enjin.officialplugin.listeners.VotifierListener;
import com.enjin.officialplugin.scheduler.TaskScheduler;
import com.enjin.officialplugin.shop.ShopEnableChat;
import com.enjin.officialplugin.shop.ShopItems;
import com.enjin.officialplugin.shop.ShopListener;
import com.enjin.officialplugin.threaded.AsyncToSyncEventThrower;
import com.enjin.officialplugin.threaded.BanLister;
import com.enjin.officialplugin.threaded.CommandExecuter;
import com.enjin.officialplugin.threaded.ConfigSender;
import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.PeriodicEnjinTask;
import com.enjin.officialplugin.threaded.PeriodicVoteTask;
import com.enjin.officialplugin.threaded.UpdateHeadsThread;
import com.enjin.officialplugin.tpsmeter.MonitorTPS;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "EnjinMinecraftPlugin", name = "EnjinMinecraftPlugin", version = "2.4.9-1.8", acceptableRemoteVersions = "*")
public class EnjinMinecraftPlugin {

	@Mod.Instance("EnjinMinecraftPlugin")
	public static EnjinMinecraftPlugin instance;
	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");

	public static String hash = "";
	MinecraftServer s;
	public static boolean debug = false;
	public boolean collectstats = false;
	public boolean supportsglobalgroups = true;
	public boolean votifierinstalled = false;
	public int xpversion = 0;

	public HeadLocations headlocation = new HeadLocations();
	public CachedHeadData headdata = new CachedHeadData(this);

	public ShopItems cachedItems = new ShopItems();

	public String mcversion = "1.8";

	int signupdateinterval = 10;

	public ShopListener shoplistener = new ShopListener();
	public ShopEnableChat shopEClistener = new ShopEnableChat(this.shoplistener);

	public boolean enable = true;

	public ConcurrentHashMap<String, ConfigValueTypes> configvalues = new ConcurrentHashMap();

	public int statssendinterval = 5;

	public static final Logger enjinlogger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());

	public CommandExecuter commandqueue = new CommandExecuter(this);

	public ConcurrentHashMap<String, String> bannedplayers = new ConcurrentHashMap();

	public ConcurrentHashMap<String, String> pardonedplayers = new ConcurrentHashMap();

	public static String apiurl = "://api.enjin.com/api/";

	public boolean autoupdate = true;
	public String newversion = "";

	public static String BUY_COMMAND = "buy";

	public boolean hasupdate = false;
	public boolean updatefailed = false;
	public boolean authkeyinvalid = false;
	public boolean unabletocontactenjin = false;
	public static final String updatejar = "http://resources.guild-hosting.net/1/downloads/emp/";
	public static File datafolder = new File("config" + File.separator + "EnjinMinecraftPlugin");

	public AsyncToSyncEventThrower eventthrower = new AsyncToSyncEventThrower(this);

	public final EMPListener listener = new EMPListener(this);
	public final HeadListener headListener = new HeadListener(this);
	public VotifierListener votelistener;
	final PeriodicEnjinTask task = new PeriodicEnjinTask(this);
	final PeriodicVoteTask votetask = new PeriodicVoteTask(this);
	public BanLister banlistertask;
	public MonitorTPS tpstask;
	int synctaskid = -1;
	int commandexectuerthread = -1;
	int votetaskid = -1;
	int banlisttask = -1;
	int tpstaskid = -1;
	int headsupdateid = -1;

	public TaskScheduler scheduler = new TaskScheduler();

	public static final ExecutorService exec = Executors.newCachedThreadPool();
	public static int minecraftport;
	public static boolean usingSSL = true;
	public NewKeyVerifier verifier = null;
	public ConcurrentHashMap<String, String> playerperms = new ConcurrentHashMap();

	public ConcurrentHashMap<String, String> playervotes = new ConcurrentHashMap();

	public EnjinErrorReport lasterror = null;

	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	public EnjinConfig config;

	public static void debug(String s) {
		if (debug) {
			System.out.println("Enjin Debug: " + s);
		}
		enjinlogger.fine(s);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (event.getSide() == Side.CLIENT) {
			this.enable = false;
		} else {
			debug("Begin init");
			initFiles();
			this.headlocation.loadHeads();
			debug("Init files done.");
			debug("Initializing internal logger");
			enjinlogger.setLevel(Level.FINEST);
			File logsfolder = new File(getDataFolder().getAbsolutePath() + File.separator + "logs");
			if (!logsfolder.exists()) {
				logsfolder.mkdirs();
			}
			try {
				FileHandler fileTxt = new FileHandler(
						getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log",
						true);
				EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
				fileTxt.setFormatter(formatterTxt);
				enjinlogger.addHandler(fileTxt);
			} catch (SecurityException e) {
				this.s.logWarning("[EnjinMinecraftPlugin] Unable to enable debug logging!");
			} catch (IOException e) {
				this.s.logWarning("[EnjinMinecraftPlugin] Unable to enable debug logging!");
			}
			enjinlogger.setUseParentHandlers(false);
			debug("Init vars done.");
		}
	}

	@Mod.EventHandler
	public void onEnable(FMLInitializationEvent event) {
		if (!this.enable) {
			return;
		}

		try {
			Thread configthread = new Thread(new ConfigSender(this));
			configthread.start();
		} catch (Throwable t) {
			MinecraftServer.getServer().logWarning(
					"[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			enjinlogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			t.printStackTrace();
			this.enable = false;
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		this.s = ev.getServer();
		if ((this.s.getCommandManager() instanceof ServerCommandManager)) {
			ServerCommandManager scm = (ServerCommandManager) this.s.getCommandManager();
			scm.registerCommand(new CommandListener(this));
			scm.registerCommand(this.shoplistener);
			scm.registerCommand(this.shopEClistener);
		}
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		initVariables();
		debug("Enabling Ban lister.");
		this.banlistertask = new BanLister(this);
		FMLCommonHandler.instance().bus().register(this.scheduler);
		this.scheduler.runTaskTimerAsynchronously(this.tpstask = new MonitorTPS(this), 40, 40);

		registerEvents();
		if (hash.length() == 50) {
			debug("Starting periodic tasks.");
			startTask();
		} else {
			this.authkeyinvalid = true;
			debug("Auth key is invalid. Wrong length.");
		}
	}

	@Mod.EventHandler
	public void serverStopped(FMLServerStoppingEvent event) {
		stopTask();
		this.scheduler.cancelAllTasks();
		unregisterEvents();
	}

	public File getDataFolder() {
		return datafolder;
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	private void initVariables() {
		minecraftport = MinecraftServer.getServer().getPort();
	}

	public void initFiles() {
		this.config = getConfig();
		File configfile = new File(getDataFolder().toString() + File.separator + "config.properties");
		if (this.config.getString("debug", "").equals("")) {
			createConfig();
		}
		this.configvalues.put("debug", ConfigValueTypes.BOOLEAN);
		debug = this.config.getBoolean("debug", false);
		this.configvalues.put("authkey", ConfigValueTypes.FORBIDDEN);
		hash = this.config.getString("authkey", "");
		debug("Key value retrieved: " + hash);
		this.configvalues.put("https", ConfigValueTypes.BOOLEAN);
		usingSSL = this.config.getBoolean("https", true);
		this.configvalues.put("autoupdate", ConfigValueTypes.BOOLEAN);
		this.autoupdate = this.config.getBoolean("autoupdate", true);
		this.configvalues.put("collectstats", ConfigValueTypes.BOOLEAN);
		this.collectstats = this.config.getBoolean("collectstats", this.collectstats);
		this.configvalues.put("sendstatsinterval", ConfigValueTypes.INT);
		this.statssendinterval = this.config.getInt("sendstatsinterval", 5);
		this.configvalues.put("buycommand", ConfigValueTypes.STRING);
		String buy = this.config.getString("buycommand");
		if (buy == null) {
			createConfig();
		}
		BUY_COMMAND = this.config.getString("buycommand", null);
	}

	private EnjinConfig getConfig() {
		return new EnjinConfig(datafolder);
	}

	private void createConfig() {
		this.config.set("debug", debug);
		this.config.set("authkey", hash);
		this.config.set("https", usingSSL);
		this.config.set("autoupdate", this.autoupdate);
		this.config.set("collectstats", this.collectstats);
		this.config.set("sendstatsinterval", this.statssendinterval);
		this.config.set("buycommand", BUY_COMMAND);
		this.config.save();
	}

	public void startTask() {
		debug("Starting tasks.");
		this.synctaskid = this.scheduler.runTaskTimerAsynchronously(this.task, 1200, 1200);

		this.commandexectuerthread = this.scheduler.runTaskTimerAsynchronously(this.commandqueue, 1300, 1200);
		this.banlisttask = this.scheduler.runTaskTimerAsynchronously(this.banlistertask, 40, 1800);

		this.headsupdateid = this.scheduler.runTaskTimerAsynchronously(new UpdateHeadsThread(this), 120,
				1200 * this.signupdateinterval);

		if (this.votifierinstalled) {
			debug("Starting votifier task.");
			this.votetaskid = this.scheduler.runTaskTimerAsynchronously(this.votetask, 80, 80);
		}
	}

	public void registerEvents() {
		debug("Registering events.");
		FMLCommonHandler.instance().bus().register(this.listener);
		MinecraftForge.EVENT_BUS.register(this.headListener);
		Loader.instance();
		if (Loader.isModLoaded("Votifier")) {
			if (this.votelistener == null) {
				this.votelistener = new VotifierListener(this);
			}
			MinecraftForge.EVENT_BUS.register(this.votelistener);
			this.votifierinstalled = true;
		}
		if (BUY_COMMAND != null) {
			MinecraftForge.EVENT_BUS.register(this.shoplistener);
			FMLCommonHandler.instance().bus().register(this.shoplistener);
		}
	}

	public void stopTask() {
		debug("Stopping tasks.");
		if (this.synctaskid != -1) {
			this.scheduler.cancelTask(this.synctaskid);
		}
		if (this.commandexectuerthread != -1) {
			this.scheduler.cancelTask(this.commandexectuerthread);
		}
		if (this.votetaskid != -1) {
			this.scheduler.cancelTask(this.votetaskid);
		}
		if (this.banlisttask != -1) {
			this.scheduler.cancelTask(this.banlisttask);
		}
		if (this.headsupdateid != -1)
			this.scheduler.cancelTask(this.headsupdateid);
	}

	public void unregisterEvents() {
		debug("Unregistering events.");
		try {
			MinecraftForge.EVENT_BUS.unregister(this.listener);
			MinecraftForge.EVENT_BUS.unregister(this.headListener);
			if (this.votelistener != null)
				MinecraftForge.EVENT_BUS.unregister(this.votelistener);
		} catch (Exception ex) {
		}
	}

	public static int sendAPIQuery(String urls, String[] queryValues) throws MalformedURLException {
		URL url = new URL((usingSSL ? "https" : "http") + apiurl + urls);
		StringBuilder query = new StringBuilder();
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setReadTimeout(3000);
			con.setConnectTimeout(3000);
			con.setDoOutput(true);
			con.setDoInput(true);
			for (String val : queryValues) {
				query.append('&');
				query.append(val);
			}
			if (queryValues.length > 0) {
				query.deleteCharAt(0);
			}
			con.setRequestProperty("Content-length", String.valueOf(query.length()));
			con.getOutputStream().write(query.toString().getBytes());
			System.out.println(query);
			if (con.getInputStream().read() == 49) {
				return 1;
			}
			return 0;
		} catch (SSLHandshakeException e) {
			enjinlogger
					.warning("SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			MinecraftServer.getServer().logWarning(
					"[Enjin Minecraft Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			usingSSL = false;
			return sendAPIQuery(urls, queryValues);
		} catch (SocketTimeoutException e) {
			enjinlogger.warning(
					"Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			MinecraftServer.getServer().logWarning(
					"[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			return 2;
		} catch (Throwable t) {
			t.printStackTrace();
			enjinlogger.warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url
					+ "?" + query.toString());
			MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Failed to send query to enjin server! "
					+ t.getClass().getName() + ". Data: " + url + "?" + query.toString());
		}
		return 2;
	}

	public static synchronized void setHash(String hash) {
		// TODO: WHAT IS THIS?
		EnjinMinecraftPlugin.hash = hash;
	}

	public static synchronized String getHash() {
		return hash;
	}

	public int getTotalXP(int level, float xp) {
		int atlevel = 0;
		int totalxp = 0;
		int xpneededforlevel = 0;
		if (this.xpversion == 1) {
			xpneededforlevel = 17;
			while (atlevel < level) {
				atlevel++;
				totalxp += xpneededforlevel;
				if (atlevel >= 16) {
					xpneededforlevel += 3;
				}
			}
		}

		xpneededforlevel = 7;
		boolean odd = true;
		while (atlevel < level) {
			atlevel++;
			totalxp += xpneededforlevel;
			if (odd) {
				xpneededforlevel += 3;
				odd = false;
				continue;
			}
			xpneededforlevel += 4;
			odd = true;
		}

		totalxp = (int) (totalxp + xp * xpneededforlevel);
		return totalxp;
	}

	public void noEnjinConnectionEvent() {
		List<String> ops = new ArrayList<String>();
		for (String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames()) {
			ops.add(s);
		}
		if (!this.unabletocontactenjin) {
			this.unabletocontactenjin = true;
			List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayerMP player : players)
				if (ops.contains(player.getCommandSenderName().toLowerCase())) {
					player.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
							+ "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings."));
					player.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
							+ "If this problem persists please send enjin the results of the /enjin log"));
				}
		}
	}

	public boolean testHTTPSconnection() {
		try {
			URL url = new URL("https://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = in.readLine();
			in.close();

			return (inputLine != null) && (inputLine.startsWith("OK"));
		} catch (SSLHandshakeException e) {
			if (debug) {
				e.printStackTrace();
			}
			return false;
		} catch (SocketTimeoutException e) {
			if (debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if (debug)
				t.printStackTrace();
		}
		return false;
	}

	public boolean testWebConnection() {
		try {
			URL url = new URL("http://google.com");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = in.readLine();
			in.close();

			return inputLine != null;
		} catch (SocketTimeoutException e) {
			if (debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if (debug)
				t.printStackTrace();
		}
		return false;
	}

	public boolean testHTTPconnection() {
		try {
			URL url = new URL("http://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = in.readLine();
			in.close();

			return (inputLine != null) && (inputLine.startsWith("OK"));
		} catch (SocketTimeoutException e) {
			if (debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if (debug)
				t.printStackTrace();
		}
		return false;
	}

	public static boolean isMineshafterPresent() {
		try {
			Class.forName("mineshafter.MineServer");
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public PeriodicEnjinTask getTask() {
		return this.task;
	}

	public String getVersion() {
		return "2.4.9-forge";
	}

	public void forceHeadUpdate() {
		exec.execute(new UpdateHeadsThread(this));
	}
}