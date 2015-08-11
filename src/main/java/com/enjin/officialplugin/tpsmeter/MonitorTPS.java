package com.enjin.officialplugin.tpsmeter;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import java.util.LinkedList;

public class MonitorTPS
  implements Runnable
{
  private long lasttime = System.currentTimeMillis();
  private LinkedList<Double> tpslist = new LinkedList();
  private int tickmeasurementinterval = 40;
  EnjinMinecraftPlugin plugin;
  private int maxentries = 25;

  public MonitorTPS(EnjinMinecraftPlugin plugin) {
    this.plugin = plugin;
  }

  public synchronized void run()
  {
    long currenttime = System.currentTimeMillis();
    double timespent = (currenttime - this.lasttime) / 1000.0D;
    double tps = this.tickmeasurementinterval / timespent;
    if (this.tpslist.size() >= this.maxentries) {
      this.tpslist.pop();
    }
    this.tpslist.add(Double.valueOf(tps));

    this.lasttime = currenttime;
  }

  public double getTPSAverage()
  {
    if (this.tpslist.size() > 0) {
      double alltps = 0.0D;
      for (Double tps : this.tpslist) {
        alltps += tps.floatValue();
      }
      return alltps / this.tpslist.size();
    }
    return -1.0D;
  }

  public double getLastTPSMeasurement() {
    if (this.tpslist.size() > 0) {
      return ((Double)this.tpslist.getLast()).doubleValue();
    }
    return -1.0D;
  }

  public void clearTPS() {
    double lastmeasurement = getLastTPSMeasurement();
    this.tpslist.clear();
    this.tpslist.add(Double.valueOf(lastmeasurement));
  }
}