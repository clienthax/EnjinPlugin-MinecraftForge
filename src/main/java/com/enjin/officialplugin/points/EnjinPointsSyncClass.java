package com.enjin.officialplugin.points;

import com.enjin.officialplugin.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class EnjinPointsSyncClass
  implements Runnable
{
  String playername;
  int points;
  PointsAPI.Type type;
  ICommandSender sender;

  public EnjinPointsSyncClass(ICommandSender sender, String playername, int points, PointsAPI.Type type)
  {
    this.playername = playername;
    this.points = points;
    this.type = type;
    this.sender = sender;
  }

  @Override
  public synchronized void run()
  {
    try {
      int amount = PointsAPI.modifyPointsToPlayer(this.playername, this.points, this.type);
      String addremove = "";
      String toplayer = "";
      switch (this.type.ordinal()) {
      case 1:
        addremove = "added";
        toplayer = "added " + this.points + " points to your account!";
        break;
      case 2:
        addremove = "removed";
        toplayer = "removed " + this.points + " points from your account!";
        break;
      case 3:
        addremove = "set";
        toplayer = "set your points balance.";
      }
      this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_GREEN + "Successfully " + addremove + " " + this.points + " points to player " + this.playername + "! The player now has " + amount + " points."));
      EntityPlayer p = PlayerUtil.getPlayer(this.playername);
      if (p != null)
        p.addChatMessage(new ChatComponentText(ChatColor.GOLD + this.sender.getCommandSenderName() + ChatColor.YELLOW + " just " + toplayer + " You now have " + ChatColor.DARK_GREEN + amount + " points."));
    }
    catch (NumberFormatException e) {
      this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "Enjin Error: Not a valid number!"));
    } catch (PlayerDoesNotExistException e) {
      this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "Enjin Error: That player has not registered on the website yet! In order to use this feature the player must be added on the website."));
    } catch (ErrorConnectingToEnjinException e) {
      e.printStackTrace();
      this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "Enjin Error: We're unable to connect to enjin at this current time, please try again later."));
    }
  }
}