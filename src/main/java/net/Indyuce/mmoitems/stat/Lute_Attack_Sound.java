package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Lute_Attack_Sound extends StringStat {
	public Lute_Attack_Sound() {
		super(new ItemStack(VersionMaterial.GOLDEN_HORSE_ARMOR.toMaterial()), "Lute Attack Sound", new String[] { "The sound played when", "basic attacking with this lute." }, "lute-attack-sound", new String[] { "lute" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_LUTE_ATTACK_SOUND", ((StringData) data).toString().toUpperCase().replace("-", "_").replace(" ", "_")));
		return true;
	}
}