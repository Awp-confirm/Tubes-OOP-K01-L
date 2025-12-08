package nimons.entity.chef;

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

    public Chef() {}

    public Chef(String id, String name, Position position, Direction direction) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.direction = direction;
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

    public void move(Direction direction) {
        if (this.busy) {
            return; // Cannot move while busy
        }
        
        Position newPosition = calculateNewPosition(direction);
        this.position = newPosition;
        this.direction = direction;
    }

    private Position calculateNewPosition(Direction direction) {
        int newX = position.getX();
        int newY = position.getY();
        
        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
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

    public boolean isHolding(Class<? extends Item> itemClass) {
        return inventory != null && itemClass.isInstance(inventory);
    }

    public Position getFacingPosition() {
        return calculateNewPosition(direction);
    }

    public void turn(Direction newDirection) {
        if (!busy) {
            this.direction = newDirection;
        }
    }
}
