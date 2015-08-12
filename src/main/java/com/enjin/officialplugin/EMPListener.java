package com.enjin.officialplugin;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class EMPListener
{
	private EnjinMinecraftPlugin plugin;

	public EMPListener(EnjinMinecraftPlugin plugin) 
	{
		this.plugin = plugin;
	}

	public void updatePlayerRanks(EntityPlayer p) 
	{
		updatePlayerRanks(p.getCommandSenderName());
	}

	public void updatePlayerRanks(String p) 
	{
		this.plugin.playerperms.put(p, "");
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		EntityPlayer p = event.player;
		ArrayList<String> ops = new ArrayList<String>();
		
		for(String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		{
			ops.add(s);
		}
		
		if (!ops.contains(p.getCommandSenderName().toLowerCase()))
		{
			return;
		}
		if (!this.plugin.newversion.equals("")) 
		{
			p.addChatMessage(new ChatComponentText("Enjin Minecraft plugin was updated to version " + this.plugin.newversion + ". Please restart your server."));
		}
		if (this.plugin.updatefailed) 
		{
			p.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "Enjin Minecraft plugin failed to update to the newest version. Please download it manually."));
		}
		if (this.plugin.authkeyinvalid) 
		{
			p.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one."));
		}
		if (this.plugin.unabletocontactenjin)
		{
			p.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings."));
			p.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin report"));
		}
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event)
	{
		updatePlayerRanks(event.player);
	}
}