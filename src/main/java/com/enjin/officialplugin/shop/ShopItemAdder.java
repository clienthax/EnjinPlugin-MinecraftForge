package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public abstract interface ShopItemAdder
{
  public abstract void addItem(AbstractShopSuperclass paramAbstractShopSuperclass)
    throws ItemTypeNotSupported;

  public abstract ArrayList<AbstractShopSuperclass> getItems();

  public abstract AbstractShopSuperclass getItem(int paramInt);

  public abstract void setType(ServerShop.Type paramType);

  public abstract ServerShop.Type getType();

  public abstract String getName();

  public abstract String getInfo();

  public abstract ShopItemAdder getParentCategory();

  public abstract void setParentCategory(ShopItemAdder paramShopItemAdder);

  public abstract void setPages(ArrayList<ArrayList<String>> paramArrayList);

  public abstract ArrayList<ArrayList<String>> getPages();
}