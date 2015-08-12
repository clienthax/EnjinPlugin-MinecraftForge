package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ServerShop extends AbstractShopSuperclass
  implements ShopItemAdder
{
  Type containertype = Type.Item;
  String name = "";
  String info = "";
  String buyurl = "";
  String currency = "USD";
  String colortitle = "6";
  String colortext = "f";
  String colorid = "6";
  String colorname = "e";
  String colorprice = "a";
  String colorbracket = "f";
  String colorurl = "f";
  String colorinfo = "7";
  String colorborder = "f";
  String colorbottom = "e";
  String border_v = "| ";
  String border_h = "-";
  String border_c = "+";
  boolean simpleitems = false;
  boolean simplecategories = false;

  ArrayList<AbstractShopSuperclass> items = new ArrayList<AbstractShopSuperclass>();

  ArrayList<ArrayList<String>> pages = null;

  public ServerShop(String name) {
    this.name = name;
  }

  public ServerShop(Type containertype) {
    this.containertype = containertype;
  }

  @Override
  public void setType(Type type) {
    this.containertype = type;
  }

  @Override
  public Type getType() {
    return this.containertype;
  }

  public void setSimpleItems(boolean value) {
    this.simpleitems = value;
  }

  public boolean simpleItemModeDisplay() {
    return this.simpleitems;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void addItem(AbstractShopSuperclass item)
    throws ItemTypeNotSupported
  {
    if (((this.containertype == Type.Item) && ((item instanceof ShopItem))) || ((this.containertype == Type.Category) && ((item instanceof ShopCategory))))
    {
      this.items.add(item);
    } else {
      String itemtype = "an unknown type";
      if ((item instanceof ShopItem))
        itemtype = "an item";
      else if ((item instanceof ShopCategory)) {
        itemtype = "a category";
      }
      throw new ItemTypeNotSupported("Got passed " + itemtype + " was expecting a " + this.containertype.toString());
    }
  }

  @Override
  public ArrayList<AbstractShopSuperclass> getItems() {
    return this.items;
  }

  @Override
  public AbstractShopSuperclass getItem(int i) {
    try {
      return this.items.get(i); } catch (Exception e) {
    }
    return null;
  }

  @Override
  public String getInfo()
  {
    return this.info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getBuyurl() {
    return this.buyurl;
  }

  public void setBuyurl(String buyurl) {
    this.buyurl = buyurl;
  }

  public String getCurrency() {
    return this.currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getColortitle() {
    return this.colortitle;
  }

  public void setColortitle(String colortitle) {
    this.colortitle = colortitle;
  }

  public String getColortext() {
    return this.colortext;
  }

  public void setColortext(String colortext) {
    this.colortext = colortext;
  }

  public String getColorid() {
    return this.colorid;
  }

  public void setColorid(String colorid) {
    this.colorid = colorid;
  }

  public String getColorname() {
    return this.colorname;
  }

  public void setColorname(String colorname) {
    this.colorname = colorname;
  }

  public String getColorprice() {
    return this.colorprice;
  }

  public void setColorprice(String colorprice) {
    this.colorprice = colorprice;
  }

  public String getColorbracket() {
    return this.colorbracket;
  }

  public void setColorbracket(String colorbracket) {
    this.colorbracket = colorbracket;
  }

  public String getColorurl() {
    return this.colorurl;
  }

  public void setColorurl(String colorurl) {
    this.colorurl = colorurl;
  }

  public String getColorinfo() {
    return this.colorinfo;
  }

  public void setColorinfo(String colorinfo) {
    this.colorinfo = colorinfo;
  }

  public String getColorborder() {
    return this.colorborder;
  }

  public void setColorborder(String colorborder) {
    this.colorborder = colorborder;
  }

  public String getColorbottom() {
    return this.colorbottom;
  }

  public void setColorbottom(String colorbottom) {
    this.colorbottom = colorbottom;
  }

  public String getBorder_v() {
    return this.border_v;
  }

  public void setBorder_v(String border_v) {
    this.border_v = border_v;
  }

  public String getBorder_h() {
    return this.border_h;
  }

  public void setBorder_h(String border_h) {
    this.border_h = border_h;
  }

  public String getBorder_c() {
    return this.border_c;
  }

  public void setBorder_c(String border_c) {
    this.border_c = border_c;
  }

  public boolean simpleCategoryModeDisplay() {
    return this.simplecategories;
  }

  public void setSimplecategories(boolean simplecategories) {
    this.simplecategories = simplecategories;
  }

  @Override
  public ShopItemAdder getParentCategory()
  {
    return null;
  }

  @Override
  public void setParentCategory(ShopItemAdder category)
  {
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

  public enum Type
  {
    Category, 
    Item
  }
}