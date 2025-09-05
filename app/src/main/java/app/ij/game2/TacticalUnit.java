package app.ij.game2;

public class TacticalUnit {
    public String name;
    public int maxHP;
    public int currentHP;
    public int attack;
    public int speed;
    public int x, y; // Grid position
    public boolean hasActed; // Has moved/attacked this turn

    public TacticalUnit(String name, int hp, int attack, int speed, int x, int y) {
        this.name = name;
        this.maxHP = hp;
        this.currentHP = hp;
        this.attack = attack;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.hasActed = false;
    }

    public void takeDamage(int damage) {
        currentHP = Math.max(0, currentHP - damage);
        android.util.Log.d("TacticalUnit", name + " took " + damage + " damage. HP: " + currentHP + "/" + maxHP);
    }

    public boolean isAlive() {
        return currentHP > 0;
    }

    public void resetTurn() {
        hasActed = false;
    }

    public int getHealthPercent() {
        return (currentHP * 100) / maxHP;
    }
}