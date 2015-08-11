package com.enjin.officialplugin.points;

public class ErrorConnectingToEnjinException extends Exception
{
  private static final long serialVersionUID = 8988005958432507165L;
  String message;

  public ErrorConnectingToEnjinException(String string)
  {
    this.message = string;
  }

  public String getMessage()
  {
    return this.message;
  }
}