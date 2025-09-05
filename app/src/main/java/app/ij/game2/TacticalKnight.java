package app.ij.game2;

public class TacticalKnight {
    private String name;
    private int hp;
    private int attack;
    private int speed;
    private int actions;
    private MovementStyle movementStyle;
    private AttackStyle attackStyle;
    private int quantity;
    private Trait currentTrait;

    // Movement and attack style enums
    public enum MovementStyle {
        INFANTRY(1, "1 tile per action"),
        CAVALRY(2, "2 tiles per action"),
        FLYING(3, "Flight - ignores terrain");

        public final int tilesPerAction;
        public final String description;

        MovementStyle(int tiles, String desc) {
            this.tilesPerAction = tiles;
            this.description = desc;
        }
    }

    public enum AttackStyle {
        MELEE(1, "Adjacent enemies only"),
        RANGED(3, "3 tile range"),
        AREA(1, "Hits multiple enemies");

        public final int range;
        public final String description;

        AttackStyle(int range, String desc) {
            this.range = range;
            this.description = desc;
        }
    }

    public TacticalKnight(String name, int hp, int attack, int speed, int actions,
                          MovementStyle movementStyle, AttackStyle attackStyle) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.speed = speed;
        this.actions = actions;
        this.movementStyle = movementStyle;
        this.attackStyle = attackStyle;
        this.quantity = 1;
        this.currentTrait = null;
    }

    // Getters
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getSpeed() { return speed; }
    public int getActions() { return actions; }
    public MovementStyle getMovementStyle() { return movementStyle; }
    public AttackStyle getAttackStyle() { return attackStyle; }
    public int getQuantity() { return quantity; }
    public Trait getTrait() { return currentTrait; }

    // Setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTrait(Trait trait) { this.currentTrait = trait; }
    public boolean hasTrait() { return currentTrait != null; }

    public String getStatsBreakdown() {
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("HP: ").append(hp).append("\n");
        breakdown.append("Attack: ").append(attack).append("\n");
        breakdown.append("Speed: ").append(speed).append(" (turn order)\n");
        breakdown.append("Actions: ").append(actions).append(" per turn\n");
        breakdown.append("Movement: ").append(movementStyle.description).append("\n");
        breakdown.append("Attack Style: ").append(attackStyle.description);

        if (currentTrait != null) {
            breakdown.append("\n\nTrait: ").append(currentTrait.getDisplayString());
        }

        return breakdown.toString();
    }
}