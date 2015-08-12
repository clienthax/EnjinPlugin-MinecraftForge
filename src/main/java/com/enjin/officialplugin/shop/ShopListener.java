package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ShopListener extends CommandBase {
	ConcurrentHashMap<String, PlayerShopsInstance> activeshops = new ConcurrentHashMap<String, PlayerShopsInstance>();
	ConcurrentHashMap<String, String> playersdisabledchat = new ConcurrentHashMap<String, String>();

	public void removePlayer(String player) {
		player = player.toLowerCase();
		this.playersdisabledchat.remove(player);
		this.activeshops.remove(player);
	}

	@SubscribeEvent
	public void playerChatEvent(ServerChatEvent event) {
		if (event.isCanceled()) {
			return;
		}

		if (this.playersdisabledchat.containsKey(event.player.getCommandSenderName().toLowerCase())) {
			this.playersdisabledchat.remove(event.player.getCommandSenderName().toLowerCase());
		}

		if (!this.playersdisabledchat.isEmpty()) {
			List<EntityPlayerMP> playerlist = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayerMP recipient : playerlist) {
				if (!this.playersdisabledchat.containsKey(recipient.getCommandSenderName().toLowerCase()))
					recipient.addChatComponentMessage(event.getComponent());
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		String player = event.player.getCommandSenderName().toLowerCase();
		this.playersdisabledchat.remove(player);
		this.activeshops.remove(player);
	}

	public void sendPlayerInitialShopData(EntityPlayerMP player, PlayerShopsInstance shops) {
		if (shops.getServerShopCount() == 1) {
			ServerShop selectedshop = shops.getServerShop(0);
			shops.setActiveShop(selectedshop);
			shops.setActiveCategory(selectedshop);

			if ((selectedshop.getType() == ServerShop.Type.Category) && (selectedshop.getItems().size() == 1)) {
				ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
				shops.setActiveCategory(category);
				sendPlayerShopData(player, shops, category, 0);
			} else {
				sendPlayerShopData(player, shops, selectedshop, 0);
			}
			return;
		}
		sendPlayerPage(player, ShopUtils.getShopListing(shops));
	}

	public static void sendPlayerPage(EntityPlayerMP player, ArrayList<String> page) {
		for (String line : page)
			player.addChatMessage(new ChatComponentText(line));
	}

	public void sendPlayerShopData(EntityPlayerMP player, PlayerShopsInstance shops, ShopItemAdder category, int page) {
		ArrayList pages;
		if (category.getPages() == null) {
			pages = ShopUtils.formatPages(shops.getActiveShop(), category);
			category.setPages(pages);
		} else {
			pages = category.getPages();
		}
		sendPlayerPage(player, (ArrayList) pages.get(page));
	}

	public void sendPlayerItemData(EntityPlayerMP player, PlayerShopsInstance shops, ShopItem item) {
		sendPlayerPage(player, ShopUtils.getItemDetailsPage(shops.getActiveShop(), item));
	}

	@Override
	public String getCommandName() {
		return EnjinMinecraftPlugin.BUY_COMMAND;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) {
		EntityPlayerMP player = null;
		if ((icommandsender instanceof EntityPlayerMP))
			player = (EntityPlayerMP) icommandsender;
		else {
			return;
		}

		if ((args.length > 0) && (args[0].equalsIgnoreCase("history"))) {
			if ((args.length > 1) && (isPlayerOp(player))) {
				player.addChatMessage(new ChatComponentText(
						ChatColor.RED + "Fetching shop history information for " + args[1] + ", please wait..."));
				Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, args[1]));
				dispatchThread.start();
			} else {
				player.addChatMessage(new ChatComponentText(ChatColor.RED + "Fetching your shop history information, please wait..."));
				Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, player.getCommandSenderName()));
				dispatchThread.start();
			}
			return;
		}

		if (this.activeshops.containsKey(player.getCommandSenderName().toLowerCase())) {
			PlayerShopsInstance psi = (PlayerShopsInstance) this.activeshops.get(player.getCommandSenderName().toLowerCase());

			if (psi.getRetrievalTime() + 600000L < System.currentTimeMillis()) {
				player.addChatMessage (new ChatComponentText(ChatColor.RED + "Fetching shop information, please wait..."));
				Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
				dispatchThread.start();
				return;
			}
			this.playersdisabledchat.put(player.getCommandSenderName().toLowerCase(), player.getCommandSenderName());

			if (args.length == 0) {
				if (psi.getActiveShop() == null) {
					sendPlayerInitialShopData(player, psi);
				} else {
					ServerShop selectedshop = psi.getActiveShop();

					if ((selectedshop.getType() == ServerShop.Type.Category) && (selectedshop.getItems().size() == 1)) {
						ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
						psi.setActiveCategory(category);
					} else {
						psi.setActiveCategory(selectedshop);
					}
					sendPlayerShopData(player, psi, psi.getActiveCategory(), 0);
				}
			} else if (args[0].equalsIgnoreCase("shop")) {
				if (args.length > 1) {
					try {
						int pagenumber = Integer.parseInt(args[1].trim()) - 1;
						if ((pagenumber < psi.getServerShopCount()) && (pagenumber >= 0)) {
							ServerShop selectedshop = psi.getServerShop(pagenumber);

							if ((selectedshop.getType() == ServerShop.Type.Category)
									&& (selectedshop.getItems().size() == 1)) {
								ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
								psi.setActiveShop(selectedshop);
								psi.setActiveCategory(category);
								sendPlayerShopData(player, psi, category, 0);
							} else {
								psi.setActiveShop(selectedshop);
								psi.setActiveCategory(selectedshop);
								sendPlayerShopData(player, psi, selectedshop, 0);
							}
						} else {
							player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
						}
					} catch (NumberFormatException e) {
						player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
					}
				} else {
					psi.setActiveCategory(null);
					psi.setActiveShop(null);
					sendPlayerInitialShopData(player, psi);
				}
			} else if (args[0].equals("page")) {
				if (args.length > 1) {
					if (psi.getActiveCategory() != null) {
						ShopItemAdder category = psi.getActiveCategory();

						if (category.getPages() == null) {
							category.setPages(ShopUtils.formatPages(psi.getActiveShop(), category));
						}
						ArrayList pages = category.getPages();
						try {
							int pagenumber = Integer.parseInt(args[1]) - 1;
							if ((pagenumber < pages.size()) && (pagenumber >= 0))
								sendPlayerPage(player, (ArrayList) pages.get(pagenumber));
							else
								player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
						} catch (NumberFormatException e) {
							player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
						}
					}
				} else
					player.addChatMessage(new ChatComponentText(ChatColor.RED + "Please specify a page number."));
			} else if (args.length > 0) {
				if (psi.getActiveShop() == null)
					player.addChatMessage(new ChatComponentText(ChatColor.RED + "You need to select a shop first! Do /"
							+ EnjinMinecraftPlugin.BUY_COMMAND + " to see the shops list."));
				else
					try {
						ShopItemAdder category = psi.getActiveCategory();
						int optionnumber = Integer.parseInt(args[0]) - 1;
						if ((optionnumber < category.getItems().size()) && (optionnumber >= 0)) {
							if (category.getType() == ServerShop.Type.Category) {
								ShopItemAdder newcategory = (ShopItemAdder) category.getItem(optionnumber);
								psi.setActiveCategory(newcategory);
								sendPlayerShopData(player, psi, newcategory, 0);
							} else {
								sendPlayerPage(player, ShopUtils.getItemDetailsPage(psi.getActiveShop(),
										(ShopItem) category.getItem(optionnumber)));
							}
						} else
							player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
					} catch (NumberFormatException e) {
						player.addChatMessage(new ChatComponentText(ChatColor.RED + "Invalid page number."));
					}
			}
		} else {
			player.addChatMessage(new ChatComponentText(ChatColor.RED + "Fetching shop information, please wait..."));
			Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
			dispatchThread.start();
		}
	}

	boolean isPlayerOp(EntityPlayerMP player) {
		ArrayList<String> ops = new ArrayList<String>();
		for(String s : MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		{
			ops.add(s);
		}
		
		return ops.contains(player.getCommandSenderName().toLowerCase());
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + EnjinMinecraftPlugin.BUY_COMMAND;
	}
}