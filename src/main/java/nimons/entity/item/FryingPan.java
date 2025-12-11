package nimons.entity.item;

import java.util.HashSet;
import java.util.Set;

import nimons.core.GameConfig;
import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.exceptions.InvalidIngredientStateException;
import nimons.exceptions.StationFullException;

public class FryingPan extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public FryingPan() {
        this("frying_pan", GameConfig.FRYING_PAN_CAPACITY, new HashSet<>());
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
        // Frying pan hanya bisa menerima ingredient dengan status CHOPPED
        Set<Preparable> contents = getContents();
        if (contents == null || ingredient == null) {
            return false;
        }
        
        // Cek apakah ingredient sudah CHOPPED
        if (ingredient instanceof nimons.entity.item.Ingredient) {
            nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) ingredient;
            boolean isChopped = ing.getState() == nimons.entity.item.IngredientState.CHOPPED;
            return contents.size() < capacity && isChopped && ingredient.canBeCooked();
        }
        
        return false;
    }

    @Override 
    public void addIngredient(Preparable ingredient) throws StationFullException, InvalidIngredientStateException {
        if (getContents() == null || ingredient == null) {
            throw new InvalidIngredientStateException("Ingredient", "null", "valid");
        }
        
        if (getContents().size() >= capacity) {
            throw new StationFullException(getName(), capacity);
        }
        
        if (!canAccept(ingredient)) {
            if (ingredient instanceof Ingredient) {
                Ingredient ing = (Ingredient) ingredient;
                throw new InvalidIngredientStateException(
                    ing.getName(),
                    ing.getState().toString(),
                    "CHOPPED"
                );
            }
            throw new InvalidIngredientStateException("Ingredient", "invalid", "CHOPPED ingredient");
        }
        
        getContents().add(ingredient);
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
