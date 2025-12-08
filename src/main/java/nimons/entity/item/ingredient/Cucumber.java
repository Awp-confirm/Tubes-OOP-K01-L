package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Cucumber extends Ingredient {
    public Cucumber() {
        super("I-Cucumber", "Timun", IngredientState.RAW);
    }

    @Override public boolean canBeChopped() { return true; }
    @Override public boolean canBeCooked() { return false; }
    @Override public boolean canBePlacedOnPlate() { return true; }

    @Override public void chop() {
        if (getState() == IngredientState.RAW) {
            this.setState(IngredientState.CHOPPED);
            this.setName("Timun (Chopped)");
        }
    }

    @Override public void cook() {}
}