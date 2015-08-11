package com.enjin.officialplugin;

public enum ConfigValueTypes
{
  STRING(0), 
  INT(1), 
  DOUBLE(2), 
  FLOAT(3), 
  BOOLEAN(4), 
  FORBIDDEN(5);

  public final byte id;

  public static ConfigValueTypes fromString(String text) { for (ConfigValueTypes m : values()) {
      if (text.equalsIgnoreCase(m.name())) {
        return m;
      }
    }
    return null;
  }

  private ConfigValueTypes(int i)
  {
    this.id = (byte)i;
  }
}