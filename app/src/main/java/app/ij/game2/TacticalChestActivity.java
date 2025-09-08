package app.ij.game2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TacticalChestActivity extends AppCompatActivity {

    private Button openChestButton, open10ChestButton, backButton;
    private ImageView chestImage;
    private TextView coinDisplay;
    private SharedPreferences sharedPreferences;
    private List<TacticalKnightDatabase.TacticalKnightData> availableTacticalKnights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();
        setContentView(R.layout.activity_tactical_chest);

        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeUI();
        initializeAvailableTacticalKnights();
        updateCoinDisplay();
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

    private void initializeUI() {
        openChestButton = findViewById(R.id.openChestButton);
        open10ChestButton = findViewById(R.id.open10ChestButton);
        backButton = findViewById(R.id.backButton);
        chestImage = findViewById(R.id.chestImage);
        coinDisplay = findViewById(R.id.coinDisplay);

        openChestButton.setOnClickListener(v -> openSingleTacticalChest());
        open10ChestButton.setOnClickListener(v -> open10TacticalChests());
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeAvailableTacticalKnights() {
        availableTacticalKnights = new ArrayList<>();

        Map<String, TacticalKnightDatabase.TacticalKnightData> allKnights = TacticalKnightDatabase.getAllTacticalKnights();

        for (TacticalKnightDatabase.TacticalKnightData knight : allKnights.values()) {
            // Include all knights since Axolotl Lord is now Common
            availableTacticalKnights.add(knight);
        }

        android.util.Log.d("TacticalChest", "Available tactical knights: " + availableTacticalKnights.size());
    }

    private void openSingleTacticalChest() {
        if (!hasEnoughCoins(5)) {
            showInsufficientCoinsDialog();
            return;
        }

        spendCoins(5);
        TacticalKnightDatabase.TacticalKnightData newKnight = getRandomTacticalKnight();
        addKnightToCollection(newKnight);
        showSingleKnightResult(newKnight);
        updateCoinDisplay();
    }

    private void open10TacticalChests() {
        if (!hasEnoughCoins(45)) {
            showInsufficientCoinsDialog();
            return;
        }

        spendCoins(45);
        List<TacticalKnightDatabase.TacticalKnightData> newKnights = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            TacticalKnightDatabase.TacticalKnightData newKnight = getRandomTacticalKnight();
            addKnightToCollection(newKnight);
            newKnights.add(newKnight);
        }

        showMultipleKnightsResult(newKnights);
        updateCoinDisplay();
    }

    private TacticalKnightDatabase.TacticalKnightData getRandomTacticalKnight() {
        Random random = new Random();
        int randomValue = random.nextInt(100);

        String targetRarity;
        if (randomValue < 40) {
            targetRarity = "Common";     // 40%
        } else if (randomValue < 70) {
            targetRarity = "Rare";       // 30%
        } else if (randomValue < 90) {
            targetRarity = "Epic";       // 20%
        } else {
            targetRarity = "Legendary";  // 10%
        }

        // Get knights of target rarity
        List<TacticalKnightDatabase.TacticalKnightData> rarityKnights = new ArrayList<>();
        for (TacticalKnightDatabase.TacticalKnightData knight : availableTacticalKnights) {
            if (knight.rarity.equals(targetRarity)) {
                rarityKnights.add(knight);
            }
        }

        if (rarityKnights.isEmpty()) {
            // Fallback to any available knight
            return availableTacticalKnights.get(random.nextInt(availableTacticalKnights.size()));
        }

        return rarityKnights.get(random.nextInt(rarityKnights.size()));
    }

    private void addKnightToCollection(TacticalKnightDatabase.TacticalKnightData knightData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Add to Chapter 1 knights collection
        String ownedChapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        if (ownedChapter1Knights.isEmpty()) {
            ownedChapter1Knights = knightData.name;
        } else if (!ownedChapter1Knights.contains(knightData.name)) {
            ownedChapter1Knights += "," + knightData.name;
        }
        editor.putString("owned_chapter1_knights", ownedChapter1Knights);

        // Store tactical knight data separately from RPG knights
        editor.putString(knightData.name + "_tactical_rarity", knightData.rarity);
        editor.putInt(knightData.name + "_hp", knightData.hp);
        editor.putInt(knightData.name + "_attack", knightData.attack);
        editor.putInt(knightData.name + "_speed", knightData.speed);
        editor.putInt(knightData.name + "_actions", knightData.actions);

        // Handle duplicates
        int currentQuantity = sharedPreferences.getInt(knightData.name + "_quantity", 0);
        int newQuantity = Math.min(currentQuantity + 1, 11);
        editor.putInt(knightData.name + "_quantity", newQuantity);

        editor.apply();
    }

    private void showSingleKnightResult(TacticalKnightDatabase.TacticalKnightData knight) {
        String rarityColor = getRarityColor(knight.rarity);

        String message = "🎉 Tactical Knight Acquired! 🎉\n\n" +
                knight.name + " (" + knight.rarity + ")\n\n" +
                "HP: " + knight.hp + "\n" +
                "Attack: " + knight.attack + "\n" +
                "Speed: " + knight.speed + "\n" +
                "Actions: " + knight.actions + "\n" +
                "Movement: " + knight.movementStyle.description + "\n" +
                "Attack Style: " + knight.attackStyle.description;

        new AlertDialog.Builder(this)
                .setTitle("New Tactical Knight!")
                .setMessage(message)
                .setPositiveButton("Continue", null)
                .show();
    }

    private void showMultipleKnightsResult(List<TacticalKnightDatabase.TacticalKnightData> knights) {
        StringBuilder message = new StringBuilder("🎉 10 Tactical Knights Acquired! 🎉\n\n");

        // Show individual knights obtained
        message.append("Knights Obtained:\n");
        for (TacticalKnightDatabase.TacticalKnightData knight : knights) {
            message.append("• ").append(knight.name).append(" (").append(knight.rarity).append(")\n");
        }

        message.append("\nSummary by Rarity:\n");

        // Count by rarity
        int common = 0, rare = 0, epic = 0, legendary = 0;
        for (TacticalKnightDatabase.TacticalKnightData knight : knights) {
            switch (knight.rarity) {
                case "Common": common++; break;
                case "Rare": rare++; break;
                case "Epic": epic++; break;
                case "Legendary": legendary++; break;
            }
        }

        if (common > 0) message.append("Common: ").append(common).append("\n");
        if (rare > 0) message.append("Rare: ").append(rare).append("\n");
        if (epic > 0) message.append("Epic: ").append(epic).append("\n");
        if (legendary > 0) message.append("Legendary: ").append(legendary).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Tactical Armory Haul!")
                .setMessage(message.toString())
                .setPositiveButton("Continue", null)
                .show();
    }

    private String getRarityColor(String rarity) {
        switch (rarity) {
            case "Common": return "#808080";    // Gray
            case "Rare": return "#0066CC";      // Blue
            case "Epic": return "#9933CC";      // Purple
            case "Legendary": return "#FF6600"; // Orange
            default: return "#FFFFFF";         // White
        }
    }

    private boolean hasEnoughCoins(int cost) {
        return sharedPreferences.getInt("coins", 0) >= cost;
    }

    private void spendCoins(int amount) {
        int currentCoins = sharedPreferences.getInt("coins", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", currentCoins - amount);
        editor.apply();
    }

    private void updateCoinDisplay() {
        int coins = sharedPreferences.getInt("coins", 0);
        coinDisplay.setText(String.valueOf(coins));
    }

    private void showInsufficientCoinsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Insufficient Coins")
                .setMessage("You need more coins to open tactical chests.\n\nEarn coins by winning battles in the prolog!")
                .setPositiveButton("OK", null)
                .show();
    }
}