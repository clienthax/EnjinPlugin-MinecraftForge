package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.server.MinecraftServer;

public class CommandExecuter
  implements Runnable
{
  EnjinMinecraftPlugin plugin;
  ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();

  public CommandExecuter(EnjinMinecraftPlugin plugin)
  {
    this.plugin = plugin;
  }

  public synchronized void addCommand(String command)
  {
    EnjinMinecraftPlugin.debug("Adding command to queue: " + command);
    System.out.println("Adding command to queue: " + command);
    this.commandqueue.add(new CommandWrapper(command));
  }

  @Override
  public synchronized void run()
  {
    EnjinMinecraftPlugin.debug("Running queued commands...");

    while (!this.commandqueue.isEmpty()) {
      CommandWrapper comm = (CommandWrapper)this.commandqueue.poll();
      EnjinMinecraftPlugin.debug("Executing queued command: " + comm.getCommand());
      System.out.println("Executing queued command: " + comm.getCommand());
      MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), comm.getCommand());
    }
  }
}