package nimons.entity.item;

import java.util.Set;
import nimons.entity.item.interfaces.Preparable;

public abstract class KitchenUtensil extends Item {

    private Set<Preparable> contents;
    private final int capacity; 

    // FIX #1: Tambahkan kembali constructor kosong
    public KitchenUtensil() {
        this("", "", true, null, 1); // Panggil constructor yang sudah ada dengan nilai default
    }

    public KitchenUtensil(String id, String name, boolean portable, Set<Preparable> contents, int capacity) {
        super(id, name, portable);
        this.contents = contents;
        this.capacity = capacity;
    }

    public Set<Preparable> getContents() { 
        return contents; 
    }

    public void setContents(Set<Preparable> contents) { 
        this.contents = contents; 
    }
    
    public int getCapacity() {
        return capacity;
    }
}