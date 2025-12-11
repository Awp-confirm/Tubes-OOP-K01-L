package nimons.entity.item;

import java.util.HashSet;
import java.util.Set;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;

public class FryingPan extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public FryingPan() {
        this("frying_pan", 2, new HashSet<>());
    }

    public FryingPan(String id, int capacity, Set<Preparable> contents) {
        super(id, "Frying Pan", true, contents);
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

    @Override 
    public boolean isPortable() { 
        return super.isPortable(); 
    }

    @Override 
    public boolean canAccept(Preparable ingredient) { 
        // Frying pan bisa menerima ingredient jika belum penuh dan ingredient bisa dimasak
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
        // Masak semua ingredient dalam frying pan
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
