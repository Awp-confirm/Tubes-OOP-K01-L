package nimons.station;

// DEPENDENCY ALERT:
// Class-class berikut masih menggunakan versi DUMMY dari package nimons.dummy. (internal)
import nimons.dummy.Chef;
import nimons.dummy.GameState;
import nimons.dummy.Position;

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
    public abstract void onInteract(Chef chef, GameState state);

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