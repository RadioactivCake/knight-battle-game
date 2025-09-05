package app.ij.game2;

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

    // Battle state
    private TacticalUnit axolotlLord;
    private TacticalUnit enemy;
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
        // REMOVED: turnIndicator, axolotlLordStats, enemyStats
        endTurnButton = findViewById(R.id.endTurnButton);
        retreatButton = findViewById(R.id.retreatButton);

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
        View tile = new View(this);

        // Set exact calculated tile size with small margins for borders
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = tileWidth - 2;  // Small reduction for border
        params.height = tileHeight - 2;
        params.setMargins(1, 1, 1, 1); // 1px margins create border effect
        tile.setLayoutParams(params);

        // Tile appearance
        tile.setBackgroundColor(0xFF4CAF50); // Green tile

        tile.setOnClickListener(v -> onTileClicked(x, y));

        return tile;
    }

    private void onTileClicked(int x, int y) {
        if (battleOver || !isPlayerTurn) return;

        android.util.Log.d("TacticalBattle", "Tile clicked: (" + x + ", " + y + ")");

        // Check if clicking on enemy - attack
        if (x == enemy.x && y == enemy.y && isWithinAttackRange(x, y)) {
            attackTile(x, y);
            android.util.Log.d("TacticalBattle", "Attacked enemy");
            updateUI();
            return;
        }

        // Check if valid movement tile
        if (canMoveToTile(x, y)) {
            moveAxolotlLord(x, y);
            android.util.Log.d("TacticalBattle", "Moved Axolotl Lord to (" + x + ", " + y + ")");
            updateUI();
        } else {
            android.util.Log.d("TacticalBattle", "Invalid move");
        }
    }





    private void initializeBattle() {
        int lordHP = sharedPreferences.getInt("Axolotl Lord_hp", 800);
        int lordAttack = sharedPreferences.getInt("Axolotl Lord_attack", 200);

        // Place Axolotl Lord on far left edge, center vertically
        axolotlLord = new TacticalUnit("Axolotl Lord", lordHP, lordAttack, 3, 0, 2); // x=0 (leftmost)

        // Place enemy on far right edge, center vertically
        enemy = new TacticalUnit("Enemy Warrior", 400, 150, 2, 9, 2); // x=9 (rightmost)

        android.util.Log.d("TacticalBattle", "Battle initialized - Grid: " + GRID_WIDTH + "x" + GRID_HEIGHT);
        android.util.Log.d("TacticalBattle", "Axolotl Lord at (0,2), Enemy at (9,2)");
    }

    private void updateUI() {
        // REMOVED all UI text updates - just update grid
        updateGridDisplay();

        // Enable/disable buttons
        endTurnButton.setEnabled(isPlayerTurn && !battleOver);
    }

    private void updateGridDisplay() {
        // Clear all tiles first
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                gridTiles[y][x].setBackgroundColor(0xFF4CAF50); // Default green
            }
        }

        // Show unit positions
        gridTiles[axolotlLord.y][axolotlLord.x].setBackgroundColor(0xFFFFD700); // Gold for Axolotl Lord
        gridTiles[enemy.y][enemy.x].setBackgroundColor(0xFFFF4444); // Red for enemy
    }

    private boolean canMoveToTile(int x, int y) {
        // Can't move to occupied tiles
        if ((x == enemy.x && y == enemy.y) || (x == axolotlLord.x && y == axolotlLord.y)) {
            return false;
        }

        // Simple movement - within 2 tiles for now
        int dx = Math.abs(x - axolotlLord.x);
        int dy = Math.abs(y - axolotlLord.y);
        return (dx <= 2 && dy <= 2);
    }

    private boolean canAttackTile(int x, int y) {
        // Can attack if enemy is on the tile and within range
        return (x == enemy.x && y == enemy.y) && isWithinAttackRange(x, y);
    }

    private boolean isWithinAttackRange(int x, int y) {
        // Adjacent tiles for melee attack
        int dx = Math.abs(x - axolotlLord.x);
        int dy = Math.abs(y - axolotlLord.y);
        return dx <= 1 && dy <= 1 && (dx + dy > 0);
    }

    private void moveAxolotlLord(int x, int y) {
        axolotlLord.x = x;
        axolotlLord.y = y;
        android.util.Log.d("TacticalBattle", "Axolotl Lord moved to (" + x + ", " + y + ")");
    }

    private void attackTile(int x, int y) {
        if (x == enemy.x && y == enemy.y) {
            enemy.takeDamage(axolotlLord.attack);
            android.util.Log.d("TacticalBattle", "Axolotl Lord attacked enemy for " + axolotlLord.attack + " damage");

            if (enemy.currentHP <= 0) {
                playerVictory();
            }
        }
    }


    private void endPlayerTurn() {
        isPlayerTurn = false;

        // Simple enemy AI - move toward player and attack if in range
        enemyTurn();

        isPlayerTurn = true;
        updateUI();
    }

    private void enemyTurn() {
        // Very simple AI - move toward Axolotl Lord
        int dx = axolotlLord.x - enemy.x;
        int dy = axolotlLord.y - enemy.y;

        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && (Math.abs(dx) + Math.abs(dy) > 0)) {
            // Enemy is adjacent - attack
            axolotlLord.takeDamage(enemy.attack);
            android.util.Log.d("TacticalBattle", "Enemy attacked Axolotl Lord for " + enemy.attack + " damage");

            if (axolotlLord.currentHP <= 0) {
                playerDefeat();
            }
        } else {
            // Move toward player
            if (Math.abs(dx) > Math.abs(dy)) {
                enemy.x += (dx > 0) ? 1 : -1;
            } else {
                enemy.y += (dy > 0) ? 1 : -1;
            }
            android.util.Log.d("TacticalBattle", "Enemy moved to (" + enemy.x + ", " + enemy.y + ")");
        }
    }

    private void playerVictory() {
        battleOver = true;
        new AlertDialog.Builder(this)
                .setTitle("🏆 VICTORY!")
                .setMessage("Axolotl Lord has defeated the enemy warrior!\n\nTactical mastery achieved!")
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
}