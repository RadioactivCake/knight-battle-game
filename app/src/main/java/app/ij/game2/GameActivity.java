package app.ij.game2;

import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.DecelerateInterpolator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    // UI Elements
    private TextView playerHealthText, enemyHealthText, enemyNameText, enemyAttackText, stageCounter, worldCounter, playerNameText, knightTypeText, squireText, surrenderButton;
    private ProgressBar playerHealthBar, enemyHealthBar;
    private Button lightAttackButton, mediumAttackButton, heavyAttackButton;
    private ImageView playerCharacterImage, enemyCharacterImage;
    private boolean isPassiveDisplayVisible = false; // Start hidden for cleaner look

    // Game Variables
    private Character player;
    private Character enemy;
    private boolean isPlayerTurn = true;
    private boolean gameOver = false;
    private boolean enemyStunned = false;
    private boolean isPlayerAnimating = false;
    private int currentPlayerIdleImage;
    private int currentPlayerAttackImage;
    private boolean pendingWorldCompletionDialog = false;
    private boolean pendingStageProgression = false;
    private boolean isEnemyAnimating = false;
    private int currentEnemyIdleImage;
    private int currentEnemyAttackImage;

    // Stage System Variables
    private int currentStage = 1;
    private int currentWorld = 1;
    private int maxStages = 5;
    private int baseEnemyHealth = 100;
    private int baseEnemyAttack = 18;
    private int stagesCompleted = 0;
    private int worldsCompleted = 0;
    private int coinsEarnedThisRun = 0; // Track coins for this run without adding to total

    // Coin System
    private SharedPreferences sharedPreferences;
    private static final int COINS_PER_STAGE = 10;
    private static final int COINS_FOR_COMPLETION = 100;

    // Knight info
    private String currentKnightName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_game);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeUI();
        initializeGame();
        loadCharacterImages();
        updateUI();
    }

    private void hideSystemUI() {
        // Enable fullscreen mode
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
        playerHealthText = findViewById(R.id.playerHealthText);
        enemyHealthText = findViewById(R.id.enemyHealthText);
        enemyNameText = findViewById(R.id.enemyNameText);
        playerHealthBar = findViewById(R.id.playerHealthBar);
        enemyHealthBar = findViewById(R.id.enemyHealthBar);
        enemyAttackText = findViewById(R.id.enemyAttackText); // NEW LINE
        lightAttackButton = findViewById(R.id.lightAttackButton);
        mediumAttackButton = findViewById(R.id.mediumAttackButton);
        heavyAttackButton = findViewById(R.id.heavyAttackButton);
        surrenderButton = findViewById(R.id.surrenderButton);
        playerCharacterImage = findViewById(R.id.playerCharacterImage);
        enemyCharacterImage = findViewById(R.id.enemyCharacterImage);
        stageCounter = findViewById(R.id.stageCounter);
        worldCounter = findViewById(R.id.worldCounter);
        playerNameText = findViewById(R.id.playerNameText);
        knightTypeText = findViewById(R.id.knightTypeText);
        squireText = findViewById(R.id.squireText);

        // Load and display player's profile name
        loadPlayerName();

        squireText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePassiveDisplay();
            }
        });

        // Set initial visibility state
        updatePassiveDisplayVisibility();

        lightAttackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver && isPlayerTurn) {
                    playerLightAttack();
                }
            }
        });

        mediumAttackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver && isPlayerTurn) {
                    playerMediumAttack();
                }
            }
        });

        heavyAttackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver && isPlayerTurn) {
                    playerHeavyAttack();
                }
            }
        });

        surrenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver) {
                    showSurrenderDialog();
                }
            }
        });
    }

    private void loadPlayerName() {
        String playerName = sharedPreferences.getString("player_name", "Warrior");
        String playerTitle = sharedPreferences.getString("player_title", "Great");

        // Create the full name and display it
        String fullName = playerName.toUpperCase() + " THE " + playerTitle.toUpperCase();
        playerNameText.setText(fullName);

        // Load and display equipped knight type
        loadKnightType();
    }

    private void loadKnightType() {
        currentKnightName = sharedPreferences.getString("equipped_knight", "Brave Knight");
        knightTypeText.setText(currentKnightName);

        // Update passive display based on current visibility state
        updatePassiveDisplayVisibility();
    }

    // Update loadCharacterImages() method to use knight-specific images
    private void loadCharacterImages() {
        // Load player character images (knight-specific)
        try {
            Bitmap playerBitmap = decodeSampledBitmapFromResource(getResources(),
                    currentPlayerIdleImage, 360, 440);
            if (playerBitmap != null) {
                playerCharacterImage.setImageBitmap(playerBitmap);
            }
        } catch (Exception e) {
            playerCharacterImage.setImageResource(currentPlayerIdleImage);
            Toast.makeText(this, "Player image too large, using default", Toast.LENGTH_SHORT).show();
        }

        // Load enemy character images (NEW: enemy-specific)
        try {
            Bitmap enemyBitmap = decodeSampledBitmapFromResource(getResources(),
                    currentEnemyIdleImage, 360, 440);
            if (enemyBitmap != null) {
                enemyCharacterImage.setImageBitmap(enemyBitmap);
            }
        } catch (Exception e) {
            enemyCharacterImage.setImageResource(currentEnemyIdleImage);
            Toast.makeText(this, "Enemy image too large, using default", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to scale down large images
    private static Bitmap decodeSampledBitmapFromResource(android.content.res.Resources res,
                                                          int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void initializeGame() {
        // Reset stage and world system
        currentStage = 1;
        currentWorld = 1;
        stagesCompleted = 0;
        worldsCompleted = 0;
        coinsEarnedThisRun = 0;
        enemyStunned = false;

        // Get equipped knight from preferences
        String equippedKnightName = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquireName = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2Name = sharedPreferences.getString("equipped_squire2", "");
        currentKnightName = equippedKnightName;

        // Load knight-specific images
        loadPlayerKnightImages();

        // Check for admin testing knight
        boolean hasAdminKnight = sharedPreferences.getBoolean("has_admin_knight", false);

        // Initialize player character based on equipped knight
        if (hasAdminKnight && equippedKnightName.equals("King's Guard")) {
            // Admin testing knight - super powerful stats
            player = new Character("Player", 1000, 1000);

            // FIXED: Apply admin knight passive!
            AdminKnight adminKnight = new AdminKnight("King's Guard", 1000, 1000, "player_character");
            Knight.PassiveEffect adminPassive = adminKnight.getPassiveEffect();
            player.addPassiveEffect(adminPassive);

            android.util.Log.d("AdminKnight", "Applied admin passive: " + adminPassive.getName() +
                    " = " + (adminPassive.getValue() * 100) + "% damage resistance");
        } else {
            // Regular knight loading and passive application
            Knight equippedKnight = loadKnightForBattle(equippedKnightName);
            if (equippedKnight != null) {
                player = new Character("Player", equippedKnight.getMaxHealth(), equippedKnight.getAttack());
            } else {
                player = new Character("Player", 100, 20);
            }

            // Apply all squire passives
            applyAllSquirePassives(equippedSquireName, equippedSquire2Name);
        }

        // Initialize first enemy
        createNewEnemy();

        gameOver = false;
        isPlayerTurn = true;
        updateButtonStates();
        updateStageDisplay();

        // Debug final player stats
        debugFinalPlayerStats();
    }


    private Knight loadSquireKnight(String squireName) {
        Knight knight = null;

        if (squireName.equals("Axolotl Knight")) {
            int quantity = sharedPreferences.getInt("Axolotl Knight_quantity", 1);
            knight = new Knight("brave_knight");
            knight.setQuantity(quantity);
        } else if (squireName.startsWith("Evolved ")) {
            // Evolved knights have saved stats
            int savedHp = sharedPreferences.getInt(squireName + "_hp", 200);
            int savedAttack = sharedPreferences.getInt(squireName + "_attack", 40);
            int quantity = sharedPreferences.getInt(squireName + "_quantity", 1);

            knight = new Knight(squireName, savedHp, savedAttack, "player_character");
            knight.setQuantity(quantity);
        } else {
            // Regular knights
            int baseHp = sharedPreferences.getInt(squireName + "_hp", 100);
            int baseAttack = sharedPreferences.getInt(squireName + "_attack", 20);
            int quantity = sharedPreferences.getInt(squireName + "_quantity", 1);

            knight = new Knight(squireName, baseHp, baseAttack, "player_character");
            knight.setQuantity(quantity);
        }

        // Load trait for the squire knight
        if (knight != null) {
            loadKnightTrait(knight);
            android.util.Log.d("SquireLoading", "Loaded squire " + squireName +
                    (knight.hasTrait() ? " with trait: " + knight.getTrait().getName() : " with no trait"));
        }

        return knight;
    }

    private void createNewEnemy() {
        double multiplier;

        if (currentWorld == 1) {
            // World 1: Normal progression (1.0, 1.1, 1.2, 1.3, 1.4)
            multiplier = 1.0 + (0.1 * (currentStage - 1));
        } else {
            // World 2+: Each world starts with 100% buff from previous world's final boss
            // Calculate the final boss multiplier from the previous world
            double previousWorldFinalMultiplier = calculateWorldFinalBossMultiplier(currentWorld - 1);

            // Start this world with 100% buff (2x) from previous world's final boss
            double thisWorldBaseMultiplier = previousWorldFinalMultiplier * 2.0;

            // Then add 10% per stage within this world
            multiplier = thisWorldBaseMultiplier * (1.0 + (0.1 * (currentStage - 1)));
        }

        int enemyHealth = (int)(baseEnemyHealth * multiplier);
        int enemyAttack = (int)(baseEnemyAttack * multiplier);

        // Set enemy name based on stage
        String enemyName;
        if (currentStage == maxStages) {
            enemyName = "Mini Boss"; // Stage 5 is the mini boss
        } else {
            enemyName = "Enemy"; // Stages 1-4 are regular enemies
        }

        enemy = new Character(enemyName, enemyHealth, enemyAttack);
        enemyStunned = false; // Reset stun when new enemy appears

        // Update enemy name display
        enemyNameText.setText(enemyName.toUpperCase());

        animateCharacterEntrance();
    }

    private double calculateWorldFinalBossMultiplier(int world) {
        if (world == 1) {
            return 1.4; // World 1 final boss: 1.4x base stats
        } else {
            // For world N, recursively calculate based on previous world
            double previousFinalMultiplier = calculateWorldFinalBossMultiplier(world - 1);
            double thisWorldBaseMultiplier = previousFinalMultiplier * 2.0;
            return thisWorldBaseMultiplier * 1.4; // Apply the 40% progression within the world
        }
    }

    private void updateStageDisplay() {
        stageCounter.setText(currentStage + "/" + maxStages);
        worldCounter.setText("WORLD " + currentWorld);
    }

    private void updateUI() {
        // Update health text
        playerHealthText.setText(player.getCurrentHealth() + "/" + player.getMaxHealth());
        enemyHealthText.setText(enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());

        // NEW: Update enemy attack display
        enemyAttackText.setText("ATK: " + enemy.getAttack());

        // Update health bars
        int playerHealthPercent = (player.getCurrentHealth() * 100) / player.getMaxHealth();
        int enemyHealthPercent = (enemy.getCurrentHealth() * 100) / enemy.getMaxHealth();

        playerHealthBar.setProgress(playerHealthPercent);
        enemyHealthBar.setProgress(enemyHealthPercent);

        updateButtonStates();
    }

    private void updateButtonStates() {
        if (isPlayerTurn && !gameOver && !isPlayerAnimating) {
            int currentAttack = player.getAttack(); // This now includes all bonuses

            int lightDamage = currentAttack / 2;
            int mediumDamage = currentAttack;

            lightAttackButton.setText("LIGHT\n(" + lightDamage + " dmg + stun chance)");
            mediumAttackButton.setText("MEDIUM\n(" + mediumDamage + " damage)");
            heavyAttackButton.setText("HEAVY\n(2x damage or miss)");

            // ENABLE buttons only when it's safe
            enableAttackButtons();

            // Enhanced debug for dual squires
            PassiveManager pm = player.getPassiveManager();
            if (pm.getActivePassives().size() > 0) {
                android.util.Log.d("BattleUI", "Active passives from " + pm.getActivePassives().size() + " squires:");
                for (int i = 0; i < pm.getActivePassives().size(); i++) {
                    Knight.PassiveEffect passive = pm.getActivePassives().get(i);
                    android.util.Log.d("BattleUI", "  " + (i+1) + ". " + passive.getName() + " = " + (passive.getValue() * 100) + "%");
                }
            }
        } else {
            // DISABLE buttons when not player's turn or during animations
            disableAttackButtons();
        }
    }

    // Update your attack methods to include animation:

    private void playerLightAttack() {
        // SIMPLIFIED SPAM PREVENTION: Only check essential conditions
        if (gameOver || !isPlayerTurn || isPlayerAnimating) {
            android.util.Log.d("AttackSpam", "Attack blocked - gameOver:" + gameOver +
                    " isPlayerTurn:" + isPlayerTurn + " playerAnimating:" + isPlayerAnimating);
            return;
        }

        // DISABLE BUTTONS immediately to prevent spam
        disableAttackButtons();

        // Play attack animation BEFORE calculating damage
        playPlayerAttackAnimation();

        // Small delay for animation to start, then execute attack
        playerCharacterImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                int damage = player.performAttack(true, false, false);
                enemy.takeDamage(damage);

                // 50% chance to stun enemy
                Random random = new Random();
                boolean stunSuccess = random.nextBoolean();
                if (stunSuccess) {
                    enemyStunned = true;
                }

                checkBattleResult();
            }
        }, 150); // Execute attack halfway through animation
    }


    private void playerMediumAttack() {
        // SIMPLIFIED SPAM PREVENTION: Only check essential conditions
        if (gameOver || !isPlayerTurn || isPlayerAnimating) {
            android.util.Log.d("AttackSpam", "Attack blocked - gameOver:" + gameOver +
                    " isPlayerTurn:" + isPlayerTurn + " playerAnimating:" + isPlayerAnimating);
            return;
        }

        // DISABLE BUTTONS immediately to prevent spam
        disableAttackButtons();

        playPlayerAttackAnimation();

        playerCharacterImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                int damage = player.performAttack(false, true, false);
                enemy.takeDamage(damage);
                checkBattleResult();
            }
        }, 150);
    }


    private void playerHeavyAttack() {
        // SIMPLIFIED SPAM PREVENTION: Only check essential conditions
        if (gameOver || !isPlayerTurn || isPlayerAnimating) {
            android.util.Log.d("AttackSpam", "Attack blocked - gameOver:" + gameOver +
                    " isPlayerTurn:" + isPlayerTurn + " playerAnimating:" + isPlayerAnimating);
            return;
        }

        // DISABLE BUTTONS immediately to prevent spam
        disableAttackButtons();

        playPlayerAttackAnimation();

        playerCharacterImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                int damage = player.performAttack(false, false, true);
                enemy.takeDamage(damage);
                checkBattleResult();
            }
        }, 150);
    }


    // COMPLETE FIXED VERSION: Replace your entire checkBattleResult() method with this:

    private void checkBattleResult() {
        updateUI();

        // Check if enemy is defeated
        if (enemy.getCurrentHealth() <= 0) {
            // PLAY VICTORY ANIMATION SEQUENCE FIRST
            animateVictorySequenceDramatic();

            stagesCompleted++;

            // Calculate base coins for this boss
            int baseCoins = 0;
            if (currentStage >= maxStages) {
                // Final boss of world - 60 base coins
                baseCoins = 60;
            } else {
                // Regular boss - 10 base coins
                baseCoins = 10;
            }

            // Apply world multiplier
            int coinsForThisBoss = baseCoins * currentWorld;
            coinsEarnedThisRun += coinsForThisBoss;

            android.util.Log.d("CoinSystem", "Stage " + currentStage + " World " + currentWorld +
                    ": Base " + baseCoins + " × " + currentWorld + " = " + coinsForThisBoss + " coins");

            // CHECK FOR EVENT BUT DON'T TRIGGER YET
            boolean eventOccurred = checkForEvent();

            // WAIT FOR VICTORY ANIMATION TO COMPLETE BEFORE PROCEEDING
            playerCharacterImage.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handlePostVictorySequence(eventOccurred);
                }
            }, 2000);

            return;
        }

        // Switch to enemy turn (existing code unchanged)
        isPlayerTurn = false;
        updateButtonStates();

        // Enemy attacks after a short delay (unless stunned)
        lightAttackButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                enemyAttack();
            }
        }, 1500);
    }

    private void enemyAttack() {
        if (gameOver) return;

        // Check if enemy is stunned
        if (enemyStunned) {
            enemyStunned = false; // Remove stun after skipping turn
            android.util.Log.d("EnemyAttack", "Enemy was stunned, skipping turn");
        } else {
            // NEW: Play enemy attack animation BEFORE dealing damage
            playEnemyAttackAnimation();

            // Small delay for animation to start, then execute attack
            enemyCharacterImage.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Enemy attacks player
                    int damage = enemy.getAttack();
                    player.takeDamage(damage);
                    android.util.Log.d("EnemyAttack", "Enemy dealt " + damage + " damage");

                    // Update UI after damage
                    updateUI();

                    // Check if player is defeated
                    if (player.getCurrentHealth() <= 0) {
                        endGame(false);
                        return;
                    }

                    // Switch back to player turn
                    isPlayerTurn = true;
                    updateUI(); // This will re-enable attack buttons
                }
            }, 150); // Execute attack halfway through animation

            return; // Exit early since we're using delayed execution
        }

        // If enemy was stunned, continue without animation
        updateUI();

        // Check if player is defeated (shouldn't happen if stunned, but safety check)
        if (player.getCurrentHealth() <= 0) {
            endGame(false);
            return;
        }

        // Switch back to player turn
        isPlayerTurn = true;
        updateUI(); // This will re-enable attack buttons
    }

    private void endGame(boolean playerWon) {
        gameOver = true;
        updateButtonStates();

        String message;
        String subMessage;

        int totalBossesDefeated = (worldsCompleted * maxStages) + stagesCompleted;
        message = "💀 DEFEATED! 💀";
        if (totalBossesDefeated > 0) {
            subMessage = "You were defeated and lost all potential rewards!\n\n" +
                    "You defeated " + totalBossesDefeated + " bosses and reached World " + currentWorld + ".\n\n" +
                    "💔 Coins lost: " + coinsEarnedThisRun + " (with world " + currentWorld + " multipliers)\n" +
                    "💡 Next time, consider surrendering when low on health to keep your coins!";
        } else {
            subMessage = "You didn't defeat any bosses.\nBetter luck next time!";
        }

        new AlertDialog.Builder(this)
                .setTitle(message)
                .setMessage(subMessage)
                .setPositiveButton("Back to Lobby", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showWorldCompletionDialog() {
        String message = "🎉 WORLD " + (currentWorld - 1) + " COMPLETED! 🎉\n\n" +
                "Incredible! You've conquered an entire world!\n\n" +
                "🔥 Entering World " + currentWorld + "\n" +
                "⚔️ Enemies are now much stronger!\n\n" +
                "💰 Potential coins so far: " + coinsEarnedThisRun + "\n" +
                "Ready for the next challenge?";

        new AlertDialog.Builder(this)
                .setTitle("World Conquered!")
                .setMessage(message)
                .setPositiveButton("Bring it on!", null)
                .show();
    }

    private void addCoins(int amount) {
        int currentCoins = sharedPreferences.getInt("coins", 0);
        int newTotal = currentCoins + amount;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", newTotal);
        editor.apply();

        // Debug logging (only in console, no toast)
        android.util.Log.d("CoinSystem", "Coins added: " + amount + ", New total: " + newTotal);
    }

    private void showSurrenderDialog() {
        String message = "Are you sure you want to surrender?\n\n" +
                "💰 GOOD NEWS: You'll keep all coins earned from defeated bosses!\n" +
                "Current earnings: " + coinsEarnedThisRun + " coins (with World " + currentWorld + " multipliers)\n\n" +
                "🌟 World " + currentWorld + " Bonus: All coins are multiplied by " + currentWorld + "x!\n\n" +
                "🛡️ STRATEGIC TIP: Surrendering when low on health is better than dying and losing everything!\n\n" +
                "⚠️ You will return to the lobby and lose progress in this run.";

        new AlertDialog.Builder(this)
                .setTitle("🏳️ Strategic Retreat")
                .setMessage(message)
                .setPositiveButton("Yes, Surrender", (dialog, which) -> {
                    surrenderGame();
                })
                .setNegativeButton("Keep Fighting", null)
                .show();
    }

    private void surrenderGame() {
        gameOver = true;
        updateButtonStates();

        addCoins(coinsEarnedThisRun);

        int totalBossesDefeated = (worldsCompleted * maxStages) + stagesCompleted;
        String message = "🏳️ Strategic Retreat! 🏳️\n\n" +
                "Smart choice! You kept all your hard-earned coins!\n\n" +
                "📊 Run Summary:\n" +
                "• Bosses defeated: " + totalBossesDefeated + "\n" +
                "• Highest world: " + currentWorld + "\n" +
                "💰 Coins earned: " + coinsEarnedThisRun + " (with " + currentWorld + "x world multiplier)";

        new AlertDialog.Builder(this)
                .setTitle("Wise Decision!")
                .setMessage(message)
                .setPositiveButton("Back to Lobby", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Updated attack animation method
    private void playPlayerAttackAnimation() {
        if (isPlayerAnimating) return; // Prevent multiple animations

        isPlayerAnimating = true;

        // Switch to attack frame (knight-specific or default)
        playerCharacterImage.setImageResource(currentPlayerAttackImage);

        // After 1000ms, switch back to idle
        playerCharacterImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                playerCharacterImage.setImageResource(currentPlayerIdleImage);
                isPlayerAnimating = false;
            }
        }, 1000); // Animation duration: 1000ms
    }

    // Method to load knight images when battle starts
    private void loadPlayerKnightImages() {
        // Load player knight images
        currentPlayerIdleImage = KnightImageUtils.getKnightIdleImage(this, currentKnightName);
        currentPlayerAttackImage = KnightImageUtils.getKnightAttackImage(this, currentKnightName);

        // Set the initial idle image
        playerCharacterImage.setImageResource(currentPlayerIdleImage);

        android.util.Log.d("KnightImages", "Loaded images for: " + currentKnightName);

        // NEW: Also load enemy images when loading player images
        loadEnemyImages();
    }

    // Add this debug method to GameActivity:
    private void debugPassiveApplication() {
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");

        android.util.Log.d("PassiveDebug", "=== PASSIVE APPLICATION DEBUG ===");
        android.util.Log.d("PassiveDebug", "Equipped Knight: " + equippedKnight);
        android.util.Log.d("PassiveDebug", "Equipped Squire: " + equippedSquire);

        // Check what the player character actually has
        Knight.PassiveEffect playerPassive = player.getPassiveEffect();
        android.util.Log.d("PassiveDebug", "Player's Passive: " + playerPassive.getName());
        android.util.Log.d("PassiveDebug", "Passive Type: " + playerPassive.getPassiveType());
        android.util.Log.d("PassiveDebug", "Passive Value: " + playerPassive.getValue());

        // Check what the squire should have
        if (!equippedSquire.isEmpty()) {
            Knight squire = loadSquireKnight(equippedSquire);
            if (squire != null) {
                Knight.PassiveEffect squirePassive = squire.getPassiveEffect();
                android.util.Log.d("PassiveDebug", "Squire Should Have: " + squirePassive.getName());
                android.util.Log.d("PassiveDebug", "Squire Type: " + squirePassive.getPassiveType());
                android.util.Log.d("PassiveDebug", "Squire Value: " + squirePassive.getValue());

                // Check if they match
                boolean passivesMatch = playerPassive.getPassiveType() == squirePassive.getPassiveType() &&
                        Math.abs(playerPassive.getValue() - squirePassive.getValue()) < 0.001f;
                android.util.Log.d("PassiveDebug", "Passives Match: " + passivesMatch);
            }
        }

        android.util.Log.d("PassiveDebug", "=== END DEBUG ===");
    }

    private boolean checkForEvent() {
        // Check if an event should occur based on current stage
        if (EventDatabase.shouldEventOccur(currentStage)) {
            android.util.Log.d("EventSystem", "Event will trigger after victory animation: after stage " + (currentStage - 1));
            return true; // Event should occur (but don't handle it yet)
        }
        return false; // No event occurred
    }

    private void showEventScreen() {
        // Get the event that was determined to occur
        Event event = EventDatabase.getRandomEvent();

        // Apply event effect to player BEFORE showing screen
        event.executeEvent(player);

        Intent intent = new Intent(GameActivity.this, EventActivity.class);
        intent.putExtra("event_name", event.getName());
        intent.putExtra("event_description", event.getDescription());
        startActivity(intent);

        android.util.Log.d("EventSystem", "Event screen shown after victory animation: " + event.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle returning from event screen FIRST
        if (pendingStageProgression) {
            pendingStageProgression = false; // Reset the flag

            // Refresh squire display in case King's Blessing was unlocked
            loadKnightType(); // <-- ADD THIS LINE

            // Now proceed to next stage after returning from event
            proceedToNextStage(false); // This will set pendingWorldCompletionDialog if needed
        }

        // THEN handle world completion dialog (if it was set by proceedToNextStage)
        if (pendingWorldCompletionDialog) {
            pendingWorldCompletionDialog = false;
            showWorldCompletionDialog();
        }
    }

    private void updateFurthestProgress() {
        // Get current progress
        int currentProgress = calculateProgressScore();

        // Get saved best progress
        int bestProgress = sharedPreferences.getInt("furthest_progress", 0);
        String bestProgressText = sharedPreferences.getString("furthest_progress_text", "World 1 - Stage 1");

        // Update if current is better
        if (currentProgress > bestProgress) {
            String currentProgressText = "World " + currentWorld + " - Stage " + currentStage;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("furthest_progress", currentProgress);
            editor.putString("furthest_progress_text", currentProgressText);
            editor.apply();

            android.util.Log.d("ProgressTracker", "New record! Reached: " + currentProgressText);
        }
    }

    private int calculateProgressScore() {
        // Convert world + stage to a single comparable number
        // World 1 Stage 1 = 101, World 1 Stage 5 = 105
        // World 2 Stage 1 = 201, World 2 Stage 3 = 203
        return (currentWorld * 100) + currentStage;
    }private void animateCharacterEntrance() {
        // Get screen width for animation distance
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Animate player character sliding in from left
        animatePlayerEntrance(screenWidth);

        // Animate enemy character sliding in from right (with delay)
        animateEnemyEntrance(screenWidth);

        android.util.Log.d("Animation", "Character entrance animations started");
    }

    private void animatePlayerEntrance(int screenWidth) {
        // Player slides in from left side of screen
        TranslateAnimation playerAnimation = new TranslateAnimation(
                -screenWidth/2,  // Start X: Off-screen left
                0,               // End X: Current position
                0,               // Start Y: No vertical movement
                0                // End Y: No vertical movement
        );

        playerAnimation.setDuration(800);  // 800ms animation
        playerAnimation.setInterpolator(new DecelerateInterpolator()); // Smooth deceleration
        playerAnimation.setFillAfter(false); // Don't keep the end state

        // Start the animation
        playerCharacterImage.startAnimation(playerAnimation);
    }

    private void animateEnemyEntrance(int screenWidth) {
        // Enemy slides in from right side of screen (with 200ms delay)
        TranslateAnimation enemyAnimation = new TranslateAnimation(
                screenWidth/2,   // Start X: Off-screen right
                0,               // End X: Current position
                0,               // Start Y: No vertical movement
                0                // End Y: No vertical movement
        );

        enemyAnimation.setDuration(800);   // 800ms animation
        enemyAnimation.setStartOffset(200); // 200ms delay after player
        enemyAnimation.setInterpolator(new DecelerateInterpolator()); // Smooth deceleration
        enemyAnimation.setFillAfter(false); // Don't keep the end state

        // Start the animation
        enemyCharacterImage.startAnimation(enemyAnimation);
    }
    private void animateVictorySequenceDramatic() {
        animateEnemyDefeatDramatic();
        animatePlayerVictoryExitDelayed();
    }

    private void animateEnemyDefeatDramatic() {
        // Enemy falls down and fades out
        AnimationSet enemyDefeatSet = new AnimationSet(true);

        // Fall down animation
        TranslateAnimation fallDown = new TranslateAnimation(0, 0, 0, 200);
        fallDown.setDuration(500);

        // Fade out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(200); // Start fading after falling starts

        enemyDefeatSet.addAnimation(fallDown);
        enemyDefeatSet.addAnimation(fadeOut);
        enemyDefeatSet.setFillAfter(true);

        enemyCharacterImage.startAnimation(enemyDefeatSet);
    }

    private void animatePlayerVictoryExitDelayed() {
        // Player walks off screen with longer delay
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        TranslateAnimation exitAnimation = new TranslateAnimation(0, screenWidth/2, 0, 0);
        exitAnimation.setDuration(1200);  // Slower, more triumphant exit
        exitAnimation.setStartOffset(600); // Wait longer after enemy defeat
        exitAnimation.setInterpolator(new DecelerateInterpolator());
        exitAnimation.setFillAfter(true);

        playerCharacterImage.startAnimation(exitAnimation);
    }

    private void proceedToNextStage(boolean eventOccurred) {
        if (currentStage >= maxStages) {
            // All stages in current world completed - move to next world
            worldsCompleted++;
            currentWorld++;
            currentStage = 1;
            stagesCompleted = 0; // Reset stages completed for new world

            updateFurthestProgress();

            createNewEnemy(); // This will trigger entrance animation
            updateStageDisplay();
            updateUI();

            // Continue to next world
            isPlayerTurn = true;
            updateButtonStates();

            // Show world completion dialog (only if no event occurred)
            if (!eventOccurred) {
                showWorldCompletionDialog();
            }
            // If event occurred, pendingWorldCompletionDialog is already set
            // and will show in onResume()
        } else {
            // Move to next stage in same world
            currentStage++;
            updateFurthestProgress();

            createNewEnemy(); // This will trigger entrance animation
            updateStageDisplay();
            updateUI();

            // Continue without healing player
            isPlayerTurn = true;
            updateButtonStates();
        }
    }

    private void handlePostVictorySequence(boolean eventOccurred) {
        // Check if we just completed World 4 (MOVED THIS LOGIC)
        boolean justCompletedWorld4 = (currentWorld == 4 && currentStage == 5);

        if (justCompletedWorld4) {
            // FIRST: Give the player their well-earned rewards
            addCoins(coinsEarnedThisRun);

            // SECOND: Show world completion celebration
            showWorld4CompletionDialog(); // New method

            // THIRD: Story will be handled in onResume when dialog is dismissed
            return;
        }

        // Rest of existing logic for normal events...
        if (eventOccurred) {
            Event event = getPendingEvent();
            event.executeEvent(player, sharedPreferences);

            Intent intent = new Intent(GameActivity.this, EventActivity.class);
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_description", event.getDescription());
            startActivity(intent);

            pendingStageProgression = true;
        } else {
            proceedToNextStage(false);
        }
    }

    private void showWorld4CompletionDialog() {
        int totalBossesDefeated = (worldsCompleted * maxStages) + stagesCompleted;

        String message = "🏆 WORLD 4 CONQUERED! 🏆\n\n" +
                "Incredible achievement! You've proven yourself as a true warrior!\n\n" +
                "📊 Epic Run Summary:\n" +
                "• Bosses defeated: " + totalBossesDefeated + "\n" +
                "• Worlds conquered: 4 complete worlds!\n" +
                "💰 Coins earned: " + coinsEarnedThisRun + " (with 4x world multipliers)\n\n" +
                "🎖️ The King has taken notice of your incredible prowess...\n\n" +
                "⭐ A special message awaits you!";

        new AlertDialog.Builder(this)
                .setTitle("🌟 LEGENDARY ACHIEVEMENT! 🌟")
                .setMessage(message)
                .setPositiveButton("Receive Royal Message", (dialog, which) -> {
                    // NOW show the story after rewards are acknowledged
                    Intent storyIntent = new Intent(GameActivity.this, StoryActivity.class);
                    storyIntent.putExtra("story_type", "WORLD4_COMPLETION");
                    startActivity(storyIntent);

                    // End the current game session
                    finish();
                })
                .setCancelable(false) // Player must acknowledge their achievement
                .show();
    }


    // NEW HELPER METHODS: Track if we're returning from an event
    private boolean shouldProceedAfterEvent() {
        // You can implement this with a flag if needed
        // For now, we'll use the existing logic since world completion is handled above
        return false; // Adjust based on your needs
    }

    private void resetEventProceedingFlag() {
        // Reset any flags used to track event state
    }

    private Event getPendingEvent() {
        // Use the smart event selection that considers King's Blessing unlock status
        return EventDatabase.getEventForStage(currentStage, sharedPreferences);
    }

    private void applySquirePassives(String squire1Name, String squire2Name) {
        // Apply first squire passive
        if (!squire1Name.isEmpty()) {
            Knight squire1 = loadSquireKnight(squire1Name);
            if (squire1 != null) {
                player.addPassiveEffect(squire1.getPassiveEffect());
                android.util.Log.d("GameActivity", "Applied squire 1 passive: " + squire1.getPassiveEffect().getName());
            }
        }

        // Apply second squire passive (if unlocked)
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);
        if (hasKingsBlessing && !squire2Name.isEmpty()) {
            Knight squire2 = loadSquireKnight(squire2Name);
            if (squire2 != null) {
                player.addPassiveEffect(squire2.getPassiveEffect());
                android.util.Log.d("GameActivity", "Applied squire 2 passive: " + squire2.getPassiveEffect().getName());
            }
        }

        // Debug log all combined effects
        player.getPassiveManager().debugLogPassives();
    }

    private Knight loadKnightForBattle(String knightName) {
        Knight knight = null;

        if (knightName.equals("Axolotl Knight")) {
            // Load from database with quantity for buff calculation
            knight = new Knight("brave_knight");
            int quantity = sharedPreferences.getInt("Axolotl Knight_quantity", 1);
            knight.setQuantity(quantity);
        } else if (knightName.startsWith("Evolved ")) {
            // Evolved knights have saved stats
            int savedHp = sharedPreferences.getInt(knightName + "_hp", 200);
            int savedAttack = sharedPreferences.getInt(knightName + "_attack", 40);
            int quantity = sharedPreferences.getInt(knightName + "_quantity", 1);

            knight = new Knight(knightName, savedHp, savedAttack, "player_character");
            knight.setQuantity(quantity);
        } else {
            // Try to load from database first
            KnightDatabase.KnightData data = KnightDatabase.getKnightDataByName(knightName);

            if (data != null) {
                knight = new Knight(data.id);
                int quantity = sharedPreferences.getInt(knightName + "_quantity", 1);
                knight.setQuantity(quantity);
            } else {
                // Legacy knight
                int baseHp = sharedPreferences.getInt(knightName + "_hp", 100);
                int baseAttack = sharedPreferences.getInt(knightName + "_attack", 20);
                int quantity = sharedPreferences.getInt(knightName + "_quantity", 1);

                knight = new Knight(knightName, baseHp, baseAttack, "player_character");
                knight.setQuantity(quantity);
            }
        }

        // IMPORTANT: Load trait for the knight (NEW)
        if (knight != null) {
            loadKnightTrait(knight);
        }

        return knight;
    }

    private void applyAllSquirePassives(String squire1Name, String squire2Name) {
        android.util.Log.d("PassiveSystem", "=== APPLYING SQUIRE PASSIVES ===");

        // Apply first squire passive
        if (!squire1Name.isEmpty()) {
            Knight squire1 = loadSquireKnight(squire1Name);
            if (squire1 != null) {
                // IMPORTANT: Load trait for squire
                loadKnightTrait(squire1);

                Knight.PassiveEffect passive1 = squire1.getPassiveEffect();

                // Check if THIS squire has Guru trait - double their own passive effect
                if (squire1.hasTrait() && squire1.getTrait().getName().equals("Guru")) {
                    passive1 = createDoubledPassive(passive1);
                    android.util.Log.d("PassiveSystem", "GURU SQUIRE: Doubled passive for " + squire1Name +
                            " from " + (squire1.getPassiveEffect().getValue() * 100) + "% to " + (passive1.getValue() * 100) + "%");
                }

                player.addPassiveEffect(passive1);
                android.util.Log.d("PassiveSystem", "Applied Squire 1 (" + squire1Name + "): " +
                        passive1.getName() + " = " + (passive1.getValue() * 100) + "% Type: " + passive1.getPassiveType());
            }
        }

        // Apply second squire passive (if King's Blessing is unlocked)
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);
        if (hasKingsBlessing && !squire2Name.isEmpty()) {
            Knight squire2 = loadSquireKnight(squire2Name);
            if (squire2 != null) {
                // IMPORTANT: Load trait for second squire too
                loadKnightTrait(squire2);

                Knight.PassiveEffect passive2 = squire2.getPassiveEffect();

                // Check if THIS squire has Guru trait - double their own passive effect
                if (squire2.hasTrait() && squire2.getTrait().getName().equals("Guru")) {
                    passive2 = createDoubledPassive(passive2);
                    android.util.Log.d("PassiveSystem", "GURU SQUIRE 2: Doubled passive for " + squire2Name +
                            " from " + (squire2.getPassiveEffect().getValue() * 100) + "% to " + (passive2.getValue() * 100) + "%");
                }

                player.addPassiveEffect(passive2);
                android.util.Log.d("PassiveSystem", "Applied Squire 2 (" + squire2Name + "): " +
                        passive2.getName() + " = " + (passive2.getValue() * 100) + "% Type: " + passive2.getPassiveType());
            }
        }

        // Fighter Lonely trait handling (existing code)
        String equippedKnightName = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String fighterTraitName = sharedPreferences.getString(equippedKnightName + "_trait", "");

        if (fighterTraitName.equals("Lonely")) {
            Knight fighter = loadKnightForBattle(equippedKnightName);
            if (fighter != null) {
                Knight.PassiveEffect fighterPassive = fighter.getPassiveEffect();
                player.addPassiveEffect(fighterPassive);
                android.util.Log.d("PassiveSystem", "Applied Lonely Fighter Passive (" + equippedKnightName + "): " +
                        fighterPassive.getName() + " = " + (fighterPassive.getValue() * 100) + "% Type: " + fighterPassive.getPassiveType());
            }
        }

        // Debug all combined effects
        player.getPassiveManager().debugLogPassives();
    }


    // Add this method to handle the toggle:
    private void togglePassiveDisplay() {
        isPassiveDisplayVisible = !isPassiveDisplayVisible;
        updatePassiveDisplayVisibility();
    }

    // Add this method to update the display based on current state:
    private void updatePassiveDisplayVisibility() {
        if (isPassiveDisplayVisible) {
            // Show detailed passive information
            showDetailedPassiveInfo();
            squireText.setVisibility(View.VISIBLE);
        } else {
            // Show compact summary
            showCompactPassiveInfo();
            squireText.setVisibility(View.VISIBLE); // Keep visible but compact
        }
    }

    // Add method for compact display (default state):
    private void showCompactPassiveInfo() {
        StringBuilder compactText = new StringBuilder();

        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String fighterTraitName = sharedPreferences.getString(equippedKnight + "_trait", "");
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);
        boolean hasLonelyTrait = fighterTraitName.equals("Lonely");

        // Count active squires and check for Guru
        int activeSquires = 0;
        boolean hasGuruSquire = false;

        if (!equippedSquire.isEmpty()) {
            activeSquires++;
            Knight squire1 = loadSquireKnight(equippedSquire);
            if (squire1 != null && squire1.hasTrait() && squire1.getTrait().getName().equals("Guru")) {
                hasGuruSquire = true;
            }
        }

        if (!equippedSquire2.isEmpty()) {
            activeSquires++;
            Knight squire2 = loadSquireKnight(equippedSquire2);
            if (squire2 != null && squire2.hasTrait() && squire2.getTrait().getName().equals("Guru")) {
                hasGuruSquire = true;
            }
        }

        // Count total active passives (squires + lonely fighter)
        int totalPassives = activeSquires;
        if (hasLonelyTrait) totalPassives++;

        if (totalPassives == 0) {
            compactText.append("🛡️ No Squires (Tap to expand)");
        } else {
            compactText.append("🛡️ ").append(totalPassives).append(" Passive");
            if (totalPassives > 1) compactText.append("s");
            compactText.append(" Active");

            // Add Guru indicator
            if (hasGuruSquire) {
                compactText.append(" 🎭");
            }

            // Show breakdown
            if (hasLonelyTrait && activeSquires > 0) {
                compactText.append(" (").append(activeSquires).append(" squire");
                if (activeSquires > 1) compactText.append("s");
                compactText.append(" + fighter)");
            } else if (hasLonelyTrait) {
                compactText.append(" (fighter only)");
            } else {
                if (hasKingsBlessing) {
                    compactText.append(" (").append(activeSquires).append("/2 squires)");
                } else {
                    compactText.append(" (").append(activeSquires).append("/1 squire)");
                }
            }

            compactText.append(" (Tap to expand)");
        }

        squireText.setText(compactText.toString());
        squireText.setMaxLines(1);
        squireText.setTextSize(8);
    }

    // Add method for detailed display (expanded state):
    private void showDetailedPassiveInfo() {
        StringBuilder detailedText = new StringBuilder();

        String equippedSquire = sharedPreferences.getString("equipped_squire", "");
        String equippedSquire2 = sharedPreferences.getString("equipped_squire2", "");
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String fighterTraitName = sharedPreferences.getString(equippedKnight + "_trait", "");
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);
        boolean hasAdminKnight = sharedPreferences.getBoolean("has_admin_knight", false);
        boolean hasLonelyTrait = fighterTraitName.equals("Lonely");

        if (hasAdminKnight && equippedKnight.equals("King's Guard")) {
            detailedText.append("🛡️ ADMIN MODE\n   No squires needed\n\n🔒 SQUIRE SYSTEM BYPASSED\n   Admin knight is overpowered");
        } else {
            // === FIGHTER PASSIVE SECTION ===
            if (hasLonelyTrait) {
                Knight fighter = loadKnightForBattle(equippedKnight);
                if (fighter != null) {
                    Knight.PassiveEffect fighterPassive = fighter.getPassiveEffect();
                    detailedText.append("⚔️ FIGHTER: ").append(equippedKnight).append(" ✨\n");
                    detailedText.append("   ").append(fighterPassive.getName()).append(" - ").append(fighterPassive.getDescription());
                    detailedText.append("\n   (Lonely trait effect)\n\n");
                }
            }

            // === FIRST SQUIRE SECTION ===
            if (!equippedSquire.isEmpty()) {
                Knight squire = loadSquireKnight(equippedSquire);
                if (squire != null) {
                    Knight.PassiveEffect passive = squire.getPassiveEffect();
                    detailedText.append("🛡️ SQUIRE: ").append(equippedSquire).append("\n");

                    // Check if this squire has Guru trait
                    boolean squireHasGuru = squire.hasTrait() && squire.getTrait().getName().equals("Guru");

                    if (squireHasGuru) {
                        // Show original passive value and doubled value
                        float originalValue = passive.getValue();
                        float doubledValue = originalValue * 2.0f;

                        detailedText.append("   ").append(passive.getName()).append(" - ");
                        detailedText.append(passive.getDescription());
                        detailedText.append("\n   🎭 GURU: ").append((int)(originalValue * 100)).append("% → ")
                                .append((int)(doubledValue * 100)).append("% (DOUBLED!)");
                    } else {
                        detailedText.append("   ").append(passive.getName()).append(" - ").append(passive.getDescription());
                    }
                }
            } else {
                detailedText.append("🛡️ SQUIRE: None Equipped\n");
                detailedText.append("   No passive effect");
            }

            // === SECOND SQUIRE SECTION ===
            if (hasKingsBlessing) {
                detailedText.append("\n\n"); // Extra spacing between squires

                if (!equippedSquire2.isEmpty()) {
                    Knight squire2 = loadSquireKnight(equippedSquire2);
                    if (squire2 != null) {
                        Knight.PassiveEffect passive2 = squire2.getPassiveEffect();
                        detailedText.append("🛡️ 2ND SQUIRE: ").append(equippedSquire2).append("\n");

                        // Check if this squire has Guru trait
                        boolean squire2HasGuru = squire2.hasTrait() && squire2.getTrait().getName().equals("Guru");

                        if (squire2HasGuru) {
                            // Show original passive value and doubled value
                            float originalValue = passive2.getValue();
                            float doubledValue = originalValue * 2.0f;

                            detailedText.append("   ").append(passive2.getName()).append(" - ");
                            detailedText.append(passive2.getDescription());
                            detailedText.append("\n   🎭 GURU: ").append((int)(originalValue * 100)).append("% → ")
                                    .append((int)(doubledValue * 100)).append("% (DOUBLED!)");
                        } else {
                            detailedText.append("   ").append(passive2.getName()).append(" - ").append(passive2.getDescription());
                        }
                    }
                } else {
                    detailedText.append("🛡️ 2ND SQUIRE: None Equipped\n");
                    detailedText.append("   No passive effect");
                }
            } else {
                // Show that second squire slot is locked
                detailedText.append("\n\n");
                detailedText.append("🔒 2ND SQUIRE: Locked\n");
                detailedText.append("   Unlock with King's Blessing event");
            }

            // Add tap hint
            detailedText.append("\n\n(Tap to collapse)");
        }

        squireText.setText(detailedText.toString());
        squireText.setMaxLines(12); // Increased to accommodate Guru info
        squireText.setTextSize(8);
    }

    private void loadEnemyImages() {
        // For now, all enemies use the same images, but this can be expanded later
        currentEnemyIdleImage = R.drawable.enemy_idle;
        currentEnemyAttackImage = R.drawable.enemy_attack;

        // Set the initial idle image
        enemyCharacterImage.setImageResource(currentEnemyIdleImage);

        android.util.Log.d("EnemyImages", "Loaded enemy images: idle and attack");
    }

    private void playEnemyAttackAnimation() {
        if (isEnemyAnimating) return; // Prevent multiple animations

        isEnemyAnimating = true;

        // Switch to attack frame
        enemyCharacterImage.setImageResource(currentEnemyAttackImage);

        // After 1000ms, switch back to idle
        enemyCharacterImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                enemyCharacterImage.setImageResource(currentEnemyIdleImage);
                isEnemyAnimating = false;
            }
        }, 1000); // Animation duration: 1000ms
    }

    private void disableAttackButtons() {
        lightAttackButton.setEnabled(false);
        mediumAttackButton.setEnabled(false);
        heavyAttackButton.setEnabled(false);

        android.util.Log.d("AttackSpam", "Attack buttons disabled");
    }

    private void enableAttackButtons() {
        if (isPlayerTurn && !gameOver && !isPlayerAnimating) {
            lightAttackButton.setEnabled(true);
            mediumAttackButton.setEnabled(true);
            heavyAttackButton.setEnabled(true);

            android.util.Log.d("AttackSpam", "Attack buttons enabled");
        } else {
            android.util.Log.d("AttackSpam", "Buttons NOT enabled - isPlayerTurn:" + isPlayerTurn +
                    " gameOver:" + gameOver + " playerAnimating:" + isPlayerAnimating);
        }
    }
    private void loadKnightTrait(Knight knight) {
        String traitName = sharedPreferences.getString(knight.getName() + "_trait", "");
        if (!traitName.isEmpty()) {
            Trait trait = TraitDatabase.getTraitByName(traitName);
            if (trait != null) {
                knight.setTrait(trait);
                android.util.Log.d("BattleSystem", "Loaded trait for fighter " + knight.getName() + ": " + trait.getDisplayString());
            }
        }
    }

    private void debugFinalPlayerStats() {
        android.util.Log.d("BattleStats", "=== FINAL BATTLE STATS ===");
        android.util.Log.d("BattleStats", "Player HP: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
        android.util.Log.d("BattleStats", "Player Attack: " + player.getAttack());

        // Show trait bonus if fighter has one
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String traitName = sharedPreferences.getString(equippedKnight + "_trait", "");
        if (!traitName.isEmpty()) {
            Trait trait = TraitDatabase.getTraitByName(traitName);
            if (trait != null) {
                android.util.Log.d("BattleStats", "Fighter Trait: " + trait.getDisplayString());
            }
        }

        // Show breakdown of passive bonuses (existing code)
        PassiveManager pm = player.getPassiveManager();
        if (pm.getCombinedHPBoost() > 0) {
            android.util.Log.d("BattleStats", "HP Boost (Passives): +" + (pm.getCombinedHPBoost() * 100) + "%");
        }
        if (pm.getCombinedAttackBoost() > 0) {
            android.util.Log.d("BattleStats", "Attack Boost (Passives): +" + (pm.getCombinedAttackBoost() * 100) + "%");
        }
        if (pm.getCombinedDamageResistance() > 0) {
            android.util.Log.d("BattleStats", "Damage Resistance: " + (pm.getCombinedDamageResistance() * 100) + "%");
        }
        if (pm.getCombinedCriticalHit() > 0) {
            android.util.Log.d("BattleStats", "Critical Hit Chance: " + (pm.getCombinedCriticalHit() * 100) + "%");
        }
        if (pm.getCombinedEvasion() > 0) {
            android.util.Log.d("BattleStats", "Evasion Chance: " + (pm.getCombinedEvasion() * 100) + "%");
        }
        if (pm.getCombinedLifeSteal() > 0) {
            android.util.Log.d("BattleStats", "Life Steal: " + (pm.getCombinedLifeSteal() * 100) + "%");
        }
        if (pm.getCombinedDoubleAttack() > 0) {
            android.util.Log.d("BattleStats", "Double Attack Chance: " + (pm.getCombinedDoubleAttack() * 100) + "%");
        }
        if (pm.getScalingAttack() > 0) {
            android.util.Log.d("BattleStats", "Scaling Attack: +" + (pm.getScalingAttack() * 100) + "% per 10% health lost");
        }

        android.util.Log.d("BattleStats", "=== END BATTLE STATS ===");
    }

    private Knight.PassiveEffect createDoubledPassive(Knight.PassiveEffect originalPassive) {
        return new Knight.PassiveEffect(
                "Enhanced " + originalPassive.getName(),
                originalPassive.getType(),
                originalPassive.getDescription() + " (Doubled by Guru)",
                originalPassive.getValue() * 2.0f, // Double the effect
                originalPassive.getPassiveType()
        );
    }




}