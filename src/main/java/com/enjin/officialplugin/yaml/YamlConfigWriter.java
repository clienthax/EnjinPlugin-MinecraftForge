package com.enjin.officialplugin.yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

public class YamlConfigWriter extends YamlWriterNode
{
  String indent;

  public YamlConfigWriter()
  {
    this.value = null;
    this.indent = "  ";
  }

  public void save(File file)
    throws IOException
  {
    if (file == null) throw new IllegalArgumentException();

    BufferedWriter out = null;
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

      for (Map.Entry child : this.children.entrySet())
        writeValues(out, (YamlWriterNode)child.getValue(), (String)child.getKey(), 0);
    }
    finally {
      if (out != null)
        out.close();
    }
  }

  private void writeValues(Writer writer, YamlWriterNode node, String nodeName, int depth)
    throws IOException
  {
    writer.write(getIndent(depth) + nodeName + ": " + node.value + "\r\n");
    for (Map.Entry child : node.children.entrySet())
      writeValues(writer, (YamlWriterNode)child.getValue(), (String)child.getKey(), depth + 1);
  }

  private String getIndent(int depth)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append(this.indent);
    }
    return sb.toString();
  }
}