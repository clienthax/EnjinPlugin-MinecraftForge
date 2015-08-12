package com.enjin.officialplugin.shop;

import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.heads.HeadLocation;

public class ShopItems
{
  private static String latestpurchase = "New Purchase ";
  private static String latestitempurchase = "Purchase ";
  private static String latestvoter = "Latest Voter ";
  private static String topplayer = "Top Player ";
  private static String topposter = "Top Poster ";
  private static String toplikes = "Top Likes ";
  private static String latestmembers = "New Member ";
  private static String toppoints = "Top Points ";
  private static String topdonatorsmoney = "Top Donator ";
  private static String topdonatorspoints = "Top Points";

  ConcurrentHashMap<String, ShopItem> shopitems = new ConcurrentHashMap<String, ShopItem>();

  public ShopItems() {
    this.shopitems.put("multiple items", new ShopItem("Multiple Items", "Multiple Items", "", "", ""));
  }

  public void addShopItem(ShopItem item) {
    this.shopitems.put(item.getId().toLowerCase(), item);
  }

  public ShopItem getShopItem(String itemId) {
    return this.shopitems.get(itemId.toLowerCase());
  }

  public void clearShopItems() {
    this.shopitems.clear();

    this.shopitems.put("multiple items", new ShopItem("Multiple Items", "Multiple Items", "", "", ""));
  }

  public String[] getSignData(String player, String itemid, HeadLocation.Type type, int position, String amount) {
    String[] signdata = new String[4];
    ShopItem si;
    switch (type.ordinal()) {
    case 1:
      signdata[0] = (latestpurchase + (position + 1));
      signdata[1] = player;
      si = getShopItem(itemid);
      if (si == null) {
        signdata[2] = "";
        signdata[3] = amount;
      } else {
        signdata[2] = si.getName();
        if (signdata[2].length() > 15) {
          signdata[2] = signdata[2].substring(0, 15);
        }
        signdata[3] = amount;
      }
      break;
    case 2:
      signdata[0] = (latestitempurchase + (position + 1));
      signdata[1] = player;
      si = getShopItem(itemid);
      if (si == null) {
        signdata[2] = "";
        signdata[3] = "";
      } else {
        signdata[2] = si.getName();
        if (signdata[2].length() > 15) {
          signdata[2] = signdata[2].substring(0, 15);
        }
        signdata[3] = amount;
      }
      break;
    case 3:
      signdata[0] = (latestvoter + (position + 1));
      signdata[1] = player;
      signdata[2] = itemid;
      signdata[3] = amount;
      break;
    case 4:
      signdata[0] = ("#" + (position + 1) + " Top Monthly");
      signdata[1] = "Voter";
      signdata[2] = player;
      signdata[3] = (amount + " Votes");
      break;
    case 5:
      signdata[0] = ("#" + (position + 1) + " Top Weekly");
      signdata[1] = "Voter";
      signdata[2] = player;
      signdata[3] = (amount + " Votes");
      break;
    case 6:
      signdata[0] = ("#" + (position + 1) + " Top Daily");
      signdata[1] = "Voter";
      signdata[2] = player;
      signdata[3] = (amount + " Votes");
      break;
    case 7:
      signdata[0] = (topplayer + (position + 1));
      signdata[1] = player;
      signdata[2] = "";
      signdata[3] = (amount + " Hours");
      break;
    case 8:
      signdata[0] = (topposter + (position + 1));
      signdata[1] = player;
      signdata[2] = "";
      signdata[3] = (amount + " Posts");
      break;
    case 9:
      signdata[0] = (toplikes + (position + 1));
      signdata[1] = player;
      signdata[2] = "";
      signdata[3] = (amount + " Likes");
      break;
    case 10:
      signdata[0] = (latestmembers + (position + 1));
      signdata[1] = player;
      signdata[2] = itemid;
      signdata[3] = amount;
      break;
    case 11:
      signdata[0] = (toppoints + (position + 1));
      signdata[1] = player;
      signdata[2] = "";
      signdata[3] = (amount + " Points");
      break;
    case 12:
      signdata[0] = (toppoints + (position + 1));
      signdata[1] = "for the Month";
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 13:
      signdata[0] = (toppoints + (position + 1));
      signdata[1] = "for the Week";
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 14:
      signdata[0] = (toppoints + (position + 1));
      signdata[1] = "for the Day";
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 15:
      signdata[0] = (topdonatorsmoney + (position + 1));
      signdata[1] = "";
      signdata[2] = player;
      signdata[3] = amount;
      break;
    case 16:
      signdata[0] = (topdonatorsmoney + (position + 1));
      signdata[1] = "for the Day";
      signdata[2] = player;
      signdata[3] = amount;
      break;
    case 17:
      signdata[0] = (topdonatorsmoney + (position + 1));
      signdata[1] = "for the Week";
      signdata[2] = player;
      signdata[3] = amount;
      break;
    case 18:
      signdata[0] = (topdonatorsmoney + (position + 1));
      signdata[1] = "for the Month";
      signdata[2] = player;
      signdata[3] = amount;
      break;
    case 19:
      signdata[0] = topdonatorspoints;
      signdata[1] = ("Donator " + (position + 1));
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 20:
      signdata[0] = topdonatorspoints;
      signdata[1] = ("Spent Today #" + (position + 1));
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 21:
      signdata[0] = topdonatorspoints;
      signdata[1] = ("Spent|Week " + (position + 1));
      signdata[2] = player;
      signdata[3] = (amount + " Points");
      break;
    case 22:
      signdata[0] = topdonatorspoints;
      signdata[1] = ("Spent|Month " + (position + 1));
      signdata[2] = player;
      signdata[3] = (amount + " Points");
    }

    return signdata;
  }

  public String[] updateSignData(String[] signdata, HeadLocation.Type type, int position) {
    switch (type.ordinal()) {
    case 1:
      signdata[0] = (latestpurchase + (position + 1));
      break;
    case 2:
      signdata[0] = (latestitempurchase + (position + 1));
      break;
    case 3:
      signdata[0] = (latestvoter + (position + 1));
      break;
    case 4:
      signdata[0] = ("#" + (position + 1) + " Top Monthly");
      signdata[1] = "Voter";
      break;
    case 5:
      signdata[0] = ("#" + (position + 1) + " Top Weekly");
      signdata[1] = "Voter";
      break;
    case 6:
      signdata[0] = ("#" + (position + 1) + " Top Daily");
      signdata[1] = "Voter";
      break;
    case 7:
      signdata[0] = (topplayer + (position + 1));
      break;
    case 8:
      signdata[0] = (topposter + (position + 1));
      break;
    case 9:
      signdata[0] = (toplikes + (position + 1));
      break;
    case 10:
      signdata[0] = (latestmembers + (position + 1));
      break;
    case 11:
    case 12:
    case 13:
    case 14:
      signdata[0] = (toppoints + (position + 1));
      break;
    case 15:
    case 16:
    case 17:
    case 18:
      signdata[0] = (topdonatorsmoney + (position + 1));
      break;
    case 19:
      signdata[1] = ("Donator " + (position + 1));
      break;
    case 20:
      signdata[1] = ("Spent Today #" + (position + 1));
      break;
    case 21:
      signdata[1] = ("Spent|Week " + (position + 1));
      break;
    case 22:
      signdata[1] = ("Spent|Month " + (position + 1));
    }

    return signdata;
  }
}