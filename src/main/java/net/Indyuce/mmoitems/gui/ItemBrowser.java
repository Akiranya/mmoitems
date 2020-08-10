package net.Indyuce.mmoitems.gui;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemBrowser extends PluginInventory {
	private final Map<String, ItemStack> cached = new HashMap<>();

	private Type type;
	private boolean deleteMode;

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };

	public ItemBrowser(Player player) {
		this(player, null);
	}

	public ItemBrowser(Player player, Type type) {
		super(player);

		this.type = type;
	}

	@Override
	public Inventory getInventory() {
		int min = (page - 1) * slots.length;
		int max = page * slots.length;
		int n = 0;

		/*
		 * displays all possible item types if no type was previously selected
		 * by the player
		 */
		if (type == null) {
			Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Explorer");
			List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
			for (int j = min; j < Math.min(max, types.size()); j++) {
				Type type = types.get(j);
				int items = type.getConfigFile().getConfig().getKeys(false).size();

				ItemStack item = type.getItem();
				item.setAmount(Math.max(1, Math.min(64, items)));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + type.getName() + ChatColor.DARK_GRAY + " (Click to browse)");
				meta.addItemFlags(ItemFlag.values());
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "There " + (items != 1 ? "are" : "is") + " "
						+ (items < 1 ? "" + ChatColor.RED + ChatColor.ITALIC + "no" : "" + ChatColor.GOLD + ChatColor.ITALIC + items) + ChatColor.GRAY
						+ ChatColor.ITALIC + " item" + (items != 1 ? "s" : "") + " in that type.");
				meta.setLore(lore);
				item.setItemMeta(meta);

				inv.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("typeId", type.getId())).toItem());
			}

			ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
			ItemMeta glassMeta = glass.getItemMeta();
			glassMeta.setDisplayName(ChatColor.RED + "- No type -");
			glass.setItemMeta(glassMeta);

			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nextMeta = next.getItemMeta();
			nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
			next.setItemMeta(nextMeta);

			ItemStack previous = new ItemStack(Material.ARROW);
			ItemMeta previousMeta = previous.getItemMeta();
			previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
			previous.setItemMeta(previousMeta);

			ItemStack switchBrowse = new ItemStack(Material.STONE);
			ItemMeta switchMeta = switchBrowse.getItemMeta();
			switchMeta.setDisplayName(ChatColor.GREEN + "Switch to Block Explorer");

			if (!MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)) {
				List<String> lore = new ArrayList<String>();
				lore.add("");
				lore.add("&cThis feature is disabled.");
				lore.add("&cUpdating to 1.13+ is recommended.");
				switchMeta.setLore(lore);
			}

			switchBrowse.setItemMeta(switchMeta);

			while (n < slots.length)
				inv.setItem(slots[n++], glass);
			inv.setItem(18, page > 1 ? previous : null);
			inv.setItem(26, max >= MMOItems.plugin.getTypes().getAll().size() ? null : next);

			inv.setItem(53, switchBrowse);

			return inv;
		}

		ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta errorMeta = error.getItemMeta();
		errorMeta.setDisplayName(ChatColor.RED + "- Error -");
		List<String> errorLore = new ArrayList<>();
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occurred while");
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that item.");
		errorMeta.setLore(errorLore);
		error.setItemMeta(errorMeta);

		List<MMOItemTemplate> templates = new ArrayList<>(MMOItems.plugin.getItems().getTemplates(type));

		/*
		 * displays every item in a specific type. items are cached inside the
		 * map at the top to reduce performance impact and are directly rendered
		 */
		Inventory inv = Bukkit.createInventory(this, 54,
				(deleteMode ? (ChatColor.UNDERLINE + "Delete Mode: ") : (ChatColor.UNDERLINE + "Item Explorer: ")) + type.getName());
		for (int j = min; j < Math.min(max, templates.size()); j++) {
			MMOItemTemplate template = templates.get(j);
			if (!cached.containsKey(template.getId())) {
				ItemStack item = template.newBuilder(getPlayerData().getRPG()).build().newBuilder().build();
				if (item == null || item.getType() == Material.AIR) {
					cached.put(template.getId(), error);
					inv.setItem(slots[n++], error);
					continue;
				}

				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
				lore.add("");
				if (deleteMode) {
					lore.add(ChatColor.RED + AltChar.cross + " CLICK TO DELETE " + AltChar.cross);
					meta.setDisplayName(ChatColor.RED + "DELETE: " + meta.getDisplayName());
				} else {
					lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to obtain this item.");
					lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to edit this item.");
				}
				meta.setLore(lore);
				item.setItemMeta(meta);

				cached.put(template.getId(), item);
			}

			inv.setItem(slots[n++], cached.get(template.getId()));
		}

		ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta noItemMeta = noItem.getItemMeta();
		noItemMeta.setDisplayName(ChatColor.RED + "- No Item -");
		noItem.setItemMeta(noItemMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack back = new ItemStack(Material.ARROW);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Back");
		back.setItemMeta(backMeta);

		ItemStack create = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
		ItemMeta createMeta = create.getItemMeta();
		createMeta.setDisplayName(ChatColor.GREEN + "Create New");
		create.setItemMeta(createMeta);

		ItemStack delete = new ItemStack(VersionMaterial.CAULDRON.toMaterial());
		ItemMeta deleteMeta = delete.getItemMeta();
		deleteMeta.setDisplayName(ChatColor.RED + (deleteMode ? "Cancel Deletion" : "Delete Item"));
		delete.setItemMeta(deleteMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		if (type == Type.BLOCK) {
			ItemStack downloadPack = new ItemStack(Material.HOPPER);
			ItemMeta downloadMeta = downloadPack.getItemMeta();
			downloadMeta.setDisplayName(ChatColor.GREEN + "Download Default Resourcepack");
			downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Only seeing stone blocks?", "",
					ChatColor.RED + "By downloading the default resourcepack you can", ChatColor.RED + "edit the blocks however you want.",
					ChatColor.RED + "You will still have to add it to your server!"));
			downloadPack.setItemMeta(downloadMeta);
			inv.setItem(45, downloadPack);
		}

		while (n < slots.length)
			inv.setItem(slots[n++], noItem);
		if (!deleteMode)
			inv.setItem(51, create);
		inv.setItem(47, delete);
		inv.setItem(49, back);
		inv.setItem(18, page > 1 ? previous : null);
		inv.setItem(26, max >= templates.size() ? null : next);
		return inv;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		ItemStack item = event.getCurrentItem();
		if (MMOUtils.isMetaItem(item, false)) {
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
				page++;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
				page--;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back"))
				new ItemBrowser(getPlayer()).open();

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Switch to Block Explorer")) {
				if (MMOLib.plugin.getVersion().isStrictlyHigher(1, 12))
					new ItemBrowser(getPlayer(), Type.BLOCK).open();
				else
					getPlayer().sendMessage(ChatColor.RED + "Blocks are only for 1.13+.");
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel Deletion")) {
				deleteMode = false;
				open();
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Create New"))
				new NewItemEdition(this).enable("Write in the chat the text you want.");

			if (type != null && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Delete Item")) {
				deleteMode = true;
				open();
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Download Default Resourcepack")) {
				MMOLib.plugin.getVersion().getWrapper().sendJson(getPlayer(),
						"[{\"text\":\"Click to download!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mythiccraft.io/resources/MICustomBlockPack.zip\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\",\"italic\":true,\"color\":\"white\"}]}}]");
				getPlayer().closeInventory();
				return;
			}

			if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- No type -")) {
				Type type = MMOItems.plugin.getTypes().get(NBTItem.get(item).getString("typeId"));
				if (type == Type.BLOCK && !MMOLib.plugin.getVersion().isStrictlyHigher(1, 12)) {
					getPlayer().sendMessage(ChatColor.RED + "Blocks are only for 1.13+.");
					return;
				}

				new ItemBrowser(getPlayer(), type).open();
			}
		}

		String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
		if (id.equals(""))
			return;

		if (deleteMode) {
			ConfigFile config = type.getConfigFile();
			config.getConfig().set(id, null);
			config.save();
			deleteMode = false;
			open();

		} else {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				// this refreshes the item if it's unstackable
				ItemStack generatedItem = (NBTItem.get(item).getBoolean("UNSTACKABLE"))
						? MMOItems.plugin.getItems().generateItem(type, id, playerData)
						: removeLastLoreLines(item, 3);
				getPlayer().getInventory().addItem(generatedItem);
				getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF)
				new ItemEdition(getPlayer(), MMOItems.plugin.getItems().getTemplate(type, id), removeLastLoreLines(item, 3)).open();
		}
	}

	private ItemStack removeLastLoreLines(ItemStack item, int amount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		meta.setLore(lore.subList(0, lore.size() - amount));

		ItemStack item1 = item.clone();
		item1.setItemMeta(meta);
		return item1;
	}
}
