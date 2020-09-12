package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryUtils;


public class BestiaryScoreboardStorage implements BestiaryStorage {
	public String getObjectiveName(SoulEntry soul) {
		return "BST_" + BestiaryUtils.nameToHex(soul.getLabel());
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
	public int getKillsForMob(Player player, String name) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		if (SoulsDatabase.getInstance().getSoul(name) == null) {
			return -1;
		}
		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			return -1;
		}

		Score score = objective.getScore(player.getDisplayName());
		if (score == null) {
			return -1;
		}

		return score.getScore();
	}

	@Override
	public boolean setKillsForMob(Player player, String name, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (SoulsDatabase.getInstance().getSoul(name) == null) {
			return false;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		try {
			Score score = objective.getScore(player.getDisplayName());
			score.setScore(amount);
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_RED + "You have not killed one of these mobs yet!");
		}
		return true;
	}


	@Override
	public boolean addKillsToMob(Player player, String name, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (SoulsDatabase.getInstance().getSoul(name) == null) {
			return false;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		try {
		Score score = objective.getScore(player.getDisplayName());
		score.setScore(score.getScore() + amount);
		} catch (Exception e) {
		player.sendMessage(ChatColor.DARK_RED + "You have not killed one of these mobs yet!");
		}
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
}
