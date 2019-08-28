package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class ProjectileData {
	private final NBTItem sourceItem;
	private final TemporaryStats playerStats;
	private final boolean customWeapon;

	public ProjectileData(NBTItem sourceItem, TemporaryStats playerStats, boolean customWeapon) {
		this.playerStats = playerStats;
		this.sourceItem = sourceItem;
		this.customWeapon = customWeapon;
	}

	public NBTItem getSourceItem() {
		return sourceItem;
	}

	public TemporaryStats getPlayerStats() {
		return playerStats;
	}

	/*
	 * if the item is an item from MMOItems, apply on-hit effects like critical
	 * strikes, pvp/pve damage and elemental damage
	 */
	public boolean isCustomWeapon() {
		return customWeapon;
	}
}