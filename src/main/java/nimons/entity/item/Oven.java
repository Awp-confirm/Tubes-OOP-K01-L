package nimons.entity.item;

import java.util.HashSet;
import java.util.Set;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;

public class Oven extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public Oven() {
        this("oven", 4, new HashSet<>());
    }

    public Oven(String id, int capacity, Set<Preparable> contents) {
        super(id, "Oven", false, contents);
        this.capacity = capacity;
    }

    
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
