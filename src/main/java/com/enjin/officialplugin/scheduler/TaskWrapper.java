package com.enjin.officialplugin.scheduler;

public class TaskWrapper
  implements Runnable
{
  Runnable task;
  boolean running = true;

  public TaskWrapper(Runnable task) {
    this.task = task;
  }

  public synchronized void stopIt() {
    this.running = false;
  }

  public synchronized void run()
  {
    while (this.running) {
      this.task.run();
      try {
        wait();
      }
      catch (InterruptedException e)
      {
      }
    }
  }
}