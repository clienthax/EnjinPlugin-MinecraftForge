package com.enjin.officialplugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EnjinErrorReport
{
  Throwable e = null;
  String info = "";
  String otherinformation = "";
  long timethrown = System.currentTimeMillis();

  public EnjinErrorReport(Throwable e, String otherinformation) {
    this.e = e;
    this.otherinformation = otherinformation;
  }

  public EnjinErrorReport(String data, String otherinformation) {
    this.info = data;
    this.otherinformation = otherinformation;
  }

  public String toString()
  {
    StringBuilder errorstring = new StringBuilder();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    Date date = new Date(this.timethrown);
    errorstring.append("Enjin plugin error report. Error generated on: " + dateFormat.format(date) + ":\n");
    errorstring.append("Extra data: " + this.otherinformation + "\n");
    if (this.e != null) {
      errorstring.append("Stack trace:\n");
      errorstring.append(this.e.toString() + "\n");
      StackTraceElement[] stacktrace = this.e.getStackTrace();
      for (int i = 0; i < stacktrace.length; i++)
        errorstring.append(stacktrace[i].toString() + "\n");
    }
    else {
      errorstring.append("More Info:\n");
      errorstring.append(this.info);
    }
    return errorstring.toString();
  }
}