package app.ij.game2;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TacticalBattleActivity extends AppCompatActivity {

    private GridLayout battleGrid;
    private Button endTurnButton, retreatButton;
    private SharedPreferences sharedPreferences;
    private boolean isAnimating = false;
    private boolean isPlayerAttacking = false;
    private boolean isPlayerMoving = false;
    private TextView turnIndicator;
    private TextView actionCounter;
    // Battle state
    private TacticalUnit playerUnit;
    private TacticalUnit enemyUnit;
    private boolean isPlayerTurn = true;
    private boolean battleOver = false;

    // Grid system
    private static final int GRID_WIDTH = 10;  // Increased from 6
    private static final int GRID_HEIGHT = 5;  // Increased from 4


    private View[][] gridTiles = new View[GRID_HEIGHT][GRID_WIDTH];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();
        setContentView(R.layout.activity_tactical_battle);

        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeUI();
        initializeBattle();
        updateUI();
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
        battleGrid = findViewById(R.id.battleGrid);
        // REMOVED: turnIndicator, playerUnitStats, enemyUnitStats
        endTurnButton = findViewById(R.id.endTurnButton);
        retreatButton = findViewById(R.id.retreatButton);
        turnIndicator = findViewById(R.id.turnIndicator);
        actionCounter = findViewById(R.id.actionCounter);
        createBattleGrid();

        endTurnButton.setOnClickListener(v -> endPlayerTurn());
        retreatButton.setOnClickListener(v -> showRetreatDialog());
    }

    private void createBattleGrid() {
        battleGrid.setColumnCount(GRID_WIDTH);
        battleGrid.setRowCount(GRID_HEIGHT);

        // Get exact screen dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Account for button area
        int buttonHeightPx = (int) (60 * getResources().getDisplayMetrics().density);
        int availableHeight = screenHeight - buttonHeightPx;

        // Calculate exact tile sizes to fill screen completely
        int tileWidth = screenWidth / GRID_WIDTH;  // No padding - use every pixel
        int tileHeight = availableHeight / GRID_HEIGHT;

        android.util.Log.d("TacticalBattle", "Screen: " + screenWidth + "x" + screenHeight);
        android.util.Log.d("TacticalBattle", "Grid area: " + screenWidth + "x" + availableHeight);
        android.util.Log.d("TacticalBattle", "Tile size: " + tileWidth + "x" + tileHeight);

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                View tile = createGridTile(x, y, tileWidth, tileHeight);
                gridTiles[y][x] = tile;
                battleGrid.addView(tile);
            }
        }
    }

    private View createGridTile(final int x, final int y, int tileWidth, int tileHeight) {
        // Create a FrameLayout to hold both background and image
        FrameLayout tileContainer = new FrameLayout(this);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = tileWidth - 2;
        params.height = tileHeight - 2;
        params.setMargins(1, 1, 1, 1);
        tileContainer.setLayoutParams(params);

        // Background view for tile color
        View backgroundTile = new View(this);
        FrameLayout.LayoutParams bgParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        backgroundTile.setLayoutParams(bgParams);
        backgroundTile.setBackgroundColor(0xFF4CAF50);
        backgroundTile.setId(View.generateViewId()); // Give it a unique ID

        // ImageView for unit sprites
        ImageView unitImage = new ImageView(this);
        FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        imgParams.leftMargin = 8;
        imgParams.topMargin = 8;
        imgParams.rightMargin = 8;
        imgParams.bottomMargin = 8;
        unitImage.setLayoutParams(imgParams);
        unitImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        unitImage.setVisibility(View.GONE);
        unitImage.setId(View.generateViewId()); // Give it a unique ID

        // Add both to container
        tileContainer.addView(backgroundTile);
        tileContainer.addView(unitImage);

        tileContainer.setOnClickListener(v -> onTileClicked(x, y));

        return tileContainer;
    }

    private View getBackgroundTile(FrameLayout tileContainer) {
        return tileContainer.getChildAt(0); // Background is first child
    }

    private ImageView getUnitImage(FrameLayout tileContainer) {
        return (ImageView) tileContainer.getChildAt(1); // Image is second child
    }

    private void onTileClicked(int x, int y) {
        if (battleOver || !isPlayerTurn || isPlayerAttacking || isPlayerMoving) return;

        // Safety check
        if (playerUnit == null || enemyUnit == null) {
            android.util.Log.d("TacticalBattle", "Battle units not initialized - ignoring click");
            return;
        }

        // Check if clicking on enemy - attack
        if (x == enemyUnit.x && y == enemyUnit.y && isAdjacentTo(playerUnit.x, playerUnit.y, x, y)) {
            attackTile(x, y);
            return;
        }

        // Check if valid movement tile
        if (canMoveToTile(x, y)) {
            isPlayerMoving = true;

            movePlayerUnit(x, y);
            playerUnit.useAction();

            // Small delay to prevent spam, then re-enable
            new android.os.Handler().postDelayed(() -> {
                isPlayerMoving = false;
                checkTurnEnd();
                updateMovementIndicators();
            }, 100);
        }
    }

    private boolean canMoveToTile(int x, int y) {
        if (!playerUnit.canAct()) return false;

        if ((x == enemyUnit.x && y == enemyUnit.y) || (x == playerUnit.x && y == playerUnit.y)) {
            return false;
        }

        return isAdjacentTo(playerUnit.x, playerUnit.y, x, y);
    }

    private void movePlayerUnit(int x, int y) {
        playerUnit.x = x;
        playerUnit.y = y;
        android.util.Log.d("TacticalBattle", playerUnit.name + " moved to (" + x + ", " + y + ")");

        // Update display immediately to show new position
        updateGridDisplay();
    }


    private boolean isAdjacentTo(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // Only allow orthogonal movement (4 directions, no diagonals)
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    private void checkTurnEnd() {
        if (!playerUnit.canAct()) {
            endPlayerTurn();
        }
    }


    private void updateMovementIndicators() {
        updateGridDisplay(); // This will show the new indicators
    }


    private void initializeBattle() {
        // Get knight assigned to a slot and find which row
        String assignedKnight = "";
        int deployedRow = 2;

        // Check all slots to find assigned knight and row
        for (int i = 1; i <= 5; i++) {
            String slotKnight = sharedPreferences.getString("chapter1_slot_" + i, "");
            if (!slotKnight.isEmpty()) {
                assignedKnight = slotKnight;
                deployedRow = i - 1;
                android.util.Log.d("TacticalBattle", "Found " + assignedKnight + " in slot " + i + " (row " + deployedRow + ")");
                break;
            }
        }

        if (assignedKnight.isEmpty()) {
            android.util.Log.d("TacticalBattle", "No knight assigned - showing dialog and ending battle");
            showNoKnightAssignedDialog();
            return; // CRITICAL: Stop execution here
        }

        // Load tactical knight data from database
        TacticalKnightDatabase.TacticalKnightData data = TacticalKnightDatabase.getTacticalKnightDataByName(assignedKnight);

        if (data != null) {
            playerUnit = new TacticalUnit(data.name, data.hp, data.attack, data.speed, 0, deployedRow);
            playerUnit.maxActions = data.actions;
            playerUnit.maxAttacks = 1;
            playerUnit.resetTurn();

            android.util.Log.d("TacticalBattle", "Loaded " + data.name + " with " + data.actions +
                    " actions, positioned at row " + deployedRow);
        } else {
            // Fallback
            int lordHP = sharedPreferences.getInt(assignedKnight + "_hp", 800);
            int lordAttack = sharedPreferences.getInt(assignedKnight + "_attack", 200);
            playerUnit = new TacticalUnit(assignedKnight, lordHP, lordAttack, 5, 0, deployedRow);
            playerUnit.maxActions = 2;
            playerUnit.maxAttacks = 1;
        }

        // Create enemy (keep at right side, middle row)
        enemyUnit = new TacticalUnit("Enemy Warrior", 400, 150, 2, 9, 2);
        enemyUnit.maxActions = 2;
        enemyUnit.maxAttacks = 1;
        enemyUnit.resetTurn();

        android.util.Log.d("TacticalBattle", "Battle initialized - Player at (0," + deployedRow + "), Enemy at (9,2)");
    }

    private void showNoKnightAssignedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Knight Assigned")
                .setMessage("You must assign a knight to a tactical slot before battle.\n\nGo to Chapter 1 Collection and assign a tactical knight to a battlefield row.")
                .setPositiveButton("Return to Chapter 1", (dialog, which) -> {
                    android.util.Log.d("TacticalBattle", "No knight dialog - returning to Chapter 1");
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    android.util.Log.d("TacticalBattle", "No knight dialog cancelled - returning to Chapter 1");
                    finish();
                })
                .setCancelable(true)
                .show();
    }

    private void updateUI() {
        // Just update the battle info display
        updateBattleInfo();
        updateGridDisplay();
    }

    private void updateGridDisplay() {
        // Don't update grid display during animations
        if (isAnimating) {
            android.util.Log.d("TacticalBattle", "Skipping grid update - animation in progress");
            return;
        }

        if (playerUnit == null || enemyUnit == null) {
            android.util.Log.d("TacticalBattle", "Battle units not initialized - skipping grid update");
            return;
        }

        // Clear all tiles and hide images
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                FrameLayout tileContainer = (FrameLayout) gridTiles[y][x];
                View backgroundTile = getBackgroundTile(tileContainer);
                ImageView unitImage = getUnitImage(tileContainer);

                // Reset background color
                backgroundTile.setBackgroundColor(0xFF4CAF50);

                // Hide unit image
                unitImage.setVisibility(View.GONE);
            }
        }

        // Show movement indicators if player turn and has actions
        if (isPlayerTurn && playerUnit.canAct()) {
            showMovementIndicators();
        }

        // Show unit images
        showUnitImage(playerUnit.x, playerUnit.y, playerUnit.name, true, false);
        showUnitImage(enemyUnit.x, enemyUnit.y, enemyUnit.name, false, false);
    }

    private void showUnitImage(int x, int y, String unitName, boolean isPlayer, boolean isAttacking) {
        android.util.Log.d("TacticalBattle", "showUnitImage: " + unitName + ", isPlayer: " + isPlayer + ", isAttacking: " + isAttacking);

        FrameLayout tileContainer = (FrameLayout) gridTiles[y][x];
        View backgroundTile = getBackgroundTile(tileContainer);
        ImageView unitImage = getUnitImage(tileContainer);

        // Set background color for unit position
        if (isPlayer) {
            backgroundTile.setBackgroundColor(0xFFFFD700); // Gold for player
        } else {
            backgroundTile.setBackgroundColor(0xFFFF4444); // Red for enemy
        }

        // Set unit image based on attack state
        if (isPlayer) {
            if (isAttacking) {
                android.util.Log.d("TacticalBattle", "Setting player attack image");
                unitImage.setImageResource(R.drawable.player_attack);
            } else {
                android.util.Log.d("TacticalBattle", "Setting player idle image");
                unitImage.setImageResource(R.drawable.player_character);
            }
        } else {
            if (isAttacking) {
                android.util.Log.d("TacticalBattle", "Setting enemy attack image");
                unitImage.setImageResource(R.drawable.enemy_attack);
            } else {
                android.util.Log.d("TacticalBattle", "Setting enemy idle image");
                unitImage.setImageResource(R.drawable.enemy_idle);
            }
        }

        unitImage.setVisibility(View.VISIBLE);
    }

    private void showMovementIndicators() {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] dir : directions) {
            int targetX = playerUnit.x + dir[0];
            int targetY = playerUnit.y + dir[1];

            if (targetX < 0 || targetX >= GRID_WIDTH || targetY < 0 || targetY >= GRID_HEIGHT) {
                continue;
            }

            FrameLayout tileContainer = (FrameLayout) gridTiles[targetY][targetX];
            View backgroundTile = getBackgroundTile(tileContainer);

            if (targetX == enemyUnit.x && targetY == enemyUnit.y) {
                backgroundTile.setBackgroundColor(0xFF333333); // Attack indicator
            } else {
                backgroundTile.setBackgroundColor(0xFF2196F3); // Movement indicator
            }
        }
    }


    private void attackTile(int x, int y) {
        if (x == enemyUnit.x && y == enemyUnit.y) {
            if (isPlayerAttacking) {
                android.util.Log.d("TacticalBattle", "Attack already in progress - ignoring spam");
                return;
            }

            if (!playerUnit.canAttack()) {
                Toast.makeText(this, "Already attacked this turn!", Toast.LENGTH_SHORT).show();
                return;
            }

            isPlayerAttacking = true;
            isAnimating = true;

            // Use attack immediately
            playerUnit.useAttack();
            updateBattleInfo(); // Update display immediately after using action

            showUnitImage(playerUnit.x, playerUnit.y, playerUnit.name, true, true);

            new android.os.Handler().postDelayed(() -> {
                enemyUnit.takeDamage(playerUnit.attack);

                isPlayerAttacking = false;
                isAnimating = false;

                showUnitImage(playerUnit.x, playerUnit.y, playerUnit.name, true, false);

                if (enemyUnit.currentHP <= 0) {
                    playerVictory();
                } else {
                    checkTurnEnd();
                    updateMovementIndicators();
                    updateBattleInfo(); // Update again after action completes
                }
            }, 500);
        }
    }


    private void endPlayerTurn() {
        // Clear all action flags
        isPlayerAttacking = false;
        isPlayerMoving = false;
        isAnimating = false;

        isPlayerTurn = false;
        enemyTurn();
        playerUnit.resetTurn();
        isPlayerTurn = true;
        updateUI();
    }

    private void enemyTurn() {
        enemyUnit.resetTurn();
        android.util.Log.d("TacticalBattle", "Enemy turn started");

        // Process enemy actions sequentially with proper delays
        processEnemyAction();
    }

    private void processEnemyAction() {
        if (!enemyUnit.canAct() || enemyUnit.currentHP <= 0 || playerUnit.currentHP <= 0) {
            android.util.Log.d("TacticalBattle", "Enemy turn ended - used " +
                    enemyUnit.actionsUsed + "/" + enemyUnit.maxActions + " actions");
            return;
        }

        android.util.Log.d("TacticalBattle", "Enemy action " + (enemyUnit.actionsUsed + 1) +
                "/" + enemyUnit.maxActions);

        // Try to attack first if adjacent and hasn't attacked yet
        if (isAdjacentTo(enemyUnit.x, enemyUnit.y, playerUnit.x, playerUnit.y) && enemyUnit.canAttack()) {
            performEnemyAttack();
        } else if (enemyUnit.canMove()) {
            // Can still move (either hasn't attacked, or attacked but has actions left)
            performEnemyMovement();
        } else {
            // No valid actions left
            android.util.Log.d("TacticalBattle", "Enemy has no valid actions - ending turn");
        }
    }

    private void performEnemyAttack() {
        android.util.Log.d("TacticalBattle", "Enemy attacking - showing animation");

        isAnimating = true;
        showUnitImage(enemyUnit.x, enemyUnit.y, enemyUnit.name, false, true);

        new android.os.Handler().postDelayed(() -> {
            playerUnit.takeDamage(enemyUnit.attack);
            enemyUnit.useAttack();
            android.util.Log.d("TacticalBattle", "Enemy attack complete - actions remaining: " +
                    enemyUnit.getRemainingActions());

            isAnimating = false;
            showUnitImage(enemyUnit.x, enemyUnit.y, enemyUnit.name, false, false);

            if (playerUnit.currentHP <= 0) {
                playerDefeat();
                return;
            }

            updateUI();

            // Continue enemy turn if they have more actions (can move after attacking)
            new android.os.Handler().postDelayed(() -> {
                processEnemyAction();
            }, 200);

        }, 500);
    }

    private void performEnemyMovement() {
        boolean moved = moveEnemyTowardPlayer();
        enemyUnit.useAction();
        android.util.Log.d("TacticalBattle", "Enemy moved - actions remaining: " +
                enemyUnit.getRemainingActions());

        if (!moved) {
            android.util.Log.d("TacticalBattle", "Enemy can't move - ending turn early");
            return;
        }

        updateUI();

        // Continue with next enemy action after a short delay
        new android.os.Handler().postDelayed(() -> {
            processEnemyAction();
        }, 300);
    }

    private boolean moveEnemyTowardPlayer() {
        int dx = playerUnit.x - enemyUnit.x;
        int dy = playerUnit.y - enemyUnit.y;

        int newX = enemyUnit.x;
        int newY = enemyUnit.y;

        // Move one step toward player (orthogonal only)
        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            newX += (dx > 0) ? 1 : -1;
        } else if (Math.abs(dy) > 0) {
            // Move vertically
            newY += (dy > 0) ? 1 : -1;
        }

        // Check if new position is valid and not occupied
        if (newX >= 0 && newX < GRID_WIDTH && newY >= 0 && newY < GRID_HEIGHT &&
                !(newX == playerUnit.x && newY == playerUnit.y)) {
            enemyUnit.x = newX;
            enemyUnit.y = newY;
            return true;
        }

        return false;
    }

    private void playerVictory() {
        battleOver = true;
        new AlertDialog.Builder(this)
                .setTitle("🏆 VICTORY!")
                .setMessage("Axolotl Lord has defeated the enemyUnit warrior!\n\nTactical mastery achieved!")
                .setPositiveButton("Return to Chapter 1", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void playerDefeat() {
        battleOver = true;
        new AlertDialog.Builder(this)
                .setTitle("💀 DEFEAT")
                .setMessage("Axolotl Lord has fallen in battle...\n\nRegroup and try again!")
                .setPositiveButton("Return to Chapter 1", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showRetreatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🏃 Retreat?")
                .setMessage("Are you sure you want to retreat from battle?")
                .setPositiveButton("Retreat", (dialog, which) -> finish())
                .setNegativeButton("Keep Fighting", null)
                .show();
    }

    private void updateTurnIndicator() {
        if (battleOver) {
            turnIndicator.setText("BATTLE OVER");
            turnIndicator.setTextColor(0xFFFF4444);
            return;
        }

        if (isPlayerTurn) {
            turnIndicator.setText("YOUR TURN");
            turnIndicator.setTextColor(0xFFFFD700);
        } else {
            turnIndicator.setText("ENEMY TURN");
            turnIndicator.setTextColor(0xFFFF4444);
        }
    }

    private void updateActionCounter() {
        if (battleOver) {
            actionCounter.setText("Battle Complete");
            return;
        }

        if (isPlayerTurn) {
            int actionsLeft = playerUnit.maxActions - playerUnit.actionsUsed;
            int attacksLeft = playerUnit.maxAttacks - playerUnit.attacksUsed;
            actionCounter.setText(playerUnit.name + " - Actions: " + actionsLeft + "/" + playerUnit.maxActions +
                    " | Attacks: " + attacksLeft + "/" + playerUnit.maxAttacks);
        } else {
            int actionsLeft = enemyUnit.maxActions - enemyUnit.actionsUsed;
            int attacksLeft = enemyUnit.maxAttacks - enemyUnit.attacksUsed;
            actionCounter.setText(enemyUnit.name + " - Actions: " + actionsLeft + "/" + enemyUnit.maxActions +
                    " | Attacks: " + attacksLeft + "/" + enemyUnit.maxAttacks);
        }
    }


    private void updateBattleInfo() {
        updateTurnIndicator();
        updateActionCounter();
    }

    // Add this call wherever you currently update the UI
    private void refreshUI() {
        updateGridDisplay();
        updateBattleInfo();
    }




}