package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Shrimp extends Ingredient {

    public Shrimp() {
        super("shrimp", "Shrimp", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return false; // Shrimp tidak perlu dipotong
    }

    @Override
    public boolean canBeCooked() {
        return getState() == IngredientState.RAW;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.COOKED;
    }

    @Override
    public void chop() {
        // Shrimp tidak bisa dipotong
    }

    @Override
    public void cook() {
        if (getState() == IngredientState.RAW) {
            // HANYA SET STATE KE COOKING untuk memulai timer
            setState(IngredientState.COOKING);
            // Reset progress jika ada
            this.currentCookTime = 0; 
        } 
        // State COOKING, COOKED, dan BURNED akan dihandle oleh updateCooking() di parent class.
    }
}
