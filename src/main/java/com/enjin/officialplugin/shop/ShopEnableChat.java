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

  public String getName()
  {
    return "ec";
  }

  public void execute(ICommandSender icommandsender, String[] astring)
  {
    EntityPlayerMP player = null;
    if ((icommandsender instanceof EntityPlayerMP))
      player = (EntityPlayerMP)icommandsender;
    else {
      return;
    }

    if (this.sl.playersdisabledchat.containsKey(player.getName().toLowerCase())) {
      this.sl.playersdisabledchat.remove(player.getName().toLowerCase());
      player.addChatMessage(new ChatComponentText(ChatColor.GREEN + "Your chat is now enabled."));
    }
  }

  public String getCommandUsage(ICommandSender icommandsender)
  {
    return "/ec";
  }
}