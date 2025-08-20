package app.ij.game2;

public class Trait {
    private String name;
    private String description;
    private String rarity;
    private float hpBonus;
    private float attackBonus;

    public Trait(String name, String description, String rarity, float hpBonus, float attackBonus) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.hpBonus = hpBonus;
        this.attackBonus = attackBonus;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    public float getHpBonus() { return hpBonus; }
    public float getAttackBonus() { return attackBonus; }

    // Apply trait bonuses to base stats (after duplicates)
    public int applyHpBonus(int currentHp) {
        if (name.equals("Lonely")) {
            return currentHp; // Lonely doesn't give stat bonuses, only passive effects
        }
        if (hpBonus > 0) {
            return (int)(currentHp * (1.0f + hpBonus));
        }
        return currentHp;
    }

    public int applyAttackBonus(int currentAttack) {
        if (name.equals("Lonely")) {
            return currentAttack; // Lonely doesn't give stat bonuses
        }
        if (name.equals("Guru")) {
            return 0; // Guru sets attack to 0
        }
        if (attackBonus > 0) {
            return (int)(currentAttack * (1.0f + attackBonus));
        }
        return currentAttack;
    }


    // Get display string for UI
    public String getDisplayString() {
        StringBuilder display = new StringBuilder();
        display.append(name).append(" (").append(rarity).append(")");

        if (name.equals("Lonely")) {
            display.append(" - Applies own passive in battle");
        } else if (name.equals("Guru")) {
            display.append(" - Attack = 0, passive doubled");
        } else if (hpBonus > 0 && attackBonus > 0) {
            display.append(" - +").append((int)(hpBonus * 100)).append("% HP, +")
                    .append((int)(attackBonus * 100)).append("% ATK");
        } else if (hpBonus > 0) {
            display.append(" - +").append((int)(hpBonus * 100)).append("% HP");
        } else if (attackBonus > 0) {
            display.append(" - +").append((int)(attackBonus * 100)).append("% ATK");
        }

        return display.toString();
    }

    // Get rarity color for UI
    public int getRarityColor() {
        switch (rarity) {
            case "COMMON":
                return 0xFF888888; // Gray
            case "RARE":
                return 0xFF4169E1; // Blue
            case "EPIC":
                return 0xFF8A2BE2; // Purple
            case "LEGENDARY":
                return 0xFFFFD700; // Gold
            default:
                return 0xFF888888;
        }
    }
}