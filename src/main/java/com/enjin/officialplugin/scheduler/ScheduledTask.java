package com.enjin.officialplugin.scheduler;

public class ScheduledTask
{
  Runnable task;
  boolean repeatTask = false;
  boolean async = false;
  int ticksToExecution = 0;
  int ticksBetweenExecution = 0;
  TaskWrapper wrapper;
  Thread thread;

  public ScheduledTask(Runnable task, int waitTime)
  {
    this.task = task;
    this.ticksToExecution = waitTime;
  }

  public ScheduledTask(Runnable task, int initialWaitTime, int waitTime) {
    this.task = task;
    this.ticksToExecution = initialWaitTime;
    this.ticksBetweenExecution = waitTime;
    this.repeatTask = true;
  }

  public void run() {
    if (this.repeatTask) {
      if (this.async) {
        if (this.wrapper == null) {
          this.wrapper = new TaskWrapper(this.task);
          this.thread = new Thread(this.wrapper);
          this.thread.start();
        } else {
          this.thread.interrupt();
        }
      }
      else this.task.run();

    }
    else if (this.async) {
      Thread t = new Thread(this.task);
      t.start();
    } else {
      this.task.run();
    }
  }

  public boolean repeatTask()
  {
    return this.repeatTask;
  }

  public void stop() {
    if ((this.repeatTask) && 
      (this.wrapper != null)) {
      this.wrapper.stopIt();
      this.thread.interrupt();
    }

    this.wrapper = null;
    this.thread = null;
    this.task = null;
  }

  public int getTicksToExecution() {
    return this.ticksToExecution;
  }

  public void setTicksToExecution(int ticksToExecution) {
    this.ticksToExecution = ticksToExecution;
  }

  public int getTicksBetweenExecution() {
    return this.ticksBetweenExecution;
  }

  public void removeTickToExecution() {
    this.ticksToExecution -= 1;
  }

  public void setAsync(boolean value) {
    this.async = value;
  }

  public boolean isAsync() {
    return this.async;
  }
}