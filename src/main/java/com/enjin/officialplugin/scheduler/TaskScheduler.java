package com.enjin.officialplugin.scheduler;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TaskScheduler
{
  int nexttaskid = 1;
  ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap();
  
  @SubscribeEvent
  public void tick(TickEvent.ServerTickEvent event)
  {
	  Set<Map.Entry<Integer, ScheduledTask>> thetasks = this.tasks.entrySet();
	    for (Map.Entry taskentry : thetasks) {
	      ScheduledTask task = (ScheduledTask)taskentry.getValue();
	      if (task.getTicksToExecution() <= 0) {
	        task.run();
	        if (task.repeatTask())
	          task.setTicksToExecution(task.getTicksBetweenExecution());
	        else
	          this.tasks.remove(taskentry.getKey());
	      }
	      else {
	        task.removeTickToExecution();
	      }
	    }
  }

  public String getLabel()
  {
    return "EnjinTaskScheduler";
  }

  int nextTaskID() {
    return ++this.nexttaskid;
  }

  public int runTaskTimerAsynchronously(Runnable task, int tickstostart, int ticksToWait) {
    ScheduledTask st = new ScheduledTask(task, tickstostart, ticksToWait);

    int taskID = nextTaskID();
    this.tasks.put(Integer.valueOf(taskID), st);
    return taskID;
  }

  public boolean cancelTask(int synctaskid) {
    Integer taskid = new Integer(synctaskid);
    ScheduledTask st = (ScheduledTask)this.tasks.remove(taskid);
    if (st != null) {
      st.stop();
      return true;
    }
    return false;
  }

  public void cancelAllTasks() {
    Set<Map.Entry<Integer, ScheduledTask>> thetasks = this.tasks.entrySet();
    for (Map.Entry taskentry : thetasks) {
      ScheduledTask task = (ScheduledTask)taskentry.getValue();
      task.stop();
      this.tasks.remove(taskentry.getKey());
    }
    thetasks.clear();
  }

  public int scheduleSyncDelayedTask(Runnable task) {
    ScheduledTask st = new ScheduledTask(task, 0);
    st.setAsync(false);

    int taskID = nextTaskID();
    this.tasks.put(Integer.valueOf(taskID), st);
    return taskID;
  }

  public int scheduleSyncDelayedTask(Runnable task, int delay) {
    ScheduledTask st = new ScheduledTask(task, delay);
    st.setAsync(false);

    int taskID = nextTaskID();
    this.tasks.put(Integer.valueOf(taskID), st);
    return taskID;
  }
}