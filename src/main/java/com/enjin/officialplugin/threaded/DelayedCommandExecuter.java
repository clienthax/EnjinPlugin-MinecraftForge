package com.enjin.officialplugin.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class DelayedCommandExecuter
implements Runnable
{
	PriorityBlockingQueue<CommandWrapper> commandqueue = new PriorityBlockingQueue();
	long nexttime = -1L;
	long lastsavetime = 0L;
	boolean dirty = false;
	EnjinMinecraftPlugin plugin;

	public DelayedCommandExecuter(EnjinMinecraftPlugin plugin)
	{
		this.lastsavetime = System.currentTimeMillis();
		this.plugin = plugin;
	}

	public synchronized void addCommand(ICommandSender sender, String command, long timetoexecute) {
		this.commandqueue.add(new CommandWrapper(sender, command, timetoexecute));
		CommandWrapper comm;
		if ((comm = (CommandWrapper)this.commandqueue.peek()) != null)
			this.nexttime = comm.getDelay();
		else {
			this.nexttime = -1L;
		}
		this.dirty = true;
	}

	public void saveCommands()
	{
		if (!this.dirty) {
			return;
		}
		BufferedWriter buffWriter = null;
		try {
			File commandstoexecute = new File(this.plugin.getDataFolder(), "commandqueue.txt");
			FileWriter fileWriter = new FileWriter(commandstoexecute, false);
			buffWriter = new BufferedWriter(fileWriter);

			this.dirty = false;
			Iterator thequeue = this.commandqueue.iterator();
			while (thequeue.hasNext()) {
				CommandWrapper thecommand = (CommandWrapper)thequeue.next();
				buffWriter.write(thecommand.toString());
				buffWriter.newLine();
			}
		}
		catch (Exception e) {
			this.dirty = true;
		} finally {
			if (buffWriter != null) {
				try {
					buffWriter.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.lastsavetime = System.currentTimeMillis();
	}

	public void loadCommands(ICommandSender sender) {
		this.commandqueue.clear();
		try {
			File commandstoexecute = new File(this.plugin.getDataFolder(), "commandqueue.txt");
			if (!commandstoexecute.exists()) {
				return;
			}
			FileReader fileReader = new FileReader(commandstoexecute.getPath());
			BufferedReader buffReader = new BufferedReader(fileReader);
			String currentLine = "";
			while ((currentLine = buffReader.readLine()) != null) {
				String[] commandsplit = currentLine.split("");
				if (commandsplit.length > 1) {
					addCommand(sender, commandsplit[0], Long.getLong(commandsplit[1]).longValue());
				}
				else {
					addCommand(sender, commandsplit[0], 0L);
				}
			}
			buffReader.close();
			return;
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public synchronized void run() {
		if ((this.nexttime > -1L) && (this.nexttime <= System.currentTimeMillis())) {
			CommandWrapper comm;
			try { while (((comm = (CommandWrapper)this.commandqueue.peek()) != null) && (comm.getDelay() <= System.currentTimeMillis())) {
				comm = (CommandWrapper)this.commandqueue.poll();
				EnjinMinecraftPlugin.debug("Executing delayed command: " + comm.getCommand());
				MinecraftServer.getServer().getCommandManager().executeCommand(comm.getSender(), comm.getCommand());
			}
			} catch (Exception e)
			{
			}
			if ((comm = (CommandWrapper)this.commandqueue.peek()) != null)
				this.nexttime = comm.getDelay();
			else {
				this.nexttime = -1L;
			}
			this.dirty = true;
		}

		if ((this.dirty) && (this.lastsavetime + 60000L > System.currentTimeMillis()))
			saveCommands();
	}
}