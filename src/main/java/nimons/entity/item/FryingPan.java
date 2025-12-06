package nimons.entity.item;

import java.util.Set;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;

public class FryingPan extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public FryingPan() {}

    public FryingPan(String id, int capacity, Set<Preparable> contents) {
        super(id, "Frying Pan", true, contents);
        this.capacity = capacity;
    }

    // getters & setters
    public int capacity() { 
        return capacity; 
    }

    public void setCapacity(int capacity) { 
        this.capacity = capacity; 
    }

    @Override public boolean isPortable() { 
        return super.isPortable(); 
    }

    @Override public boolean canAccept(Preparable ingredient) { 
        return false; 
    }

    @Override public void addIngredient(Preparable ingredient) {

    }

    @Override public void startCooking() {
        
    }
}
