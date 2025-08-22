// Create new file: KnightDatabase.java

package app.ij.game2;

import java.util.HashMap;
import java.util.Map;

public class KnightDatabase {

    // Knight data class
    public static class KnightData {
        public final String id;
        public final String name;
        public final int baseHP;
        public final int baseAttack;
        public final String rarity;
        public final Knight.PassiveEffect passiveEffect;

        public KnightData(String id, String name, int baseHP, int baseAttack, String rarity, Knight.PassiveEffect passiveEffect) {
            this.id = id;
            this.name = name;
            this.baseHP = baseHP;
            this.baseAttack = baseAttack;
            this.rarity = rarity;
            this.passiveEffect = passiveEffect;
        }
    }

    // The master database - ALL knight data in one place
    private static final Map<String, KnightData> KNIGHT_DATA = new HashMap<>();

    static {
        // COMMON KNIGHTS (120 total stats)
        KNIGHT_DATA.put("brave_knight", new KnightData(
                "brave_knight", "Axolotl Knight", 100, 20, "COMMON",
                new Knight.PassiveEffect("Bravery", "HP Boost", "+15% Max HP", 0.15f, Knight.PassiveType.HP_BOOST)
        ));

        KNIGHT_DATA.put("fire_paladin", new KnightData(
                "fire_paladin", "Fire Paladin", 90, 30, "COMMON",
                new Knight.PassiveEffect("Burning Spirit", "Attack Boost", "+10% Attack", 0.10f, Knight.PassiveType.ATTACK_BOOST)
        ));

        KNIGHT_DATA.put("ice_guardian", new KnightData(
                "ice_guardian", "Ice Guardian", 105, 15, "COMMON",
                new Knight.PassiveEffect("Frost Shield", "Damage Resistance", "10% damage reduction", 0.10f, Knight.PassiveType.DAMAGE_RESISTANCE)
        ));

        // RARE KNIGHTS (150 total stats)
        KNIGHT_DATA.put("shadow_warrior", new KnightData(
                "shadow_warrior", "Shadow Warrior", 115, 35, "RARE",
                new Knight.PassiveEffect("Shadow Step", "Evasion", "15% chance to dodge", 0.15f, Knight.PassiveType.EVASION)
        ));

        KNIGHT_DATA.put("earth_defender", new KnightData(
                "earth_defender", "Earth Defender", 130, 20, "RARE",
                new Knight.PassiveEffect("Stone Skin", "Damage Resistance", "15% damage reduction", 0.15f, Knight.PassiveType.DAMAGE_RESISTANCE)
        ));

        KNIGHT_DATA.put("berserker_warrior", new KnightData(
                "berserker_warrior", "Berserker Warrior", 125, 25, "RARE",
                new Knight.PassiveEffect("Battle Fury", "Scaling Attack", "+5% attack per 10% health lost", 0.05f, Knight.PassiveType.SCALING_ATTACK)
        ));

        // EPIC KNIGHTS (200 total stats)
        KNIGHT_DATA.put("lightning_striker", new KnightData(
                "lightning_striker", "Lightning Striker", 160, 40, "EPIC",
                new Knight.PassiveEffect("Lightning Speed", "Critical Hit", "20% crit chance (2x damage)", 0.20f, Knight.PassiveType.CRITICAL_HIT)
        ));

        KNIGHT_DATA.put("vampire_lord", new KnightData(
                "vampire_lord", "Vampire Lord", 170, 30, "EPIC",
                new Knight.PassiveEffect("Blood Drain", "Life Steal", "25% life steal", 0.25f, Knight.PassiveType.LIFE_STEAL)
        ));

        KNIGHT_DATA.put("wind_tempest", new KnightData(
                "wind_tempest", "Wind Tempest", 150, 50, "EPIC",
                new Knight.PassiveEffect("Storm Speed", "Double Attack", "25% chance to attack twice", 0.25f, Knight.PassiveType.DOUBLE_ATTACK)
        ));

        // LEGENDARY KNIGHTS (220 total stats)
        KNIGHT_DATA.put("phoenix_knight", new KnightData(
                "phoenix_knight", "Phoenix Knight", 160, 60, "LEGENDARY",
                new Knight.PassiveEffect("Phoenix Flames", "Critical Hit", "35% crit chance (2x damage)", 0.35f, Knight.PassiveType.CRITICAL_HIT)
        ));

        KNIGHT_DATA.put("titan_guardian", new KnightData(
                "titan_guardian", "Titan Guardian", 180, 40, "LEGENDARY",
                new Knight.PassiveEffect("Titan's Endurance", "Damage Resistance", "30% damage reduction", 0.30f, Knight.PassiveType.DAMAGE_RESISTANCE)
        ));

        KNIGHT_DATA.put("divine_warrior", new KnightData(
                "divine_warrior", "Divine Warrior", 140, 80, "LEGENDARY",
                new Knight.PassiveEffect("Divine Blessing", "Life Steal", "30% life steal", 0.30f, Knight.PassiveType.LIFE_STEAL)
        ));

        // CHAPTER 1 STORY KNIGHTS (for chest system)
        KNIGHT_DATA.put("axolotl_lord", new KnightData(
                "axolotl_lord",
                "Axolotl Lord",
                800,  // Base HP
                200,  // Base ATK
                "COMMON", // CHANGED: Common rarity for Chapter 1 chest drops
                new Knight.PassiveEffect(
                        "Lord's Dominion",
                        "Ultimate Power",
                        "Massive combat bonuses",
                        0.50f,
                        Knight.PassiveType.CRITICAL_HIT
                )
        ));
    }

    // Utility methods
    public static KnightData getKnightData(String knightId) {
        return KNIGHT_DATA.get(knightId);
    }

    public static KnightData getKnightDataByName(String knightName) {
        for (KnightData data : KNIGHT_DATA.values()) {
            if (data.name.equals(knightName)) {
                return data;
            }
        }
        return null;
    }

    public static String getKnightName(String knightId) {
        KnightData data = KNIGHT_DATA.get(knightId);
        return data != null ? data.name : knightId; // Fallback to ID if not found
    }

    public static String getKnightId(String knightName) {
        for (KnightData data : KNIGHT_DATA.values()) {
            if (data.name.equals(knightName)) {
                return data.id;
            }
        }
        return knightName.toLowerCase().replace(" ", "_"); // Fallback
    }

    public static Map<String, KnightData> getAllKnights() {
        return new HashMap<>(KNIGHT_DATA);
    }

    // Get knights by rarity
    public static Map<String, KnightData> getKnightsByRarity(String rarity) {
        Map<String, KnightData> result = new HashMap<>();
        for (Map.Entry<String, KnightData> entry : KNIGHT_DATA.entrySet()) {
            if (entry.getValue().rarity.equals(rarity)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // Enhanced passive effects for evolved knights
    public static Knight.PassiveEffect getEvolvedPassiveEffect(String baseKnightId) {
        KnightData baseData = KNIGHT_DATA.get(baseKnightId);
        if (baseData == null) return null;

        Knight.PassiveEffect baseEffect = baseData.passiveEffect;

        // Enhanced values for evolved knights
        float enhancedValue;
        String enhancedDescription;

        switch (baseKnightId) {
            case "brave_knight":
                enhancedValue = 0.25f; // 25% HP boost
                enhancedDescription = "+25% Max HP";
                break;
            case "fire_paladin":
                enhancedValue = 0.25f; // 25% attack boost
                enhancedDescription = "+25% Attack";
                break;
            case "ice_guardian":
                enhancedValue = 0.20f; // 20% damage resistance
                enhancedDescription = "20% damage reduction";
                break;
            case "shadow_warrior":
                enhancedValue = 0.25f; // 25% evasion
                enhancedDescription = "25% chance to dodge";
                break;
            case "earth_defender":
                enhancedValue = 0.25f; // 25% damage resistance
                enhancedDescription = "25% damage reduction";
                break;
            case "berserker_warrior":
                enhancedValue = 0.075f; // 7.5% attack per 10% health lost
                enhancedDescription = "+7.5% attack per 10% health lost";
                break;
            case "lightning_striker":
                enhancedValue = 0.30f; // 30% crit chance
                enhancedDescription = "30% crit chance (2x damage)";
                break;
            case "vampire_lord":
                enhancedValue = 0.40f; // 40% life steal
                enhancedDescription = "40% life steal";
                break;
            case "wind_tempest":
                enhancedValue = 0.40f; // 40% chance for double attack
                enhancedDescription = "40% chance to attack twice";
                break;
            case "phoenix_knight":
                enhancedValue = 0.45f; // 45% crit chance
                enhancedDescription = "45% crit chance (2x damage)";
                break;
            case "titan_guardian":
                enhancedValue = 0.40f; // 40% damage resistance
                enhancedDescription = "40% damage reduction";
                break;
            case "divine_warrior":
                enhancedValue = 0.45f; // 45% life steal
                enhancedDescription = "45% life steal";
                break;
            default:
                enhancedValue = baseEffect.getValue() * 1.5f;
                enhancedDescription = baseEffect.getDescription();
                break;
        }

        return new Knight.PassiveEffect(
                "Enhanced " + baseEffect.getName(),
                baseEffect.getType(),
                enhancedDescription,
                enhancedValue,
                baseEffect.getPassiveType()
        );
    }
}