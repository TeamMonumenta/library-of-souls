package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BestiaryScoreboardStorage implements BestiaryStorage {
	@Override
	public void load(Plugin plugin, Player player, SoulsDatabase database) {
		// Nothing to do
	}

	@Override
	public void save(Player player, SoulsDatabase database) {
		// Nothing to do
	}

	@Override
	public void recordKill(Player player, SoulEntry soul) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		Score score = objective.getScore(player.getName());
		score.setScore(score.getScore() + 1);
	}

	@Override
	public int getKillsForMob(Player player, SoulEntry soul) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			return 0;
		}

		Score score = objective.getScore(player.getDisplayName());
		if (score == null || !score.isScoreSet()) {
			return 0;
		}

		return score.getScore();
	}

	@Override
	public boolean setKillsForMob(Player player, SoulEntry soul, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		Score score = objective.getScore(player.getDisplayName());
		score.setScore(amount);
		return true;
	}

	@Override
	public boolean addKillsForMob(Player player, SoulEntry soul, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		Score score = objective.getScore(player.getDisplayName());
		score.setScore(score.getScore() + amount);
		return true;
	}

	@Override
	public Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Map<SoulEntry, Integer> map = new HashMap<>();

		for (SoulEntry soul : searchSouls) {
			String objectiveName = getObjectiveName(soul);
			Objective objective = scoreboard.getObjective(objectiveName);
			if (objective == null) {
				map.put(soul, 0);
			} else {
				map.put(soul, objective.getScore(player.getName()).getScore());
			}
		}

		return map;
	}

	private static String nameToHex(String name) {
		return Integer.toHexString(name.hashCode());
	}

	private static String getObjectiveName(SoulEntry soul) {
		return "BST_" + nameToHex(soul.getLabel());
	}
}
