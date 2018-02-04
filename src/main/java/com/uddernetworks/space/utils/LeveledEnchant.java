package com.uddernetworks.space.utils;

import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeveledEnchant {
    private int level;
    private Enchantment enchantment;
    private boolean ignoreLevelRestriction;

    public LeveledEnchant(int level, Enchantment enchantment, boolean ignoreLevelRestriction) {
        this.level = level;
        this.enchantment = enchantment;
        this.ignoreLevelRestriction = ignoreLevelRestriction;
    }

    public static List<LeveledEnchant> fromList(Map<Enchantment, Integer> enchantments) {
        List<LeveledEnchant> leveledEnchants = new ArrayList<>();
        enchantments.forEach((enchantment, level) -> {
            leveledEnchants.add(new LeveledEnchant(level, enchantment, true));
        });

        return leveledEnchants;
    }

    public int getLevel() {
        return level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public boolean ignoresLevelRestriction() {
        return ignoreLevelRestriction;
    }
}