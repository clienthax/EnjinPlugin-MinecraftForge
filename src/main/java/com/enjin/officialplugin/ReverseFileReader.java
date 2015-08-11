package com.enjin.officialplugin;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReverseFileReader
{
  private RandomAccessFile randomfile;
  private long position;

  public ReverseFileReader(String filename)
    throws Exception
  {
    this.randomfile = new RandomAccessFile(filename, "r");
    this.position = this.randomfile.length();
    this.randomfile.seek(this.position);

    String thisLine = this.randomfile.readLine();
    while (thisLine == null) {
      this.position -= 1L;
      this.randomfile.seek(this.position);
      thisLine = this.randomfile.readLine();
      this.randomfile.seek(this.position);
    }
  }

  public String readLine()
    throws Exception
  {
    String finalLine = "";

    if (this.position < 0L) {
      return null;
    }

    while (this.position >= 0L)
    {
      this.randomfile.seek(this.position);

      int thisCode = this.randomfile.readByte();
      char thisChar = (char)thisCode;

      if ((thisCode == 13) || (thisCode == 10))
      {
        this.randomfile.seek(this.position - 1L);
        int nextCode = this.randomfile.readByte();
        if (((thisCode == 10) && (nextCode == 13)) || ((thisCode == 13) && (nextCode == 10)))
        {
          this.position -= 1L;
        }

        this.position -= 1L;
        break;
      }

      finalLine = thisChar + finalLine;

      this.position -= 1L;
    }

    return finalLine;
  }

  public void close() {
    try {
      this.randomfile.close();
    }
    catch (IOException e)
    {
    }
    catch (Exception e)
    {
    }
  }
}