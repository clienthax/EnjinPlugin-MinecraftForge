package com.enjin.officialplugin.yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class YamlConfigSection
{
  List<String> lines;
  int indL;
  String indS;
  Map<String, Integer> paths;

  public YamlConfigSection()
  {
    this.lines = new ArrayList();
    this.paths = new HashMap();
  }

  private YamlConfigSection(List<String> lines, Map<String, Integer> paths)
  {
    this.lines = lines;
    this.paths = paths;
  }

  public void load(File file)
    throws FileNotFoundException, IOException, InvalidYamlConfigurationException
  {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));

      String line = null;
      while ((line = br.readLine()) != null)
        this.lines.add(line);
    }
    finally {
      if (br != null) {
        br.close();
      }
    }

    checkIndent();
    getPaths();
  }

  public void checkIndent()
    throws InvalidYamlConfigurationException
  {
    for (int i = 0; i < this.lines.size(); i++) {
      String line = (String)this.lines.get(i);
      if (line.trim().equals(""))
        continue;
      if (this.indL == 0) {
        if (line.startsWith(" ")) {
          this.indL = YamlUtil.getLeadingSpaces(line);
          this.indS = YamlUtil.emptyString(this.indL);
        }
      }
      else if (YamlUtil.getLeadingSpaces(line) % this.indL != 0)
        throw new InvalidYamlConfigurationException("Inconsistent indentation on line " + i);
    }
  }

  public void getPaths()
    throws InvalidYamlConfigurationException
  {
    int depth = 0;
    LinkedList path = new LinkedList();

    for (int i = 0; i < this.lines.size(); i++) {
      String line = (String)this.lines.get(i);
      if (line.trim().equals(""))
        continue;
      int currentDepth = YamlUtil.getLeadingSpaces(line) / this.indL;
      String node = line.split(":", 2)[0].trim();

      if (path.isEmpty()) {
        path.add(node);
      }
      else if (currentDepth == depth) {
        path.set(path.size() - 1, node);
      } else if (currentDepth > depth) {
        path.add(node);
      } else if (currentDepth < depth) {
        for (int j = 0; j < depth - currentDepth; j++) {
          path.removeLast();
        }

        if (path.isEmpty()) {
          throw new InvalidYamlConfigurationException("The indentation at line " + i + " is less than the starting indentation");
        }
        path.set(path.size() - 1, node);
      }

      depth = currentDepth;
      this.paths.put(YamlUtil.listToPath(path), Integer.valueOf(i));
    }
  }

  public YamlConfigSection getConfigSection(String path)
  {
    HashMap newPaths = new HashMap();

    for (Map.Entry pathEntry : this.paths.entrySet()) {
      if (((String)pathEntry.getKey()).startsWith(path + ".")) {
        newPaths.put(((String)pathEntry.getKey()).substring(path.length() + 1), pathEntry.getValue());
      }
    }

    return new YamlConfigSection(this.lines, newPaths);
  }

  public Map<String, Object> getValues(boolean deep)
  {
    HashMap map = new HashMap();

    for (Map.Entry pathEntry : this.paths.entrySet()) {
      if ((deep) || (!((String)pathEntry.getKey()).contains("."))) {
        map.put(pathEntry.getKey(), getString((String)pathEntry.getKey(), ""));
      }
    }

    return map;
  }

  public boolean getBoolean(String path, boolean def)
  {
    Integer lineNum = (Integer)this.paths.get(path);
    if (lineNum == null)
      return def;
    try
    {
      return Boolean.parseBoolean(((String)this.lines.get(lineNum.intValue())).split(":", 2)[1].trim()); } catch (Exception e) {
    }
    return def;
  }

  public int getInt(String path, int def)
  {
    Integer lineNum = (Integer)this.paths.get(path);
    if (lineNum == null)
      return def;
    try
    {
      return Integer.parseInt(((String)this.lines.get(lineNum.intValue())).split(":", 2)[1].trim()); } catch (Exception e) {
    }
    return def;
  }

  public String getString(String path, String def)
  {
    Integer lineNum = (Integer)this.paths.get(path);
    if (lineNum == null)
      return def;
    try
    {
      return ((String)this.lines.get(lineNum.intValue())).split(":", 2)[1].trim(); } catch (Exception e) {
    }
    return def;
  }
}