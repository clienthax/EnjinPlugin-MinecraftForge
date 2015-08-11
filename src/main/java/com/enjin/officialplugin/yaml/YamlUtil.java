package com.enjin.officialplugin.yaml;

import java.util.List;

public class YamlUtil
{
  public static int getLeadingSpaces(String line)
  {
    int amount = 0;
    for (char c : line.toCharArray()) {
      if (c != ' ') break;
      amount++;
    }

    return amount;
  }

  public static String emptyString(int length)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  public static String listToPath(List<String> list)
  {
    StringBuilder sb = new StringBuilder();
    for (String string : list) {
      if (sb.length() == 0)
        sb.append(string);
      else {
        sb.append("." + string);
      }
    }
    return sb.toString();
  }
}