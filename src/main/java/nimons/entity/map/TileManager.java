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

    // ====== placeholders ======

    public boolean isWalkable(Position pos) {
        return false;
    }

    public Station getStationAt(Position pos) {
        return null;
    }

    public Item getItemAt(Position pos) {
        return null;
    }

    public void placeItem(Position pos, Item item) {
        // no-op
    }

    public void removeItem(Position pos) {
        // no-op
    }

    public List<Position> getSpawnPoints() {
        return null;
    }
}
