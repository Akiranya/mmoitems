package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class SoulboundLevel extends DoubleStat {
	public SoulboundLevel() {
		super("SOULBOUND_LEVEL", VersionMaterial.ENDER_EYE.toMaterial(), "Soulbinding Level", new String[] { "The soulbound level defines how much", "damage players will take when trying", "to use a soulbound item. It also determines", "how hard it is to break the binding." }, new String[] { "consumable" });
	}

	// writes soulbound level with roman writing in lore
	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		int value = (int) ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag("MMOITEMS_SOULBOUND_LEVEL", value));
		item.getLore().insert("soulbound-level", formatNumericStat(value, "#", MMOUtils.intToRoman(value)));
	}
}
