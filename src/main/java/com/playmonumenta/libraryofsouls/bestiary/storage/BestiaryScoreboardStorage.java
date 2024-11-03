package com.playmonumenta.libraryofsouls.bestiary.storage;

import com.playmonumenta.libraryofsouls.SoulEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BestiaryScoreboardStorage implements BestiaryStorage {
	@Override
	public void recordKill(Player player, SoulEntry soul) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName().append(Component.text(" kills", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
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

		Score score = objective.getScore(player.getName());
		if (!score.isScoreSet()) {
			return 0;
		}

		return score.getScore();
	}

	@Override
	public void setKillsForMob(Player player, SoulEntry soul, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName().append(Component.text(" kills", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
		}

		Score score = objective.getScore(player.getName());
		score.setScore(amount);
	}

	@Override
	public int addKillsForMob(Player player, SoulEntry soul, int amount) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		String objectiveName = getObjectiveName(soul);
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objectiveName, "dummy", soul.getName().append(Component.text(" kills", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
		}

		Score score = objective.getScore(player.getName());
		amount = score.getScore() + amount;
		score.setScore(amount);
		return amount;
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
