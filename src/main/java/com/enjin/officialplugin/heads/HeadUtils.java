package com.enjin.officialplugin.heads;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class HeadUtils
{
	public static boolean updateHead(HeadLocation head, HeadData data)
	{
		if (head.hasHead()) {
			return updateHead(head.getSignLocation(), head.getHeadLocation(), data.getPlayername(), data.getSigndata());
		}
		return updateSign(head.getSignLocation(), data.getSigndata());
	}

	public static boolean updateHead(Location signloc, Location headloc, String playername, String[] signlines)
	{
		BlockPos pos = new BlockPos(headloc.getX(), headloc.getY(), headloc.getZ());
		TileEntity headblock = headloc.getWorldObj().getTileEntity(pos);
		if ((headblock != null) && ((headblock instanceof TileEntitySkull))) {
			TileEntitySkull skullblock = (TileEntitySkull)headblock;
			skullblock.setType(3);
			GameProfile playerProfile = null;
			for(GameProfile profile : MinecraftServer.getServer().getGameProfiles())
			{
				if(profile.getName().equals(playername))
				{
					playerProfile = profile;
					break;
				}
			}
			skullblock.setPlayerProfile(playerProfile);
			headloc.getWorldObj().markBlockForUpdate(pos);
		} else {
			return false;
		}
		return updateSign(signloc, signlines);
	}

	public static String getHeadName(Location headlocation)
	{
		BlockPos pos = new BlockPos(headlocation.getX(), headlocation.getY(), headlocation.getZ());
		TileEntity headblock = headlocation.getWorldObj().getTileEntity(pos);
		if ((headblock != null) && ((headblock instanceof TileEntitySkull))) {
			TileEntitySkull skullblock = (TileEntitySkull)headblock;
			return skullblock.getPlayerProfile().getName();
		}
		return null;
	}

	public static String[] getSignData(Location signloc)
	{
		BlockPos pos = new BlockPos(signloc.getX(), signloc.getY(), signloc.getZ());
		TileEntity sign = signloc.getWorldObj().getTileEntity(pos);
		if ((sign != null) && ((sign instanceof TileEntitySign))) {
			List<String> list = new ArrayList<String>();
			TileEntitySign signtype = (TileEntitySign)sign;
			for(IChatComponent component : signtype.signText)
			{
				if(component instanceof ChatComponentText)
				{
					ChatComponentText comp = (ChatComponentText) component;
					list.add(comp.getUnformattedText());
				}
			}
			String[] stringList = new String[list.size()];
			stringList = list.toArray(stringList);
			return stringList;
		}
		return null;
	}

	public static boolean updateSign(Location signloc, String[] lines)
	{

		BlockPos pos = new BlockPos(signloc.getX(), signloc.getY(), signloc.getZ());
		TileEntity sign = signloc.getWorldObj().getTileEntity(pos);
		if ((sign != null) && ((sign instanceof TileEntitySign))) {
			TileEntitySign signtype = (TileEntitySign)sign;
			signtype.signText[0] = new ChatComponentText(lines[0]);
			signtype.signText[1] = new ChatComponentText(lines[1]);
			signtype.signText[2] = new ChatComponentText(lines[2]);
			signtype.signText[3] = new ChatComponentText(lines[3]);
			signloc.getWorldObj().markBlockForUpdate(pos);
			return true;
		}
		return false;
	}

	public static World getWorldByName(String name)
	{
		for (World world : MinecraftServer.getServer().worldServers) {
			if (world.getWorldInfo().getWorldName().equalsIgnoreCase(name)) {
				return world;
			}
		}
		return null;
	}
}