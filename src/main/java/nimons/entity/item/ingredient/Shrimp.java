package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Shrimp extends Ingredient {

    public Shrimp() {
        super("shrimp", "Shrimp", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return getState() == IngredientState.RAW; // Shrimp perlu dipotong dulu
    }

    @Override
    public boolean canBeCooked() {
        return getState() == IngredientState.CHOPPED; // Shrimp hanya bisa dimasak setelah CHOPPED
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.COOKED; // Shrimp yang sudah dimasak bisa diletakkan di plate
    }

    @Override
    public void chop() {
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.CHOPPED);
        }
    }

    @Override
    public void cook() {
        if (getState() == IngredientState.CHOPPED) {
            // HANYA SET STATE KE COOKING untuk memulai timer
            setState(IngredientState.COOKING);
            // Reset progress jika ada
            this.currentCookTime = 0; 
        } 
        // State COOKING, COOKED, dan BURNED akan dihandle oleh updateCooking() di parent class.
    }
}
