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
}
