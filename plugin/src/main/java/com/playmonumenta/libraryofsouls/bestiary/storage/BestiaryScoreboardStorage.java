package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.playmonumenta.libraryofsouls.SoulEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;


public class BestiaryScoreboardStorage implements BestiaryStorage {
	public String getObjectiveName(SoulEntry soul) {
		return "BST_" + soul.getLabel();
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
