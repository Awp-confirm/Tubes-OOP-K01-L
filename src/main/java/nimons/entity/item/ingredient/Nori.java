package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Nori extends Ingredient {

    public Nori() {
        super("nori", "Nori", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return false; // Nori tidak perlu dipotong
    }

    @Override
    public boolean canBeCooked() {
        return false; // Nori tidak perlu dimasak
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return true; // Nori bisa langsung diletakkan di plate
    }

    @Override
    public void chop() {
        // Nori tidak bisa dipotong
    }

    @Override
    public void cook() {
        // Nori tidak bisa dimasak
    }
}
