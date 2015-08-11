package com.enjin.officialplugin.points;

public class PlayerDoesNotExistException extends Exception
{
  String message;
  private static final long serialVersionUID = 7598930389486470420L;

  public PlayerDoesNotExistException(String error)
  {
    this.message = error;
  }

  public String getMessage()
  {
    return this.message;
  }
}