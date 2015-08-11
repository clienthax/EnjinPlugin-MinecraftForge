package com.enjin.officialplugin.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlWriterNode
{
  Map<String, YamlWriterNode> children;
  String value;

  public YamlWriterNode()
  {
    this.children = new LinkedHashMap();
    this.value = "";
  }

  public void set(String path, Object value)
  {
    if (path.trim().equals("")) {
      this.value = value.toString();
    } else {
      String[] pathNodes = path.split("\\.", 2);

      YamlWriterNode lowerNode = (YamlWriterNode)this.children.get(pathNodes[0]);
      if (lowerNode == null) {
        this.children.put(pathNodes[0], lowerNode = new YamlWriterNode());
      }

      lowerNode.set(pathNodes.length == 1 ? "" : pathNodes[1], value);
    }
  }

  public void load(YamlConfigSection section)
  {
    for (String paths : section.paths.keySet())
      set(paths, section.getString(paths, ""));
  }
}