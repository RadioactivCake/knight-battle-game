package app.ij.game2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChestActivity extends AppCompatActivity {

    private ImageView chestImage, rewardImage;
    private Button backButton, openChestButton, open10ChestButton;
    private TextView coinDisplay;
    private SharedPreferences sharedPreferences;
    private boolean chestOpened = false;
    private static final int CHEST_COST = 5;
    private static final int CHEST_10X_COST = 45; // 10% discount (50 - 5 = 45)

    // Available knights that can be obtained from chests
    private List<Knight> availableKnights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_chest);

        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeAvailableKnights();
        initializeUI();
        updateCoinDisplay();
        updateChestButtons();
        debugChestRates();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void initializeAvailableKnights() {
        availableKnights = new ArrayList<>();

        // COMMON KNIGHTS (40% total chance - 13.33% each)
        availableKnights.add(new Knight("brave_knight"));
        availableKnights.add(new Knight("fire_paladin"));
        availableKnights.add(new Knight("ice_guardian"));

        // RARE KNIGHTS (30% total chance - 10% each)
        availableKnights.add(new Knight("shadow_warrior"));
        availableKnights.add(new Knight("earth_defender"));
        availableKnights.add(new Knight("berserker_warrior"));

        // EPIC KNIGHTS (20% total chance - 6.67% each)
        availableKnights.add(new Knight("lightning_striker"));
        availableKnights.add(new Knight("vampire_lord"));
        availableKnights.add(new Knight("wind_tempest"));

        // LEGENDARY KNIGHTS (10% total chance - 3.33% each)
        availableKnights.add(new Knight("phoenix_knight"));
        availableKnights.add(new Knight("titan_guardian"));
        availableKnights.add(new Knight("divine_warrior"));
    }

    private Knight getRandomKnightWithRarity() {
        Random random = new Random();
        int chance = random.nextInt(100); // Generate 0-99

        // New rarity chances:
        // 0-39 = Common (40%)
        // 40-69 = Rare (30%)
        // 70-89 = Epic (20%)
        // 90-99 = Legendary (10%)

        if (chance < 40) {
            // COMMON (40% chance) - Pick randomly from first 3 knights
            int commonIndex = random.nextInt(3); // 0, 1, or 2
            return availableKnights.get(commonIndex);
        }
        else if (chance < 70) {
            // RARE (30% chance) - Pick randomly from knights 3-5
            int rareIndex = 3 + random.nextInt(3); // 3, 4, or 5
            return availableKnights.get(rareIndex);
        }
        else if (chance < 90) {
            // EPIC (20% chance) - Pick randomly from knights 6-8
            int epicIndex = 6 + random.nextInt(3); // 6, 7, or 8
            return availableKnights.get(epicIndex);
        }
        else {
            // LEGENDARY (10% chance) - Pick randomly from knights 9-11
            int legendaryIndex = 9 + random.nextInt(3); // 9, 10, or 11
            return availableKnights.get(legendaryIndex);
        }
    }

    private void initializeUI() {
        chestImage = findViewById(R.id.chestImage);
        rewardImage = findViewById(R.id.rewardImage);
        backButton = findViewById(R.id.backButton);
        openChestButton = findViewById(R.id.openChestButton);
        open10ChestButton = findViewById(R.id.open10ChestButton);
        coinDisplay = findViewById(R.id.coinDisplay);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        openChestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chestOpened) {
                    attemptOpenChest();
                } else {
                    resetChest();
                }
            }
        });

        open10ChestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptOpen10Chests();
            }
        });

        // Initially hide reward image
        rewardImage.setVisibility(View.GONE);
    }

    private void updateCoinDisplay() {
        int coins = sharedPreferences.getInt("coins", 0);
        coinDisplay.setText(String.valueOf(coins));
    }

    private void updateChestButtons() {
        int coins = sharedPreferences.getInt("coins", 0);

        // Update single chest button
        if (!chestOpened) {
            if (coins >= CHEST_COST) {
                openChestButton.setText("OPEN (" + CHEST_COST + " coins)");
                openChestButton.setEnabled(true);
            } else {
                openChestButton.setText("OPEN (" + CHEST_COST + " coins)\nNot enough coins!");
                openChestButton.setEnabled(false);
            }
        } else {
            openChestButton.setText("OPEN ANOTHER CHEST");
            openChestButton.setEnabled(coins >= CHEST_COST);
        }

        // Update 10x chest button
        if (coins >= CHEST_10X_COST) {
            open10ChestButton.setText("OPEN 10x (" + CHEST_10X_COST + " coins)\n10% DISCOUNT!");
            open10ChestButton.setEnabled(true);
        } else {
            open10ChestButton.setText("OPEN 10x (" + CHEST_10X_COST + " coins)\nNot enough coins!");
            open10ChestButton.setEnabled(false);
        }
    }

    private void attemptOpenChest() {
        int currentCoins = sharedPreferences.getInt("coins", 0);

        if (currentCoins < CHEST_COST) {
            Toast.makeText(this, "Not enough coins! You need " + CHEST_COST + " coins.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deduct coins
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", currentCoins - CHEST_COST);
        editor.apply();

        // Get random knight with rarity system
        Knight rewardKnight = getRandomKnightWithRarity();

        // Check if knight is duplicate BEFORE saving
        String existingKnights = sharedPreferences.getString("owned_knights", "Brave Knight");
        boolean isDuplicate = existingKnights.contains(rewardKnight.getName());

        // Check if already at max quantity
        int currentQuantity = sharedPreferences.getInt(rewardKnight.getName() + "_quantity", 0);
        boolean wasAlreadyMaxed = currentQuantity >= 11;

        // Save the knight to collection
        saveKnightToCollection(rewardKnight);

        // Get the final quantity for display
        int finalQuantity = sharedPreferences.getInt(rewardKnight.getName() + "_quantity", 1);

        // Show chest opening result
        showChestReward(rewardKnight, isDuplicate, finalQuantity, wasAlreadyMaxed);

        chestOpened = true;
        updateCoinDisplay();
        updateChestButtons();
    }



    private void attemptOpen10Chests() {
        int currentCoins = sharedPreferences.getInt("coins", 0);

        if (currentCoins < CHEST_10X_COST) {
            Toast.makeText(this, "Not enough coins! You need " + CHEST_10X_COST + " coins.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deduct coins
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", currentCoins - CHEST_10X_COST);
        editor.apply();

        // Open 10 chests and collect results
        List<Knight> rewardKnights = new ArrayList<>();
        Map<String, Integer> knightCounts = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            Knight rewardKnight = getRandomKnightWithRarity();
            rewardKnights.add(rewardKnight);

            // Count occurrences for summary
            knightCounts.put(rewardKnight.getName(),
                    knightCounts.getOrDefault(rewardKnight.getName(), 0) + 1);

            // Save each knight to collection
            saveKnightToCollection(rewardKnight);
        }

        // Show 10x chest opening results
        show10ChestRewards(knightCounts);

        updateCoinDisplay();
        updateChestButtons();
    }

    private void saveKnightToCollection(Knight knight) {
        // Get existing knights from preferences
        String existingKnights = sharedPreferences.getString("owned_knights", "Brave Knight");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Check if knight is already owned
        if (existingKnights.contains(knight.getName())) {
            // Knight already exists - increase quantity (max 11)
            int currentQuantity = sharedPreferences.getInt(knight.getName() + "_quantity", 1);
            if (currentQuantity < 11) {
                editor.putInt(knight.getName() + "_quantity", currentQuantity + 1);
            }
            // If already at max, quantity stays the same (no increase)
        } else {
            // New knight - add to collection
            String updatedKnights = existingKnights + "," + knight.getName();
            editor.putString("owned_knights", updatedKnights);

            // Save knight stats and initial quantity
            editor.putInt(knight.getName() + "_hp", knight.getBaseMaxHealth());
            editor.putInt(knight.getName() + "_attack", knight.getBaseAttack());
            editor.putString(knight.getName() + "_image", knight.getImageName());
            editor.putInt(knight.getName() + "_quantity", 1);
        }

        editor.apply();
    }

    private void showChestReward(Knight knight, boolean isDuplicate, int finalQuantity, boolean wasAlreadyMaxed) {
        // Hide chest, show reward
        chestImage.setVisibility(View.GONE);
        rewardImage.setVisibility(View.VISIBLE);

        // Use player_character image for all knights until specific images are added
        rewardImage.setImageResource(R.drawable.player_character);

        // Determine rarity
        String rarity = getRarityForKnight(knight.getName());

        // Calculate duplicates for display
        int duplicates = finalQuantity - 1;

        // Show reward dialog with correct duplicate status
        String message;
        if (isDuplicate) {
            if (wasAlreadyMaxed) {
                message = "🎁 Chest Opened! 🎁\n" +
                        "Rarity: " + rarity + "\n\n" +
                        "You obtained:\n" +
                        knight.getName() + "\n\n" +
                        "⚠️ Already at maximum!\n" +
                        "You have " + duplicates + " duplicates (100% buff)\n" +
                        "Knight received but no additional buff.";
            } else {
                int buffPercentage = duplicates * 10;
                message = "🎉 Congratulations! 🎉\n" +
                        "Rarity: " + rarity + "\n\n" +
                        "You obtained:\n" +
                        knight.getName() + "\n\n" +
                        "⭐ Duplicate! You now have " + duplicates + " duplicates\n" +
                        "Buff: +" + buffPercentage + "% to HP and Attack!";

                if (finalQuantity >= 11) {
                    message += "\n🎊 MAXIMUM REACHED! 100% buff achieved!";
                }
            }
        } else {
            message = "🎉 Congratulations! 🎉\n" +
                    "Rarity: " + rarity + "\n\n" +
                    "You obtained:\n" +
                    knight.getName() + "\n\n" +
                    "HP: " + knight.getBaseMaxHealth() + "\n" +
                    "Attack: " + knight.getBaseAttack() + "\n\n" +
                    "✨ New knight added to your collection!";
        }

        new AlertDialog.Builder(this)
                .setTitle("Chest Opened!")
                .setMessage(message)
                .setPositiveButton("Awesome!", null)
                .show();
    }

    private void show10ChestRewards(Map<String, Integer> knightCounts) {
        StringBuilder message = new StringBuilder();
        message.append("🎉 10 CHESTS OPENED! 🎉\n\n");
        message.append("You received:\n\n");

        int totalKnights = 0;
        int commonCount = 0, rareCount = 0, epicCount = 0, legendaryCount = 0;

        for (Map.Entry<String, Integer> entry : knightCounts.entrySet()) {
            String knightName = entry.getKey();
            int count = entry.getValue();
            String rarity = getRarityForKnight(knightName);

            // Get current duplicates after opening chests
            int currentQuantity = sharedPreferences.getInt(knightName + "_quantity", 1);
            int currentDuplicates = currentQuantity - 1;

            message.append("• ").append(knightName).append(" x").append(count);

            if (currentDuplicates > 0) {
                message.append(" (").append(currentDuplicates).append(" duplicates)");
                if (currentQuantity >= 11) {
                    message.append(" [MAX]");
                }
            } else {
                message.append(" (NEW!)");
            }

            message.append(" ").append(rarity).append("\n");

            totalKnights += count;

            // Count by rarity for summary
            switch (rarity) {
                case "⚪ COMMON":
                    commonCount += count;
                    break;
                case "🔵 RARE":
                    rareCount += count;
                    break;
                case "🟣 EPIC":
                    epicCount += count;
                    break;
                case "🟡 LEGENDARY":
                    legendaryCount += count;
                    break;
            }
        }

        message.append("\n📊 Summary:\n");
        message.append("Common: ").append(commonCount).append(" | ");
        message.append("Rare: ").append(rareCount).append(" | ");
        message.append("Epic: ").append(epicCount).append(" | ");
        message.append("Legendary: ").append(legendaryCount).append("\n\n");
        message.append("💰 You saved 5 coins with the 10x discount!");

        new AlertDialog.Builder(this)
                .setTitle("10x Chest Opening!")
                .setMessage(message.toString())
                .setPositiveButton("Amazing!", null)
                .show();
    }

    // Update the getRarityForKnight() method to use database
    private String getRarityForKnight(String knightName) {
        // First try to get from database by name
        KnightDatabase.KnightData data = KnightDatabase.getKnightDataByName(knightName);
        if (data != null) {
            switch (data.rarity) {
                case "COMMON":
                    return "⚪ COMMON";
                case "RARE":
                    return "🔵 RARE";
                case "EPIC":
                    return "🟣 EPIC";
                case "LEGENDARY":
                    return "🟡 LEGENDARY";
            }
        }

        // Fallback to old hardcoded method for backwards compatibility
        switch (knightName) {
            case "Brave Knight":
            case "Fire Paladin":
            case "Ice Guardian":
                return "⚪ COMMON";
            case "Shadow Warrior":
            case "Earth Defender":
            case "Berserker Warrior":
                return "🔵 RARE";
            case "Lightning Striker":
            case "Vampire Lord":
            case "Wind Tempest":
                return "🟣 EPIC";
            case "Phoenix Knight":
            case "Titan Guardian":
            case "Divine Warrior":
                return "🟡 LEGENDARY";
            default:
                return "⚪ COMMON";
        }
    }

    private void debugChestRates() {
        int totalTests = 1000;
        int commonCount = 0, rareCount = 0, epicCount = 0, legendaryCount = 0;

        for (int i = 0; i < totalTests; i++) {
            Knight testKnight = getRandomKnightWithRarity();
            String rarity = getRarityForKnight(testKnight.getName());

            switch (rarity) {
                case "⚪ COMMON":
                    commonCount++;
                    break;
                case "🔵 RARE":
                    rareCount++;
                    break;
                case "🟣 EPIC":
                    epicCount++;
                    break;
                case "🟡 LEGENDARY":
                    legendaryCount++;
                    break;
            }
        }

        android.util.Log.d("ChestDebug", "=== DROP RATE TEST (1000 attempts) ===");
        android.util.Log.d("ChestDebug", "Common: " + commonCount + " (" + (commonCount/10.0f) + "%)");
        android.util.Log.d("ChestDebug", "Rare: " + rareCount + " (" + (rareCount/10.0f) + "%)");
        android.util.Log.d("ChestDebug", "Epic: " + epicCount + " (" + (epicCount/10.0f) + "%)");
        android.util.Log.d("ChestDebug", "Legendary: " + legendaryCount + " (" + (legendaryCount/10.0f) + "%)");
        android.util.Log.d("ChestDebug", "=== EXPECTED: 40%, 30%, 20%, 10% ===");
    }

    // Also add this to test the random number generation:
    private void debugRandomNumbers() {
        android.util.Log.d("ChestDebug", "=== RANDOM NUMBER TEST ===");
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int chance = random.nextInt(100);
            String tier = "";
            if (chance < 40) tier = "COMMON";
            else if (chance < 70) tier = "RARE";
            else if (chance < 90) tier = "EPIC";
            else tier = "LEGENDARY";

            android.util.Log.d("ChestDebug", "Roll " + i + ": " + chance + " -> " + tier);
        }
    }

    // Add this method to your ChestActivity.java class:

    private void resetChest() {
        // Reset visual state
        chestOpened = false;

        // Show chest image, hide reward image
        chestImage.setVisibility(View.VISIBLE);
        rewardImage.setVisibility(View.GONE);

        // Update button states
        updateChestButtons();

        // Log for debugging
        android.util.Log.d("ChestActivity", "Chest reset - ready for next opening");
    }

}