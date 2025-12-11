package nimons.entity.item;

import java.util.HashSet;
import java.util.Set;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;

public class BoilingPot extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public BoilingPot() {
        this("boiling_pot", 3, new HashSet<>());
    }

    public BoilingPot(String id, int capacity, Set<Preparable> contents) {
        super(id, "Boiling Pot", true, contents);
        this.capacity = capacity;
    }

    // getters & setters
    @Override
    public int capacity() { 
        return capacity; 
    }
    
    @Override
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) { 
        this.capacity = capacity; 
    }

    // interface methods
    @Override 
    public boolean isPortable() { 
        return super.isPortable(); 
    }

    @Override 
    public boolean canAccept(Preparable ingredient) { 
        // Boiling pot bisa menerima ingredient jika belum penuh dan ingredient bisa dimasak
        Set<Preparable> contents = getContents();
        if (contents == null) {
            return false;
        }
        return contents.size() < capacity && ingredient != null && ingredient.canBeCooked();
    }

    @Override 
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient) && getContents() != null) {
            getContents().add(ingredient);
        }
    }

    @Override 
    public void startCooking() {
        // Masak semua ingredient dalam pot
        if (getContents() != null) {
            for (Preparable ingredient : getContents()) {
                if (ingredient.canBeCooked()) {
                    ingredient.cook();
                }
            }
        }
    }

    @Override
    public void update(long deltaTime) {

    }
    
    @Override
    public void reset() {
        if (getContents() != null) {
            getContents().clear();
        }
    }
}
