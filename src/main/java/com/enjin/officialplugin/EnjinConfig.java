package com.enjin.officialplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import net.minecraft.server.MinecraftServer;

public class EnjinConfig
{
  File INIFILE;
  Properties iniSettings = new Properties();
  boolean newconfigfile = false;

  public EnjinConfig(File datafolder) {
    this.INIFILE = new File(datafolder, "config.properties");
    loadIni();
  }

  public void loadIni() {
    if (this.INIFILE.exists())
      try {
        this.iniSettings.load(new FileInputStream(this.INIFILE));
      }
      catch (Exception e) {
        MinecraftServer.getServer().logInfo("[EnjinMinecraftPlugin] - properties file load failed, using defaults.");
      }
    else
      createIni();
  }

  public void createIni()
  {
    this.newconfigfile = true;
    try {
      this.INIFILE.getParentFile().mkdirs();
      this.INIFILE.createNewFile();
      this.iniSettings.load(new FileInputStream(this.INIFILE));
    } catch (Exception e) {
      MinecraftServer.getServer().logInfo("[EnjinMinecraftPlugin] - properties file creation failed, using defaults.");
    }
  }

  public boolean isNewConfigFile() {
    return this.newconfigfile;
  }

  public boolean getBoolean(String key) {
    String value = this.iniSettings.getProperty(key, "");

    return value.equalsIgnoreCase("true");
  }

  public boolean getBoolean(String key, boolean other)
  {
    String value = this.iniSettings.getProperty(key, "");
    if (value.equalsIgnoreCase("true"))
      return true;
    if (value.equalsIgnoreCase("false")) {
      return false;
    }
    return other;
  }

  public double getDouble(String key, double default1)
  {
    String value = this.iniSettings.getProperty(key, "");
    try {
      return Double.parseDouble(value.trim()); } catch (Exception e) {
    }
    return default1;
  }

  public float getFloat(String key, float default1)
  {
    String value = this.iniSettings.getProperty(key, "");
    try {
      return Float.parseFloat(value.trim()); } catch (Exception e) {
    }
    return default1;
  }

  public int getInt(String key, int default1)
  {
    String value = this.iniSettings.getProperty(key, "");
    try {
      return Integer.parseInt(value.trim()); } catch (Exception e) {
    }
    return default1;
  }

  public long getLong(String key, long default1)
  {
    String value = this.iniSettings.getProperty(key, "");
    try {
      return Long.parseLong(value.trim()); } catch (Exception e) {
    }
    return default1;
  }

  public String getString(String key, String default1)
  {
    return this.iniSettings.getProperty(key, default1);
  }

  public void set(String key, boolean value) {
    this.iniSettings.setProperty(key, Boolean.toString(value));
  }

  public void set(String key, String value) {
    this.iniSettings.setProperty(key, value);
  }

  public void set(String key, int value) {
    this.iniSettings.setProperty(key, Integer.toString(value));
  }

  public void save() {
    try {
      this.iniSettings.store(new FileOutputStream(this.INIFILE), "");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void set(String key, double number) {
    this.iniSettings.setProperty(key, Double.toString(number));
  }

  public void set(String key, float number) {
    this.iniSettings.setProperty(key, Float.toString(number));
  }

  public String getString(String key) {
    return this.iniSettings.getProperty(key);
  }
}