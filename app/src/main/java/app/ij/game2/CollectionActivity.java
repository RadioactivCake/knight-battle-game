package app.ij.game2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends AppCompatActivity {

    private Button chapterMainButton, chapter1Button;
    private String currentChapter = "MAIN"; // Track current chapter
    private LinearLayout knightContainer, filterContainer;
    private Button backButton, massEvolveButton;
    private Button filterAllButton, filterCommonButton, filterRareButton, filterEpicButton, filterEvolvedButton, filterLegendaryButton;
    private List<Knight> knights;
    private List<Knight> allKnights; // Store all knights for filtering
    private SharedPreferences sharedPreferences;
    private String currentFilter = "ALL"; // Track current filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_collection);

        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeUI();
        loadKnights();
        displayKnights();
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
        knightContainer = findViewById(R.id.knightContainer);
        filterContainer = findViewById(R.id.filterContainer);
        backButton = findViewById(R.id.backButton);
        massEvolveButton = findViewById(R.id.massEvolveButton);

        // Initialize filter buttons
        filterAllButton = findViewById(R.id.filterAllButton);
        filterCommonButton = findViewById(R.id.filterCommonButton);
        filterRareButton = findViewById(R.id.filterRareButton);
        filterEpicButton = findViewById(R.id.filterEpicButton);
        filterEvolvedButton = findViewById(R.id.filterEvolvedButton);
        filterLegendaryButton = findViewById(R.id.filterLegendaryButton);
        chapterMainButton = findViewById(R.id.chapterMainButton);
        chapter1Button = findViewById(R.id.chapter1Button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        massEvolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMassEvolveDialog();
            }
        });

        // Set up filter button listeners
        filterAllButton.setOnClickListener(v -> applyFilter("ALL"));
        filterCommonButton.setOnClickListener(v -> applyFilter("COMMON"));
        filterRareButton.setOnClickListener(v -> applyFilter("RARE"));
        filterEpicButton.setOnClickListener(v -> applyFilter("EPIC"));
        filterEvolvedButton.setOnClickListener(v -> applyFilter("EVOLVED"));
        filterLegendaryButton.setOnClickListener(v -> applyFilter("LEGENDARY"));
        chapterMainButton.setOnClickListener(v -> switchToChapter("MAIN"));
        chapter1Button.setOnClickListener(v -> switchToChapter("CHAPTER1"));
        // Set initial filter state
        updateFilterButtons();
        updateChapterButtons();

    }

    // Update the loadKnights() method in CollectionActivity.java

    // Replace the loadKnights() method in CollectionActivity:

    private void loadKnights() {
        allKnights = new ArrayList<>();

        // Get owned knights from preferences
        String ownedKnights = sharedPreferences.getString("owned_knights", "Brave Knight");
        String ownedChapter1Knights = sharedPreferences.getString("owned_chapter1_knights", ""); // NEW
        String[] knightNames;

        // Determine which knights to load based on current chapter
        if (currentChapter.equals("CHAPTER1")) {
            knightNames = ownedChapter1Knights.isEmpty() ? new String[0] : ownedChapter1Knights.split(",");
        } else {
            knightNames = ownedKnights.split(",");
        }

        // Get equipped knight and squire (for main chapter only)
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");

        for (String knightName : knightNames) {
            knightName = knightName.trim();
            if (knightName.isEmpty()) continue;

            if (currentChapter.equals("CHAPTER1")) {
                // Load Chapter 1 knights (special handling)
                loadChapter1Knight(knightName);
            } else {
                // Load main chapter knights (existing logic)
                loadMainChapterKnight(knightName, equippedKnight, equippedSquire);
            }
        }

        // Apply current filter to set the displayed knights
        applyCurrentFilter();
    }

    private void loadChapter1Knight(String knightName) {
        if (knightName.equals("Axolotl Lord")) {
            // Load Axolotl Lord with special stats (600 total, 4:1 ratio)
            int savedHp = sharedPreferences.getInt(knightName + "_hp", 480);
            int savedAttack = sharedPreferences.getInt(knightName + "_attack", 120);
            int quantity = sharedPreferences.getInt(knightName + "_quantity", 1);

            Knight knight = new Knight(knightName, savedHp, savedAttack, "player_character");
            knight.setQuantity(quantity);
            loadKnightTrait(knight);
            allKnights.add(knight);

            android.util.Log.d("ChapterSystem", "Loaded Axolotl Lord: " + savedHp + " HP, " + savedAttack + " ATK");
        }
        // Add more Chapter 1 knights here in the future
    }

    private void loadMainChapterKnight(String knightName, String equippedKnight, String equippedSquire) {
        // Existing knight loading logic from your current loadKnights() method
        // (All the existing code for King's Guard, evolved knights, database knights, etc.)

        if (knightName.equals("King's Guard")) {
            Knight adminKnight = new AdminKnight("King's Guard", 1000, 1000, "player_character");
            adminKnight.setEquipped(equippedKnight.equals(adminKnight.getName()));
            adminKnight.setQuantity(1);
            loadKnightTrait(adminKnight);
            allKnights.add(adminKnight);
        } else if (knightName.startsWith("Evolved ")) {
            int savedHp = sharedPreferences.getInt(knightName + "_hp", 200);
            int savedAttack = sharedPreferences.getInt(knightName + "_attack", 40);
            int quantity = sharedPreferences.getInt(knightName + "_quantity", 1);

            Knight knight = new Knight(knightName, savedHp, savedAttack, "player_character");
            knight.setEquipped(equippedKnight.equals(knight.getName()));
            knight.setQuantity(quantity);
            loadKnightTrait(knight);
            allKnights.add(knight);
        } else {
            // Regular knights and database knights (existing logic)
            KnightDatabase.KnightData data = KnightDatabase.getKnightDataByName(knightName);

            Knight knight;
            if (data != null) {
                knight = new Knight(data.id);
            } else {
                int baseHp = sharedPreferences.getInt(knightName + "_hp", 100);
                int baseAttack = sharedPreferences.getInt(knightName + "_attack", 20);
                knight = new Knight(knightName, baseHp, baseAttack, "player_character");
            }

            knight.setEquipped(equippedKnight.equals(knight.getName()));
            knight.setQuantity(sharedPreferences.getInt(knightName + "_quantity", 1));
            loadKnightTrait(knight);
            allKnights.add(knight);
        }
    }

    private void updateMassEvolveButtonVisibility() {
        // Check if any knights can evolve (only base knights, not evolved ones)
        boolean hasEvolvableKnights = false;
        for (Knight knight : allKnights) {
            if (knight.getQuantity() >= 11 &&
                    !knight.getName().startsWith("Evolved ") &&
                    !knight.getName().equals("King's Guard")) {
                hasEvolvableKnights = true;
                break;
            }
        }

        // Show or hide the button
        if (hasEvolvableKnights) {
            massEvolveButton.setVisibility(View.VISIBLE);
        } else {
            massEvolveButton.setVisibility(View.GONE);
        }
    }

    private void showMassEvolveDialog() {
        // Find all knights that can evolve (only base knights)
        List<Knight> evolvableKnights = new ArrayList<>();
        for (Knight knight : allKnights) {
            if (knight.getQuantity() >= 11 &&
                    !knight.getName().startsWith("Evolved ") &&
                    !knight.getName().equals("King's Guard")) {
                evolvableKnights.add(knight);
            }
        }

        if (evolvableKnights.isEmpty()) {
            Toast.makeText(this, "No base knights available for evolution!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build confirmation message
        StringBuilder message = new StringBuilder();
        message.append("Evolve all eligible base knights?\n\n");
        message.append("Knights ready for evolution:\n");
        for (Knight knight : evolvableKnights) {
            message.append("• ").append(knight.getName()).append("\n");
        }
        message.append("\n⚠️ This cannot be undone!");

        new AlertDialog.Builder(this)
                .setTitle("🔮 Mass Evolution")
                .setMessage(message.toString())
                .setPositiveButton("Evolve All!", (dialog, which) -> {
                    massEvolveKnights(evolvableKnights);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private View createKnightView(Knight knight) {
        // Create knight card container
        LinearLayout knightCard = new LinearLayout(this);
        knightCard.setOrientation(LinearLayout.HORIZONTAL);

        // Set background based on rarity
        setKnightCardBackground(knightCard, knight);

        knightCard.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 20);
        knightCard.setLayoutParams(cardParams);

        // Knight image
        ImageView knightImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 150);
        imageParams.setMargins(0, 0, 20, 0);
        knightImage.setLayoutParams(imageParams);
        knightImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Use the knight image system
        int knightImageResource = KnightImageUtils.getKnightIdleImage(this, knight.getName());
        knightImage.setImageResource(knightImageResource);

        // Knight info container
        LinearLayout infoContainer = new LinearLayout(this);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Knight name
        TextView nameText = new TextView(this);
        nameText.setText(knight.getName());
        nameText.setTextSize(18);
        nameText.setTextColor(getResources().getColor(android.R.color.white));
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);

        // Knight stats (show final stats with all bonuses)
        TextView statsText = new TextView(this);
        String statsString = "HP: " + knight.getMaxHealth() + " | ATK: " + knight.getAttack();
        statsText.setText(statsString);
        statsText.setTextSize(14);
        statsText.setTextColor(getResources().getColor(android.R.color.white));

        // Knight quantity (show duplicates)
        TextView quantityText = new TextView(this);
        int duplicates = knight.getQuantity() - 1;
        String quantityString = "Duplicates: " + duplicates;
        if (knight.getQuantity() >= 11) {
            quantityString += " (MAX)";
        }
        quantityText.setText(quantityString);
        quantityText.setTextSize(12);
        quantityText.setTextColor(getResources().getColor(android.R.color.white));
        quantityText.setAlpha(0.8f);

        // Passive effect display
        TextView passiveText = new TextView(this);
        Knight.PassiveEffect passive = knight.getPassiveEffect();
        passiveText.setText("🎯 " + passive.getName() + ": " + passive.getDescription());
        passiveText.setTextSize(10);
        passiveText.setTextColor(getResources().getColor(android.R.color.white));
        passiveText.setAlpha(0.9f);
        passiveText.setMaxLines(2);

        // Trait display (NEW)
        // Trait display (FIXED with shadow outline)
        TextView traitText = new TextView(this);
        if (knight.hasTrait()) {
            traitText.setText("✨ " + knight.getTrait().getDisplayString());
            traitText.setTextColor(knight.getTrait().getRarityColor());

            // ADD BLACK SHADOW OUTLINE (works on all Android versions)
            traitText.setShadowLayer(3, 0, 0, 0xFF000000); // Black shadow outline
        } else {
            traitText.setText("✨ No Trait");
            traitText.setTextColor(getResources().getColor(android.R.color.white));
            traitText.setAlpha(0.6f);

            // ADD BLACK SHADOW for "No Trait" text too
            traitText.setShadowLayer(3, 0, 0, 0xFF000000); // Black shadow outline
        }
        traitText.setTextSize(10);
        traitText.setTypeface(null, android.graphics.Typeface.BOLD);

        // Equipped status
        TextView statusText = new TextView(this);
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");

        if (knight.getName().equals(equippedKnight)) {
            statusText.setText("⚔️ FIGHTER");
        } else if (knight.getName().equals(equippedSquire)) {
            statusText.setText("🛡️ SQUIRE");
        } else if (knight.getName().equals(equippedSquire2)) {
            statusText.setText("🛡️ 2ND SQUIRE");
        } else {
            statusText.setText("");
        }
        statusText.setTextSize(12);
        statusText.setTextColor(getResources().getColor(android.R.color.white));

        // Add all text views to info container
        infoContainer.addView(nameText);
        infoContainer.addView(statsText);
        infoContainer.addView(quantityText);
        infoContainer.addView(passiveText);
        infoContainer.addView(traitText); // NEW
        infoContainer.addView(statusText);

        knightCard.addView(knightImage);
        knightCard.addView(infoContainer);

        // Button container for evolve and trait buttons
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Evolve button (existing logic)
        if (knight.getQuantity() >= 11 && !knight.getName().startsWith("Evolved ") && !knight.getName().equals("King's Guard")) {
            Button evolveButton = new Button(this);
            evolveButton.setText("EVOLVE");
            evolveButton.setTextSize(12);
            evolveButton.setTypeface(null, android.graphics.Typeface.BOLD);
            evolveButton.setTextColor(getResources().getColor(android.R.color.white));
            evolveButton.setBackgroundResource(R.drawable.circle_button_style);
            evolveButton.setPadding(12, 12, 12, 12);

            LinearLayout.LayoutParams evolveParams = new LinearLayout.LayoutParams(180, 70);
            evolveParams.setMargins(15, 10, 0, 5);
            evolveButton.setLayoutParams(evolveParams);

            evolveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEvolveDialog(knight);
                }
            });

            buttonContainer.addView(evolveButton);
        }
        if (knight.getName().equals("Evolved Axolotl Knight")) {
            checkForAxolotlLordEvolution(knight);

            // Show evolution button if requirements are met
            boolean hasMaxCopies = knight.getQuantity() >= 11;
            boolean hasCharacterDevelopment = knight.hasTrait() &&
                    knight.getTrait().getName().equals("Character Development");

            if (hasMaxCopies && hasCharacterDevelopment) {
                Button ultimateEvolveButton = new Button(this);
                ultimateEvolveButton.setText("ULTIMATE\nEVOLUTION");
                ultimateEvolveButton.setTextSize(10);
                ultimateEvolveButton.setTypeface(null, android.graphics.Typeface.BOLD);
                ultimateEvolveButton.setTextColor(0xFFFFFFFF);
                ultimateEvolveButton.setBackgroundColor(0xFFFF1493); // Divine pink
                ultimateEvolveButton.setPadding(8, 8, 8, 8);

                LinearLayout.LayoutParams ultimateParams = new LinearLayout.LayoutParams(180, 80);
                ultimateParams.setMargins(15, 5, 0, 10);
                ultimateEvolveButton.setLayoutParams(ultimateParams);

                ultimateEvolveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAxolotlLordEvolutionDialog(knight);
                    }
                });

                buttonContainer.addView(ultimateEvolveButton);
            }
        }

        // Trait button (NEW) - Available for all knights except King's Guard
        if (!knight.getName().equals("King's Guard")) {
            Button traitButton = new Button(this);
            traitButton.setText("ROLL TRAIT\n(100 coins)");
            traitButton.setTextSize(10);
            traitButton.setTypeface(null, android.graphics.Typeface.BOLD);
            traitButton.setTextColor(getResources().getColor(android.R.color.white));
            traitButton.setBackgroundResource(R.drawable.attack_button_style);
            traitButton.setPadding(8, 8, 8, 8);

            LinearLayout.LayoutParams traitParams = new LinearLayout.LayoutParams(180, 100); // Increased height from 60 to 70
            traitParams.setMargins(15, 5, 0, 10);
            traitButton.setLayoutParams(traitParams);

            // Check if player has enough coins
            int currentCoins = sharedPreferences.getInt("coins", 0);
            if (currentCoins < 100) {
                traitButton.setEnabled(false);
                traitButton.setText("ROLL TRAIT\n(100 coins)");
                traitButton.setAlpha(0.5f);
            }

            traitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTraitRollDialog(knight);
                }
            });

            buttonContainer.addView(traitButton);
        }

        // Add button container to main card
        if (buttonContainer.getChildCount() > 0) {
            knightCard.addView(buttonContainer);
        }

        // Click listener for knight details
        knightCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKnightDetails(knight);
            }
        });

        return knightCard;
    }


    private void showKnightDetailsSimple(Knight knight) {
        String message = "Name: " + knight.getName() + "\n";

        int duplicates = knight.getQuantity() - 1;

        if (knight.getBuffPercentage() > 0) {
            message += "Base Stats:\n" +
                    "  Health: " + knight.getBaseMaxHealth() + "\n" +
                    "  Attack: " + knight.getBaseAttack() + "\n\n" +
                    "Buffed Stats (+" + knight.getBuffPercentage() + "%):\n" +
                    "  Health: " + knight.getMaxHealth() + "\n" +
                    "  Attack: " + knight.getAttack() + "\n\n";
        } else {
            message += "Health: " + knight.getMaxHealth() + "\n" +
                    "Attack: " + knight.getAttack() + "\n\n";
        }

        message += "Duplicates: " + duplicates;
        if (knight.getQuantity() >= 11) {
            message += " (MAX - 100% buff!)";
        }
        message += "\n\n";

        // Add passive effect info
        Knight.PassiveEffect passive = knight.getPassiveEffect();
        message += "🎯 Passive: " + passive.getName() + "\n";
        message += "Effect: " + passive.getDescription() + "\n\n";

        // TEMPORARILY SKIP PROPHECY INFO TO AVOID CRASHES
        // TODO: Add prophecy info back once basic loading works

        // Check current equipment status
        String equippedKnightName = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");

        if (knight.getName().equals(equippedKnightName)) {
            message += "⚔️ Currently equipped as FIGHTER!";
        } else if (knight.getName().equals(equippedSquire)) {
            message += "🛡️ Currently equipped as SQUIRE!";
        } else {
            message += "Equip this knight as your fighter or squire?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Knight Details");
        builder.setMessage(message);

        // Standard buttons (no prophecy management yet)
        if (!knight.getName().equals(equippedKnightName)) {
            builder.setPositiveButton("Equip as Fighter", (dialog, which) -> {
                equipKnight(knight);
            });
        }

        if (!knight.getName().equals(equippedSquire) && !knight.getName().equals(equippedKnightName)) {
            builder.setNeutralButton("Equip as Squire", (dialog, which) -> {
                equipSquire(knight);
            });
        }

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showEvolveDialog(Knight knight) {
        String message = "Evolve " + knight.getName() + "?\n\n" +
                "Current Stats (with 100% buff):\n" +
                "HP: " + knight.getMaxHealth() + "\n" +
                "ATK: " + knight.getAttack() + "\n\n" +
                "After Evolution:\n" +
                "• You'll lose all 10 duplicates\n" +
                "• Get \"Evolved " + knight.getName() + "\"\n" +
                "• New base stats = current buffed stats\n" +
                "• Can collect duplicates again\n\n" +
                "⚠️ This cannot be undone!";

        new AlertDialog.Builder(this)
                .setTitle("🔮 Evolution")
                .setMessage(message)
                .setPositiveButton("Evolve!", (dialog, which) -> {
                    evolveKnight(knight);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void evolveKnight(Knight knight) {
        // Create evolved knight name
        String evolvedName = "Evolved " + knight.getName();

        // Get current buffed stats (these become the base stats of evolved knight)
        int newBaseHp = knight.getMaxHealth();
        int newBaseAttack = knight.getAttack();

        // Check if evolved version already exists
        String existingKnights = sharedPreferences.getString("owned_knights", "Brave Knight");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (existingKnights.contains(evolvedName)) {
            // Evolved version already exists - increase its quantity
            int currentEvolvedQuantity = sharedPreferences.getInt(evolvedName + "_quantity", 1);
            editor.putInt(evolvedName + "_quantity", currentEvolvedQuantity + 1);
        } else {
            // Create new evolved knight
            String updatedKnights = existingKnights + "," + evolvedName;
            editor.putString("owned_knights", updatedKnights);

            // Save evolved knight data
            editor.putInt(evolvedName + "_hp", newBaseHp);
            editor.putInt(evolvedName + "_attack", newBaseAttack);
            editor.putString(evolvedName + "_image", "player_character");
            editor.putInt(evolvedName + "_quantity", 1);
        }

        // Reset original knight to quantity 1 (remove all duplicates)
        editor.putInt(knight.getName() + "_quantity", 1);

        // If the evolved knight was equipped, switch to evolved version
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        if (equippedKnight.equals(knight.getName())) {
            editor.putString("equipped_knight", evolvedName);
        }
        if (equippedSquire.equals(knight.getName())) {
            editor.putString("equipped_squire", evolvedName);
        }

        editor.apply();

        // Show success message
        String successMessage = "🎉 Evolution Complete! 🎉\n\n" +
                evolvedName + " has been added to your collection!\n\n" +
                "New Base Stats:\n" +
                "HP: " + newBaseHp + "\n" +
                "ATK: " + newBaseAttack;

        new AlertDialog.Builder(this)
                .setTitle("Evolution Successful!")
                .setMessage(successMessage)
                .setPositiveButton("Awesome!", (dialog, which) -> {
                    // Refresh the collection display
                    loadKnights();
                    displayKnights();
                })
                .show();
    }

    private void equipKnight(Knight knight) {
        // Unequip all knights
        for (Knight k : knights) {
            k.setEquipped(false);
        }

        // Equip selected knight
        knight.setEquipped(true);

        // Save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("equipped_knight", knight.getName());
        editor.apply();

        // Refresh display
        displayKnights();
    }

    private void equipSquire(Knight knight) {
        // Save squire to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("equipped_squire", knight.getName());
        editor.apply();

        // Refresh display
        displayKnights();
    }

    private void setKnightCardBackground(LinearLayout knightCard, Knight knight) {
        String rarity = getKnightRarity(knight.getName());

        switch (rarity) {
            case "COMMON":
                knightCard.setBackgroundColor(0xFF888888); // Light gray
                break;
            case "RARE":
                knightCard.setBackgroundColor(0xFF4169E1); // Royal blue
                break;
            case "EPIC":
                knightCard.setBackgroundColor(0xFF8A2BE2); // Purple
                break;
            case "LEGENDARY":
                knightCard.setBackgroundColor(0xFFFFD700); // Gold
                break;
            case "EVOLVED":
                knightCard.setBackgroundColor(0xFF32CD32); // Green
                break;
            default:
                knightCard.setBackgroundResource(R.drawable.player_card_background);
                break;
        }
    }

    private void massEvolveKnights(List<Knight> knightsToEvolve) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder evolvedNames = new StringBuilder();
        int evolvedCount = 0;

        for (Knight knight : knightsToEvolve) {
            // Create evolved knight name
            String evolvedName = "Evolved " + knight.getName();

            // Get current buffed stats (these become the base stats of evolved knight)
            int newBaseHp = knight.getMaxHealth();
            int newBaseAttack = knight.getAttack();

            // FIXED: Get fresh owned_knights string each time
            String existingKnights = sharedPreferences.getString("owned_knights", "Brave Knight");

            if (existingKnights.contains(evolvedName)) {
                // Evolved version already exists - increase its quantity
                int currentEvolvedQuantity = sharedPreferences.getInt(evolvedName + "_quantity", 1);
                editor.putInt(evolvedName + "_quantity", currentEvolvedQuantity + 1);
            } else {
                // Create new evolved knight
                String updatedKnights = existingKnights + "," + evolvedName;
                editor.putString("owned_knights", updatedKnights);

                // Save evolved knight data
                editor.putInt(evolvedName + "_hp", newBaseHp);
                editor.putInt(evolvedName + "_attack", newBaseAttack);
                editor.putString(evolvedName + "_image", "player_character");
                editor.putInt(evolvedName + "_quantity", 1);

                // FIXED: Apply changes immediately so next knight sees the update
                editor.apply();

                // FIXED: Create a new editor for the next knight
                editor = sharedPreferences.edit();
            }

            // Reset original knight to quantity 1 (remove all duplicates)
            editor.putInt(knight.getName() + "_quantity", 1);

            // Handle equipment switches
            String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
            String equippedSquire = sharedPreferences.getString("equipped_squire", "");
            String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");

            if (equippedKnight.equals(knight.getName())) {
                editor.putString("equipped_knight", evolvedName);
            }
            if (equippedSquire.equals(knight.getName())) {
                editor.putString("equipped_squire", evolvedName);
            }
            if (equippedSquire2.equals(knight.getName())) {
                editor.putString("equipped_squire2", evolvedName);
            }

            // Add to results
            if (evolvedCount > 0) {
                evolvedNames.append(", ");
            }
            evolvedNames.append(evolvedName);
            evolvedCount++;

            android.util.Log.d("MassEvolution", "Evolved " + knight.getName() + " → " + evolvedName);
        }

        // Apply final changes
        editor.apply();

        // Show success message
        String successMessage = "🎉 Mass Evolution Complete! 🎉\n\n" +
                "Evolved " + evolvedCount + " knights:\n" +
                evolvedNames.toString() + "\n\n" +
                "All evolved knights have been added to your collection!";

        new AlertDialog.Builder(this)
                .setTitle("Evolution Successful!")
                .setMessage(successMessage)
                .setPositiveButton("Amazing!", (dialog, which) -> {
                    // Refresh the collection display
                    loadKnights();
                    displayKnights();
                })
                .show();
    }

    // Update the getKnightRarity() method to use database
    private String getKnightRarity(String knightName) {
        // Check if evolved knight
        if (knightName.startsWith("Evolved ")) {
            return "EVOLVED";
        }

        // Try to get from database first
        KnightDatabase.KnightData data = KnightDatabase.getKnightDataByName(knightName);
        if (data != null) {
            return data.rarity;
        }

        // Fallback to old hardcoded method for backwards compatibility
        switch (knightName) {
            case "Brave Knight":
            case "Fire Paladin":
            case "Ice Guardian":
                return "COMMON";
            case "Shadow Warrior":
            case "Earth Defender":
            case "Berserker Warrior":
                return "RARE";
            case "Lightning Striker":
            case "Vampire Lord":
            case "Wind Tempest":
                return "EPIC";
            case "Phoenix Knight":
            case "Titan Guardian":
            case "Divine Warrior":
                return "LEGENDARY";
            default:
                return "COMMON"; // Default to common for unknown knights
        }
    }

    private void applyFilter(String filterType) {
        currentFilter = filterType;
        applyCurrentFilter();
        updateFilterButtons();
        displayKnights();
    }

    private void applyCurrentFilter() {
        knights = new ArrayList<>();

        for (Knight knight : allKnights) {
            String rarity = getKnightRarity(knight.getName());

            if (currentFilter.equals("ALL") || currentFilter.equals(rarity)) {
                knights.add(knight);
            }
        }
    }

    private void updateFilterButtons() {
        // Reset all button backgrounds to default
        filterAllButton.setBackgroundColor(0xFF444444);
        filterCommonButton.setBackgroundColor(0xFF444444);
        filterRareButton.setBackgroundColor(0xFF444444);
        filterEpicButton.setBackgroundColor(0xFF444444);
        filterEvolvedButton.setBackgroundColor(0xFF444444);
        filterLegendaryButton.setBackgroundColor(0xFF444444);

        // Highlight the active filter button
        switch (currentFilter) {
            case "ALL":
                filterAllButton.setBackgroundColor(0xFFFFFFFF);
                break;
            case "COMMON":
                filterCommonButton.setBackgroundColor(0xFF888888);
                break;
            case "RARE":
                filterRareButton.setBackgroundColor(0xFF4169E1);
                break;
            case "EPIC":
                filterEpicButton.setBackgroundColor(0xFF8A2BE2);
                break;
            case "LEGENDARY":
                filterLegendaryButton.setBackgroundColor(0xFFFFD700);
                break;
            case "EVOLVED":
                filterEvolvedButton.setBackgroundColor(0xFF32CD32);
                break;
        }
    }


    private void showKnightDetails(Knight knight) {
        String message = "Name: " + knight.getName() + "\n\n";

        // Show detailed stats breakdown
        message += knight.getStatsBreakdown() + "\n\n";

        // Add passive effect info
        Knight.PassiveEffect passive = knight.getPassiveEffect();
        message += "🎯 Passive: " + passive.getName() + "\n";
        message += "Effect: " + passive.getDescription() + "\n\n";

        // Check current equipment status
        String equippedKnightName = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);

        if (knight.getName().equals(equippedKnightName)) {
            message += "⚔️ Currently equipped as FIGHTER!";
        } else if (knight.getName().equals(equippedSquire)) {
            message += "🛡️ Currently equipped as SQUIRE!";
        } else if (knight.getName().equals(equippedSquire2)) {
            message += "🛡️ Currently equipped as SECOND SQUIRE!";
        } else {
            message += "Equip this knight?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Knight Details");
        builder.setMessage(message);

        // Add equipment buttons (existing logic)
        if (!knight.getName().equals(equippedKnightName)) {
            builder.setPositiveButton("Equip as Fighter", (dialog, which) -> {
                equipKnight(knight);
            });
        }

        if (!knight.getName().equals(equippedSquire) && !knight.getName().equals(equippedKnightName)) {
            builder.setNeutralButton("Equip as Squire", (dialog, which) -> {
                equipSquire(knight);
            });
        }

        if (hasKingsBlessing && !knight.getName().equals(equippedSquire2) &&
                !knight.getName().equals(equippedKnightName) && !knight.getName().equals(equippedSquire)) {

            builder.setNegativeButton("Equip as 2nd Squire", (dialog, which) -> {
                equipSecondSquire(knight);
            });
        }

        if (knight.getName().equals(equippedKnightName) ||
                (!hasKingsBlessing && (knight.getName().equals(equippedSquire))) ||
                (hasKingsBlessing && knight.getName().equals(equippedSquire) && knight.getName().equals(equippedSquire2))) {
            builder.setNegativeButton("Close", null);
        } else if (!hasKingsBlessing) {
            builder.setNegativeButton("Close", null);
        }

        builder.show();
    }

    private void displayKnights() {
        knightContainer.removeAllViews();

        for (Knight knight : knights) {
            View knightView = createKnightView(knight);
            knightContainer.addView(knightView);
        }

        // Update mass evolve button visibility
        updateMassEvolveButtonVisibility();
    }

    private void equipSecondSquire(Knight knight) {
        // Save second squire to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("equipped_squire2", knight.getName());
        editor.apply();

        android.util.Log.d("CollectionActivity", "Equipped second squire: " + knight.getName());

        // Refresh display
        displayKnights();
    }

    private void showTraitRollDialog(Knight knight) {
        int currentCoins = sharedPreferences.getInt("coins", 0);

        if (currentCoins < 100) {
            Toast.makeText(this, "Not enough coins! You need 100 coins to roll for a trait.", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Roll for a trait for " + knight.getName() + "?\n\n" +
                "💰 Cost: 100 coins\n" +
                "🎲 Chances:\n";

        if (knight.getName().equals("Evolved Axolotl Knight")) {
            // Special chances for Evolved Axolotl Knight
            message += "• Common (36%): Tough (+25% HP) or Flash (+25% ATK)\n" +
                    "• Rare (30%): Golem (+50% HP) or Blitz (+50% ATK)\n" +
                    "• Epic (20%): Expert (+50% HP and ATK) or Lonely\n" +
                    "• Legendary (10%): Main Character (+100% HP and ATK) or Guru\n" +
                    "• Divine (4%): Character Development (+200% HP and ATK) ⭐\n\n" +
                    "✨ This knight has access to the unique Divine trait! ✨\n\n";
        } else {
            // Normal chances for other knights
            message += "• Common (40%): Tough (+25% HP) or Flash (+25% ATK)\n" +
                    "• Rare (30%): Golem (+50% HP) or Blitz (+50% ATK)\n" +
                    "• Epic (20%): Expert (+50% HP and ATK) or Lonely\n" +
                    "• Legendary (10%): Main Character (+100% HP and ATK) or Guru\n\n";
        }

        if (knight.hasTrait()) {
            message += "⚠️ This will replace current trait: " + knight.getTrait().getDisplayString() + "\n\n";
        }

        message += "Trait bonuses apply after duplicate bonuses.";

        new AlertDialog.Builder(this)
                .setTitle("🎲 Roll for Trait")
                .setMessage(message)
                .setPositiveButton("Roll!", (dialog, which) -> {
                    rollTraitForKnight(knight);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rollTraitForKnight(Knight knight) {
        // Deduct coins
        int currentCoins = sharedPreferences.getInt("coins", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", currentCoins - 100);

        // Roll for trait based on knight type
        Trait newTrait;
        if (knight.getName().equals("Evolved Axolotl Knight")) {
            // Special rolling for Evolved Axolotl Knight (includes Divine trait)
            newTrait = TraitDatabase.rollTraitForEvolvedAxolotlKnight();
            android.util.Log.d("TraitSystem", "Evolved Axolotl Knight trait roll: " + newTrait.getName());
        } else {
            // Regular rolling for all other knights
            newTrait = TraitDatabase.rollRandomTrait();
        }

        // Save trait to knight
        knight.setTrait(newTrait);
        saveKnightTrait(knight.getName(), newTrait.getName());

        // Apply changes
        editor.apply();

        // Show result
        showTraitRollResult(knight, newTrait);

        // Refresh display
        loadKnights();
        displayKnights();
    }

    private void showTraitRollResult(Knight knight, Trait trait) {
        String message = "🎉 Trait Rolled! 🎉\n\n" +
                "Knight: " + knight.getName() + "\n" +
                "New Trait: " + trait.getDisplayString() + "\n\n" +
                "Updated Stats:\n" +
                knight.getStatsBreakdown();

        new AlertDialog.Builder(this)
                .setTitle("✨ New Trait Acquired!")
                .setMessage(message)
                .setPositiveButton("Awesome!", null)
                .show();
    }

    private void saveKnightTrait(String knightName, String traitName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(knightName + "_trait", traitName);
        editor.apply();

        android.util.Log.d("TraitSystem", "Saved trait for " + knightName + ": " + traitName);
    }

    private void loadKnightTrait(Knight knight) {
        String traitName = sharedPreferences.getString(knight.getName() + "_trait", "");
        if (!traitName.isEmpty()) {
            Trait trait = TraitDatabase.getTraitByName(traitName);
            if (trait != null) {
                knight.setTrait(trait);
                android.util.Log.d("TraitSystem", "Loaded trait for " + knight.getName() + ": " + traitName);
            }
        }
    }

    private void switchToChapter(String chapter) {
        currentChapter = chapter;
        currentFilter = "ALL"; // Reset filter when switching chapters

        updateChapterButtons();
        updateFilterButtons();
        loadKnights();
        displayKnights();

        android.util.Log.d("ChapterSystem", "Switched to chapter: " + chapter);
    }

    private void updateChapterButtons() {
        // Reset all chapter button backgrounds
        chapterMainButton.setBackgroundColor(0xFF444444);
        chapterMainButton.setTextColor(0xFFFFFFFF);
        chapter1Button.setBackgroundColor(0xFF444444);
        chapter1Button.setTextColor(0xFFFFFFFF);

        // Highlight active chapter
        switch (currentChapter) {
            case "MAIN":
                chapterMainButton.setBackgroundColor(0xFFFFFFFF);
                chapterMainButton.setTextColor(0xFF000000);
                break;
            case "CHAPTER1":
                chapter1Button.setBackgroundColor(0xFFFFD700); // Gold for special chapter
                chapter1Button.setTextColor(0xFF000000);
                break;
        }
    }

    private void checkForAxolotlLordEvolution(Knight knight) {
        // Check if this is Evolved Axolotl Knight with requirements
        if (!knight.getName().equals("Evolved Axolotl Knight")) {
            return;
        }

        // Check requirements: 11 copies (10 duplicates) and Character Development trait
        boolean hasMaxCopies = knight.getQuantity() >= 11;
        boolean hasCharacterDevelopment = knight.hasTrait() &&
                knight.getTrait().getName().equals("Character Development");

        if (hasMaxCopies && hasCharacterDevelopment) {
            // Show Axolotl Lord evolution option
            showAxolotlLordEvolutionDialog(knight);
        }
    }

    private void showAxolotlLordEvolutionDialog(Knight knight) {
        String message = "🌟 ULTIMATE EVOLUTION AVAILABLE! 🌟\n\n" +
                "Your Evolved Axolotl Knight has reached the pinnacle of power!\n\n" +
                "Requirements Met:\n" +
                "✅ 10 Duplicates (100% stat bonus)\n" +
                "✅ Character Development Trait (+200% stats)\n\n" +
                "Transform into:\n" +
                "🔥 AXOLOTL LORD 🔥\n" +
                "Base Stats: 480 HP / 120 ATK (600 total)\n" +
                "Chapter: Story Chapter 1\n\n" +
                "⚠️ This evolution is PERMANENT and moves the knight to Chapter 1!\n" +
                "⚠️ You will lose all duplicates and traits!\n\n" +
                "Begin the ultimate transformation?";

        new AlertDialog.Builder(this)
                .setTitle("🌟 Ultimate Evolution")
                .setMessage(message)
                .setPositiveButton("TRANSFORM!", (dialog, which) -> {
                    evolveToAxolotlLord(knight);
                })
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void evolveToAxolotlLord(Knight knight) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Remove Evolved Axolotl Knight from main collection
        String ownedKnights = sharedPreferences.getString("owned_knights", "");
        String updatedKnights = ownedKnights.replace(",Evolved Axolotl Knight", "")
                .replace("Evolved Axolotl Knight,", "")
                .replace("Evolved Axolotl Knight", "");
        if (updatedKnights.isEmpty()) {
            updatedKnights = "Brave Knight"; // Fallback
        }
        editor.putString("owned_knights", updatedKnights);

        // Clear Evolved Axolotl Knight data
        editor.remove("Evolved Axolotl Knight_quantity");
        editor.remove("Evolved Axolotl Knight_hp");
        editor.remove("Evolved Axolotl Knight_attack");
        editor.remove("Evolved Axolotl Knight_trait");

        // Add Axolotl Lord to Chapter 1
        String chapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        if (chapter1Knights.isEmpty()) {
            chapter1Knights = "Axolotl Lord";
        } else {
            chapter1Knights += ",Axolotl Lord";
        }
        editor.putString("owned_chapter1_knights", chapter1Knights);

        // Set Axolotl Lord stats (600 total with 4:1 ratio)
        editor.putInt("Axolotl Lord_hp", 480);    // 4/5 of 600 = 480
        editor.putInt("Axolotl Lord_attack", 120); // 1/5 of 600 = 120
        editor.putInt("Axolotl Lord_quantity", 1);
        editor.putString("Axolotl Lord_image", "player_character");

        // If Evolved Axolotl Knight was equipped, unequip
        String equippedKnight = sharedPreferences.getString("equipped_knight", "");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");

        if (equippedKnight.equals("Evolved Axolotl Knight")) {
            editor.putString("equipped_knight", "Brave Knight"); // Fallback
        }
        if (equippedSquire.equals("Evolved Axolotl Knight")) {
            editor.putString("equipped_squire", "");
        }
        if (equippedSquire2.equals("Evolved Axolotl Knight")) {
            editor.putString("equipped_squire2", "");
        }

        editor.apply();

        // Show success message and switch to Chapter 1
        showAxolotlLordSuccess();
    }

    private void showAxolotlLordSuccess() {
        String message = "🌟 TRANSFORMATION COMPLETE! 🌟\n\n" +
                "The Evolved Axolotl Knight has transcended its limits!\n\n" +
                "🔥 AXOLOTL LORD has been born! 🔥\n\n" +
                "Base Stats: 480 HP / 120 ATK (600 total)\n" +
                "Location: Story Chapter 1\n\n" +
                "The story begins... ⚔️";

        new AlertDialog.Builder(this)
                .setTitle("🌟 Ultimate Evolution Complete!")
                .setMessage(message)
                .setPositiveButton("Enter Chapter 1", (dialog, which) -> {
                    switchToChapter("CHAPTER1");
                })
                .setCancelable(false)
                .show();
    }
}