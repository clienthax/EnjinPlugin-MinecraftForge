package com.enjin.officialplugin.heads;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HeadListener
{
	EnjinMinecraftPlugin plugin;
	Pattern recentitempattern = Pattern.compile("\\[donation([1-9]|10)\\]");
	Pattern topvoterpattern = Pattern.compile("\\[topvoter([1-9]|10)\\]");
	Pattern recentvoterspattern = Pattern.compile("\\[voter([1-9]|10)\\]");
	Pattern topplayerpattern = Pattern.compile("\\[topplayer([1-9]|10)\\]");
	Pattern topposterpattern = Pattern.compile("\\[topposter([1-9]|10)\\]");
	Pattern toplikespattern = Pattern.compile("\\[toplikes([1-9]|10)\\]");
	Pattern latestmemberpattern = Pattern.compile("\\[newmember([1-9]|10)\\]");
	Pattern toppointspattern = Pattern.compile("\\[toppoints([1-9]|10)\\]");
	Pattern topdonatorpointspattern = Pattern.compile("\\[pointsspent([1-9]|10)\\]");
	Pattern topdonatormoneypattern = Pattern.compile("\\[moneyspent([1-9]|10)\\]");

	public HeadListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	@SubscribeEvent
	public void onHeadDataUpdated(HeadsUpdatedEvent event) {
		ArrayList<HeadLocation> headlist = (ArrayList)this.plugin.headlocation.headlist.get(event.getType());
		if (headlist == null) {
			return;
		}
		for (HeadLocation hloc : headlist) {
			HeadData headdata = this.plugin.headdata.getHead(event.getType(), hloc.getPosition(), hloc.getItemid());

			if (headdata == null) {
				String[] signlines = this.plugin.cachedItems.getSignData("", "", event.getType(), hloc.getPosition(), "");
				headdata = new HeadData("", signlines, event.getType(), hloc.getPosition());
			}

			if (!HeadUtils.updateHead(hloc, headdata)) {
				this.plugin.headlocation.removeHead(hloc.getSignLocation());
				this.plugin.headlocation.saveHeads();
			}
		}
	}

	@SubscribeEvent
	public void onBlockPunch(PlayerInteractEvent event) {
		if (event.isCanceled()) {
			return;
		}
		if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
			return;
		}

		World world = event.entityPlayer.worldObj;
		BlockPos pos = new BlockPos(event.pos.getX(), event.pos.getY(), event.pos.getZ());
		TileEntity block = world.getTileEntity(pos);

		if (block == null) {
			return;
		}

		ArrayList<String> ops = new ArrayList<String>();
		for(String str : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
			ops.add(str);

		if (!ops.contains(event.entityPlayer.getName().toLowerCase())) {
			if (((block instanceof TileEntitySkull)) || ((block instanceof TileEntitySign))) {
				Location loc = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				if (this.plugin.headlocation.hasHeadHere(loc)) {
					event.entityPlayer.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "I'm sorry you don't have permission to remove this sign!"));
					event.setCanceled(true);
				}
			}

			return;
		}

		if ((block instanceof TileEntitySign))
		{
			TileEntitySign sign = (TileEntitySign)block;

			ArrayList<String> stockList = new ArrayList<String>();
			for(IChatComponent comp : sign.signText)
			{
				if(comp instanceof ChatComponentText)
				{
					stockList.add(comp.getUnformattedText());
				}
			}

			String[] signlines = new String[stockList.size()];
			signlines = stockList.toArray(signlines);

			Matcher recentitemmatcher = this.recentitempattern.matcher(signlines[0]);
			Matcher topvotermatcher = this.topvoterpattern.matcher(signlines[0]);
			Matcher recentvotersmatcher = this.recentvoterspattern.matcher(signlines[0]);
			Matcher topplayermatcher = this.topplayerpattern.matcher(signlines[0]);
			Matcher toppostermatcher = this.topposterpattern.matcher(signlines[0]);
			Matcher toplikesmatcher = this.toplikespattern.matcher(signlines[0]);
			Matcher latestmembermatcher = this.latestmemberpattern.matcher(signlines[0]);
			Matcher toppointsmatcher = this.toppointspattern.matcher(signlines[0]);
			Matcher topdonatormoneymatcher = this.topdonatormoneypattern.matcher(signlines[0]);
			Matcher topdonatorpointsmatcher = this.topdonatorpointspattern.matcher(signlines[0]);
			HeadLocation hl = null;
			if (recentitemmatcher.find()) {
				int position = Integer.parseInt(recentitemmatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type;
				if (signlines[1].trim().equals(""))
					type = HeadLocation.Type.RecentDonator;
				else {
					type = HeadLocation.Type.RecentItemDonator;
				}
				if (headlocation != null) {
					EnjinMinecraftPlugin.debug("A head was found");
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					EnjinMinecraftPlugin.debug("A head was not found");
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}

				if (type == HeadLocation.Type.RecentItemDonator)
					hl.setItemid(signlines[1].trim());
			}
			else if (topvotermatcher.find()) {
				int position = Integer.parseInt(topvotermatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type;
				if (signlines[1].trim().equals("")) {
					type = HeadLocation.Type.TopMonthlyVoter;
				}
				else
				{
					if (signlines[1].trim().toLowerCase().startsWith("m")) {
						type = HeadLocation.Type.TopMonthlyVoter;
					}
					else
					{
						if (signlines[1].trim().toLowerCase().startsWith("w")) {
							type = HeadLocation.Type.TopWeeklyVoter;
						}
						else
						{
							if (signlines[1].trim().toLowerCase().startsWith("d"))
								type = HeadLocation.Type.TopDailyVoter;
							else
								type = HeadLocation.Type.TopMonthlyVoter; 
						}
					}
				}
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (recentvotersmatcher.find()) {
				int position = Integer.parseInt(recentvotersmatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.RecentVoter;
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (topplayermatcher.find()) {
				int position = Integer.parseInt(topplayermatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopPlayer;
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (toppostermatcher.find()) {
				int position = Integer.parseInt(toppostermatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopPoster;
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (toplikesmatcher.find()) {
				int position = Integer.parseInt(toplikesmatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopLikes;
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (latestmembermatcher.find()) {
				int position = Integer.parseInt(latestmembermatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.LatestMembers;
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (toppointsmatcher.find()) {
				int position = Integer.parseInt(toppointsmatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopPoints;
				if (signlines[1].trim().toLowerCase().startsWith("m"))
					type = HeadLocation.Type.TopPointsMonth;
				else if (signlines[1].trim().toLowerCase().startsWith("w"))
					type = HeadLocation.Type.TopPointsWeek;
				else if (signlines[1].trim().toLowerCase().startsWith("d")) {
					type = HeadLocation.Type.TopPointsDay;
				}
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (topdonatorpointsmatcher.find()) {
				int position = Integer.parseInt(topdonatorpointsmatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopPointsDonators;
				if (signlines[1].trim().toLowerCase().startsWith("m"))
					type = HeadLocation.Type.TopPointsDonatorsMonth;
				else if (signlines[1].trim().toLowerCase().startsWith("w"))
					type = HeadLocation.Type.TopPointsDonatorsWeek;
				else if (signlines[1].trim().toLowerCase().startsWith("d")) {
					type = HeadLocation.Type.TopPointsDonatorsDay;
				}
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}
			else if (topdonatormoneymatcher.find()) {
				int position = Integer.parseInt(topdonatormoneymatcher.group(1)) - 1;
				Location signlocation = new Location(world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				Location headlocation = findHead(signlocation);
				HeadLocation.Type type = HeadLocation.Type.TopDonators;
				if (signlines[1].trim().toLowerCase().startsWith("m"))
					type = HeadLocation.Type.TopDonatorsMonth;
				else if (signlines[1].trim().toLowerCase().startsWith("w"))
					type = HeadLocation.Type.TopDonatorsWeek;
				else if (signlines[1].trim().toLowerCase().startsWith("d")) {
					type = HeadLocation.Type.TopDonatorsDay;
				}
				if (headlocation != null) {
					hl = new HeadLocation(signlocation.getWorld(), headlocation.getX(), headlocation.getY(), headlocation.getZ(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
				else
				{
					hl = new HeadLocation(signlocation.getWorld(), signlocation.getX(), signlocation.getY(), signlocation.getZ(), type, position);
				}
			}

			if (hl != null) {
				if (hl.hasHead());
				this.plugin.headlocation.addHead(hl);
				this.plugin.headlocation.saveHeads();
				EnjinMinecraftPlugin.debug("Grabbing head data");
				HeadData headdata = this.plugin.headdata.getHead(hl.getType(), hl.getPosition(), hl.getItemid());
				if (headdata == null)
				{
					this.plugin.forceHeadUpdate();
					String[] lines = this.plugin.cachedItems.getSignData("", "", hl.getType(), hl.getPosition(), "");
					headdata = new HeadData("", lines, hl.getType(), hl.getPosition());
				}
				HeadUtils.updateSign(new Location(event.entityPlayer.worldObj, event.pos.getX(), event.pos.getY(), event.pos.getZ()), headdata.getSigndata());

				if (!HeadUtils.updateHead(hl, headdata)) {
					this.plugin.headlocation.removeHead(hl.getSignLocation());
					this.plugin.headlocation.saveHeads();
				}
			}
		}
	}

	private Location findHead(Location loc)
	{
		for (int x = loc.getX() - 1; x < loc.getX() + 2; x++) {
			for (int y = loc.getY() - 1; y < loc.getY() + 2; y++) {
				for (int z = loc.getZ() - 1; z < loc.getZ() + 2; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					TileEntity tblock = loc.getWorldObj().getTileEntity(pos);
					if ((tblock != null) && ((tblock instanceof TileEntitySkull))) {
						Location headLoc = new Location(loc.getWorld(), x, y, z);

						if (!this.plugin.headlocation.hasHeadHere(headLoc)) {
							return headLoc;
						}
					}
				}
			}
		}

		Location upperLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 2, loc.getZ());
		BlockPos pos = new BlockPos(upperLoc.getX(), upperLoc.getY(), upperLoc.getZ());
		TileEntity tblock = loc.getWorldObj().getTileEntity(pos);
		if ((tblock != null) && ((tblock instanceof TileEntitySkull)))
		{
			if (!this.plugin.headlocation.hasHeadHere(upperLoc)) {
				return upperLoc;
			}
		}
		return null;
	}
}