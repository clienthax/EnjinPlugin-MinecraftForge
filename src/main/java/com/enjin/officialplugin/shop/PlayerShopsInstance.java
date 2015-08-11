package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class PlayerShopsInstance
{
  ArrayList<ServerShop> servershops = new ArrayList();
  ServerShop selectedshop = null;
  ShopItemAdder selectedcategory = null;
  long retrievaltime;

  public PlayerShopsInstance()
  {
    this.retrievaltime = System.currentTimeMillis();
  }

  public void addServerShop(ServerShop shop) {
    this.servershops.add(shop);
  }

  public ArrayList<ServerShop> getServerShops() {
    return this.servershops;
  }

  public ServerShop getServerShop(int i) {
    return (ServerShop)this.servershops.get(i);
  }

  public int getServerShopCount() {
    return this.servershops.size();
  }

  public void setActiveShop(int i) {
    this.selectedshop = ((ServerShop)this.servershops.get(i));
  }

  public void setActiveShop(ServerShop shop) {
    this.selectedshop = shop;
  }

  public ServerShop getActiveShop() {
    return this.selectedshop;
  }

  public void setActiveCategory(ShopItemAdder cat) {
    this.selectedcategory = cat;
  }

  public ShopItemAdder getActiveCategory() {
    return this.selectedcategory;
  }

  public long getRetrievalTime() {
    return this.retrievaltime;
  }
}