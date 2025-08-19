package app.ij.game2;

import java.util.Random;

public class Character {
    private String name;
    private int maxHealth;
    private int currentHealth;
    private int baseAttack;
    private Knight.PassiveEffect passiveEffect;
    private PassiveManager passiveManager;
    private int originalMaxHealth; // Store original before any boosts


    public Character(String name, int maxHealth, int attack) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.originalMaxHealth = maxHealth; // Store original
        this.currentHealth = maxHealth;
        this.baseAttack = attack;
        this.passiveManager = new PassiveManager(); // Initialize passive manager
    }

    // Apply passive effects from squire
    public void addPassiveEffect(Knight.PassiveEffect passive) {
        passiveManager.addPassive(passive);

        // Recalculate stats with all passives combined
        recalculateStatsFromPassives();
    }

    public String getName() {
        return name;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getAttack() {
        int attack = baseAttack;

        // Apply combined attack boost
        float attackBoost = passiveManager.getCombinedAttackBoost();
        if (attackBoost > 0) {
            attack = (int)(baseAttack * (1.0f + attackBoost));
        }

        // Apply scaling attack (use highest, not combined)
        float scalingAttack = passiveManager.getScalingAttack();
        if (scalingAttack > 0) {
            float healthPercentage = (float)currentHealth / maxHealth;
            float healthLost = 1.0f - healthPercentage;
            int tenPercentSteps = (int)(healthLost * 10);
            float bonusAttack = tenPercentSteps * scalingAttack;
            attack = (int)(attack * (1.0f + bonusAttack));
        }

        return attack;
    }

    public Knight.PassiveEffect getPassiveEffect() {
        return passiveEffect;
    }

    public void takeDamage(int damage) {
        Random random = new Random();

        // Check for combined evasion
        float evasionChance = passiveManager.getCombinedEvasion();
        if (evasionChance > 0 && random.nextFloat() < evasionChance) {
            android.util.Log.d("Character", "Attack evaded! (" + (evasionChance * 100) + "% chance)");
            return;
        }

        // Apply combined damage resistance
        float damageResistance = passiveManager.getCombinedDamageResistance();
        if (damageResistance > 0) {
            damage = (int)(damage * (1.0f - damageResistance));
            android.util.Log.d("Character", "Damage reduced by " + (damageResistance * 100) + "%");
        }

        currentHealth = Math.max(0, currentHealth - damage);
    }

    public int performAttack(boolean isLightAttack, boolean isMediumAttack, boolean isHeavyAttack) {
        int damage = getAttack();
        Random random = new Random();

        // Modify damage based on attack type
        if (isLightAttack) {
            damage = damage / 2;
        } else if (isHeavyAttack) {
            if (random.nextBoolean()) {
                damage = damage * 2;
            } else {
                damage = 0;
            }
        }

        // Check for combined critical hit
        float critChance = passiveManager.getCombinedCriticalHit();
        if (critChance > 0 && random.nextFloat() < critChance) {
            damage = damage * 2;
            android.util.Log.d("Character", "Critical hit! (" + (critChance * 100) + "% chance)");
        }

        // Check for combined double attack
        float doubleAttackChance = passiveManager.getCombinedDoubleAttack();
        if (doubleAttackChance > 0 && random.nextFloat() < doubleAttackChance) {
            damage = damage * 2;
            android.util.Log.d("Character", "Double attack! (" + (doubleAttackChance * 100) + "% chance)");
        }

        // Apply combined life steal
        float lifeStealPercent = passiveManager.getCombinedLifeSteal();
        if (lifeStealPercent > 0 && damage > 0) {
            int healAmount = (int)(damage * lifeStealPercent);
            heal(healAmount);
            android.util.Log.d("Character", "Life steal: " + healAmount + " HP (" + (lifeStealPercent * 100) + "%)");
        }

        return damage;
    }

    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public boolean isDefeated() {
        return currentHealth <= 0;
    }

    private void recalculateStatsFromPassives() {
        // Reset to original stats
        this.maxHealth = originalMaxHealth;

        // Apply combined HP boost
        float hpBoost = passiveManager.getCombinedHPBoost();
        if (hpBoost > 0) {
            this.maxHealth = (int)(originalMaxHealth * (1.0f + hpBoost));
        }

        // If player was at full health, scale current health too
        if (this.currentHealth == this.originalMaxHealth) {
            this.currentHealth = this.maxHealth;
        }

        android.util.Log.d("Character", "Recalculated stats - HP: " + maxHealth +
                " (boost: " + (hpBoost * 100) + "%)");
    }

    public PassiveManager getPassiveManager() {
        return passiveManager;
    }
}