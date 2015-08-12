package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.ChatColor;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class ShopEnableChat extends CommandBase
{
  ShopListener sl;

  public ShopEnableChat(ShopListener listen)
  {
    this.sl = listen;
  }

  @Override
  public String getCommandName()
  {
    return "ec";
  }

  @Override
  public void processCommand(ICommandSender icommandsender, String[] astring)
  {
    EntityPlayerMP player = null;
    if ((icommandsender instanceof EntityPlayerMP))
      player = (EntityPlayerMP)icommandsender;
    else {
      return;
    }

    if (this.sl.playersdisabledchat.containsKey(player.getCommandSenderName().toLowerCase())) {
      this.sl.playersdisabledchat.remove(player.getCommandSenderName().toLowerCase());
      player.addChatMessage(new ChatComponentText(ChatColor.GREEN + "Your chat is now enabled."));
    }
  }

  @Override
  public String getCommandUsage(ICommandSender icommandsender)
  {
    return "/ec";
  }
}