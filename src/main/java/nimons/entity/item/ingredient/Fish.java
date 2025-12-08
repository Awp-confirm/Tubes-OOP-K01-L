package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Fish extends Ingredient {

    public Fish() {
        super("fish", "Fish", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return false; // Fish tidak perlu dipotong untuk sushi
    }

    @Override
    public boolean canBeCooked() {
        return false; // Fish untuk sushi dimakan mentah
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.RAW;
    }

    @Override
    public void chop() {
        // Fish tidak bisa dipotong
    }

    @Override
    public void cook() {
        // Fish tidak bisa dimasak (untuk sushi)
    }
}
