package nimons.entity.chef;

import nimons.entity.common.Position;
import nimons.entity.item.Item;

public class Chef {
    private String id;
    private String name;
    private Position position;
    private Direction facingDirection;
    private Item inventory;
    
    // ATRIBUT BARU: Status Sibuk
    private boolean isBusy = false;

    public Chef(String id, String name, Position position, Direction facingDirection) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.facingDirection = facingDirection;
    }

    public void move(Direction dir, int mapWidth, int mapHeight) {
        // GDD: Chef tidak bisa gerak kalau sedang BUSY
        if (isBusy) return;

        this.facingDirection = dir;
        int newX = position.getX() + dir.getDx();
        int newY = position.getY() + dir.getDy();

        if (newX >= 0 && newX < mapWidth && newY >= 0 && newY < mapHeight) {
            this.position = new Position(newX, newY);
        }
    }

    // --- GETTER SETTER BUSY ---
    public boolean isBusy() { return isBusy; }
    public void setBusy(boolean busy) { this.isBusy = busy; }

    public Item getInventory() { return inventory; }
    public void setInventory(Item item) { this.inventory = item; }
    public Position getPosition() { return position; }
    public Position getFacingPosition() {
        return new Position(position.getX() + facingDirection.getDx(), position.getY() + facingDirection.getDy());
    }
    public String getName() { return name; }
}