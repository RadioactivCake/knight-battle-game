package app.ij.game2;

import android.content.SharedPreferences;

public class Event {
    private String name;
    private String description;
    private String rarity;
    private float baseChance;

    public Event(String name, String description, String rarity, float baseChance) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.baseChance = baseChance;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    public float getBaseChance() { return baseChance; }

    // UPDATED: Execute the event effect with SharedPreferences access
    public void executeEvent(Character player, SharedPreferences sharedPreferences) {
        switch (name) {
            case "Life Tree":
                // Restore all health
                int healAmount = player.getMaxHealth() - player.getCurrentHealth();
                player.heal(healAmount);
                android.util.Log.d("EventSystem", "Life Tree healed: " + healAmount + " HP");
                break;

            case "King's Blessing":
                // Unlock second squire slot permanently
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("has_kings_blessing", true);
                editor.apply();
                android.util.Log.d("EventSystem", "King's Blessing unlocked! Second squire slot now available.");
                break;
        }
    }

    // Keep the old method for backwards compatibility
    public void executeEvent(Character player) {
        // This version can't handle King's Blessing, but won't crash
        switch (name) {
            case "Life Tree":
                int healAmount = player.getMaxHealth() - player.getCurrentHealth();
                player.heal(healAmount);
                break;
            case "King's Blessing":
                android.util.Log.w("EventSystem", "King's Blessing requires SharedPreferences - use new executeEvent method");
                break;
        }
    }
}