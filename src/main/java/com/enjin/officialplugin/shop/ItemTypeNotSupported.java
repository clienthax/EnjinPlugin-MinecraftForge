package com.enjin.officialplugin.shop;

public class ItemTypeNotSupported extends Throwable
{
  private static final long serialVersionUID = 5517328419867401859L;

  public ItemTypeNotSupported(String string)
  {
    super(string);
  }
}