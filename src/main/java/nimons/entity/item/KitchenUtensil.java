package nimons.entity.item;

import java.util.Set;

import nimons.entity.item.interfaces.Preparable;

public abstract class KitchenUtensil extends Item {

    private Set<Preparable> contents;

    public KitchenUtensil() {}

    public KitchenUtensil(String id, String name, boolean portable, Set<Preparable> contents) {
        super(id, name, portable);
        this.contents = contents;
    }

    // getters & setters
    public Set<Preparable> getContents() { 
        return contents; 
    }

    public void setContents(Set<Preparable> contents) { 
        this.contents = contents; 
    }
    
    /**
     * Get capacity of this utensil
     * Should be overridden by subclasses that have capacity limits
     */
    public int getCapacity() {
        return Integer.MAX_VALUE; // Default: unlimited capacity
    }
}
