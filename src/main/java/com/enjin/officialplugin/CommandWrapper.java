package com.enjin.officialplugin;

import net.minecraft.command.ICommandSender;

public class CommandWrapper
{
  String command;
  long delay;
  ICommandSender sender;

  public CommandWrapper(String command)
  {
    this.command = command;
  }

  public CommandWrapper(ICommandSender sender, String command, long delay) {
    this.sender = sender;
    this.command = command;
    this.delay = delay;
  }

  public String getCommand() {
    return this.command;
  }

  public long getDelay() {
    return this.delay;
  }

  public ICommandSender getSender() {
    return this.sender;
  }
}