package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ShopCategory extends AbstractShopSuperclass
  implements ShopItemAdder
{
  String id = "";
  String name = "";
  String info = "";
  ServerShop.Type type = ServerShop.Type.Item;
  ShopItemAdder parentcategory = null;

  ArrayList<AbstractShopSuperclass> items = new ArrayList();

  ArrayList<ArrayList<String>> pages = null;

  public ShopCategory(String name, String id) {
    this.name = name;
    this.id = id;
  }

  @Override
  public void addItem(AbstractShopSuperclass item)
    throws ItemTypeNotSupported
  {
    if (((this.type == ServerShop.Type.Item) && ((item instanceof ShopItem))) || ((this.type == ServerShop.Type.Category) && ((item instanceof ShopCategory))))
    {
      this.items.add(item);
    } else {
      String itemtype = "an unknown type";
      if ((item instanceof ShopItem))
        itemtype = "an item";
      else if ((item instanceof ShopCategory)) {
        itemtype = "a category";
      }
      throw new ItemTypeNotSupported("Got passed " + itemtype + " was expecting a " + this.type.toString());
    }
  }

  @Override
  public ArrayList<AbstractShopSuperclass> getItems() {
    return this.items;
  }

  @Override
  public AbstractShopSuperclass getItem(int i) {
    try {
      return (AbstractShopSuperclass)this.items.get(i); } catch (Exception e) {
    }
    return null;
  }

  public String getId()
  {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getInfo() {
    return this.info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  @Override
  public ServerShop.Type getType() {
    return this.type;
  }

  @Override
  public void setType(ServerShop.Type type) {
    this.type = type;
  }

  @Override
  public ShopItemAdder getParentCategory()
  {
    return this.parentcategory;
  }

  @Override
  public void setParentCategory(ShopItemAdder cat)
  {
    this.parentcategory = cat;
  }

  @Override
  public void setPages(ArrayList<ArrayList<String>> pages)
  {
    this.pages = pages;
  }

  @Override
  public ArrayList<ArrayList<String>> getPages()
  {
    return this.pages;
  }
}