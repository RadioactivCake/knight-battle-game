package app.ij.game2;

public class TacticalUnit {
    public String name;
    public int maxHP;
    public int currentHP;
    public int attack;
    public int speed;
    public int x, y; // Grid position
    public boolean hasActed; // Has moved/attacked this turn
    public int maxActions;
    public int actionsUsed;
    public int maxAttacks;  // NEW: Separate attack limit
    public int attacksUsed; // NEW: Track attacks separately

    public TacticalUnit(String name, int hp, int attack, int speed, int x, int y) {
        this.name = name;
        this.maxHP = hp;
        this.currentHP = hp;
        this.attack = attack;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.hasActed = false;
        this.maxActions = 2; // Default 2 actions per turn
        this.maxAttacks = 1;    // NEW: Only 1 attack per turn
        this.actionsUsed = 0;
        this.attacksUsed = 0;   // NEW: Track attacks used
    }

    public void takeDamage(int damage) {
        currentHP = Math.max(0, currentHP - damage);
        android.util.Log.d("TacticalUnit", name + " took " + damage + " damage. HP: " + currentHP + "/" + maxHP);
    }

    public boolean isAlive() {
        return currentHP > 0;
    }


    public int getHealthPercent() {
        return (currentHP * 100) / maxHP;
    }

    public void resetTurn() {
        actionsUsed = 0;
        attacksUsed = 0;  // NEW: Reset attacks
    }

    public boolean canAct() {
        return actionsUsed < maxActions;
    }

    public void useAction() {
        actionsUsed++;
    }

    public int getRemainingActions() {
        return maxActions - actionsUsed;
    }

    public boolean canAttack() {
        return actionsUsed < maxActions && attacksUsed < maxAttacks; // NEW: Check both
    }

    public void useAttack() {  // NEW: Separate method for attacks
        actionsUsed++;
        attacksUsed++;
    }

    public int getRemainingAttacks() {  // NEW: Track remaining attacks
        return maxAttacks - attacksUsed;
    }

    public boolean canMove() {
        return actionsUsed < maxActions; // Can move as long as has actions remaining
    }

}