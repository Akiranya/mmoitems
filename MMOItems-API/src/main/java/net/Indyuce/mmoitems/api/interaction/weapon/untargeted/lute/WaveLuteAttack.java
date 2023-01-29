package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WaveLuteAttack implements LuteAttackHandler {

    @Override
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, double range, @NotNull Vector weight, @NotNull SoundReader sound, @NotNull ProjectileParticlesData projParticle) {
        new BukkitRunnable() {
            final Vector vec = caster.getPlayer().getEyeLocation().getDirection().multiply(.4);
            final Location loc = caster.getPlayer().getEyeLocation();
            int ti = 0;

            public void run() {
                if (ti++ > range) cancel();

                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec.add(weight));
                    if (loc.getBlock().getType().isSolid()) {
                        cancel();
                        break;
                    }

                    // Display particle
                    final Vector vec = MMOUtils.rotateFunc(new Vector(.5, 0, 0), loc);
                    projParticle.shootParticle(loc.clone().add(vec.clone().multiply(Math.sin((double) ti / 2))));
                    projParticle.shootParticle(loc.clone().add(vec.clone().multiply(-1)));

                    if (j == 0) sound.play(loc, 2, (float) (.5 + (double) ti / range));

                    for (Entity target : entities)
                        if (UtilityMethods.canTarget(caster.getPlayer(), loc, target, InteractionType.OFFENSE_ACTION)) {
                            caster.attack((LivingEntity) target, damage, DamageType.WEAPON, DamageType.MAGIC);
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}

