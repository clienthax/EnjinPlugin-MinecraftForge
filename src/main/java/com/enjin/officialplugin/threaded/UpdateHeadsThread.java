package com.enjin.officialplugin.threaded;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadData;
import com.enjin.officialplugin.heads.HeadLocation;
import com.enjin.officialplugin.shop.ShopItem;
import com.enjin.officialplugin.shop.ShopItemOptions;
import com.enjin.officialplugin.shop.ShopUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class UpdateHeadsThread implements Runnable {
	EnjinMinecraftPlugin plugin;
	SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
	SimpleDateFormat time = new SimpleDateFormat("h:mm:ss a z");
	ICommandSender sender = null;

	public UpdateHeadsThread(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	public UpdateHeadsThread(EnjinMinecraftPlugin plugin, ICommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
	}

	public synchronized void updateHeads() {
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin for package data for heads...");
			URL enjinurl = getItemsUrl();
			HttpURLConnection con;
			if (EnjinMinecraftPlugin.isMineshafterPresent())
				con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
			else {
				con = (HttpURLConnection) enjinurl.openConnection();
			}
			con.setRequestMethod("POST");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash) + "&player=0");
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());

			InputStream in = con.getInputStream();

			String json = parseInput(in);

			EnjinMinecraftPlugin.debug("Content of package data update:\n" + json);

			JSONParser parser = new JSONParser();
			try {
				JSONArray array = (JSONArray) parser.parse(json);
				this.plugin.cachedItems.clearShopItems();
				for (Iterator it = array.iterator(); it.hasNext();) {
					Object oitem = it.next();
					JSONObject item = (JSONObject) oitem;
					ShopItem sitem = new ShopItem((String) item.get("name"), (String) item.get("id"),
							ShopUtils.getPriceString(item.get("price")), (String) item.get("info"),
							ShopUtils.getPointsString(item.get("points")));

					Object options = item.get("variables");
					if ((options != null) && ((options instanceof JSONArray)) && (((JSONArray) options).size() > 0)) {
						JSONArray joptions = (JSONArray) options;

						Iterator optionsiterator = joptions.iterator();
						while (optionsiterator.hasNext()) {
							JSONObject option = (JSONObject) optionsiterator.next();
							ShopItemOptions soptions = new ShopItemOptions((String) option.get("name"),
									ShopUtils.getPriceString(option.get("pricemin")),
									ShopUtils.getPriceString(option.get("pricemax")),
									ShopUtils.getPointsString(option.get("pointsmin")),
									ShopUtils.getPointsString(option.get("pointsmax")));

							sitem.addOption(soptions);
						}
					}
					this.plugin.cachedItems.addShopItem(sitem);
				}
			} catch (ParseException e) {
				Iterator iterator;
				if (this.sender != null) {
					this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
							+ "There was an error parsing the shop data, donations won't show package information."));
				}
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			if (this.sender != null)
				this.sender.addChatMessage(new ChatComponentText(
						ChatColor.DARK_RED + "There was an error connecting to enjin, please try again later."));
		} catch (Throwable t) {
			if (this.sender != null) {
				this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
						+ "There was an error syncing the shop's packages, please fill out a support ticket at http://enjin.com/support and include the results of your /enjin report"));
			}
			t.printStackTrace();
		}
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin for stats data for heads...");
			URL enjinurl = getUrl();
			HttpURLConnection con;
			if (EnjinMinecraftPlugin.isMineshafterPresent())
				con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
			else {
				con = (HttpURLConnection) enjinurl.openConnection();
			}
			con.setRequestMethod("POST");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			ArrayList<HeadLocation> itemdonators = this.plugin.headlocation.getHeads(HeadLocation.Type.RecentItemDonator);
			ArrayList specificitems = new ArrayList();
			for (HeadLocation loc : itemdonators) {
				if (!specificitems.contains(loc.getItemid())) {
					specificitems.add(loc.getItemid());
				}
			}
			if (specificitems.size() > 0) {
				builder.append("&items=");
				for (int i = 0; i < specificitems.size(); i++) {
					if (i > 0) {
						builder.append(",");
					}
					builder.append((String) specificitems.get(i));
				}
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());

			InputStream in = con.getInputStream();

			String json = parseInput(in);

			EnjinMinecraftPlugin.debug("Content of heads update:\n" + json);

			JSONParser parser = new JSONParser();
			try {
				JSONObject array = (JSONObject) parser.parse(json);
				if ((array.get("recent_purchases") instanceof JSONArray)) {
					JSONArray recentpurchases = (JSONArray) array.get("recent_purchases");
					if (recentpurchases != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.RecentDonator);
						int i = 0;
						for (Iterator iterator = recentpurchases.iterator(); iterator.hasNext();) {
							Object purchase = iterator.next();
							if ((purchase instanceof JSONObject)) {
								JSONObject tpurchase = (JSONObject) purchase;
								String playername = (String) tpurchase.get("player_name");
								String price = tpurchase.get("price").toString();
								String itemid = "";
								Object items = tpurchase.get("items");
								if ((items != null) && ((items instanceof JSONArray))
										&& (((JSONArray) items).size() > 0)) {
									if (((JSONArray) items).size() == 1)
										itemid = (String) ((JSONArray) items).get(0);
									else {
										itemid = "Multiple Items";
									}
								}
								String[] signdata = this.plugin.cachedItems.getSignData(playername, itemid,
										HeadLocation.Type.RecentDonator, i, price);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.RecentDonator, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentDonator));
					}
				}
				JSONObject recentitempurchases;
				Iterator iterator;
				if ((array.get("item_purchases") instanceof JSONObject)) {
					recentitempurchases = (JSONObject) array.get("item_purchases");
					if (recentitempurchases != null) {
						Set items = recentitempurchases.keySet();

						for (iterator = items.iterator(); iterator.hasNext();) {
							Object item = iterator.next();
							if ((item instanceof String)) {
								String itemid = (String) item;
								JSONArray itemlist = (JSONArray) recentitempurchases.get(item);
								int i = 0;
								for (Iterator itr = itemlist.iterator(); itr.hasNext();) {
									Object purchase = itr.next();
									if ((purchase instanceof JSONObject)) {
										JSONObject tpurchase = (JSONObject) purchase;
										String playername = (String) tpurchase.get("player_name");
										String price = tpurchase.get("price").toString();
										String[] signdata = this.plugin.cachedItems.getSignData(playername, itemid,
												HeadLocation.Type.RecentItemDonator, i, price);
										HeadData hd = new HeadData(playername, signdata,
												HeadLocation.Type.RecentItemDonator, i, itemid);
										this.plugin.headdata.setHead(hd, false);
										i++;
									}
								}
								this.plugin.eventthrower
								.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentItemDonator, itemid));
							}
						}
					}
				}
				if ((array.get("top_voters_day") instanceof JSONArray)) {
					JSONArray topvotersday = (JSONArray) array.get("top_voters_day");
					if (topvotersday != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopDailyVoter);
						int i = 0;
						for (Iterator itr = topvotersday.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopDailyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopDailyVoter, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopDailyVoter));
					}
				}
				if ((array.get("top_voters_week") instanceof JSONArray)) {
					JSONArray topvotersweek = (JSONArray) array.get("top_voters_week");
					if (topvotersweek != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopWeeklyVoter);
						int i = 0;
						for (Iterator itr = topvotersweek.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopWeeklyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopWeeklyVoter, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopWeeklyVoter));
					}
				}
				if ((array.get("top_voters_month") instanceof JSONArray)) {
					JSONArray topvotersmonth = (JSONArray) array.get("top_voters_month");
					if (topvotersmonth != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopMonthlyVoter);
						int i = 0;
						for (Iterator itr = topvotersmonth.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopMonthlyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopMonthlyVoter, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopMonthlyVoter));
					}
				}
				if ((array.get("recent_voters") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("recent_voters");
					if (recentvoters != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.RecentVoter);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String votetime = tvoter.get("vote_time").toString() + "000";
								String voteday = "";
								String svotetime = "";
								try {
									long realvotetime = Long.parseLong(votetime);
									Date votedate = new Date(realvotetime);
									voteday = this.date.format(votedate);
									svotetime = this.time.format(votedate);
								} catch (NumberFormatException e) {
								}
								String[] signdata = this.plugin.cachedItems.getSignData(playername, voteday,
										HeadLocation.Type.RecentVoter, i, svotetime);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.RecentVoter, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentVoter));
					}
				}
				if ((array.get("top_players") instanceof JSONArray)) {
					JSONArray topplayers = (JSONArray) array.get("top_players");
					if (topplayers != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopPlayer);
						int i = 0;
						for (Iterator itr = topplayers.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String hours = tvoter.get("hours").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopPlayer, i, hours);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopPlayer, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopPlayer));
					}
				}
				if ((array.get("top_posters") instanceof JSONArray)) {
					JSONArray topplayers = (JSONArray) array.get("top_posters");
					if (topplayers != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopPoster);
						int i = 0;
						for (Iterator itr = topplayers.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String posts = tvoter.get("posts").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopPoster, i, posts);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopPoster, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopPoster));
					}
				}
				if ((array.get("top_forum_likes") instanceof JSONArray)) {
					JSONArray topplayers = (JSONArray) array.get("top_forum_likes");
					if (topplayers != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.TopLikes);
						int i = 0;
						for (Iterator itr = topplayers.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String likes = tvoter.get("likes").toString();
								String[] signdata = this.plugin.cachedItems.getSignData(playername, "",
										HeadLocation.Type.TopLikes, i, likes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopLikes, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopLikes));
					}
				}
				if ((array.get("latest_members") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("latest_members");
					if (recentvoters != null) {
						this.plugin.headdata.clearHeadData(HeadLocation.Type.LatestMembers);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String votetime = tvoter.get("datejoined").toString() + "000";
								String voteday = "";
								String svotetime = "";
								try {
									long realvotetime = Long.parseLong(votetime);
									Date votedate = new Date(realvotetime);
									voteday = this.date.format(votedate);
									svotetime = this.time.format(votedate);
								} catch (NumberFormatException e) {
								}
								String[] signdata = this.plugin.cachedItems.getSignData(playername, voteday,
										HeadLocation.Type.LatestMembers, i, svotetime);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.LatestMembers, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(HeadLocation.Type.LatestMembers));
					}
				}
				if ((array.get("top_points") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_points");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPoints;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String points = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i,
										points);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_points_month") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_points_month");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsMonth;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String points = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i,
										points);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_points_week") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_points_week");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsWeek;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String points = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i,
										points);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_points_day") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_points_day");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsDay;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String points = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i,
										points);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_money") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_money");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopDonators;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("price").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_money_day") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_money_day");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopDonatorsDay;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("price").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_money_week") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_money_week");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopDonatorsWeek;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("price").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_money_month") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_money_month");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopDonatorsMonth;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("price").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_points") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_points");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsDonators;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_points_day") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_points_day");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsDonatorsDay;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_points_week") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_points_week");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsDonatorsWeek;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				if ((array.get("top_donators_points_month") instanceof JSONArray)) {
					JSONArray recentvoters = (JSONArray) array.get("top_donators_points_month");
					if (recentvoters != null) {
						HeadLocation.Type type = HeadLocation.Type.TopPointsDonatorsMonth;
						this.plugin.headdata.clearHeadData(type);
						int i = 0;
						for (Iterator itr = recentvoters.iterator(); itr.hasNext();) {
							Object voter = itr.next();
							if ((voter instanceof JSONObject)) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");

								String price = tvoter.get("points").toString();

								String[] signdata = this.plugin.cachedItems.getSignData(playername, "", type, i, price);
								HeadData hd = new HeadData(playername, signdata, type, i);
								this.plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						this.plugin.eventthrower.addEvent(new HeadsUpdatedEvent(type));
					}
				}
				this.plugin.scheduler.scheduleSyncDelayedTask(this.plugin.eventthrower);
				if (this.sender != null)
					this.sender.addChatMessage(new ChatComponentText(ChatColor.GREEN + "Player head data successfully synched!"));
			} catch (ParseException e) {
				if (this.sender != null) {
					this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED + "There was an error parsing the head data."));
				}
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			if (this.sender != null)
				this.sender.addChatMessage(new ChatComponentText(
						ChatColor.DARK_RED + "There was an error connecting to enjin, please try again later."));
		} catch (Throwable t) {
			if (this.sender != null) {
				this.sender.addChatMessage(new ChatComponentText(ChatColor.DARK_RED
						+ "There was an error syncing the heads, please fill out a support ticket at http://enjin.com/support and include the results of your /enjin report"));
			}
			t.printStackTrace();
		}
	}

	public synchronized void run() {
		updateHeads();
	}

	public static String parseInput(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead = in.read(buffer);
		StringBuilder builder = new StringBuilder();
		while (bytesRead > 0) {
			builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
			bytesRead = in.read(buffer);
		}
		return builder.toString();
	}

	private URL getUrl() throws Throwable {
		return new URL(
				(EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-stats");
	}

	private URL getItemsUrl() throws Throwable {
		return new URL(
				(EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "m-shopping-items");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
	}
}