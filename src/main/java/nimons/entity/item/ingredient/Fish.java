package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Fish extends Ingredient {

    public Fish() {
        super("fish", "Fish", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return false; 
    }

    @Override
    public boolean canBeCooked() {
        // Mengizinkan Fish masuk ke Cooking Station HANYA jika masih RAW
        return getState() == IngredientState.RAW; 
    }

    @Override
    public boolean canBePlacedOnPlate() {
        // Hanya RAW yang valid untuk resep sushi
        // Jika ingin mengizinkan COOKED untuk test case kegagalan, logic ini harus diubah.
        return getState() == IngredientState.RAW; 
    }

    @Override
    public void chop() {
        // Tidak ada perubahan state
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