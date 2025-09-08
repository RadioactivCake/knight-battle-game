package app.ij.game2;

import java.util.HashMap;
import java.util.Map;

public class TacticalKnightDatabase {

    public static class TacticalKnightData {
        public final String id;
        public final String name;
        public final int hp;
        public final int attack;
        public final int speed;
        public final int actions;
        public final TacticalKnight.MovementStyle movementStyle;
        public final TacticalKnight.AttackStyle attackStyle;
        public final String rarity; // Add this field

        public TacticalKnightData(String id, String name, int hp, int attack, int speed,
                                  int actions, TacticalKnight.MovementStyle movementStyle,
                                  TacticalKnight.AttackStyle attackStyle, String rarity) {
            this.id = id;
            this.name = name;
            this.hp = hp;
            this.attack = attack;
            this.speed = speed;
            this.actions = actions;
            this.movementStyle = movementStyle;
            this.attackStyle = attackStyle;
            this.rarity = rarity; // Add this line
        }
    }

    private static final Map<String, TacticalKnightData> TACTICAL_KNIGHTS = new HashMap<>();

    static {
        // COMMON KNIGHTS (40%)
        TACTICAL_KNIGHTS.put("royal_guardian", new TacticalKnightData(
                "royal_guardian", "Royal Guardian",
                1000, 150, 3, 2,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.MELEE,
                "Common"
        ));

        TACTICAL_KNIGHTS.put("wind_scout", new TacticalKnightData(
                "wind_scout", "Wind Scout",
                300, 120, 9, 4,
                TacticalKnight.MovementStyle.CAVALRY,
                TacticalKnight.AttackStyle.MELEE,
                "Common"
        ));

        // Make Axolotl Lord common for chest availability
        TACTICAL_KNIGHTS.put("axolotl_lord", new TacticalKnightData(
                "axolotl_lord", "Axolotl Lord",
                800, 200, 5, 2,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.MELEE,
                "Common"
        ));

        // RARE KNIGHTS (30%)
        TACTICAL_KNIGHTS.put("berserker_warrior", new TacticalKnightData(
                "berserker_warrior", "Berserker Warrior",
                600, 250, 6, 2,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.MELEE,
                "Rare"
        ));

        TACTICAL_KNIGHTS.put("royal_archer", new TacticalKnightData(
                "royal_archer", "Royal Archer",
                400, 160, 5, 1,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.RANGED,
                "Rare"
        ));

        // EPIC KNIGHTS (20%)
        TACTICAL_KNIGHTS.put("spear_knight", new TacticalKnightData(
                "spear_knight", "Spear Knight",
                700, 180, 4, 2,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.RANGED,
                "Epic"
        ));

        TACTICAL_KNIGHTS.put("cavalry_lancer", new TacticalKnightData(
                "cavalry_lancer", "Cavalry Lancer",
                650, 200, 7, 3,
                TacticalKnight.MovementStyle.CAVALRY,
                TacticalKnight.AttackStyle.MELEE,
                "Epic"
        ));

        // LEGENDARY KNIGHTS (10%)
        TACTICAL_KNIGHTS.put("dragon_knight", new TacticalKnightData(
                "dragon_knight", "Dragon Knight",
                800, 190, 8, 4,
                TacticalKnight.MovementStyle.FLYING,
                TacticalKnight.AttackStyle.MELEE,
                "Legendary"
        ));

        TACTICAL_KNIGHTS.put("fire_mage", new TacticalKnightData(
                "fire_mage", "Fire Mage",
                350, 220, 4, 1,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.AREA,
                "Legendary"
        ));
    }

    // ... rest of your existing methods stay the same
    public static TacticalKnightData getTacticalKnightData(String knightId) {
        return TACTICAL_KNIGHTS.get(knightId);
    }

    public static TacticalKnightData getTacticalKnightDataByName(String knightName) {
        for (TacticalKnightData data : TACTICAL_KNIGHTS.values()) {
            if (data.name.equals(knightName)) {
                return data;
            }
        }
        return null;
    }

    public static Map<String, TacticalKnightData> getAllTacticalKnights() {
        return new HashMap<>(TACTICAL_KNIGHTS);
    }

    public static Map<String, TacticalKnightData> getTacticalKnightsByRarity(String rarity) {
        Map<String, TacticalKnightData> filtered = new HashMap<>();
        for (Map.Entry<String, TacticalKnightData> entry : TACTICAL_KNIGHTS.entrySet()) {
            if (entry.getValue().rarity.equals(rarity)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }
}