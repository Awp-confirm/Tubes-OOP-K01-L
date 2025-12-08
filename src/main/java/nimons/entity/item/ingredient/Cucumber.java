package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Cucumber extends Ingredient {

    public Cucumber() {
        super("cucumber", "Cucumber", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return getState() == IngredientState.RAW;
    }

    @Override
    public boolean canBeCooked() {
        return false; // Cucumber tidak perlu dimasak
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.CHOPPED;
    }

    @Override
    public void chop() {
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.CHOPPED);
        }
    }

    @Override
    public void cook() {
        // Cucumber tidak bisa dimasak
    }
}
