package nimons.entity.chef;

import nimons.core.GameConfig;
import nimons.entity.common.Position;
import nimons.entity.item.Item;

public class Chef {

    private String id;
    private String name;
    private Position position;
    private Direction direction;
    private Item inventory;
    private ChefAction currentAction;
    private boolean busy;
    
    // Dash attributes
    private boolean isDashing;
    private long lastDashTime;
    
    private static final long NANOSECONDS_TO_MS = 1_000_000;

    public Chef() {
        this.isDashing = false;
        this.lastDashTime = 0;
    }

    public Chef(String id, String name, Position position, Direction direction) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.direction = direction;
        this.isDashing = false;
        this.lastDashTime = 0;
    }

    // getters & setters
    public String getId() { 
        return id; 
    }

    public void setId(String id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
        }

    public Position getPosition() { 
        return position; 
    }

    public void setPosition(Position position) { 
        this.position = position; 
    }

    public Direction getDirection() { 
        return direction; 
    }

    public void setDirection(Direction direction) { 
        this.direction = direction; 
    }

    public Item getInventory() { 
        return inventory; 
    }

    public void setInventory(Item inventory) { 
        this.inventory = inventory; 
    }

    public ChefAction getCurrentAction() { 
        return currentAction; 
    }

    public void setCurrentAction(ChefAction currentAction) {
        this.currentAction = currentAction; 
    }

    public boolean isBusy() { 
        return busy; 
    }
    
    public void setBusy(boolean busy) { 
        this.busy = busy; 
    }
    
    public boolean isDashing() {
        return isDashing;
    }
    
    public void setDashing(boolean dashing) {
        this.isDashing = dashing;
    }

    public void move(Direction direction) {
        if (this.busy) {
            return; // Cannot move while busy
        }
        
        Position newPosition = calculateNewPosition(direction);
        this.position = newPosition;
        this.direction = direction;
    }

    private Position calculateNewPosition(Direction direction) {
        return calculatePositionInDirection(position, direction, 1);
    }
    
    /**
     * Calculate position in a given direction with specified distance
     * 
     * @param current starting position
     * @param direction direction to move
     * @param distance number of tiles to move
     * @return new position
     */
    private Position calculatePositionInDirection(Position current, Direction direction, int distance) {
        int newX = current.getX();
        int newY = current.getY();
        
        switch (direction) {
            case UP:
                newY -= distance;
                break;
            case DOWN:
                newY += distance;
                break;
            case LEFT:
                newX -= distance;
                break;
            case RIGHT:
                newX += distance;
                break;
        }
        
        return new Position(newX, newY);
    }

    public boolean canMoveTo(Position targetPosition) {
        // Movement rules:
        // - Cannot move if busy
        // - Cannot move to wall
        // - Cannot move to station tile
        // - Cannot move to tile occupied by another chef
        return !this.busy;
    }


    public boolean isInventoryEmpty() {
        return inventory == null;
    }


    public boolean canPickupItem() {
        return isInventoryEmpty() && !busy;
    }

    public boolean pickupItem(Item item) {
        if (!canPickupItem() || item == null) {
            return false;
        }
        
        this.inventory = item;
        return true;
    }

    public Item dropItem() {
        if (isInventoryEmpty()) {
            return null;
        }
        
        Item droppedItem = this.inventory;
        this.inventory = null;
        return droppedItem;
    }

    public void turn(Direction newDirection) {
        if (!busy) {
            this.direction = newDirection;
        }
    }
    
    /**
     * Dash ke arah yang dituju dengan jarak beberapa tile
     * Memiliki cooldown
     * 
     * @param direction arah dash
     * @param currentTime waktu saat ini (System.nanoTime())
     * @return posisi target dash, atau null jika gagal
     */
    public Position dash(Direction direction, long currentTime) {
        long timeSinceLastDash = (currentTime - lastDashTime) / NANOSECONDS_TO_MS;
        if (timeSinceLastDash < GameConfig.DASH_COOLDOWN_MS) {
            return null;
        }
        
        if (this.busy) {
            return null;
        }
        
        Position targetPosition = calculatePositionInDirection(position, direction, GameConfig.DASH_DISTANCE_TILES);
        
        this.isDashing = true;
        this.direction = direction;
        this.lastDashTime = currentTime;
        
        return targetPosition;
    }
    
    /**
     * Cek apakah dash sedang dalam cooldown
     * 
     * @param currentTime waktu saat ini (System.nanoTime())
     * @return true jika masih cooldown
     */
    public boolean isDashOnCooldown(long currentTime) {
        long timeSinceLastDash = (currentTime - lastDashTime) / 1_000_000;
        return timeSinceLastDash < GameConfig.DASH_COOLDOWN_MS;
    }
    
    /**
     * Get sisa cooldown dash dalam milliseconds
     * 
     * @param currentTime waktu saat ini (System.nanoTime())
     * @return sisa cooldown (ms), atau 0 jika sudah bisa dash
     */
    public long getDashCooldownRemaining(long currentTime) {
        long timeSinceLastDash = (currentTime - lastDashTime) / 1_000_000;
        long remaining = GameConfig.DASH_COOLDOWN_MS - timeSinceLastDash;
        return remaining > 0 ? remaining : 0;
    }
}
