package nimons.entity.item;

import java.util.HashSet;
import java.util.Set;

import nimons.core.GameConfig;
import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.exceptions.InvalidIngredientStateException;
import nimons.exceptions.StationFullException;

public class BoilingPot extends KitchenUtensil implements CookingDevice {

    private int capacity;

    public BoilingPot() {
        this("boiling_pot", GameConfig.BOILING_POT_CAPACITY, new HashSet<>());
    }

    public BoilingPot(String id, int capacity, Set<Preparable> contents) {
        super(id, "Boiling Pot", true, contents);
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
        // Proses validasi: cek apakah ingredient adalah Rice/Pasta dan bisa dimasak
        Set<Preparable> contents = getContents();
        if (contents == null || ingredient == null) {
            return false;
        }
        
        if (ingredient instanceof nimons.entity.item.Ingredient) {
            nimons.entity.item.Ingredient ing = (nimons.entity.item.Ingredient) ingredient;
            String ingredientType = ing.getId();
            boolean isRiceOrPasta = ingredientType.equals("rice") || ingredientType.equals("pasta");
            return contents.size() < capacity && isRiceOrPasta && ingredient.canBeCooked();
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
                    "Not rice/pasta or " + ing.getState().toString(),
                    "Rice or Pasta in cookable state"
                );
            }
            throw new InvalidIngredientStateException("Ingredient", "invalid", "rice or pasta");
        }
        
        getContents().add(ingredient);
    }

    @Override 
    public void startCooking() {
        // Proses memasak: panggil cook() untuk semua ingredient yang ada
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
