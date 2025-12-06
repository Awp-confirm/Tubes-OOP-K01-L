package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;

public abstract class Station {

    protected String name;
    protected Position position;

    public Station(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Method untuk interaksi.
     * Logika spesifik (Masak/Potong/Cuci) diimplementasikan di subclass.
     */
    public abstract void onInteract(Chef chef);

    // Getter & Setter
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
}