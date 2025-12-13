package nimons.entity.map;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.station.Station;

public class Tile {

    private Position position;
    private Station station;
    private Item itemOnTile;
    private Chef chefOnTile;
    private boolean wall;

    public Tile() {}

    public Tile(Position position, boolean wall) {
        this.position = position;
        this.wall = wall;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Item getItemOnTile() {
        return itemOnTile;
    }

    public void setItemOnTile(Item itemOnTile) {
        this.itemOnTile = itemOnTile;
    }

    public Chef getChefOnTile() {
        return chefOnTile;
    }

    public void setChefOnTile(Chef chefOnTile) {
        this.chefOnTile = chefOnTile;
    }

    public boolean isWall() {
        return wall;
    }

    public void setWall(boolean wall) {
        this.wall = wall;
    }

    /**
     * Mengecek apakah tile dapat dilalui (tidak ada wall, station, chef, atau item)
     */
    public boolean isWalkable() {
        return !wall && station == null && chefOnTile == null && itemOnTile == null;
    }
}
