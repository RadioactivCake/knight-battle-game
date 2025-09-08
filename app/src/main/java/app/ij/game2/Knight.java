package app.ij.game2;

import java.util.List;
import java.util.ArrayList;

public class Knight {
    private String knightId; // New: unique identifier
    private String name;     // Now pulled from database
    private int baseMaxHealth;
    private int baseAttack;
    private String imageName;
    private boolean isEquipped;
    private int quantity;
    private Trait currentTrait;
    private String tacticalRarity = null; // Add this field

    // Constructor now takes knight ID and loads data from database
    public Knight(String knightId) {
        KnightDatabase.KnightData data = KnightDatabase.getKnightData(knightId);
        if (data != null) {
            this.knightId = knightId;
            this.name = data.name;
            this.baseMaxHealth = data.baseHP;
            this.baseAttack = data.baseAttack;
        } else {
            // Fallback for unknown knights
            this.knightId = knightId;
            this.name = knightId;
            this.baseMaxHealth = 100;
            this.baseAttack = 20;
        }

        this.imageName = "player_character"; // Default image
        this.isEquipped = false;
        this.quantity = 1;
        this.currentTrait = null; // ADD THIS LINE
    }

    // Legacy constructor for backwards compatibility
    public Knight(String name, int maxHealth, int attack, String imageName) {
        this.knightId = KnightDatabase.getKnightId(name);
        this.name = name;
        this.baseMaxHealth = maxHealth;
        this.baseAttack = attack;
        this.imageName = imageName;
        this.isEquipped = false;
        this.quantity = 1;
        this.currentTrait = null; // ADD THIS LINE
    }

    // Getters
    public String getKnightId() {
        return knightId;
    }

    public String getName() {
        return name;
    }

    public int getBaseMaxHealth() {
        return baseMaxHealth;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getBuffPercentage() {
        return (quantity - 1) * 10;
    }

    // Setters
    public void setEquipped(boolean equipped) {
        this.isEquipped = equipped;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.min(quantity, 11); // Cap at 11 copies
    }

    public void addQuantity(int amount) {
        this.quantity = Math.min(this.quantity + amount, 11); // Cap at 11 copies
    }

    // Passive system - now uses database
    public PassiveEffect getPassiveEffect() {
        // Check if this is an evolved knight
        if (this.name.startsWith("Evolved ")) {
            String baseName = this.name.replace("Evolved ", "");
            String baseKnightId = KnightDatabase.getKnightId(baseName);
            return KnightDatabase.getEvolvedPassiveEffect(baseKnightId);
        }

        // Get base knight passive from database
        KnightDatabase.KnightData data = KnightDatabase.getKnightData(this.knightId);
        if (data != null) {
            return data.passiveEffect;
        }

        // Fallback for unknown knights
        return new PassiveEffect("None", "No Effect", "No passive effect", 0.0f, PassiveType.NONE);
    }

    // Passive effect data class (unchanged)
    public static class PassiveEffect {
        private String name;
        private String type;
        private String description;
        private float value;
        private PassiveType passiveType;

        public PassiveEffect(String name, String type, String description, float value, PassiveType passiveType) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.value = value;
            this.passiveType = passiveType;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public float getValue() { return value; }
        public PassiveType getPassiveType() { return passiveType; }
    }

    // Enum for different passive types (unchanged)
    public enum PassiveType {
        NONE,
        HP_BOOST,
        ATTACK_BOOST,
        DAMAGE_RESISTANCE,
        EVASION,
        CRITICAL_HIT,
        LIFE_STEAL,
        SCALING_ATTACK,
        DOUBLE_ATTACK
    }

    public void setTrait(Trait trait) {
        this.currentTrait = trait;
    }

    public Trait getTrait() {
        return currentTrait;
    }

    public boolean hasTrait() {
        return currentTrait != null;
    }

    public int getMaxHealth() {
        // Start with base HP
        int hp = baseMaxHealth;

        // Apply duplicate buff first
        int buffPercentage = (quantity - 1) * 10; // 10% per duplicate
        hp = hp + (hp * buffPercentage / 100);

        // Apply trait bonus second (if exists)
        if (currentTrait != null) {
            hp = currentTrait.applyHpBonus(hp);
        }

        return hp;
    }

    public int getAttack() {
        // Start with base attack
        int attack = baseAttack;

        // Apply duplicate buff first
        int buffPercentage = (quantity - 1) * 10; // 10% per duplicate
        attack = attack + (attack * buffPercentage / 100);

        // Apply trait bonus second (if exists)
        if (currentTrait != null) {
            attack = currentTrait.applyAttackBonus(attack);
        }

        return attack;
    }

    // Method to get stats breakdown for detailed display
    public String getStatsBreakdown() {
        StringBuilder breakdown = new StringBuilder();

        // Base stats
        breakdown.append("Base: ").append(baseMaxHealth).append(" HP, ").append(baseAttack).append(" ATK\n");

        // Duplicate bonus
        int duplicates = quantity - 1;
        if (duplicates > 0) {
            int dupHp = baseMaxHealth + (baseMaxHealth * duplicates * 10 / 100);
            int dupAttack = baseAttack + (baseAttack * duplicates * 10 / 100);
            breakdown.append("With Duplicates (+").append(duplicates * 10).append("%): ")
                    .append(dupHp).append(" HP, ").append(dupAttack).append(" ATK\n");
        }

        // Trait bonus
        if (currentTrait != null) {
            breakdown.append("Trait: ").append(currentTrait.getDisplayString()).append("\n");
            breakdown.append("Final Stats: ").append(getMaxHealth()).append(" HP, ").append(getAttack()).append(" ATK");
        } else {
            if (duplicates > 0) {
                breakdown.append("Final Stats: ").append(getMaxHealth()).append(" HP, ").append(getAttack()).append(" ATK");
            }
        }

        return breakdown.toString();
    }

    public void setTacticalRarity(String rarity) {
        this.tacticalRarity = rarity;
    }

    public String getTacticalRarity() {
        return tacticalRarity;
    }

}

// Special admin knight class for testing
class AdminKnight extends Knight {
    public AdminKnight(String name, int maxHealth, int attack, String imageName) {
        super(name, maxHealth, attack, imageName);
    }

    @Override
    public PassiveEffect getPassiveEffect() {
        return new PassiveEffect("Royal Protection", "Damage Resistance", "95% damage reduction", 0.95f, PassiveType.DAMAGE_RESISTANCE);
    }
}