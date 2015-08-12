package com.enjin.officialplugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class EnjinLogFormatter extends Formatter
{
  DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

  @Override
  public String format(LogRecord rec)
  {
    StringBuffer buf = new StringBuffer(5);
    buf.append(calcDate(rec.getMillis()) + " ");
    buf.append("[" + rec.getLevel() + "]");
    buf.append(' ');
    buf.append(formatMessage(rec) + "\n");
    return buf.toString();
  }

  private String calcDate(long millisecs) {
    Date resultdate = new Date(millisecs);
    return this.dateFormat.format(resultdate);
  }

  @Override
  public String getHead(Handler h)
  {
    return "Started logging the Enjin Plugin on " + calcDate(System.currentTimeMillis()) + "\n";
  }

  @Override
  public String getTail(Handler h)
  {
    return "";
  }
}