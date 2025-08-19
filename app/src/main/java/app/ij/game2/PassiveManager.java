package app.ij.game2;

import java.util.ArrayList;
import java.util.List;

public class PassiveManager {

    // Container for multiple passive effects
    private List<Knight.PassiveEffect> activePassives;

    public PassiveManager() {
        this.activePassives = new ArrayList<>();
    }

    // Add a passive effect to the stack
    public void addPassive(Knight.PassiveEffect passive) {
        activePassives.add(passive);
    }

    // Clear all passives (for battle reset)
    public void clearPassives() {
        activePassives.clear();
    }

    // Get combined HP boost percentage
    public float getCombinedHPBoost() {
        float totalBoost = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.HP_BOOST) {
                totalBoost += passive.getValue();
            }
        }

        return totalBoost;
    }

    // Get combined attack boost percentage
    public float getCombinedAttackBoost() {
        float totalBoost = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.ATTACK_BOOST) {
                totalBoost += passive.getValue();
            }
        }

        return totalBoost;
    }

    // Get combined damage resistance (capped at 95% for balance)
    public float getCombinedDamageResistance() {
        float totalResistance = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.DAMAGE_RESISTANCE) {
                totalResistance += passive.getValue();
            }
        }

        return Math.min(totalResistance, 0.95f); // Cap at 95% resistance
    }

    // Get combined evasion chance (capped at 90% for balance)
    public float getCombinedEvasion() {
        float totalEvasion = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.EVASION) {
                totalEvasion += passive.getValue();
            }
        }

        return Math.min(totalEvasion, 0.90f); // Cap at 90% evasion
    }

    // Get combined critical hit chance (capped at 100%)
    public float getCombinedCriticalHit() {
        float totalCrit = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.CRITICAL_HIT) {
                totalCrit += passive.getValue();
            }
        }

        return Math.min(totalCrit, 1.0f); // Cap at 100%
    }

    // Get combined life steal percentage (capped at 100%)
    public float getCombinedLifeSteal() {
        float totalLifeSteal = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.LIFE_STEAL) {
                totalLifeSteal += passive.getValue();
            }
        }

        return Math.min(totalLifeSteal, 1.0f); // Cap at 100%
    }

    // Get combined double attack chance (capped at 90% for balance)
    public float getCombinedDoubleAttack() {
        float totalDoubleAttack = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.DOUBLE_ATTACK) {
                totalDoubleAttack += passive.getValue();
            }
        }

        return Math.min(totalDoubleAttack, 0.90f); // Cap at 90%
    }

    // Get the strongest scaling attack (doesn't stack - use highest value)
    public float getScalingAttack() {
        float highestScaling = 0.0f;

        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == Knight.PassiveType.SCALING_ATTACK) {
                highestScaling = Math.max(highestScaling, passive.getValue());
            }
        }

        return highestScaling;
    }

    // Check if any passive of a specific type is active
    public boolean hasPassiveType(Knight.PassiveType type) {
        for (Knight.PassiveEffect passive : activePassives) {
            if (passive.getPassiveType() == type) {
                return true;
            }
        }
        return false;
    }

    // Get all active passives (for debugging)
    public List<Knight.PassiveEffect> getActivePassives() {
        return new ArrayList<>(activePassives);
    }

    // Debug method to log all active effects
    public void debugLogPassives() {
        android.util.Log.d("PassiveManager", "=== ACTIVE PASSIVES ===");
        for (int i = 0; i < activePassives.size(); i++) {
            Knight.PassiveEffect passive = activePassives.get(i);
            android.util.Log.d("PassiveManager", "Passive " + (i + 1) + ": " +
                    passive.getName() + " (" + passive.getPassiveType() + ") = " + passive.getValue());
        }

        // Log combined effects
        android.util.Log.d("PassiveManager", "Combined HP Boost: " + (getCombinedHPBoost() * 100) + "%");
        android.util.Log.d("PassiveManager", "Combined Attack Boost: " + (getCombinedAttackBoost() * 100) + "%");
        android.util.Log.d("PassiveManager", "Combined Damage Resistance: " + (getCombinedDamageResistance() * 100) + "%");
        android.util.Log.d("PassiveManager", "Combined Critical Hit: " + (getCombinedCriticalHit() * 100) + "%");
        android.util.Log.d("PassiveManager", "=== END PASSIVES ===");
    }
}
