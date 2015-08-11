package com.enjin.officialplugin.listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinConsole;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.points.EnjinPointsSyncClass;
import com.enjin.officialplugin.points.PointsAPI;
import com.enjin.officialplugin.points.RetrievePointsSyncClass;
import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.ReportMakerThread;
import com.enjin.officialplugin.threaded.UpdateHeadsThread;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class CommandListener extends CommandBase
{
	EnjinMinecraftPlugin plugin;

	public CommandListener(EnjinMinecraftPlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public String getName()
	{
		return "enjin";
	}

	public List getCommandAliases()
	{
		LinkedList aliases = new LinkedList();
		aliases.add("e");
		return aliases;
	}

	@Override
	public void execute(ICommandSender sender, String[] args)
	{
		ArrayList<String> ops = new ArrayList<String>();
		for (String name : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		{
			ops.add(name);
		}
		if (((sender instanceof EntityPlayerMP)) && (!ops.contains(sender.getName())))
		{
			return;
		}

		EntityPlayerMP player = null;
		if ((sender instanceof EntityPlayerMP))
		{
			player = (EntityPlayerMP) sender;
		}

		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("key"))
			{
				if (args.length != 2)
				{
					return;
				}
				EnjinMinecraftPlugin.enjinlogger.info("Checking if key is valid");
				MinecraftServer.getServer().logInfo("Checking if key is valid");

				if ((this.plugin.verifier == null) || (this.plugin.verifier.completed))
				{
					this.plugin.verifier = new NewKeyVerifier(this.plugin, args[1], player, false);
					Thread verifierthread = new Thread(this.plugin.verifier);
					verifierthread.start();
				}
				else
				{
					sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!", sender);
				}
				return;
			}
			if (args[0].equalsIgnoreCase("headdebug"))
			{
				this.plugin.headlocation.outputHeads();
			}
			else
			{
				if (args[0].equalsIgnoreCase("report"))
				{
					sendMessage(ChatColor.GREEN + "Please wait as we generate the report", sender);
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
					Date date = new Date();
					StringBuilder report = new StringBuilder();
					report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
					report.append("Enjin plugin version: " + this.plugin.getVersion() + "\n");

					report.append("Forge version: " + MinecraftForge.getBrandingVersion() + "\n");
					report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
					report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

					if (this.plugin.authkeyinvalid)
					{
						report.append("ERROR: Authkey reported by plugin as invalid!\n");
					}
					if (this.plugin.unabletocontactenjin)
					{
						report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
					}

					report.append("\nMods: \n");
					List<ModContainer> modlist = Loader.instance().getActiveModList();
					for (ModContainer p : modlist)
					{
						report.append(p.getName() + " version " + p.getVersion() + "\n");
					}
					report.append("\nWorlds: \n");
					WorldServer[] worldservers = MinecraftServer.getServer().worldServers;
					for (WorldServer world : worldservers)
					{
						report.append(world.getWorldInfo().getWorldName() + "\n");
					}
					ReportMakerThread rmthread = new ReportMakerThread(this.plugin, report, player);
					Thread dispatchThread = new Thread(rmthread);
					dispatchThread.start();
					return;
				}
				if (args[0].equalsIgnoreCase("debug"))
				{
					if (EnjinMinecraftPlugin.debug)
						EnjinMinecraftPlugin.debug = false;
					else
					{
						EnjinMinecraftPlugin.debug = true;
					}
					sendMessage(ChatColor.GREEN + "Debugging has been set to " + EnjinMinecraftPlugin.debug, sender);
					return;
				}
				if ((args[0].equalsIgnoreCase("updateheads")) || (args[0].equalsIgnoreCase("syncheads")))
				{
					UpdateHeadsThread uhthread = new UpdateHeadsThread(this.plugin, sender);
					EnjinMinecraftPlugin.exec.execute(uhthread);
					sendMessage(ChatColor.GREEN + "Head update queued, please wait...", sender);
					return;
				}

				if (args[0].equalsIgnoreCase("inform"))
				{
					if (args.length < 3)
					{
						sendMessage(ChatColor.RED + "To send a message do: /enjin inform playername message", sender);
						return;
					}
					EntityPlayerMP receiver = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[1]);
					if (receiver == null)
					{
						sendMessage(ChatColor.RED + "That player isn't on the server at the moment.", sender);
						return;
					}
					StringBuilder thestring = new StringBuilder();
					for (int i = 2; i < args.length; i++)
					{
						if (i > 2)
						{
							thestring.append(" ");
						}
						thestring.append(args[i]);
					}
					receiver.addChatMessage(new ChatComponentText(EnjinConsole.translateColorCodes(thestring.toString())));
					return;
				}
				if (args[0].equalsIgnoreCase("broadcast"))
				{
					if (args.length < 2)
					{
						sendMessage(ChatColor.RED + "To broadcast a message do: /enjin broadcast message", sender);
					}
					StringBuilder thestring = new StringBuilder();
					for (int i = 1; i < args.length; i++)
					{
						if (i > 1)
						{
							thestring.append(" ");
						}
						thestring.append(args[i]);
					}
					List<EntityPlayerMP> playerlist = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
					for (EntityPlayerMP receiver : playerlist)
					{
						receiver.addChatMessage(new ChatComponentText(EnjinConsole.translateColorCodes(thestring.toString())));
					}
					return;
				}
				if (args[0].equalsIgnoreCase("lag"))
				{
					Runtime runtime = Runtime.getRuntime();
					long memused = runtime.totalMemory() / 1048576L;
					long maxmemory = runtime.maxMemory() / 1048576L;
					sendMessage(ChatColor.GOLD + "Average TPS: " + ChatColor.GREEN + this.plugin.tpstask.getTPSAverage(), sender);
					sendMessage(ChatColor.GOLD + "Last TPS measurement: " + ChatColor.GREEN + this.plugin.tpstask.getLastTPSMeasurement(), sender);
					sendMessage(ChatColor.GOLD + "Memory Used: " + ChatColor.GREEN + memused + "MB/" + maxmemory + "MB", sender);
					return;
				}
				if (args[0].equalsIgnoreCase("addpoints"))
				{
					if (args.length > 2)
					{
						String playername = args[1].trim();
						int pointsamount = 1;
						try
						{
							pointsamount = Integer.parseInt(args[2].trim());
						}
						catch (NumberFormatException e)
						{
							sendMessage(ChatColor.DARK_RED + "Usage: /enjin addpoints [player] [amount]", sender);
							return;
						}
						if (pointsamount < 1)
						{
							sendMessage(ChatColor.DARK_RED + "You cannot add less than 1 point to a user. You might want to try /enjin removepoints!", sender);
							return;
						}
						sendMessage(ChatColor.GOLD + "Please wait as we add the " + pointsamount + " points to " + playername + "...", sender);
						EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.AddPoints);
						Thread dispatchThread = new Thread(mthread);
						dispatchThread.start();
					}
					else
					{
						sendMessage(ChatColor.DARK_RED + "Usage: /enjin addpoints [player] [amount]", sender);
					}
					return;
				}
				if (args[0].equalsIgnoreCase("removepoints"))
				{
					if (args.length > 2)
					{
						String playername = args[1].trim();
						int pointsamount = 1;
						try
						{
							pointsamount = Integer.parseInt(args[2].trim());
						}
						catch (NumberFormatException e)
						{
							sendMessage(ChatColor.DARK_RED + "Usage: /enjin removepoints [player] [amount]", sender);
							return;
						}
						if (pointsamount < 1)
						{
							sendMessage(ChatColor.DARK_RED + "You cannot remove less than 1 point to a user.", sender);
							return;
						}
						sendMessage(ChatColor.GOLD + "Please wait as we remove the " + pointsamount + " points from " + playername + "...", sender);
						EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.RemovePoints);
						Thread dispatchThread = new Thread(mthread);
						dispatchThread.start();
					}
					else
					{
						sendMessage(ChatColor.DARK_RED + "Usage: /enjin removepoints [player] [amount]", sender);
					}
				}
				else
				{
					if (args[0].equalsIgnoreCase("setpoints"))
					{
						if (args.length > 2)
						{
							String playername = args[1].trim();
							int pointsamount = 1;
							try
							{
								pointsamount = Integer.parseInt(args[2].trim());
							}
							catch (NumberFormatException e)
							{
								sendMessage(ChatColor.DARK_RED + "Usage: /enjin setpoints [player] [amount]", sender);
								return;
							}
							sendMessage(ChatColor.GOLD + "Please wait as we set the points to " + pointsamount + " points for " + playername + "...", sender);
							EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.SetPoints);
							Thread dispatchThread = new Thread(mthread);
							dispatchThread.start();
						}
						else
						{
							sendMessage(ChatColor.DARK_RED + "Usage: /enjin setpoints [player] [amount]", sender);
						}
						return;
					}
					if (args[0].equalsIgnoreCase("points"))
					{
						if (args.length > 1)
						{
							String playername = args[1].trim();
							sendMessage(ChatColor.GOLD + "Please wait as we retrieve the points balance for " + playername + "...", sender);
							RetrievePointsSyncClass mthread = new RetrievePointsSyncClass(sender, playername, false);
							Thread dispatchThread = new Thread(mthread);
							dispatchThread.start();
						}
						else if (player == null)
						{
							sendMessage(ChatColor.RED + "You must specify a player name", sender);
						}
						else
						{
							sendMessage(ChatColor.GOLD + "Please wait as we retrieve your points balance...", sender);
							RetrievePointsSyncClass mthread = new RetrievePointsSyncClass(sender, player.getName(), true);
							Thread dispatchThread = new Thread(mthread);
							dispatchThread.start();
						}

						return;
					}
					if ((args[0].equalsIgnoreCase("head")) || (args[0].equalsIgnoreCase("heads")))
					{
						sendMessage(EnjinConsole.header(), sender);

						sendMessage(ChatColor.AQUA + "To set a sign with a head, just place the head, then place the sign either above or below it.", sender);
						sendMessage(ChatColor.AQUA + "To create a sign of a specific type just put the code on the first line. # denotes the number.", sender);
						sendMessage(ChatColor.AQUA + " Example: [donation2] would show the second most recent donation.", sender);
						sendMessage(ChatColor.AQUA + "If there are sub-types, those go on the second line of the sign.", sender);
						sendMessage(ChatColor.GOLD + "[donation#] " + ChatColor.RESET + " - Most recent donation.", sender);
						sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " Place the item id on the second line to only get donations for that package.", sender);
						sendMessage(ChatColor.GOLD + "[topvoter#] " + ChatColor.RESET + " - Top voter of the month.", sender);
						sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes it to the top voter of the day/week/month.", sender);
						sendMessage(ChatColor.GOLD + "[voter#] " + ChatColor.RESET + " - Most recent voter.", sender);
						sendMessage(ChatColor.GOLD + "[topplayer#] " + ChatColor.RESET + " - Top player (gets data from module on website).", sender);
						sendMessage(ChatColor.GOLD + "[topposter#] " + ChatColor.RESET + " - Top poster on the forum.", sender);
						sendMessage(ChatColor.GOLD + "[toplikes#] " + ChatColor.RESET + " - Top forum likes.", sender);
						sendMessage(ChatColor.GOLD + "[newmember#] " + ChatColor.RESET + " - Latest player to sign up on the website.", sender);
						sendMessage(ChatColor.GOLD + "[toppoints#] " + ChatColor.RESET + " - Which player has the most unspent points.", sender);
						sendMessage(ChatColor.GOLD + "[pointsspent#] " + ChatColor.RESET + " - Player which has spent the most points overall.", sender);
						sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.", sender);
						sendMessage(ChatColor.GOLD + "[moneyspent#] " + ChatColor.RESET + " - Player which has spent the most money on the server overall.", sender);
						sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.", sender);
						return;
					}
				}
			}
		}
		else
		{
			sendMessage(EnjinConsole.header(), sender);

			sendMessage(ChatColor.GOLD + "/enjin key <KEY>: " + ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.", sender);

			sendMessage(ChatColor.GOLD + "/enjin broadcast <MESSAGE>: " + ChatColor.RESET + "Broadcast a message to all players.", sender);

			sendMessage(ChatColor.GOLD + "/enjin push: " + ChatColor.RESET + "Sync your website tags with the current ranks.", sender);

			sendMessage(ChatColor.GOLD + "/enjin lag: " + ChatColor.RESET + "Display TPS average and memory usage.", sender);

			sendMessage(ChatColor.GOLD + "/enjin debug: " + ChatColor.RESET + "Enable debug mode and display extra information in console.", sender);

			sendMessage(ChatColor.GOLD + "/enjin report: " + ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.", sender);

			sendMessage(ChatColor.GOLD + "/enjin heads: " + ChatColor.RESET + "Shows in game help for the heads and sign stats part of the plugin.", sender);

			return;
		}
	}

	private void sendMessage(String message, ICommandSender sender)
	{
		if ((sender == null) || (!(sender instanceof EntityPlayerMP)))
			MinecraftServer.getServer().logInfo(EnjinConsole.stripColor(message));
		else
			sender.addChatMessage(new ChatComponentText(message));
	}

	private void sendMessage(String[] messages, ICommandSender sender)
	{
		if (sender == null)
		{
			for (String message : messages)
				MinecraftServer.getServer().logInfo(EnjinConsole.stripColor(message));
		}
		else
			for (String message : messages)
				sender.addChatMessage(new ChatComponentText(message));
	}

	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "/enjin help";
	}
}
