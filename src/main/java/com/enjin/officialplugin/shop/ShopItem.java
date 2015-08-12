package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ShopItem extends AbstractShopSuperclass
{
  String id = "";
  String name = "";
  String price = "";
  String info = "";
  String points = "";

  ArrayList<ShopItemOptions> options = new ArrayList<ShopItemOptions>();

  public ShopItem(String name, String id, String price, String info, String points) {
    this.name = name;
    this.id = id;
    this.price = price;
    this.info = info;
    this.points = points;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getPrice() {
    return this.price;
  }

  public String getInfo() {
    return this.info;
  }

  public void addOption(ShopItemOptions option) {
    this.options.add(option);
  }

  public ArrayList<ShopItemOptions> getOptions() {
    return this.options;
  }

  public ShopItemOptions getOption(int i) {
    try {
      return this.options.get(i); } catch (Exception e) {
    }
    return null;
  }
}