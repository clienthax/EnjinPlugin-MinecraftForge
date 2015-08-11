package com.enjin.officialplugin.shop;

public class ShopItemOptions
{
  String name = "";
  String minprice = "";
  String maxprice = "";
  String minpoints = "";
  String maxpoints = "";

  public ShopItemOptions(String name, String pricemin, String pricemax, String minpoints, String maxpoints) {
    this.name = name;
    this.minprice = pricemin;
    this.maxprice = pricemax;
    this.minpoints = minpoints;
    this.maxpoints = maxpoints;
  }

  public String getName() {
    return this.name;
  }

  public String getMinPrice() {
    return this.minprice;
  }

  public String getMaxPrice() {
    return this.maxprice;
  }

  public String getMinPoints() {
    return this.minpoints;
  }

  public String getMaxPoints() {
    return this.maxpoints;
  }
}