package net.Indyuce.mmoitems.api.item.plugin.crafting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.condition.Condition.ConditionInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class CraftingRecipeDisplay extends ConfigItem {
	private static final DecimalFormat craftingTimeFormat = new DecimalFormat("0.#");

	public CraftingRecipeDisplay() {
		super("CRAFTING_RECIPE_DISPLAY", Material.BARRIER, "&a&lCraft&f #name#", "{conditions}", "#condition_class#", "#condition_level#", "#condition_profession#", "#condition_mana#", "#condition_stamina#", "#condition_food#", "{crafting_time}", "{crafting_time}&7Crafting Time: &c#crafting-time#&7s", "", "&8Ingredients:", "#ingredients#", "", "&eLeft-Click to craft!", "&eRight-Click to preview!");
	}

	public ItemBuilder newBuilder(RecipeInfo recipe) {
		return new ItemBuilder(recipe);
	}

	/*
	 * allows to build an unidentified item based on the given NBTItem.
	 */
	public class ItemBuilder {
		private final RecipeInfo recipe;
		private final CraftingRecipe craftingRecipe;

		private String name = new String(getName());
		private List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(RecipeInfo recipe) {
			this.recipe = recipe;
			craftingRecipe = (CraftingRecipe) recipe.getRecipe();
		}

		public ItemStack build() {
			Map<String, String> replace = new HashMap<>();

			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String str = iterator.next();

				/*
				 * crafting time
				 */
				if (str.startsWith("{crafting_time}")) {
					if (craftingRecipe.getCraftingTime() <= 0) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{crafting_time}", "").replace("#crafting-time#", craftingTimeFormat.format(craftingRecipe.getCraftingTime())));
				}

				if (str.equals("{conditions}")) {
					if (recipe.getConditions().size() == 0) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{conditions}", ""));
				}

				/*
				 * load conditions
				 */
				if (str.startsWith("#condition_")) {
					String format = str.substring("#condition_".length(), str.length() - 1);
					ConditionInfo info = recipe.getCondition(format);
					if (info != null && info.getCondition().displays())
						replace.put(str, info.getCondition().formatDisplay(info.isMet() ? info.getCondition().getDisplay().getPositive() : info.getCondition().getDisplay().getNegative()));
					else
						iterator.remove();
				}
			}

			for (String key : replace.keySet())
				lore.set(lore.indexOf(key), replace.get(key));

			/*
			 * load ingredients. will register an error if the lore does not
			 * contain that line. users MUST display the ingredients somewhere.
			 */
			int index = lore.indexOf("#ingredients#");
			lore.remove(index);
			recipe.getIngredients().forEach(info -> lore.add(index, info.getIngredient().formatDisplay(info.isHad() ? info.getIngredient().getDisplay().getPositive() : info.getIngredient().getDisplay().getNegative())));

			/*
			 * apply color to lore
			 */
			for (int n = 0; n < lore.size(); n++)
				lore.set(n, ChatColor.translateAlternateColorCodes('&', lore.get(n)));

			ItemStack item = craftingRecipe.getOutput().getPreview();
			int amount = craftingRecipe.getOutput().getAmount();
			
			if(amount > 64) lore.add(0, Message.STATION_BIG_STACK.format(ChatColor.GOLD, "#size#", "" + amount).toString());
			else item.setAmount(amount);
			
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.replace("#name#", (amount > 1 ? (ChatColor.WHITE + "" + amount + " x ") : "") + MMOUtils.getDisplayName(item))));
			meta.setLore(lore);
			item.setItemMeta(meta);

			return NBTItem.get(item).addTag(new ItemTag("recipeId", craftingRecipe.getId())).toItem();
		}
	}
}
