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

        public TacticalKnightData(String id, String name, int hp, int attack, int speed,
                                  int actions, TacticalKnight.MovementStyle movementStyle,
                                  TacticalKnight.AttackStyle attackStyle) {
            this.id = id;
            this.name = name;
            this.hp = hp;
            this.attack = attack;
            this.speed = speed;
            this.actions = actions;
            this.movementStyle = movementStyle;
            this.attackStyle = attackStyle;
        }
    }

    private static final Map<String, TacticalKnightData> TACTICAL_KNIGHTS = new HashMap<>();

    static {
        // Axolotl Lord - Heavy Infantry
        TACTICAL_KNIGHTS.put("axolotl_lord", new TacticalKnightData(
                "axolotl_lord", "Axolotl Lord",
                800, 200, 5, 2,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.MELEE
        ));

        // Future Chapter 1 units can be added here
        // Example: Dragon Knight - Flying unit
        TACTICAL_KNIGHTS.put("dragon_knight", new TacticalKnightData(
                "dragon_knight", "Dragon Knight",
                600, 180, 8, 2,
                TacticalKnight.MovementStyle.FLYING,
                TacticalKnight.AttackStyle.RANGED
        ));

        // Example: Archer - Ranged unit
        TACTICAL_KNIGHTS.put("royal_archer", new TacticalKnightData(
                "royal_archer", "Royal Archer",
                400, 150, 6, 1,
                TacticalKnight.MovementStyle.INFANTRY,
                TacticalKnight.AttackStyle.RANGED
        ));
    }

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
}