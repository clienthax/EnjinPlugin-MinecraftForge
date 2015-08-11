package com.enjin.officialplugin.listeners;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VotifierListener
{
	EnjinMinecraftPlugin plugin;

	public VotifierListener(EnjinMinecraftPlugin plugin)
	{
		this.plugin = plugin;
	}

	@SubscribeEvent
	public void voteRecieved(VotifierEvent event)
	{
		if ((event.getVote().getUsername().equalsIgnoreCase("test")) || (event.getVote().getUsername().isEmpty()))
		{
			return;
		}
		
		Vote vote = event.getVote();
		String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
		
		if (username.isEmpty())
		{
			return;
		}
		
		String lists = "";
		
		if (this.plugin.playervotes.containsKey(username))
		{
			lists = (String) this.plugin.playervotes.get(username);
			lists = lists + "," + vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
		}
		else
		{
			lists = vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
		}
		
		this.plugin.playervotes.put(username, lists);
	}
}
