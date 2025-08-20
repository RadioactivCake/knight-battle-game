package app.ij.game2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TraitDatabase {

    private static final List<Trait> ALL_TRAITS = new ArrayList<>();

    static {
        // COMMON TRAITS (40% total chance)
        ALL_TRAITS.add(new Trait("Tough", "+25% HP", "COMMON", 0.25f, 0.0f));
        ALL_TRAITS.add(new Trait("Flash", "+25% ATK", "COMMON", 0.0f, 0.25f));

        // RARE TRAITS (30% total chance)
        ALL_TRAITS.add(new Trait("Golem", "+50% HP", "RARE", 0.50f, 0.0f));
        ALL_TRAITS.add(new Trait("Blitz", "+50% ATK", "RARE", 0.0f, 0.50f));

        // EPIC TRAITS (20% total chance)
        // Add to TraitDatabase.java static block
        ALL_TRAITS.add(new Trait("Lonely", "Applies own passive to self in battle", "EPIC", 0.0f, 0.0f));
        ALL_TRAITS.add(new Trait("Expert", "+50% HP and ATK", "EPIC", 0.50f, 0.50f));

        // LEGENDARY TRAITS (10% total chance)
        ALL_TRAITS.add(new Trait("Main Character", "+100% HP and ATK", "LEGENDARY", 1.0f, 1.0f));
    }

    // Roll for a random trait using chest-like probabilities
    public static Trait rollRandomTrait() {
        Random random = new Random();
        int chance = random.nextInt(100); // 0-99

        // Rarity distribution (same as chest system):
        // 0-39 = Common (40%)
        // 40-69 = Rare (30%)
        // 70-89 = Epic (20%)
        // 90-99 = Legendary (10%)

        if (chance < 40) {
            // COMMON (40% chance) - Pick randomly from Tough or Flash
            return random.nextBoolean() ? getAllTraits().get(0) : getAllTraits().get(1);
        }
        else if (chance < 70) {
            // RARE (30% chance) - Pick randomly from Golem or Blitz
            return random.nextBoolean() ? getAllTraits().get(2) : getAllTraits().get(3);
        }
        else if (chance < 90) {
            // EPIC (20% chance) - Pick randomly from Expert or Lonely
            return random.nextBoolean() ? getAllTraits().get(4) : getAllTraits().get(5);
        }
        else {
            // LEGENDARY (10% chance) - Main Character
            return getAllTraits().get(6); // NOTE: Main Character moves to index 6
        }
    }

    // Get trait by name (for loading from storage)
    public static Trait getTraitByName(String traitName) {
        if (traitName == null || traitName.isEmpty()) {
            return null;
        }

        for (Trait trait : ALL_TRAITS) {
            if (trait.getName().equals(traitName)) {
                return trait;
            }
        }
        return null;
    }

    // Get all available traits
    public static List<Trait> getAllTraits() {
        return new ArrayList<>(ALL_TRAITS);
    }

    // Get traits by rarity (for testing/debugging)
    public static List<Trait> getTraitsByRarity(String rarity) {
        List<Trait> result = new ArrayList<>();
        for (Trait trait : ALL_TRAITS) {
            if (trait.getRarity().equals(rarity)) {
                result.add(trait);
            }
        }
        return result;
    }

    // Debug method to test trait distribution
    public static void debugTraitRates() {
        int totalTests = 1000;
        int commonCount = 0, rareCount = 0, epicCount = 0, legendaryCount = 0;

        for (int i = 0; i < totalTests; i++) {
            Trait testTrait = rollRandomTrait();
            String rarity = testTrait.getRarity();

            switch (rarity) {
                case "COMMON":
                    commonCount++;
                    break;
                case "RARE":
                    rareCount++;
                    break;
                case "EPIC":
                    epicCount++;
                    break;
                case "LEGENDARY":
                    legendaryCount++;
                    break;
            }
        }

        android.util.Log.d("TraitDebug", "=== TRAIT RATE TEST (1000 attempts) ===");
        android.util.Log.d("TraitDebug", "Common: " + commonCount + " (" + (commonCount/10.0f) + "%)");
        android.util.Log.d("TraitDebug", "Rare: " + rareCount + " (" + (rareCount/10.0f) + "%)");
        android.util.Log.d("TraitDebug", "Epic: " + epicCount + " (" + (epicCount/10.0f) + "%)");
        android.util.Log.d("TraitDebug", "Legendary: " + legendaryCount + " (" + (legendaryCount/10.0f) + "%)");
        android.util.Log.d("TraitDebug", "=== EXPECTED: 40%, 30%, 20%, 10% ===");
    }
}