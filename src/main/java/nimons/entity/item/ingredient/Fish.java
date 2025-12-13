package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Fish extends Ingredient {

    public Fish() {
        super("fish", "Fish", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return getState() == IngredientState.RAW; // Fish perlu dipotong
    }

    @Override
    public boolean canBeCooked() {
        return false; // Fish tidak perlu dimasak, hanya dipotong
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.CHOPPED; // Fish yang sudah dipotong bisa diletakkan di plate
    }

    @Override
    public void chop() {
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.CHOPPED);
        }
    }

    @Override
    public void cook() {
        // --- REVISI: Pindahkan state ke COOKING untuk memicu update timer ---
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.COOKING);
            // Asumsi: currentCookTime akan direset/diinisialisasi oleh Cooking Station atau Utensil
        } 
        // State COOKING, COOKED, dan BURNED akan dihandle oleh update loop
    }
}