package nimons.entity.map;

import java.util.List;

import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.station.Station;

public class TileManager {

    private int width;
    private int height;
    private Tile[][] tiles;

    public TileManager() {}

    public TileManager(int width, int height, Tile[][] tiles) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
    }

    // getters & setters
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    /**
     * Check if position is within map bounds
     */
    public boolean isInBounds(Position pos) {
        return pos.getX() >= 0 && pos.getX() < width &&
               pos.getY() >= 0 && pos.getY() < height;
    }

    /**
     * Get tile at position
     */
    public Tile getTileAt(Position pos) {
        if (!isInBounds(pos)) {
            return null;
        }
        return tiles[pos.getY()][pos.getX()];
    }

    /**
     * Check if position is walkable
     */
    public boolean isWalkable(Position pos) {
        if (!isInBounds(pos)) {
            return false;
        }
        Tile tile = getTileAt(pos);
        return tile != null && tile.isWalkable();
    }

    /**
     * Get station at position
     */
    public Station getStationAt(Position pos) {
        Tile tile = getTileAt(pos);
        return tile != null ? tile.getStation() : null;
    }

    /**
     * Get item at position
     */
    public Item getItemAt(Position pos) {
        Tile tile = getTileAt(pos);
        return tile != null ? tile.getItemOnTile() : null;
    }

    /**
     * Place item on tile
     */
    public void placeItem(Position pos, Item item) {
        Tile tile = getTileAt(pos);
        if (tile != null) {
            tile.setItemOnTile(item);
        }
    }

    /**
     * Remove item from tile
     */
    public void removeItem(Position pos) {
        Tile tile = getTileAt(pos);
        if (tile != null) {
            tile.setItemOnTile(null);
        }
    }

    /**
     * Get spawn points (placeholder)
     */
    public List<Position> getSpawnPoints() {
        return null;
    }
}
